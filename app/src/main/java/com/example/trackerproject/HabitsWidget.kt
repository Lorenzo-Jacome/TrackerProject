package com.example.trackerproject

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.trackerproject.data.AppDataStore
import java.text.SimpleDateFormat
import java.util.*

class HabitsWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.habits_widget_layout)
            val habits = AppDataStore.getHabits(context)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val itemIds = listOf(R.id.item_0, R.id.item_1, R.id.item_2, R.id.item_3, R.id.item_4)
            for (i in itemIds.indices) {
                val habit = habits.getOrNull(i)
                val text = if (habit != null) {
                    val done = today in habit.completedDates
                    "${if (done) "✅" else "⬜"} ${habit.name}"
                } else ""
                views.setTextViewText(itemIds[i], text)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
