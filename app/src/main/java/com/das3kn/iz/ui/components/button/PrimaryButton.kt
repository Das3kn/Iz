package com.das3kn.iz.ui.components.button

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String
) {
    Button(
        onClick = { onClick.invoke() },
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    PrimaryButton(text = "", modifier = Modifier, onClick = {})
}