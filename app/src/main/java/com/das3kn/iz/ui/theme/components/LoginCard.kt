package com.das3kn.iz.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.das3kn.iz.ui.theme.components.button.PrimaryButton
import com.das3kn.iz.ui.theme.components.textField.TextFieldWithIcon

@Composable
fun LoginCard(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White, shape = RoundedCornerShape(16.dp))
    ) {
        Text(
            text = "Giriş Yap",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )

        TextFieldWithIcon(
            value = username,
            onValueChange = {
                    username = it
            },
            leadingIcon = Icons.Outlined.Person
        )

        TextFieldWithIcon(
            value = password,
            onValueChange = {
                    password = it
            },
            leadingIcon = Icons.Filled.Lock
        )

        PrimaryButton(
            text = "Giriş Yap",
            onClick = {
                // Giriş işlemleri burada gerçekleştirilebilir
            },
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Text(text = "Hesabınız Yok Mu? Kayıt Olun", modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Preview
@Composable
private fun LoginCardPreview() {
    LoginCard()
}