package be.ugent.idlab.predict.ocmt.egress

import be.ugent.idlab.predict.ocmt.egress.services.Forecasting
import be.ugent.idlab.predict.ocmt.egress.services.Influx
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    // manually starting the sync jobs
    Forecasting.start()
    EngineMain.main(args)
    Forecasting.stop()
    Influx.close()
}
