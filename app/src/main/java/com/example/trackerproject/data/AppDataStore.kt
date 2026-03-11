package com.example.trackerproject.data

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.example.trackerproject.HabitsWidget
import com.example.trackerproject.TasksWidget
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object AppDataStore {
    private const val PREFS_NAME = "tracker_data"
    private const val KEY_TASKS = "tasks"
    private const val KEY_HABITS = "habits"

    fun newId(): String = UUID.randomUUID().toString()

    // --- Tasks ---

    fun getTasks(context: Context): MutableList<Task> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TASKS, "[]") ?: "[]"
        return parseTasks(json)
    }

    fun saveTasks(context: Context, tasks: List<Task>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_TASKS, encodeTasks(tasks)).apply()
        notifyTasksWidget(context)
    }

    private fun parseTasks(json: String): MutableList<Task> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val stArr = obj.getJSONArray("subtasks")
            val subtasks = (0 until stArr.length()).map { j ->
                val st = stArr.getJSONObject(j)
                Subtask(st.getString("name"), st.getBoolean("isCompleted"))
            }
            Task(obj.getString("id"), obj.getString("name"), subtasks, obj.getBoolean("isCompleted"))
        }.toMutableList()
    }

    private fun encodeTasks(tasks: List<Task>): String {
        val arr = JSONArray()
        for (task in tasks) {
            val obj = JSONObject()
            obj.put("id", task.id)
            obj.put("name", task.name)
            obj.put("isCompleted", task.isCompleted)
            val stArr = JSONArray()
            for (st in task.subtasks) {
                val stObj = JSONObject()
                stObj.put("name", st.name)
                stObj.put("isCompleted", st.isCompleted)
                stArr.put(stObj)
            }
            obj.put("subtasks", stArr)
            arr.put(obj)
        }
        return arr.toString()
    }

    // --- Habits ---

    fun getHabits(context: Context): MutableList<Habit> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HABITS, "[]") ?: "[]"
        return parseHabits(json)
    }

    fun saveHabits(context: Context, habits: List<Habit>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_HABITS, encodeHabits(habits)).apply()
        notifyHabitsWidget(context)
    }

    private fun parseHabits(json: String): MutableList<Habit> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val datesArr = obj.getJSONArray("completedDates")
            val dates = (0 until datesArr.length()).map { j -> datesArr.getString(j) }
            Habit(obj.getString("id"), obj.getString("name"), obj.getInt("timesPerWeek"), obj.optInt("color", 0xFF42A5F5.toInt()), dates)
        }.toMutableList()
    }

    private fun encodeHabits(habits: List<Habit>): String {
        val arr = JSONArray()
        for (habit in habits) {
            val obj = JSONObject()
            obj.put("id", habit.id)
            obj.put("name", habit.name)
            obj.put("timesPerWeek", habit.timesPerWeek)
            obj.put("color", habit.color)
            val datesArr = JSONArray()
            for (date in habit.completedDates) datesArr.put(date)
            obj.put("completedDates", datesArr)
            arr.put(obj)
        }
        return arr.toString()
    }

    // --- Widget updates ---

    private fun notifyTasksWidget(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, TasksWidget::class.java))
        for (id in ids) TasksWidget.updateWidget(context, manager, id)
    }

    private fun notifyHabitsWidget(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, HabitsWidget::class.java))
        for (id in ids) HabitsWidget.updateWidget(context, manager, id)
    }
}
