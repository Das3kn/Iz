package com.das3kn.iz.ui.components.textField
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun TextFieldWithIcon(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    placeholderText: String = "Placeholder",
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange.invoke(it) },
        leadingIcon = {
            Image(
                imageVector = leadingIcon,
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray),
            )
        },
        shape = RoundedCornerShape(20.dp), // Kavisli kenarlar
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = Color.LightGray,
            cursorColor = Color.Black,
            disabledLeadingIconColor = Color.LightGray,
            focusedLeadingIconColor = Color.LightGray,
            unfocusedLeadingIconColor = Color.LightGray
        ),
        placeholder = {
                      Text(text = placeholderText, color = Color.Gray)
        },
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp)) // Dış çerçeveyi iyice belirginleştirir
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTextFieldWithIcon() {
    TextFieldWithIcon(
        value = "",
        onValueChange = {},
        leadingIcon = Icons.Outlined.Person,
        placeholderText = "Placeholder Text"
    )
}
