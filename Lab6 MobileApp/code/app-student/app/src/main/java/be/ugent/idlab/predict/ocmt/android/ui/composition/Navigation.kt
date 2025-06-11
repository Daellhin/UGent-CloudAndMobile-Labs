package be.ugent.idlab.predict.ocmt.android.ui.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

class NavigationHost private constructor(
    private val controller: NavHostController
) {

    fun navigate(route: String) = controller.navigate(route)

    operator fun invoke() = controller

    companion object {

        @Composable
        fun create(): NavigationHost {
            val controller = rememberNavController()
            return remember {
                NavigationHost(
                    controller = controller
                )
            }
        }

    }

}

val LocalNavigation = compositionLocalOf<NavigationHost> {
    throw Error("No default navigation available")
}
