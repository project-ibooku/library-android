package com.project.ibooku.presentation.ui.screens.home

import com.project.ibooku.domain.model.PopularBooksModel

data class HomeState(
    var popularBooks: List<PopularBooksModel> = emptyList(),
    var isLoading: Boolean = false,
)