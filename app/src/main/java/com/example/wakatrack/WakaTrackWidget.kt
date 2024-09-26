package com.example.wakatrack

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.RemoteViews

class WakaTrackWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Loop through each widget ID and update accordingly
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Optionally handle widget deletion (e.g., clean up shared preferences)
        super.onDeleted(context, appWidgetIds)
    }

    companion object {
        private const val SHARED_PREFS_KEY = "WakaTrackPrefs"
        private const val TOTAL_TIME_KEY = "total_time_spent_today"
        private const val PROJECT_LIST_KEY = "project_list_today"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            // Load data from SharedPreferences
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            val totalTimeSpent = sharedPreferences.getString(TOTAL_TIME_KEY, "0 hours") ?: "0 hours"
            val projectList = sharedPreferences.getString(PROJECT_LIST_KEY, "") ?: ""

            // Split and filter project list (ignore empty lines)
            val projects = projectList.split("\n").filter { it.isNotBlank() }

            // Set up the widget layout
            val views = RemoteViews(context.packageName, R.layout.wakatrack_widget)

            // Update total time spent today
            views.setTextViewText(R.id.tv_total_time, "Total Time: $totalTimeSpent")

            // Update individual project times (up to 3 projects)
            val projectTextViews = listOf(
                R.id.tv_project_1,
                R.id.tv_project_2,
                R.id.tv_project_3
            )

            // Dynamically display or hide project TextViews
            for (i in projectTextViews.indices) {
                if (i < projects.size) {
                    views.setTextViewText(projectTextViews[i], projects[i])
                    views.setViewVisibility(projectTextViews[i], View.VISIBLE)
                } else {
                    views.setViewVisibility(projectTextViews[i], View.GONE)
                }
            }

            // Update the widget with the new layout
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
