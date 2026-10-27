package com.app.core.domain

interface ReminderScheduler {
    fun scheduleReminder(recipeId: String, recipeName: String, triggerAtMillis: Long)
    fun cancelReminder(recipeId: String)
}
