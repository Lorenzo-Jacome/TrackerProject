package com.example.trackerproject.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trackerproject.data.AppDataStore
import com.example.trackerproject.data.Subtask
import com.example.trackerproject.data.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(context: Context, modifier: Modifier = Modifier) {
    var tasks by remember { mutableStateOf(AppDataStore.getTasks(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }

    val mainTasks = tasks.filter { !it.isCompleted && it.priority == "Main" }
    val sideTasks = tasks.filter { !it.isCompleted && it.priority == "Side" }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (mainTasks.isNotEmpty()) {
                    item { SectionHeader("MAIN") }
                    items(mainTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            isExpanded = task.id in expandedIds,
                            onExpandToggle = {
                                expandedIds = if (task.id in expandedIds)
                                    expandedIds - task.id else expandedIds + task.id
                            },
                            onTaskToggle = {
                                tasks = tasks.map { t ->
                                    if (t.id == task.id) t.copy(isCompleted = true) else t
                                }.toMutableList()
                                AppDataStore.saveTasks(context, tasks)
                            },
                            onSubtaskToggle = { subtaskIndex ->
                                val updatedSubtasks = task.subtasks.toMutableList()
                                updatedSubtasks[subtaskIndex] =
                                    updatedSubtasks[subtaskIndex].copy(isCompleted = !updatedSubtasks[subtaskIndex].isCompleted)
                                val allDone = updatedSubtasks.all { it.isCompleted }
                                tasks = tasks.map { t ->
                                    if (t.id == task.id) t.copy(subtasks = updatedSubtasks, isCompleted = allDone) else t
                                }.toMutableList()
                                AppDataStore.saveTasks(context, tasks)
                            },
                            onLongPress = { editingTask = task }
                        )
                        HorizontalDivider()
                    }
                }

                if (sideTasks.isNotEmpty()) {
                    item { SectionHeader("SIDE") }
                    items(sideTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            isExpanded = task.id in expandedIds,
                            onExpandToggle = {
                                expandedIds = if (task.id in expandedIds)
                                    expandedIds - task.id else expandedIds + task.id
                            },
                            onTaskToggle = {
                                tasks = tasks.map { t ->
                                    if (t.id == task.id) t.copy(isCompleted = true) else t
                                }.toMutableList()
                                AppDataStore.saveTasks(context, tasks)
                            },
                            onSubtaskToggle = { subtaskIndex ->
                                val updatedSubtasks = task.subtasks.toMutableList()
                                updatedSubtasks[subtaskIndex] =
                                    updatedSubtasks[subtaskIndex].copy(isCompleted = !updatedSubtasks[subtaskIndex].isCompleted)
                                val allDone = updatedSubtasks.all { it.isCompleted }
                                tasks = tasks.map { t ->
                                    if (t.id == task.id) t.copy(subtasks = updatedSubtasks, isCompleted = allDone) else t
                                }.toMutableList()
                                AppDataStore.saveTasks(context, tasks)
                            },
                            onLongPress = { editingTask = task }
                        )
                        HorizontalDivider()
                    }
                }

                if (mainTasks.isEmpty() && sideTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No tasks yet. Tap + to add one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, subtaskNames, dueDate, priority ->
                val newTask = Task(
                    id = AppDataStore.newId(),
                    name = name,
                    subtasks = subtaskNames.map { Subtask(it) },
                    dueDate = dueDate,
                    priority = priority
                )
                tasks = (tasks + newTask).toMutableList()
                AppDataStore.saveTasks(context, tasks)
                showAddDialog = false
            }
        )
    }

    editingTask?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { editingTask = null },
            onSave = { updatedSubtasks, updatedDueDate, updatedPriority ->
                val allDone = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.isCompleted }
                tasks = tasks.map {
                    if (it.id == task.id) it.copy(
                        subtasks = updatedSubtasks,
                        isCompleted = allDone,
                        dueDate = updatedDueDate,
                        priority = updatedPriority
                    ) else it
                }.toMutableList()
                AppDataStore.saveTasks(context, tasks)
                editingTask = null
            },
            onDelete = {
                tasks = tasks.filter { it.id != task.id }.toMutableList()
                AppDataStore.saveTasks(context, tasks)
                editingTask = null
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    )
    HorizontalDivider()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskItem(
    task: Task,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onTaskToggle: () -> Unit,
    onSubtaskToggle: (Int) -> Unit,
    onLongPress: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onTaskToggle() })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(text = task.name, style = MaterialTheme.typography.bodyLarge)
                if (task.dueDate != null) {
                    Text(
                        text = "Due: ${formatDate(task.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            if (task.subtasks.isNotEmpty()) {
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
        }
        AnimatedVisibility(visible = isExpanded) {
            Column {
                task.subtasks.forEachIndexed { index, subtask ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = subtask.isCompleted,
                            onCheckedChange = { onSubtaskToggle(index) }
                        )
                        Text(
                            text = subtask.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (List<Subtask>, String?, String) -> Unit,
    onDelete: () -> Unit
) {
    val subtasks = remember { mutableStateListOf<Subtask>().also { it.addAll(task.subtasks) } }
    var subtaskInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var priority by remember { mutableStateOf(task.priority) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate?.let { dateStringToMillis(it) }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = millisToDateString(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
        return
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete task?") },
            text = { Text("\"${task.name}\" and all its subtasks will be removed.") },
            confirmButton = {
                TextButton(onClick = onDelete) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(task.name) },
        text = {
            Column {
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Main", "Side").forEach { p ->
                        OutlinedButton(
                            onClick = { priority = p },
                            colors = if (priority == p)
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else
                                ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) { Text(p) }
                        if (p == "Main") Spacer(Modifier.width(8.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (subtasks.isNotEmpty()) {
                    Text("Subtasks:", style = MaterialTheme.typography.labelMedium)
                    subtasks.forEachIndexed { index, subtask ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${subtask.name}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f).padding(top = 2.dp)
                            )
                            IconButton(onClick = { subtasks.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove subtask")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        label = { Text("Add subtask") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (subtaskInput.isNotBlank()) {
                            subtasks.add(Subtask(subtaskInput.trim()))
                            subtaskInput = ""
                        }
                    }) { Icon(Icons.Default.Add, contentDescription = "Add subtask") }
                }
                Spacer(Modifier.height(12.dp))
                if (dueDate != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Due: ${formatDate(dueDate!!)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showDatePicker = true }) { Text("Change") }
                        IconButton(onClick = { dueDate = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove date")
                        }
                    }
                } else {
                    TextButton(onClick = { showDatePicker = true }) { Text("Set due date") }
                }
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Delete task")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(subtasks.toList(), dueDate, priority) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>, String?, String) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var subtaskInput by remember { mutableStateOf("") }
    val subtasks = remember { mutableStateListOf<String>() }
    var dueDate by remember { mutableStateOf<String?>(null) }
    var priority by remember { mutableStateOf("Main") }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = millisToDateString(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Main", "Side").forEach { p ->
                        OutlinedButton(
                            onClick = { priority = p },
                            colors = if (priority == p)
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else
                                ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) { Text(p) }
                        if (p == "Main") Spacer(Modifier.width(8.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (subtasks.isNotEmpty()) {
                    Text("Subtasks:", style = MaterialTheme.typography.labelMedium)
                    subtasks.forEach { st ->
                        Text(
                            text = "  • $st",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        label = { Text("Add subtask (optional)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (subtaskInput.isNotBlank()) {
                            subtasks.add(subtaskInput.trim())
                            subtaskInput = ""
                        }
                    }) { Icon(Icons.Default.Add, contentDescription = "Add subtask") }
                }
                Spacer(Modifier.height(12.dp))
                if (dueDate != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Due: ${formatDate(dueDate!!)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showDatePicker = true }) { Text("Change") }
                        IconButton(onClick = { dueDate = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove date")
                        }
                    }
                } else {
                    TextButton(onClick = { showDatePicker = true }) { Text("Set due date") }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (taskName.isNotBlank()) onConfirm(taskName.trim(), subtasks.toList(), dueDate, priority) },
                enabled = taskName.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatDate(dateStr: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        output.format(input.parse(dateStr)!!)
    } catch (e: Exception) { dateStr }
}

private fun millisToDateString(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}

private fun dateStringToMillis(dateStr: String): Long {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
}
