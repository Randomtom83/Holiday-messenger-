package com.holidaymessenger.util

import kotlin.math.min

object JoyMeterRules {
    const val POINTS_PER_SQUAD_MEMBER = 5
    const val POINTS_PER_ENABLED_HOLIDAY = 10
    const val POINTS_PER_SENT = 20
    const val MAX_POINTS = 200

    data class Breakdown(
        val squadCount: Int,
        val enabledHolidayCount: Int,
        val sentCount: Int
    ) {
        val total: Int
            get() = squadCount * POINTS_PER_SQUAD_MEMBER +
                    enabledHolidayCount * POINTS_PER_ENABLED_HOLIDAY +
                    sentCount * POINTS_PER_SENT

        val percent: Float
            get() = min(total / MAX_POINTS.toFloat(), 1f)
    }

    fun compute(squadCount: Int, enabledHolidayCount: Int, sentCount: Int) =
        Breakdown(squadCount, enabledHolidayCount, sentCount)
}
