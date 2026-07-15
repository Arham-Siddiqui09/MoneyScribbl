package com.moneyscribbl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.moneyscribbl.ui.home.components.WeeklyExpenseChart
import com.moneyscribbl.ui.theme.moneyscribblTheme
import org.junit.Rule
import org.junit.Test

class WeeklyExpenseChartTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersAllWeekdayLabels() {
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        composeRule.setContent {
            moneyscribblTheme {
                WeeklyExpenseChart(
                    data = listOf(120f, 80f, 140f, 60f, 200f, 40f, 90f),
                    labels = labels,
                    currentDayIndex = 4
                )
            }
        }

        labels.forEach { label ->
            composeRule.onNodeWithText(label).assertIsDisplayed()
        }
    }
}

