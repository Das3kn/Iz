package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.das3kn.iz.R

@Composable
fun ContentFunctions(
    onLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.empty_like_icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.comment_svgrepo_com),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.repost_svgrepo_com),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.options_svgrepo_com),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}