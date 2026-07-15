package com.moneyscribbl.ui.transactions.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneyscribbl.ui.theme.IndigoPrimary

@Composable
fun FormFilterRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) IndigoPrimary else Color.Transparent,
                animationSpec = spring(),
                label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(),
                label = "chipText"
            )
            val modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(bgColor)
                .clickable { onSelect(option) }
            
            val finalModifier = if (!isSelected) {
                modifier.border(1.dp, IndigoPrimary.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
            } else modifier

            Text(
                text = option,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                modifier = finalModifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}
