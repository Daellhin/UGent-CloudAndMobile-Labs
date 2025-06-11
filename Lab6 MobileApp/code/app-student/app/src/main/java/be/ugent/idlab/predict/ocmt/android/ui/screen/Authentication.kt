package be.ugent.idlab.predict.ocmt.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import be.ugent.idlab.predict.ocmt.android.data.Credentials
import be.ugent.idlab.predict.ocmt.android.util.getUserSession

@Composable
fun AuthenticationScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.Center)
        ) {
            Text(
                text = "Log in",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            TextField(
                label = { Text("Username") },
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                isError = username.contains(" "),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                label = { Text("Password") },
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = password.contains(" "),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val session = getUserSession()
                var enabled by remember { mutableStateOf(true) }
                TextButton(
                    enabled = enabled &&
                            username.isNotEmpty() && !username.contains(" ") &&
                            password.isNotEmpty() && !password.contains(" "),
                    onClick = {
                        enabled = false
                        val credentials = Credentials(
                            username = username,
                            password = password
                        )
                        session.register(
                            credentials = credentials,
                            onSuccess = { enabled = true },
                            onFailure = { enabled = true }
                        )
                    }
                ) { Text(text = "Register") }
                Button(
                    enabled = enabled &&
                            username.isNotEmpty() && !username.contains(" ") &&
                            password.isNotEmpty() && !password.contains(" "),
                    onClick = {
                        enabled = false
                        val credentials = Credentials(
                            username = username,
                            password = password
                        )
                        session.login(
                            credentials = credentials,
                            onSuccess = { enabled = true },
                            onFailure = { enabled = true }
                        )
                    }
                ) { Text(text = "Login") }
            }
        }
    }
}
