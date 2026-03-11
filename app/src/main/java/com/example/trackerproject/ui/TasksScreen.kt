package com.example.trackerproject.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.example.trackerproject.data.AppDataStore
import com.example.trackerproject.data.Subtask
import com.example.trackerproject.data.Task

@Composable
fun TasksScreen(context: Context, modifier: Modifier = Modifier) {
    var tasks by remember { mutableStateOf(AppDataStore.getTasks(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "◆ TASKS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    thickness = 1.dp
                )
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(tasks) { taskIndex, task ->
                    TaskItem(
                        task = task,
                        isExpanded = task.id in expandedIds,
                        onExpandToggle = {
                            expandedIds = if (task.id in expandedIds)
                                expandedIds - task.id else expandedIds + task.id
                        },
                        onTaskToggle = {
                            val updated = tasks.toMutableList()
                            updated[taskIndex] = task.copy(isCompleted = !task.isCompleted)
                            tasks = updated
                            AppDataStore.saveTasks(context, tasks)
                        },
                        onSubtaskToggle = { subtaskIndex ->
                            val updatedSubtasks = task.subtasks.toMutableList()
                            updatedSubtasks[subtaskIndex] =
                                updatedSubtasks[subtaskIndex].copy(isCompleted = !updatedSubtasks[subtaskIndex].isCompleted)
                            val allDone = updatedSubtasks.all { it.isCompleted }
                            val updated = tasks.toMutableList()
                            updated[taskIndex] = task.copy(subtasks = updatedSubtasks, isCompleted = allDone)
                            tasks = updated
                            AppDataStore.saveTasks(context, tasks)
                        },
                        onLongPress = { editingTask = task }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 0.5.dp
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            shape = CutCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
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
            onConfirm = { name, subtaskNames ->
                val newTask = Task(
                    id = AppDataStore.newId(),
                    name = name,
                    subtasks = subtaskNames.map { Subtask(it) }
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
            onSave = { updatedSubtasks ->
                val allDone = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.isCompleted }
                tasks = tasks.map {
                    if (it.id == task.id) it.copy(subtasks = updatedSubtasks, isCompleted = allDone) else it
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
    val accent = if (task.isCompleted)
        MaterialTheme.colorScheme.outlineVariant
    else
        MaterialTheme.colorScheme.primary

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .combinedClickable(onClick = {}, onLongClick = onLongPress),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Spacer(Modifier.width(12.dp))
            SquareCheckbox(checked = task.isCompleted, onCheckedChange = onTaskToggle, color = accent)
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.isCompleted)
                    MaterialTheme.colorScheme.outlineVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 14.dp, bottom = 14.dp)
            )
            if (task.subtasks.isNotEmpty()) {
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
        }
        AnimatedVisibility(visible = isExpanded) {
            Column {
                task.subtasks.forEachIndexed { index, subtask ->
                    val subAccent = if (subtask.isCompleted)
                        MaterialTheme.colorScheme.outlineVariant
                    else
                        MaterialTheme.colorScheme.secondary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquareCheckbox(
                            checked = subtask.isCompleted,
                            onCheckedChange = { onSubtaskToggle(index) },
                            color = subAccent
                        )
                        Text(
                            text = subtask.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (subtask.isCompleted)
                                MaterialTheme.colorScheme.outlineVariant
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SquareCheckbox(checked: Boolean, onCheckedChange: () -> Unit, color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .border(1.5.dp, if (checked) color else color.copy(alpha = 0.5f), RectangleShape)
            .background(if (checked) color.copy(alpha = 0.2f) else Color.Transparent, RectangleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, RectangleShape)
            )
        }
    }
}

@Composable
private fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (List<Subtask>) -> Unit,
    onDelete: () -> Unit
) {
    val subtasks = remember { mutableStateListOf<Subtask>().also { it.addAll(task.subtasks) } }
    var subtaskInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("DELETE TASK?") },
            text = { Text("\"${task.name}\" and all its subtasks will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("DELETE") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("CANCEL") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(task.name.uppercase()) },
        text = {
            Column {
                if (subtasks.isNotEmpty()) {
                    Text(
                        "SUBTASKS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    subtasks.forEachIndexed { index, subtask ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "▸ ${subtask.name}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 2.dp)
                            )
                            IconButton(onClick = { subtasks.removeAt(index) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove subtask",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        label = { Text("ADD SUBTASK") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (subtaskInput.isNotBlank()) {
                            subtasks.add(Subtask(subtaskInput.trim()))
                            subtaskInput = ""
                        }
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add subtask",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("DELETE TASK")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(subtasks.toList()) }) { Text("SAVE") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, List<String>) -> Unit) {
    var taskName by remember { mutableStateOf("") }
    var subtaskInput by remember { mutableStateOf("") }
    val subtasks = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW TASK") },
        text = {
            Column {
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("TASK NAME") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                if (subtasks.isNotEmpty()) {
                    Text(
                        "SUBTASKS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    subtasks.forEach { st ->
                        Text(
                            text = "  ▸ $st",
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
                        label = { Text("ADD SUBTASK") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (subtaskInput.isNotBlank()) {
                            subtasks.add(subtaskInput.trim())
                            subtaskInput = ""
                        }
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add subtask",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (taskName.isNotBlank()) onConfirm(taskName.trim(), subtasks.toList()) },
                enabled = taskName.isNotBlank()
            ) { Text("ADD") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
