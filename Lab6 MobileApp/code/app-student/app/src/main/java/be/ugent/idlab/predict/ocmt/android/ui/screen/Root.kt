package be.ugent.idlab.predict.ocmt.android.ui.screen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import be.ugent.idlab.predict.ocmt.android.ui.composition.LocalNavigation
import be.ugent.idlab.predict.ocmt.android.ui.composition.NavigationHost
import be.ugent.idlab.predict.ocmt.android.util.getUserSession

@Composable
fun Root() {
    val controller = NavigationHost.create()
    CompositionLocalProvider(
        LocalNavigation provides controller
    ) {
        Scaffold(
            snackbarHost = { ErrorSnackbar() }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .imePadding()
            ) {
                /**
                 * Logic deciding what UI to show: login screen if the currently collected user value
                 *  is null, or the regular navigation-based screens otherwise
                 */
                val signedIn = getUserSession().state.collectAsState().value != null
                if (!signedIn) {
                    AuthenticationScreen()
                } else {
                    NavHost(
                        navController = controller(),
                        startDestination = "landing",
                        enterTransition = { slideInHorizontally { it } + fadeIn() },
                        exitTransition = { slideOutHorizontally { - it } + fadeOut() },
                        popEnterTransition = { slideInHorizontally { - it } + fadeIn() },
                        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
                    ) {
                        /**
                         * The landing page, showing the various data sources as obtained by
                         *  the SourcesService
                         */
                        composable("landing") {
                            LandingScreen()
                        }
                        /**
                         * An individual source's detail page
                         */
                        composable(
                            route = "observations?source={source}",
                            arguments = listOf(
                                navArgument("source") { type = NavType.StringType; nullable = true }
                            )
                        ) {
                            val source = it.arguments?.getString("source")
                            source ?: return@composable
                            ObservationsScreen(source = source)
                        }
                    }
                }
            }
        }
    }
}
