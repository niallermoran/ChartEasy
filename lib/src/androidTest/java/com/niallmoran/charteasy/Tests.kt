package com.niallmoran.charteasy

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.niallermoran.charteasy.DataProvider
import com.niallermoran.charteasy.PieChart
import com.niallermoran.charteasy.PieChartConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Tests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val data = DataProvider()

    private val points = data.points
    private val timeSeries = data.timeSeries
    private val piePoints = data.piePoints

    /**
     * Create a min and max value for the y-axis so we have some buffer
     */
    val minY = points.minOf { it.yValue } * 0.95f
    val maxY = points.maxOf { it.yValue } * 1.05f

    /**
     * Create a min and max value for the y-axis so we have some buffer
     */
    val minYTimeSeries = timeSeries.minOf { it.yValue } * 0.95f
    val maxYTimeSeries = timeSeries.maxOf { it.yValue } * 1.05f

    @Test
    fun simpleLineChartTest() {
        composeTestRule.setContent {
            PieChart(modifier = Modifier.padding(12.dp), config = PieChartConfig(
                dataPoints = piePoints
            ) )
        }
    }

}