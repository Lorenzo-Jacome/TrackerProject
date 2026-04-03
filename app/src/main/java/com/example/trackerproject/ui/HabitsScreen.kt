package com.example.trackerproject.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackerproject.R
import com.example.trackerproject.data.AppDataStore
import com.example.trackerproject.data.Habit
import java.text.SimpleDateFormat
import java.util.*

private val GeistPixelLine = FontFamily(Font(R.font.geistpixel_line))

@Composable
private fun HabitSquareCheckbox(checked: Boolean, color: Color, onCheckedChange: () -> Unit) {
    val size = 20.dp
    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (checked) Modifier.background(color)
                else Modifier.border(2.dp, Color.White)
            )
            .clickable { onCheckedChange() }
    ) {
        if (checked) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val padding = w * 0.22f
                drawLine(
                    color = Color.Black,
                    start = Offset(padding, h - padding),
                    end = Offset(w - padding, padding),
                    strokeWidth = w * 0.12f,
                    cap = StrokeCap.Square
                )
            }
        }
    }
}

private val COLOR_PALETTE = listOf(
    0xFFEF5350.toInt(), // Red
    0xFFFF7043.toInt(), // Deep Orange
    0xFFFFCA28.toInt(), // Amber
    0xFF66BB6A.toInt(), // Green
    0xFF26C6DA.toInt(), // Cyan
    0xFF42A5F5.toInt(), // Blue
    0xFF7E57C2.toInt(), // Deep Purple
    0xFFEC407A.toInt(), // Pink
    0xFF8D6E63.toInt(), // Brown
    0xFF78909C.toInt(), // Blue Grey
    0xFFFFFFFF.toInt(), // White
    0xFF212121.toInt(), // Near Black
)

private val MONTH_LABELS = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

