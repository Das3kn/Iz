package com.das3kn.iz.ui.presentation.groups

import com.das3kn.iz.data.model.MediaType
import com.das3kn.iz.data.model.Post

data class GroupUserUiModel(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String,
)

data class GroupUiModel(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val profileImageUrl: String,
    val membersCount: Int,
    val postsCount: Int,
    val isJoined: Boolean,
    val admin: GroupUserUiModel,
    val isPrivate: Boolean = false,
    val memberIds: List<String> = emptyList(),
    val invitedUserIds: List<String> = emptyList(),
    val pendingMemberIds: List<String> = emptyList(),
)

data class GroupDetailUiModel(
    val group: GroupUiModel,
    val members: List<GroupUserUiModel>,
    val posts: List<Post>,
)

object GroupMockData {

    val currentUser = GroupUserUiModel(
        id = "1",
        name = "Elif Kaya",
        username = "elifkaya",
        avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&h=400&fit=crop",
    )

    private val ayseDemir = GroupUserUiModel(
        id = "2",
        name = "Ayşe Demir",
        username = "aysedemir",
        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=400&fit=crop",
    )

    private val ahmetYilmaz = GroupUserUiModel(
        id = "3",
        name = "Ahmet Yılmaz",
        username = "ahmetyilmaz",
        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&h=400&fit=crop",
    )

    private val mehmetKaya = GroupUserUiModel(
        id = "4",
        name = "Mehmet Kaya",
        username = "mehmetkaya",
        avatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=400&h=400&fit=crop",
    )

    private val nazliAydin = GroupUserUiModel(
        id = "5",
        name = "Nazlı Aydın",
        username = "nazli.dev",
        avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&h=400&fit=crop",
    )

    private val keremOz = GroupUserUiModel(
        id = "6",
        name = "Kerem Öz",
        username = "keremoz",
        avatarUrl = "https://images.unsplash.com/photo-1544723795-3fb6469f5b39?w=400&h=400&fit=crop",
    )

    private val groups = listOf(
        GroupUiModel(
            id = "tech-ai",
            name = "Yapay Zeka Tutkunları",
            description = "AI, ML ve veri bilimi meraklılarının bir araya geldiği topluluk.",
            imageUrl = "https://images.unsplash.com/photo-1517430816045-df4b7de11d1d?w=1200&h=600&fit=crop",
            profileImageUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&h=400&fit=crop",
            membersCount = 128,
            postsCount = 42,
            isJoined = true,
            admin = currentUser,
            isPrivate = false,
            memberIds = listOf(currentUser.id, ahmetYilmaz.id, ayseDemir.id, mehmetKaya.id),
        ),
        GroupUiModel(
            id = "designers",
            name = "Ürün Tasarımcıları",
            description = "Tasarım trendleri, figma tüyoları ve portfolyo paylaşımları.",
            imageUrl = "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200&h=600&fit=crop",
            profileImageUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=400&fit=crop",
            membersCount = 86,
            postsCount = 28,
            isJoined = false,
            admin = GroupUserUiModel(
                id = "7",
                name = "Mert Yıldız",
                username = "mertyildiz",
                avatarUrl = "https://images.unsplash.com/photo-1519345182560-3f2917c472ef?w=400&h=400&fit=crop",
            ),
            isPrivate = true,
            memberIds = listOf("7", ayseDemir.id, keremOz.id),
            invitedUserIds = listOf(currentUser.id),
        ),
        GroupUiModel(
            id = "mobile-dev",
            name = "Mobil Geliştiriciler",
            description = "Android, iOS ve cross-platform geliştirme üzerine sohbetler.",
            imageUrl = "https://images.unsplash.com/photo-1517433456452-f9633a875f6f?w=1200&h=600&fit=crop",
            profileImageUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&h=400&fit=crop",
            membersCount = 210,
            postsCount = 56,
            isJoined = true,
            admin = nazliAydin,
            memberIds = listOf(nazliAydin.id, mehmetKaya.id, currentUser.id),
        ),
        GroupUiModel(
            id = "startup-tr",
            name = "Startup Türkiye",
            description = "Girişimciler ve yatırımcılar için deneyim paylaşımı alanı.",
            imageUrl = "https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?w=1200&h=600&fit=crop",
            profileImageUrl = "https://images.unsplash.com/photo-1544723795-3fb6469f5b39?w=400&h=400&fit=crop",
            membersCount = 340,
            postsCount = 75,
            isJoined = false,
            admin = keremOz,
            memberIds = listOf(keremOz.id, ahmetYilmaz.id, currentUser.id),
        ),
    )

    private val groupMembers: Map<String, List<GroupUserUiModel>> = mapOf(
        "tech-ai" to listOf(ahmetYilmaz, ayseDemir, mehmetKaya, currentUser),
        "designers" to listOf(ayseDemir, keremOz, currentUser),
        "mobile-dev" to listOf(mehmetKaya, nazliAydin, currentUser),
        "startup-tr" to listOf(keremOz, ahmetYilmaz, currentUser),
    )

    private val groupPosts: Map<String, List<Post>> = mapOf(
        "tech-ai" to listOf(
            Post(
                id = "g1",
                userId = ayseDemir.id,
                username = ayseDemir.name,
                userProfileImage = ayseDemir.avatarUrl,
                content = "Yeni AI teknolojileri hakkında ne düşünüyorsunuz?",
                createdAt = System.currentTimeMillis() - 30 * 60 * 1000,
                likes = List(45) { "like_$it" },
                commentCount = 12,
                shares = 3,
            ),
            Post(
                id = "g2",
                userId = ahmetYilmaz.id,
                username = ahmetYilmaz.name,
                userProfileImage = ahmetYilmaz.avatarUrl,
                content = "Bugün yeni bir proje başlattım, heyecanlıyım! 🚀",
                mediaUrls = listOf("https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&h=600&fit=crop"),
                mediaType = MediaType.IMAGE,
                createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
                likes = List(78) { "like_g2_$it" },
                commentCount = 23,
                shares = 15,
            ),
        ),
        "mobile-dev" to listOf(
            Post(
                id = "g3",
                userId = nazliAydin.id,
                username = nazliAydin.name,
                userProfileImage = nazliAydin.avatarUrl,
                content = "Compose Multiplatform hakkında ne düşünüyorsunuz?",
                createdAt = System.currentTimeMillis() - 90 * 60 * 1000,
                likes = List(32) { "like_g3_$it" },
                commentCount = 8,
                shares = 2,
            ),
        ),
    )

    fun initialGroups(): List<GroupUiModel> = groups.map { it.copy() }

    fun groupDetail(groupId: String): GroupDetailUiModel? {
        val group = groups.find { it.id == groupId } ?: return null
        val members = groupMembers[groupId] ?: emptyList()
        val posts = groupPosts[groupId] ?: emptyList()
        return GroupDetailUiModel(group = group, members = members, posts = posts)
    }
}
