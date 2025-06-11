package be.ugent.idlab.predict.ocmt.egress.services

import java.util.*

fun Properties.resolve(name: String): String = getProperty(name)
    .replace(Regex("\\$\\{(.*?)}")) {
        System.getenv()[it.groupValues[1]] ?: ""
    }