@Composable
fun HabitsScreen(context: Context, modifier: Modifier = Modifier) {
    var habits by remember { mutableStateOf(AppDataStore.getHabits(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val today = remember { sdf.format(Date()) }
    val weekStart = remember { getWeekStart() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFF0963))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = Color.White
                    )
                )
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(habits) { index, habit ->
                    val doneToday = today in habit.completedDates
                    val doneThisWeek = habit.completedDates.count { it >= weekStart && it <= today }
                    HabitItem(
                        habit = habit,
                        habitColor = Color(habit.color),
                        doneToday = doneToday,
                        doneThisWeek = doneThisWeek,
                        isExpanded = habit.id in expandedIds,
                        onExpandToggle = {
                            expandedIds = if (habit.id in expandedIds)
                                expandedIds - habit.id else expandedIds + habit.id
                        },
                        onToggleToday = {
                            val updatedDates = habit.completedDates.toMutableList()
                            if (doneToday) updatedDates.remove(today) else updatedDates.add(today)
                            val updated = habits.toMutableList()
                            updated[index] = habit.copy(completedDates = updatedDates)
                            habits = updated
                            AppDataStore.saveHabits(context, habits)
                        },
                        onLongPress = { editingHabit = habit }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            shape = androidx.compose.ui.graphics.RectangleShape,
            containerColor = Color(0xFF1F00FF),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = Color.White)
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, timesPerWeek, color ->
                val newHabit = Habit(
                    id = AppDataStore.newId(),
                    name = name,
                    timesPerWeek = timesPerWeek,
                    color = color
                )
                habits = (habits + newHabit).toMutableList()
                AppDataStore.saveHabits(context, habits)
                showAddDialog = false
            }
        )
    }

    editingHabit?.let { habit ->
        EditHabitDialog(
            habit = habit,
            onDismiss = { editingHabit = null },
            onSave = { name, timesPerWeek, color ->
                habits = habits.map { if (it.id == habit.id) it.copy(name = name, timesPerWeek = timesPerWeek, color = color) else it }.toMutableList()
                AppDataStore.saveHabits(context, habits)
                editingHabit = null
            },
            onDelete = {
                habits = habits.filter { it.id != habit.id }.toMutableList()
                AppDataStore.saveHabits(context, habits)
                editingHabit = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitItem(
    habit: Habit,
    habitColor: Color,
    doneToday: Boolean,
    doneThisWeek: Int,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onToggleToday: () -> Unit,
    onLongPress: () -> Unit
) {
    val progress = (doneThisWeek.toFloat() / habit.timesPerWeek.toFloat()).coerceIn(0f, 1f)
    val streak = remember(habit.completedDates) { computeWeekStreak(habit, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    val greyColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HabitSquareCheckbox(checked = doneToday, color = habitColor, onCheckedChange = { onToggleToday() })
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = "${streak}W_STREAK",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
                    color = if (streak == 0) Color(0xFFD6071D) else greyColor
                )
                Text(
                    text = "$doneThisWeek | ${habit.timesPerWeek} THIS_WEEK",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
                    color = greyColor
                )
            }
            IconButton(onClick = onExpandToggle) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .height(4.dp)
        ) {
            drawRect(color = habitColor.copy(alpha = 0.2f), size = this.size)
            drawRect(color = habitColor, size = this.size.copy(width = this.size.width * progress))
        }
        AnimatedVisibility(visible = isExpanded) {
            HabitYearCalendar(habit = habit, habitColor = habitColor)
        }
    }
}

@Composable
private fun HabitYearCalendar(habit: Habit, habitColor: Color) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayCal = remember { Calendar.getInstance() }
    val todayStr = remember { sdf.format(todayCal.time) }
    val year = todayCal.get(Calendar.YEAR)
    val completedSet = habit.completedDates.toSet()

    // Build weeks: each is a list of 7 date strings (or null if outside the year)
    // plus a month label if this week starts a new month
    data class WeekData(val days: List<String?>, val monthLabel: String?)

    val weeks = remember(year) {
        val result = mutableListOf<WeekData>()
        // Start on the Monday on or before Jan 1
        val cal = Calendar.getInstance().apply {
            set(year, Calendar.JANUARY, 1)
            val dow = get(Calendar.DAY_OF_WEEK)
            val back = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
            add(Calendar.DAY_OF_YEAR, -back)
        }
        val endCal = Calendar.getInstance().apply { set(year, Calendar.DECEMBER, 31) }
        var lastMonth = -1
        while (!cal.after(endCal)) {
            val days = (0..6).map {
                val day = cal.clone() as Calendar
                cal.add(Calendar.DAY_OF_YEAR, 1)
                if (day.get(Calendar.YEAR) == year) sdf.format(day.time) else null
            }
            // Show month label on the week containing the 1st of that month
            val firstDate = days.firstOrNull { it != null }
            val monthLabel = if (firstDate != null) {
                val tempCal = Calendar.getInstance().apply { time = sdf.parse(firstDate)!! }
                val m = tempCal.get(Calendar.MONTH)
                val dom = tempCal.get(Calendar.DAY_OF_MONTH)
                if (m != lastMonth && dom <= 7) { lastMonth = m; MONTH_LABELS[m] } else null
            } else null
            result.add(WeekData(days, monthLabel))
        }
        result
    }

    val cellSize = 11.dp
    val cellSpacing = 2.dp

    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        // Day-of-week labels column
        Column(
            modifier = Modifier.padding(top = 14.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            listOf("M", "", "W", "", "F", "", "S").forEach { label ->
                Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                    if (label.isNotEmpty()) {
                        Text(label, fontSize = 7.sp, lineHeight = 8.sp)
                    }
                }
            }
        }
        // Week columns
        weeks.forEach { weekData ->
            Column(
                modifier = Modifier.padding(end = cellSpacing),
                verticalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                // Month label row
                Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.BottomStart) {
                    if (weekData.monthLabel != null) {
                        Text(weekData.monthLabel, fontSize = 7.sp, lineHeight = 8.sp)
                    }
                }
                // Day cells
                weekData.days.forEach { dateStr ->
                    val isCompleted = dateStr != null && dateStr in completedSet
                    val isToday = dateStr == todayStr
                    val isFuture = dateStr != null && dateStr > todayStr
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .background(
                                color = when {
                                    dateStr == null -> Color.Transparent
                                    isCompleted    -> habitColor
                                    isFuture       -> Color.Transparent
                                    else           -> habitColor.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(2.dp)
                            )
                            .then(
                                if (isToday) Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    RoundedCornerShape(2.dp)
                                ) else Modifier
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String, Int, Int) -> Unit) {
    var habitName by remember { mutableStateOf("") }
    var timesPerWeek by remember { mutableStateOf(3) }
    var selectedColor by remember { mutableStateOf(COLOR_PALETTE[5]) } // default blue

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.ui.graphics.RectangleShape,
        title = { Text("NEW HABIT", fontFamily = GeistPixelLine) },
        text = {
            Column {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Times per week", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { if (timesPerWeek > 1) timesPerWeek-- }) {
                        Text("-", style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        text = timesPerWeek.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    TextButton(onClick = { if (timesPerWeek < 7) timesPerWeek++ }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                COLOR_PALETTE.chunked(6).forEach { rowColors ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        rowColors.forEach { colorInt ->
                            val isSelected = colorInt == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorInt))
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColor = colorInt }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (habitName.isNotBlank()) onConfirm(habitName.trim(), timesPerWeek, selectedColor) },
                enabled = habitName.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditHabitDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int) -> Unit,
    onDelete: () -> Unit
) {
    var habitName by remember { mutableStateOf(habit.name) }
    var timesPerWeek by remember { mutableStateOf(habit.timesPerWeek) }
    var selectedColor by remember { mutableStateOf(habit.color) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = androidx.compose.ui.graphics.RectangleShape,
            title = { Text("Delete habit?") },
            text = { Text("\"${habit.name}\" will be permanently removed.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.ui.graphics.RectangleShape,
        title = { Text(habit.name, fontFamily = GeistPixelLine) },
        text = {
            Column {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Times per week", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { if (timesPerWeek > 1) timesPerWeek-- }) {
                        Text("-", style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        text = timesPerWeek.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    TextButton(onClick = { if (timesPerWeek < 7) timesPerWeek++ }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                COLOR_PALETTE.chunked(6).forEach { rowColors ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        rowColors.forEach { colorInt ->
                            val isSelected = colorInt == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorInt))
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColor = colorInt }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Delete habit")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (habitName.isNotBlank()) onSave(habitName.trim(), timesPerWeek, selectedColor) },
                enabled = habitName.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun computeWeekStreak(habit: Habit, todayStr: String): Int {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.parse(todayStr) ?: return 0
    val cal = Calendar.getInstance().apply { time = today }
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
    var streak = 0
    repeat(53) {
        val weekEndCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 6) }
        val ws = sdf.format(cal.time)
        val we = sdf.format(weekEndCal.time)
        val count = habit.completedDates.count { it >= ws && it <= we }
        if (count >= habit.timesPerWeek) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -7)
        } else {
            return streak
        }
    }
    return streak
}

private fun getWeekStart(): String {
    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}
