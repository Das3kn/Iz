package com.das3kn.iz.ui.theme.components.button

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.das3kn.iz.R

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String
) {
    OutlinedButton(
        onClick = { onClick.invoke() },
        modifier = modifier
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.roboto_medium)),
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(vertical = 1.dp)
        )
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    SecondaryButton(text = "", modifier = Modifier, onClick = {})
}