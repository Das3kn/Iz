package com.das3kn.iz.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.das3kn.iz.ui.presentation.GroupsContentScreen
import com.das3kn.iz.ui.presentation.ProfileScreen
import com.das3kn.iz.ui.presentation.blogs.BlogContentScreen
import com.das3kn.iz.ui.presentation.blogs.BlogsScreen
import com.das3kn.iz.ui.presentation.chat.ChatListScreen
import com.das3kn.iz.ui.presentation.chat.ChatScreen
import com.das3kn.iz.ui.presentation.chat.NewChatScreen
import com.das3kn.iz.ui.presentation.groups.GroupsScreen
import com.das3kn.iz.ui.presentation.home.HomeScreen
import com.das3kn.iz.ui.presentation.people.PeopleScreen
import com.das3kn.iz.ui.presentation.posts.CreatePostScreen

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
            ProfileScreen(
                navController = navController
            )
        }
        composable(MainNavTarget.GroupContentScreen.route){
            GroupsContentScreen(
                navController = navController
            )
        }
        composable(MainNavTarget.PeopleScreen.route) {
            PeopleScreen(
                navController = navController
            )
        }
        composable(MainNavTarget.BlogsScreen.route) {
            BlogsScreen(
                navController = navController
            )
        }
        composable(MainNavTarget.BlogContent.route) {
            BlogContentScreen(
                navController = navController
            )
        }
        composable(MainNavTarget.ChatListScreen.route) {
            ChatListScreen(
                onNavigateToChat = { chatId ->
                    navController.navigate("${MainNavTarget.ChatScreen.route}/$chatId")
                },
                onNavigateToNewChat = {
                    navController.navigate(MainNavTarget.NewChatScreen.route)
                }
            )
        }
        composable(MainNavTarget.NewChatScreen.route) {
            NewChatScreen(
                onNavigateToChat = { chatId ->
                    navController.navigate("${MainNavTarget.ChatScreen.route}/$chatId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "${MainNavTarget.ChatScreen.route}/{chatId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(MainNavTarget.CreatePostScreen.route) {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

enum class MainNavTarget(val route: String){
    HomeScreen("home_screen"),
    DetailScreen("detail_screen"),
    ProfileScreen("profile_screen"),
    GroupsScreen("groups_screen"),
    GroupContentScreen("group_content_screen"),
    PeopleScreen("people_screen"),
    BlogsScreen("blogs_screen"),
    BlogContent("blog_content"),
    ChatListScreen("chat_list_screen"),
    ChatScreen("chat_screen"),
    NewChatScreen("new_chat_screen"),
    CreatePostScreen("create_post_screen")
}