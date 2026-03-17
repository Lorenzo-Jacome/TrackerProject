package com.example.trackerproject.data

data class Subtask(
    val name: String,
    val isCompleted: Boolean = false
)

data class Task(
    val id: String,
    val name: String,
    val subtasks: List<Subtask> = emptyList(),
    val isCompleted: Boolean = false,
    val dueDate: String? = null,
    val priority: String = "Main"
)

data class Habit(
    val id: String,
    val name: String,
    val timesPerWeek: Int,
    val color: Int,
    val completedDates: List<String> = emptyList()
)
