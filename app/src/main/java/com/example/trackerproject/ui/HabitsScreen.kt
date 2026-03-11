package com.example.trackerproject.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackerproject.data.AppDataStore
import com.example.trackerproject.data.Habit
import java.text.SimpleDateFormat
import java.util.*

private val COLOR_PALETTE = listOf(
    0xFF00E5FF.toInt(), // Neon Cyan
    0xFF39FF14.toInt(), // Neon Green
    0xFFFF3355.toInt(), // Neon Red
    0xFFFF6B00.toInt(), // Neon Orange
    0xFFFFE600.toInt(), // Neon Yellow
    0xFFBF00FF.toInt(), // Neon Purple
    0xFF00BFFF.toInt(), // Electric Blue
    0xFFFF0099.toInt(), // Neon Magenta
    0xFF00FF99.toInt(), // Neon Teal
    0xFF7FFF00.toInt(), // Neon Lime
    0xFFFF4500.toInt(), // Neon Orange-Red
    0xFF1AFFD5.toInt(), // Neon Turquoise
)

private val MONTH_LABELS = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

@Composable
fun HabitsScreen(context: Context, modifier: Modifier = Modifier) {
    var habits by remember { mutableStateOf(AppDataStore.getHabits(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val today = remember { sdf.format(Date()) }
    val weekStart = remember { getWeekStart() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "◆ HABITS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = today,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    thickness = 1.dp
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
                        }
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
            Icon(Icons.Default.Add, contentDescription = "Add Habit")
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
}

@Composable
private fun HabitItem(
    habit: Habit,
    habitColor: Color,
    doneToday: Boolean,
    doneThisWeek: Int,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onToggleToday: () -> Unit
) {
    val progress = (doneThisWeek.toFloat() / habit.timesPerWeek.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(habitColor)
            )
            Spacer(Modifier.width(12.dp))
            SquareCheckbox(checked = doneToday, onCheckedChange = onToggleToday, color = habitColor)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$doneThisWeek / ${habit.timesPerWeek}×  THIS WEEK",
                    style = MaterialTheme.typography.labelMedium,
                    color = habitColor.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onExpandToggle) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = habitColor.copy(alpha = 0.7f)
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(start = 3.dp)
                .clip(RectangleShape),
            color = habitColor,
            trackColor = habitColor.copy(alpha = 0.15f),
        )
        AnimatedVisibility(visible = isExpanded) {
            HabitYearCalendar(habit = habit, habitColor = habitColor)
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
private fun HabitYearCalendar(habit: Habit, habitColor: Color) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayCal = remember { Calendar.getInstance() }
    val todayStr = remember { sdf.format(todayCal.time) }
    val year = todayCal.get(Calendar.YEAR)
    val completedSet = habit.completedDates.toSet()

    data class WeekData(val days: List<String?>, val monthLabel: String?)

    val weeks = remember(year) {
        val result = mutableListOf<WeekData>()
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
        Column(
            modifier = Modifier.padding(top = 14.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            listOf("M", "", "W", "", "F", "", "S").forEach { label ->
                Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                    if (label.isNotEmpty()) {
                        Text(
                            label,
                            fontSize = 7.sp,
                            lineHeight = 8.sp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
        weeks.forEach { weekData ->
            Column(
                modifier = Modifier.padding(end = cellSpacing),
                verticalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.BottomStart) {
                    if (weekData.monthLabel != null) {
                        Text(
                            weekData.monthLabel,
                            fontSize = 7.sp,
                            lineHeight = 8.sp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
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
                                shape = RectangleShape
                            )
                            .then(
                                if (isToday) Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RectangleShape
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
    var selectedColor by remember { mutableStateOf(COLOR_PALETTE[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW HABIT") },
        text = {
            Column {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("HABIT NAME") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "TIMES PER WEEK",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { if (timesPerWeek > 1) timesPerWeek-- }) {
                        Text("−", style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        text = timesPerWeek.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    TextButton(onClick = { if (timesPerWeek < 7) timesPerWeek++ }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "COLOR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
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
                                    .clip(CutCornerShape(6.dp))
                                    .background(Color(colorInt))
                                    .then(
                                        if (isSelected) Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CutCornerShape(6.dp)
                                        ) else Modifier
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
            ) { Text("ADD") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

private fun getWeekStart(): String {
    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}
