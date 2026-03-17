package com.example.trackerproject.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trackerproject.data.AppDataStore
import com.example.trackerproject.data.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusScreen(context: Context, modifier: Modifier = Modifier) {
    var tasks by remember { mutableStateOf(AppDataStore.getTasks(context)) }
    val completedTasks = tasks.filter { it.isCompleted }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        if (completedTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No completed tasks yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn {
                items(completedTasks, key = { it.id }) { task ->
                    CompletedTaskItem(
                        task = task,
                        onUncomplete = {
                            tasks = tasks.map { t ->
                                if (t.id == task.id) t.copy(isCompleted = false) else t
                            }.toMutableList()
                            AppDataStore.saveTasks(context, tasks)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CompletedTaskItem(task: Task, onUncomplete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = true, onCheckedChange = { onUncomplete() })
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = task.priority,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (task.dueDate != null) {
                    Text(
                        text = "Due: ${formatStatusDate(task.dueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private fun formatStatusDate(dateStr: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        output.format(input.parse(dateStr)!!)
    } catch (e: Exception) { dateStr }
}
