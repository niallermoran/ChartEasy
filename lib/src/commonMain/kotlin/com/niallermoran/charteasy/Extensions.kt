package com.niallermoran.charteasy

import kotlin.math.pow
import kotlin.math.round

/**
 * Returns number with a defined decimal place
 */
fun Double.toPrecision(precision: Int): Double {
    val factor = 10.0.pow(precision.toDouble())
    return round (this * factor) / factor
}
