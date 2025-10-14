package com.das3kn.iz.ui.presentation.groups

import com.das3kn.iz.data.model.Group

fun Group.toUiModel(currentUserId: String?): GroupUiModel {
    val isMember = currentUserId != null &&
        (memberIds.contains(currentUserId) || pendingMemberIds.contains(currentUserId) || invitedUserIds.contains(currentUserId) || adminId == currentUserId)

    return GroupUiModel(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        profileImageUrl = profileImageUrl.ifBlank { imageUrl },
        membersCount = if (membersCount > 0) membersCount else memberIds.size,
        postsCount = postsCount,
        isJoined = isMember,
        admin = GroupUserUiModel(
            id = adminId,
            name = adminName,
            username = adminUsername,
            avatarUrl = adminAvatarUrl
        ),
        isPrivate = isPrivate,
        memberIds = memberIds,
        invitedUserIds = invitedUserIds,
        pendingMemberIds = pendingMemberIds
    )
}
