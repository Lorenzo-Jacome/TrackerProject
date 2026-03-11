package com.example.trackerproject

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.trackerproject.data.AppDataStore

class TasksWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val tasks = AppDataStore.getTasks(context)
            val pending = tasks.filter { !it.isCompleted }.take(5)
            val itemIds = listOf(R.id.item_0, R.id.item_1, R.id.item_2, R.id.item_3, R.id.item_4)
            for (i in itemIds.indices) {
                val task = pending.getOrNull(i)
                views.setTextViewText(itemIds[i], if (task != null) "• ${task.name}" else "")
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
