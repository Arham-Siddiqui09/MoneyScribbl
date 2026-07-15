package com.moneyscribbl.ui.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.data.TransactionType
import com.moneyscribbl.ui.theme.ExpenseRed
import com.moneyscribbl.ui.theme.IncomeGreen
import com.moneyscribbl.ui.theme.IndigoPrimary

data class TransactionDetailUiModel(
    val type: TransactionType,
    val emoji: String? = null,
    val vectorIcon: ImageVector? = null,
    val title: String,
    val sourceLabel: String,
    val amountString: String,
    val typeLabel: String,
    val category: String,
    val secondaryFieldLabel: String,
    val secondaryFieldValue: String,
    val dateTimeString: String
)

@Composable
fun TransactionDetailSheet(
    uiModel: TransactionDetailUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = uiModel.type == TransactionType.INCOME
    val tintColor = if (isIncome) IncomeGreen else ExpenseRed

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag handle
       /* Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 16.dp)
                .width(40.dp)
                .height(4.dp)
                .background(Color.LightGray.copy(alpha = 0.5f), CircleShape)
        )*/

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Row: Icon Badge + Title/Source + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(tintColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiModel.emoji != null) {
                        Text(text = uiModel.emoji, fontSize = 22.sp)
                    } else if (uiModel.vectorIcon != null) {
                        Icon(imageVector = uiModel.vectorIcon, contentDescription = null, tint = tintColor)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title and Source
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = uiModel.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = com.moneyscribbl.ui.theme.SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = uiModel.sourceLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Close Button
               /* Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }*/
            }

            // Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = uiModel.amountString,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                    color = tintColor
                )
                
                // Credit/Debit Pill
                Box(
                    modifier = Modifier
                        .background(tintColor.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = uiModel.typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tintColor,
                        fontSize = 10.sp
                    )
                }
            }

            // Detail Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category
                    DetailRow(label = "Category") {
                        Box(
                            modifier = Modifier
                                .background(tintColor.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "• ${uiModel.category}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = tintColor
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    
                    // Secondary Field
                    DetailRow(label = uiModel.secondaryFieldLabel) {
                        Text(
                            text = uiModel.secondaryFieldValue,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Date & Time
                    DetailRow(label = "Date & time") {
                        Text(
                            text = uiModel.dateTimeString,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit transaction",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ExpenseRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Footer Text
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Swipe down to close",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}
