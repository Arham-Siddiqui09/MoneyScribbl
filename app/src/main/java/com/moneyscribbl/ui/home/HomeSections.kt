package com.moneyscribbl.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.theme.*

data class CategorySpend(
    val name: String,
    val amount: String, // formatted amount "₹#,##0.00"
    val iconLetter: String,
    val bgColor: Color,
    val fgColor: Color,
    val sharePercent: Int, // 0-100
    val emoji: String? = null
)

data class FolderItem(
    val name: String,
    val iconLetter: String,
    val emoji: String? = null,
    val spent: String? = null,
    val limit: String? = null,
    val rawName: String, // to use in callbacks
    val progress: Float = 0f
) {
    val hasLimit get() = limit != null
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = title,
                fontFamily = SpaceGrotesk,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = actionText,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onActionClick)
        )
    }
}

@Composable
fun TopCategoriesCard(
    categories: List<CategorySpend>
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            categories.forEachIndexed { index, cat ->
                CategoryRow(cat = cat)
                if (index != categories.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun CategoryRow(cat: CategorySpend) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cat.bgColor),
            contentAlignment = Alignment.Center
        ) {
            if (cat.emoji != null) {
                Text(
                    text = cat.emoji,
                    fontSize = 20.sp
                )
            } else {
                Text(
                    text = cat.iconLetter,
                    style = MaterialTheme.typography.titleMedium,
                    color = cat.fgColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cat.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = cat.amount,
                fontFamily = IBMPlexMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun Modifier.dashedBorder(width: Float, color: Color, shape: androidx.compose.ui.graphics.Shape) = this.drawWithContent {
    drawContent()
    drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        color = color,
        style = Stroke(
            width = width,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
        )
    )
}

@Composable
fun MyFoldersCard(
    folders: List<FolderItem>,
    onSetLimitClick: (String) -> Unit,
    onRenameFolderClick: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    isRemovableMap: Map<String, Boolean>
) {
    val maxVisible = 3
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            val visibleFolders = if (expanded) folders else folders.take(maxVisible)
            visibleFolders.forEachIndexed { index, folder ->
                FolderRow(
                    folder = folder,
                    onSetLimitClick = { onSetLimitClick(folder.rawName) },
                    onRenameFolderClick = { onRenameFolderClick(folder.rawName) },
                    onDeleteFolder = { onDeleteFolder(folder.rawName) },
                    isRemovable = isRemovableMap[folder.rawName] ?: false
                )
                if (index != visibleFolders.lastIndex || folders.size > maxVisible) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                }
            }
            
            if (folders.size > maxVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Show less" else "View all ${folders.size} folders",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Indigo
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Show less" else "View all folders",
                        tint = Indigo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FolderRow(
    folder: FolderItem,
    onSetLimitClick: () -> Unit,
    onRenameFolderClick: () -> Unit,
    onDeleteFolder: () -> Unit,
    isRemovable: Boolean
) {
    var showDropdown by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = Color(0xFFE4E7EC)
            val naturalGray = Color(0xFF6B7280)
            
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                if (!folder.hasLimit) {
                    drawCircle(
                        color = naturalGray,
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                        )
                    )
                } else {
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = naturalGray,
                        startAngle = -90f,
                        sweepAngle = (folder.progress * 360f).coerceIn(0f, 360f),
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            if (!folder.hasLimit) {
                if (folder.emoji != null) {
                    Text(
                        text = folder.emoji,
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = folder.iconLetter,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "${(folder.progress * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (folder.hasLimit) {
                Text(
                    text = "₹${folder.spent} / ₹${folder.limit} limit",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(

                text = "Set a limit →",

                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Indigo,
                    modifier = Modifier.clickable(onClick = onSetLimitClick)
                )
            }
        }
        Box {
            IconButton(
                onClick = { showDropdown = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Set limit", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        showDropdown = false
                        onSetLimitClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        showDropdown = false
                        onRenameFolderClick()
                    }
                )
                if (isRemovable) {
                    DropdownMenuItem(
                        text = { Text("Delete", color = ExpenseRed) },
                        onClick = {
                            showDropdown = false
                            onDeleteFolder()
                        }
                    )
                }
            }
        }
    }
}

