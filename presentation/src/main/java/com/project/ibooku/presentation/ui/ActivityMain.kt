package com.project.ibooku.presentation.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.ibooku.presentation.R
import com.project.ibooku.presentation.ui.feature.book.BookDetailScreen
import com.project.ibooku.presentation.ui.feature.home.HomeScreen
import com.project.ibooku.presentation.ui.feature.map.BookReviewLocationMapScreen
import com.project.ibooku.presentation.ui.feature.map.BookReviewReadMap
import com.project.ibooku.presentation.ui.feature.review.BookReviewViewModel
import com.project.ibooku.presentation.ui.feature.review.screen.BookReviewCompleteScreen
import com.project.ibooku.presentation.ui.feature.review.screen.BookReviewLocationScreen
import com.project.ibooku.presentation.ui.feature.review.screen.BookReviewOnboardingScreen
import com.project.ibooku.presentation.ui.feature.review.screen.BookReviewWriteScreen
import com.project.ibooku.presentation.ui.feature.review.screen.BookSearchScreenAtReview
import com.project.ibooku.presentation.ui.feature.search.BookInfoViewModel
import com.project.ibooku.presentation.ui.feature.search.BookSearchScreen
import com.project.ibooku.presentation.ui.theme.Black
import com.project.ibooku.presentation.ui.theme.Gray50
import com.project.ibooku.presentation.ui.theme.SkyBlue10
import com.project.ibooku.presentation.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityHome : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavigationGraph(navController = navController)
        }
    }
}

@Composable
fun StatusBarColorsTheme(
    statusBarColor: Color? = null,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor =
                statusBarColor?.toArgb() ?: if (isDarkTheme) Black.toArgb() else White.toArgb()

            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = if (statusBarColor != null) false else !isDarkTheme
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val items = listOf(
        NavItem.Home,
        NavItem.Menu
    )

    NavigationBar(
        containerColor = White,
        contentColor = Gray50
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { item ->
            val (titleId, icon) = when (item.route) {
                "home" -> Pair(R.string.nav_home, Icons.Default.Home)
                "menu" -> Pair(R.string.nav_menu, Icons.Default.Menu)
                else -> Pair(null, Icons.Default.Close)
            }
            NavigationBarItem(
                selected = currentDestination?.route == item.route,
                icon = {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = icon,
                        contentDescription = titleId?.let { stringResource(id = it) }
                    )
                },
                label = {
                    Text(
                        text = titleId?.let { stringResource(id = it) } ?: "",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SkyBlue10,
                    unselectedIconColor = Gray50,
                    selectedTextColor = SkyBlue10,
                    unselectedTextColor = Gray50,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}


@Composable
private fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavItem.Home.route,
        modifier = modifier
    ) {
        composable(NavItem.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(NavItem.Menu.route) {

        }
        composable(NavItem.BookSearch.route) { backStackEntry ->
            val prevRoute = navController.previousBackStackEntry?.destination?.route
            // 리뷰 온보딩에서 진입한 경우에는 리뷰용 검색 화면에 진입하도록 함.
            if (prevRoute == NavItem.BookReviewOnboarding.route) {
                val prevEntry = remember(backStackEntry){
                    navController.getBackStackEntry(NavItem.BookReviewOnboarding.route)
                }
                BookSearchScreenAtReview(navController = navController, viewModel = hiltViewModel<BookReviewViewModel>(prevEntry))
            } else {
                BookSearchScreen(navController = navController,)
            }
        }
        composable(NavItem.BookDetail.route) { backStackEntry ->
            val prevEntry = remember(backStackEntry){
                navController.getBackStackEntry(NavItem.BookSearch.route)
            }
            BookDetailScreen(navController = navController, viewModel = hiltViewModel<BookInfoViewModel>(prevEntry))
        }
        composable(NavItem.BookNearLibraryMap.route) {

        }
        composable(NavItem.BookReviewReadMap.route) {
            BookReviewReadMap(navController = navController)
        }
        composable(NavItem.BookReviewOnboarding.route) {
            BookReviewOnboardingScreen(navController = navController, viewModel = hiltViewModel<BookReviewViewModel>())
        }
        composable(NavItem.BookReviewWrite.route) { backStackEntry ->
            val prevEntry = remember(backStackEntry){
                navController.getBackStackEntry(NavItem.BookReviewOnboarding.route)
            }
            BookReviewWriteScreen(navController = navController, viewModel = hiltViewModel<BookReviewViewModel>(prevEntry))
        }
        composable(NavItem.BookReviewLocation.route) { backStackEntry ->
            val prevEntry = remember(backStackEntry){
                navController.getBackStackEntry(NavItem.BookReviewOnboarding.route)
            }
            BookReviewLocationScreen(navController = navController, viewModel = hiltViewModel<BookReviewViewModel>(prevEntry))
        }
        composable(NavItem.BookReviewLocationMap.route) { backStackEntry ->
            val prevEntry = remember(backStackEntry){
                navController.getBackStackEntry(NavItem.BookReviewOnboarding.route)
            }
            BookReviewLocationMapScreen(navController = navController, viewModel = hiltViewModel<BookReviewViewModel>(prevEntry))
        }
        composable(NavItem.BookReviewComplete.route) { backStackEntry ->
            val prevEntry = remember(backStackEntry){
                navController.getBackStackEntry(NavItem.BookReviewOnboarding.route)
            }
            BookReviewCompleteScreen(navController = navController, viewModel = hiltViewModel<BookReviewViewModel>(prevEntry))
        }
    }
}


sealed class NavItem(val route: String) {
    data object Home : NavItem("home")
    data object Menu : NavItem("menu")
    data object BookSearch : NavItem("book_search")
    data object BookDetail : NavItem("book_detail")
    data object BookNearLibraryMap : NavItem("book_near_library_map")
    data object BookReviewReadMap : NavItem("book_review_read_map")
    data object BookReviewOnboarding : NavItem("book_review_onboarding")
    data object BookReviewWrite : NavItem("book_review_write")
    data object BookReviewLocation : NavItem("book_review_location")
    data object BookReviewLocationMap : NavItem("book_review_location_map")
    data object BookReviewComplete : NavItem("book_review_complete")
}

