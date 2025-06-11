package be.ugent.idlab.predict.ocmt.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import be.ugent.idlab.predict.ocmt.android.ui.components.RouteEntry
import be.ugent.idlab.predict.ocmt.android.ui.components.RouteWidget
import be.ugent.idlab.predict.ocmt.android.util.getSourcesService
import be.ugent.idlab.predict.ocmt.android.util.getUserSession


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen() {
    val sourcesService = getSourcesService()
    val sources by sourcesService.sources.collectAsState()
    val entries = remember(sources) {
        sources.map { source ->
            RouteEntry(
                title = "Counts from $source",
                description = "Go see the historical observations on its dedicated screen!",
                route = "observations?source=${source}"
            )
        }
    }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        LargeTopAppBar(
            title = {
                Text("Home")
            },
            actions = {
                val session = getUserSession()
                IconButton(onClick = { session.logout() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null
                    )
                }
                IconButton(onClick = { sourcesService.refresh() }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null
                    )
                }
            },
            scrollBehavior = topAppBarScrollBehavior
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        ) {
            items(entries) { entry ->
                RouteWidget(entry = entry)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
