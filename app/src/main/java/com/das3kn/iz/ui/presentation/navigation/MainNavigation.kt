package com.das3kn.iz.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.das3kn.iz.ui.presentation.GroupsContentScreen
import com.das3kn.iz.ui.presentation.ProfileScreen
import com.das3kn.iz.ui.presentation.groups.GroupsScreen
import com.das3kn.iz.ui.presentation.home.HomeScreen

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        startDestination = MainNavTarget.HomeScreen.route,
        navController = navController
    ){
        composable(MainNavTarget.HomeScreen.route){
            HomeScreen(
                navController = navController
            )
        }
        composable(MainNavTarget.GroupsScreen.route){
            GroupsScreen(
                navController = navController
            )
        }
        composable(MainNavTarget.ProfileScreen.route){
            ProfileScreen()
        }
        composable(MainNavTarget.GroupContentScreen.route){
            GroupsContentScreen()
        }
    }
}

enum class MainNavTarget(val route: String){
    HomeScreen("home_screen"),
    DetailScreen("detail_screen"),
    ProfileScreen("profile_screen"),
    GroupsScreen("groups_screen"),
    GroupContentScreen("group_content_screen")
}