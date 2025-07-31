package pl.soulsnaps.util

import kotlin.math.PI

fun Float.toRadians(): Float = (this * PI.toFloat() / 180f)

/**
 * Extension function to convert radians to degrees.
 */
fun Float.toDegrees(): Float = (this * 180f / PI.toFloat())

/**
 * Extension function to convert degrees to radians (Double version).
 */
fun Double.toRadians(): Double = (this * PI / 180.0)

/**
 * Extension function to convert radians to degrees (Double version).
 */
fun Double.toDegrees(): Double = (this * 180.0 / PI)