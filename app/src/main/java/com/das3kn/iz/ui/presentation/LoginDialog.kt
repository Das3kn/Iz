package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.das3kn.iz.ui.theme.components.textField.TextFieldWithIcon

@Composable
fun LoginDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Column {
            /*TextFieldWithIcon(
                value = ,
                onValueChange = ,
                leadingIcon =
            )

            TextFieldWithIcon(
                value = ,
                onValueChange = ,
                leadingIcon =
            )*/
        }
    }
}