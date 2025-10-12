package com.das3kn.iz.ui.presentation.people

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget

@Composable
fun PeopleScreen(navController: NavHostController) {
    FriendsScreenContent(
        onBack = { navController.popBackStack() },
        onUserClick = { userId ->
            navController.navigate("${MainNavTarget.ProfileScreen.route}/$userId")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendsScreenContent(
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val allUsers = remember { sampleUsers }
    val friends = remember { mutableStateListOf(*allUsers.filter { it.isFollowing }.toTypedArray()) }
    val suggestions = remember { allUsers.filter { !it.isFollowing } }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf(FriendsTab.FRIENDS) }

    val filteredFriends by remember(searchQuery, friends) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) {
                friends.toList()
            } else {
                friends.filter { user ->
                    user.name.lowercase().contains(query) ||
                        user.username.lowercase().contains(query)
                }
            }
        }
    }

    val filteredSuggestions by remember(searchQuery) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            if (query.isEmpty()) {
                suggestions
            } else {
                suggestions.filter { user ->
                    user.name.lowercase().contains(query) ||
                        user.username.lowercase().contains(query)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Surface(
            shadowElevation = 2.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Arkadaşlar",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Arkadaş ara...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF8B5CF6),
                        cursorColor = Color(0xFF8B5CF6)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        val tabs = remember { FriendsTab.values() }
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.White,
            divider = {
                Divider(color = Color(0xFFE5E7EB))
            }
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = when (tab) {
                                FriendsTab.FRIENDS -> "Arkadaşlar (${friends.size})"
                                FriendsTab.SUGGESTIONS -> "Öneriler"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            FriendsTab.FRIENDS -> {
                FriendsList(
                    friends = filteredFriends,
                    searchActive = searchQuery.isNotBlank(),
                    onUserClick = onUserClick,
                    onUnfollow = { userId ->
                        friends.removeAll { it.id == userId }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            FriendsTab.SUGGESTIONS -> {
                SuggestionsList(
                    suggestions = filteredSuggestions,
                    onUserClick = onUserClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FriendsList(
    friends: List<FriendUser>,
    searchActive: Boolean,
    onUserClick: (String) -> Unit,
    onUnfollow: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (friends.isEmpty()) {
        val message = if (searchActive) {
            "Arkadaş bulunamadı"
        } else {
            "Henüz arkadaşınız yok"
        }
        EmptyState(
            message = message,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(friends, key = { it.id }) { friend ->
            UserRowContainer {
                FriendRow(
                    user = friend,
                    onUserClick = onUserClick,
                    onUnfollow = onUnfollow
                )
            }
        }
    }
}

@Composable
private fun SuggestionsList(
    suggestions: List<FriendUser>,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) {
        EmptyState(message = "Öneri bulunamadı", modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(suggestions, key = { it.id }) { suggestion ->
            UserRowContainer {
                SuggestionRow(
                    user = suggestion,
                    onUserClick = onUserClick
                )
            }
        }
    }
}

@Composable
private fun UserRowContainer(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        content()
        Divider(color = Color(0xFFE5E7EB))
    }
}

@Composable
private fun FriendRow(
    user: FriendUser,
    onUserClick: (String) -> Unit,
    onUnfollow: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UserAvatar(
            user = user,
            modifier = Modifier.clickable { onUserClick(user.id) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onUserClick(user.id) }
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { /* TODO: open message */ },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(44.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubble,
                    contentDescription = null,
                    tint = Color(0xFF4B5563)
                )
            }

            OutlinedButton(
                onClick = { onUnfollow(user.id) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(44.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonRemove,
                    contentDescription = null,
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    user: FriendUser,
    onUserClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UserAvatar(
            user = user,
            modifier = Modifier.clickable { onUserClick(user.id) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onUserClick(user.id) }
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            user.bio?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Button(
            onClick = { /* TODO: follow */ },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(text = "Takip Et")
        }
    }
}

@Composable
private fun UserAvatar(
    user: FriendUser,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = user.avatar,
            contentDescription = user.name,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        if (user.avatar.isBlank()) {
            Text(
                text = user.initials,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
    }
}

private enum class FriendsTab {
    FRIENDS,
    SUGGESTIONS
}

private data class FriendUser(
    val id: String,
    val name: String,
    val username: String,
    val avatar: String,
    val bio: String? = null,
    val isFollowing: Boolean
) {
    val initials: String
        get() = name.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString(separator = "") { it.first().uppercase() }
}

private val sampleUsers = listOf(
    FriendUser(
        id = "1",
        name = "Elif Yıldız",
        username = "elify",
        avatar = "https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=facearea&w=200&h=200&q=80",
        isFollowing = true
    ),
    FriendUser(
        id = "2",
        name = "Mehmet Ak",
        username = "mehmetak",
        avatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=facearea&w=200&h=200&q=80",
        isFollowing = true
    ),
    FriendUser(
        id = "3",
        name = "Deniz Kurt",
        username = "denizk",
        avatar = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=facearea&w=200&h=200&q=80",
        isFollowing = true
    ),
    FriendUser(
        id = "4",
        name = "Cem Demir",
        username = "cemdemir",
        avatar = "https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=facearea&w=200&h=200&q=80",
        bio = "UX Tasarımcı",
        isFollowing = false
    ),
    FriendUser(
        id = "5",
        name = "Selin Kara",
        username = "selink",
        avatar = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=facearea&w=200&h=200&q=80",
        bio = "İçerik üreticisi",
        isFollowing = false
    ),
    FriendUser(
        id = "6",
        name = "Onur Taş",
        username = "onurtas",
        avatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=facearea&w=200&h=200&q=80",
        bio = "Yazılım geliştirici",
        isFollowing = false
    )
)

@Preview(showBackground = true)
@Composable
private fun FriendsScreenPreview() {
    FriendsScreenContent(onBack = {}, onUserClick = {})
}
