package be.ugent.idlab.predict.ocmt.android.ui.screen

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import be.ugent.idlab.predict.ocmt.android.data.errors

@Composable
fun ErrorSnackbar() {
    val snackbarHost = remember { SnackbarHostState() }
    val exception = errors.collectAsState(initial = null).value
    LaunchedEffect(key1 = exception) {
        if (exception != null) {
            println("Displaying snackbar for exception:")
            exception.printStackTrace()
            snackbarHost.showSnackbar(
                message = "${exception::class.simpleName} - ${exception.message?.lineSequence()?.firstOrNull()}",
                withDismissAction = true
            )
        }
    }
    SnackbarHost(hostState = snackbarHost)
}
