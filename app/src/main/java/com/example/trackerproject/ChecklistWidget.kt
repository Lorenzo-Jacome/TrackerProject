package com.example.trackerproject

// This class is kept for manifest compatibility.
// The active tasks widget is TasksWidget.kt
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

@Deprecated("Replaced by TasksWidget")
class ChecklistWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) TasksWidget.updateWidget(context, appWidgetManager, id)
    }
}