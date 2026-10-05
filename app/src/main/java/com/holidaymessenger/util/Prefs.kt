package com.holidaymessenger.util

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS_NAME = "holiday_messenger_prefs"
    const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    const val KEY_LAST_SEEN_LOG_ID = "last_seen_log_id"
    const val KEY_STREAK_COUNT = "streak_count"
    const val KEY_LAST_STREAK_HOLIDAY = "last_streak_holiday"

    fun get(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingComplete(context: Context): Boolean =
        get(context).getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingComplete(context: Context, value: Boolean) {
        get(context).edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()
    }

    fun getLastSeenLogId(context: Context): Long =
        get(context).getLong(KEY_LAST_SEEN_LOG_ID, -1L)

    fun setLastSeenLogId(context: Context, value: Long) {
        get(context).edit().putLong(KEY_LAST_SEEN_LOG_ID, value).apply()
    }
}
