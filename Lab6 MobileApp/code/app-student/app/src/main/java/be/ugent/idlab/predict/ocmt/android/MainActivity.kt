package be.ugent.idlab.predict.ocmt.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import be.ugent.idlab.predict.ocmt.android.data.service.AttendanceService
import be.ugent.idlab.predict.ocmt.android.data.service.CountsService
import be.ugent.idlab.predict.ocmt.android.data.service.ForecastService
import be.ugent.idlab.predict.ocmt.android.data.service.SourcesService
import be.ugent.idlab.predict.ocmt.android.data.service.UserSession
import be.ugent.idlab.predict.ocmt.android.ui.screen.Root
import be.ugent.idlab.predict.ocmt.android.ui.theme.OCMTTheme

class MainActivity : ComponentActivity() {
    val userSession = UserSession(context = this, scope = lifecycleScope)
    val sourcesService = SourcesService(context = this, scope = lifecycleScope)
    val countsService = CountsService(context = this)
    val attendanceService = AttendanceService(context = this)
    val forecastService = ForecastService(context = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userSession.loadToken()
        setContent {
            OCMTTheme {
                Root()
            }
        }
    }
}
