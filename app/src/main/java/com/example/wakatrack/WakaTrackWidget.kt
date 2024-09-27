package com.example.wakatrack

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.RemoteViews
import android.util.Log

class WakaTrackWidget : AppWidgetProvider() {

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

            var totalMinutes = 0
            val projectTimesInMinutes = mutableListOf<Int>()

            for (project in projects) {
                val projectParts = project.split(":")
                val projectTime = projectParts.getOrElse(1) { "0h 0m" }.trim()

                val timeParts = projectTime.split(" ")
                var minutes = 0
                for (part in timeParts) {
                    when {
                        part.endsWith("h") -> minutes += part.removeSuffix("h").toInt() * 60
                        part.endsWith("m") -> minutes += part.removeSuffix("m").toInt()
                    }
                }

                projectTimesInMinutes.add(minutes)
                totalMinutes += minutes
            }

            // Print scaled times for each project on a scale of 100
            if (totalMinutes > 0) {
                for (i in projectTimesInMinutes.indices) {
                    val scaledTime = (projectTimesInMinutes[i].toDouble() / totalMinutes) * 100
                    Log.d("WakaTrackWidget", "Project ${i + 1} Time on scale of 100: $scaledTime")
                }
            }

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
