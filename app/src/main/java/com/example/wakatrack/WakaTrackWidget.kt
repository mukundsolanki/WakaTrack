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
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            val totalTimeSpent = sharedPreferences.getString(TOTAL_TIME_KEY, "0 hours") ?: "0 hours"
            val projectList = sharedPreferences.getString(PROJECT_LIST_KEY, "") ?: ""

            val projects = projectList.split("\n").filter { it.isNotBlank() }

            val views = RemoteViews(context.packageName, R.layout.wakatrack_widget)

            views.setTextViewText(R.id.tv_total_time, "Total Time: $totalTimeSpent")

            val projectNameTextViews = listOf(
                R.id.tv_project_name_1,
                R.id.tv_project_name_2,
                R.id.tv_project_name_3
            )

            val projectTimeTextViews = listOf(
                R.id.tv_project_time_1,
                R.id.tv_project_time_2,
                R.id.tv_project_time_3
            )

            for (i in projectNameTextViews.indices) {
                if (i < projects.size) {
                    val projectParts = projects[i].split(":")
                    val projectName = projectParts[0].trim()
                    val projectTime = projectParts.getOrElse(1) { "0h 0m" }.trim()

                    views.setTextViewText(projectNameTextViews[i], projectName)
                    views.setTextViewText(projectTimeTextViews[i], projectTime)

                    views.setViewVisibility(projectNameTextViews[i], View.VISIBLE)
                    views.setViewVisibility(projectTimeTextViews[i], View.VISIBLE)
                } else {
                    // Hide the views if there are no more projects
                    views.setViewVisibility(projectNameTextViews[i], View.GONE)
                    views.setViewVisibility(projectTimeTextViews[i], View.GONE)
                }
            }

            // Update the widget with the new layout
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
