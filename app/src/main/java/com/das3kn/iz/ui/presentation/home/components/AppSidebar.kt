package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import com.das3kn.iz.data.model.User
import com.das3kn.iz.ui.theme.components.LoginCard
import com.google.firebase.auth.FirebaseUser

/**
 * Represents the available destinations inside the sidebar navigation drawer.
 */
enum class SidebarDestination {
    Feed,
    Groups,
    Chats,
    Notifications,
    Profile,
    Friends,
    Saved,
    Settings,
    Help
}

private data class SidebarMenuItem(
    val destination: SidebarDestination,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppSidebar(
    currentUser: FirebaseUser?,
    userProfile: User?,
    isLoadingProfile: Boolean,
    selectedDestination: SidebarDestination,
    onNavigate: (SidebarDestination) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarUrl = remember(userProfile, currentUser) {
        userProfile?.profileImageUrl?.takeIf { it.isNotBlank() }
            ?: currentUser?.photoUrl?.toString()
    }
    val displayName = remember(userProfile, currentUser) {
        userProfile?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore('@')
    }
    val username = remember(userProfile, currentUser) {
        userProfile?.username?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore('@')
    }
    val followersCount = remember(userProfile) { userProfile?.followers?.size ?: 0 }
    val followingCount = remember(userProfile) { userProfile?.following?.size ?: 0 }

    val primaryItems = remember {
        listOf(
            SidebarMenuItem(
                destination = SidebarDestination.Feed,
                label = "Ana Sayfa",
                icon = Icons.Filled.Home
            ),
            SidebarMenuItem(
                destination = SidebarDestination.Groups,
                label = "Gruplar",
                icon = Icons.Filled.Groups
            ),
            SidebarMenuItem(
                destination = SidebarDestination.Chats,
                label = "Mesajlar",
                icon = Icons.Filled.ChatBubble
            ),
            SidebarMenuItem(
                destination = SidebarDestination.Notifications,
                label = "Bildirimler",
                icon = Icons.Filled.Notifications
            ),
            SidebarMenuItem(
                destination = SidebarDestination.Profile,
                label = "Profil",
                icon = Icons.Filled.Person
            )
        )
    }

    val secondaryItems = remember {
        listOf(
            SidebarMenuItem(
                destination = SidebarDestination.Friends,
                label = "Arkadaşlar",
                icon = Icons.Filled.PersonAdd
            ),
            SidebarMenuItem(
                destination = SidebarDestination.Saved,
                label = "Kaydedilenler",
                icon = Icons.Filled.Bookmark
            ),
            SidebarMenuItem(
                destination = SidebarDestination.Settings,
                label = "Ayarlar",
                icon = Icons.Filled.Settings
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(min = 280.dp, max = 340.dp)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
    ) {
        if (currentUser == null && userProfile == null) {
            LoginCard(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            )
        } else {
            SidebarProfileSection(
                displayName = displayName ?: "Kullanıcı",
                username = username,
                avatarUrl = avatarUrl,
                followers = followersCount,
                following = followingCount,
                isLoading = isLoadingProfile,
                onProfileClick = {
                    onNavigate(SidebarDestination.Profile)
                    onClose()
                }
            )
        }

        Divider(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        )

        SidebarMenuSection(
            title = null,
            items = primaryItems,
            selectedDestination = selectedDestination,
            onItemClick = { destination ->
                onNavigate(destination)
                onClose()
            }
        )

        Divider(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        )

        SidebarMenuSection(
            title = null,
            items = secondaryItems,
            selectedDestination = selectedDestination,
            onItemClick = { destination ->
                onNavigate(destination)
                onClose()
            }
        )

        SidebarMenuItemButton(
            label = "Yardım",
            isSelected = selectedDestination == SidebarDestination.Help,
            onClick = {
                onNavigate(SidebarDestination.Help)
                onClose()
            },
            icon = Icons.Filled.HelpOutline
        )

        Spacer(modifier = Modifier.weight(1f, fill = true))

        SidebarLogoutButton(
            onLogout = {
                onLogout()
                onClose()
            }
        )
    }
}

@Composable
private fun SidebarProfileSection(
    displayName: String,
    username: String?,
    avatarUrl: String?,
    followers: Int,
    following: Int,
    isLoading: Boolean,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = !isLoading) { onProfileClick() }
                .padding(12.dp)
        ) {
            SidebarAvatar(
                displayName = displayName,
                avatarUrl = avatarUrl,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                username?.let {
                    Text(
                        text = "@$it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SidebarStat(label = "Takip", value = following)
            SidebarStat(label = "Takipçi", value = followers)
        }
    }
}

@Composable
private fun SidebarAvatar(
    displayName: String,
    avatarUrl: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(72.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }

                avatarUrl != null -> {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profil fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    Text(
                        text = displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SidebarMenuSection(
    title: String?,
    items: List<SidebarMenuItem>,
    selectedDestination: SidebarDestination,
    onItemClick: (SidebarDestination) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        items.forEach { item ->
            SidebarMenuItemButton(
                label = item.label,
                icon = item.icon,
                isSelected = item.destination == selectedDestination,
                onClick = { onItemClick(item.destination) }
            )
        }
    }
}

@Composable
private fun SidebarMenuItemButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SidebarLogoutButton(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onLogout,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Logout,
            contentDescription = "Çıkış Yap",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = "Çıkış Yap",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

