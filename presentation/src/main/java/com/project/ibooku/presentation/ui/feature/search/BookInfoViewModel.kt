package com.project.ibooku.presentation.ui.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.ibooku.core.util.Resources
import com.project.ibooku.domain.model.external.KeywordSearchResultModel
import com.project.ibooku.domain.usecase.external.KeywordSearchResultUseCase
import com.project.ibooku.presentation.ui.feature.map.ReviewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookInfoViewModel @Inject constructor(
    val keywordSearchResultUseCase: KeywordSearchResultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BookInfoState())
    val state = _state.asStateFlow()

    // 이벤트 처리를 위한 함수
    fun onEvent(event: BookSearchEvents) {
        when (event) {
            is BookSearchEvents.SearchTextChanged -> {
                if (event.newText.isEmpty()) {
                    // 입력 키워드가 비어있을땐 검색된 결과 목록을 지우고
                    // 최근 검색어 및 인기 키워드 화면 띄우기 위해 해당 코드 작성
                    _state.value = _state.value.copy(
                        searchKeyword = event.newText,
                        relatedKeywordList = listOf(),
                        searchResult = KeywordSearchResultModel(
                            searchedKeyword = "",
                            resultList = listOf()
                        )
                    )
                } else {
                    _state.value = _state.value.copy(searchKeyword = event.newText)
                    getRelatedSearchResult()
                }
            }

            is BookSearchEvents.SearchKeyword -> {
                // 입력 키워드가 비어있지 않을 경우만 검색
                if (_state.value.searchKeyword.isNotEmpty()) {
                    _state.value = _state.value.copy(relatedKeywordList = listOf())
                    getKeywordSearchResult()
                }
            }

            is BookSearchEvents.SearchWithSelectionSomething -> {
                _state.value = _state.value.copy(
                    searchKeyword = event.keyword,
                    relatedKeywordList = listOf()
                )
                getKeywordSearchResult()
            }

            is BookSearchEvents.BookSelected -> {
                _state.value = _state.value.copy(
                    selectedBook = event.selectedBook
                )
            }

            is BookSearchEvents.ReviewOrderChanged -> {
                _state.value = _state.value.copy(
                    reviewOrder = event.reviewOrder
                )
                _state.value = when(event.reviewOrder){
                    ReviewOrder.RECENT -> {
                        _state.value.copy(
                            selectedBookReviewList = _state.value.selectedBookReviewList.sortedByDescending { it.datetime }
                        )
                    }
                    ReviewOrder.PAST -> {
                        _state.value.copy(
                            selectedBookReviewList = _state.value.selectedBookReviewList.sortedBy { it.datetime }
                        )
                    }
                    ReviewOrder.HIGH_RATING -> {
                        _state.value.copy(
                            selectedBookReviewList = _state.value.selectedBookReviewList.sortedWith(
                                compareByDescending<ReviewItem>{ it.rating }.thenByDescending { it.datetime }
                            )
                        )
                    }
                    ReviewOrder.LOW_RATING -> {
                        _state.value.copy(
                            selectedBookReviewList = _state.value.selectedBookReviewList.sortedWith(
                                compareBy<ReviewItem>{ it.rating }.thenByDescending { it.datetime }
                            )
                        )
                    }
                }
            }

            is BookSearchEvents.OnIsNoContentExcludedChanged -> {
                _state.value = _state.value.copy(
                    isNoContentExcluded = _state.value.isNoContentExcluded.not(),
                )
            }

            is BookSearchEvents.OnIsSpoilerExcluded -> {
                _state.value = _state.value.copy(
                    isSpoilerExcluded = _state.value.isSpoilerExcluded.not()
                )
            }
        }
    }

    /**
     * 검색한 키워드를 기준으로 검색 결과를 가져온다
     */
    private fun getKeywordSearchResult() {
        val keyword = _state.value.searchKeyword
        viewModelScope.launch {
            keywordSearchResultUseCase(keyword).collect { result ->
                when (result) {
                    is Resources.Loading -> {
                        _state.value = _state.value.copy(isLoading = result.isLoading)
                    }

                    is Resources.Success -> {
                        result.data?.let { searchResult ->
                            _state.value = _state.value.copy(searchResult = searchResult)
                        }
                    }

                    is Resources.Error -> {
                        _state.value = _state.value.copy(isLoading = false)
                    }
                }
            }
        }
    }


    /**
     * 검색창에 입력되는 검색어를 기준으로 연관 검색어들을 가져온다.
     */
    private fun getRelatedSearchResult() {
        val keyword = _state.value.searchKeyword
        viewModelScope.launch {
            keywordSearchResultUseCase(keyword).collect { result ->
                when (result) {
                    is Resources.Success -> {
                        result.data?.let { searchResult ->
                            _state.value =
                                _state.value.copy(relatedKeywordList = searchResult.resultList.map { it.titleInfo })
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }
}