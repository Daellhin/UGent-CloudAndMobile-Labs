package be.ugent.idlab.predict.ocmt.android.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import be.ugent.idlab.predict.ocmt.android.MainActivity
import be.ugent.idlab.predict.ocmt.android.data.service.AttendanceService
import be.ugent.idlab.predict.ocmt.android.data.service.CountsService
import be.ugent.idlab.predict.ocmt.android.data.service.ForecastService
import be.ugent.idlab.predict.ocmt.android.data.service.SourcesService
import be.ugent.idlab.predict.ocmt.android.data.service.UserSession


val Context.userSession: UserSession
    get() = (this as MainActivity).userSession

@Composable
fun getUserSession() = LocalContext.current.userSession


val Context.sourcesService: SourcesService
    get() = (this as MainActivity).sourcesService

@Composable
fun getSourcesService() = LocalContext.current.sourcesService


val Context.countsService: CountsService
    get() = (this as MainActivity).countsService

@Composable
fun getCountsService() = LocalContext.current.countsService


val Context.attendanceService: AttendanceService
    get() = (this as MainActivity).attendanceService

@Composable
fun getAttendanceService() = LocalContext.current.attendanceService


val Context.forecastService: ForecastService
    get() = (this as MainActivity).forecastService

@Composable
fun getForecastService() = LocalContext.current.forecastService
