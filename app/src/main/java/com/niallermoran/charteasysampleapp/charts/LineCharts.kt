package com.niallermoran.charteasysampleapp.charts

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.niallmoran.charteasy.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

/**
 * Creates a very simple line chart with all default configurations
 */
@Composable
fun LineChartSampleMinimal(modifier:Modifier = Modifier) {

    val points = generateRandomIntegers()

    /**
     * Creates a chart with minimum configuration.
     * Labels will be created for every point and the chart defaults to a line chart
     */
    ChartEasy(
        modifier=modifier,
        config = Config(
            leftAxisConfig = AxisConfig(
                dataPoints = points
            )
        )
    )
}

/**
 * Creates a very simple line chart with some formatting
 */
@Composable
fun LineChartSampleFormatted(modifier:Modifier = Modifier) {

    val points = generateRandomIntegers()

    /**
     * Creates a chart with minimum configuration.
     * Labels will be created for every point and the chart defaults to a line chart
     */
    ChartEasy(
        modifier=modifier,
        config = Config(
            leftAxisConfig = AxisConfig(
                dataPoints = points,
                tickColor = Color.DarkGray,
                axisColor = Color.DarkGray,
                lineColor = Color.Magenta,
                fillColour = Color.Magenta
            ),
            bottomAxisConfig = BottomAxisConfig(
                tickColor = Color.DarkGray,
                axisColor = Color.DarkGray,
            )
        )
    )
}

/**
 * Creates a very simple line chart with label formatting of time series data
 */
@Composable
fun LineChartTimeSeries(modifier:Modifier = Modifier) {

    val points = generateTimeSeries()

    /**
     * Creates a chart with minimum configuration.
     * Labels will be created for every point and the chart defaults to a line chart
     */
    ChartEasy(
        modifier=modifier.padding(end=12.dp), // use some padding so last x-axis label is not clipped
        config = Config(
            leftAxisConfig = AxisConfig(
                dataPoints = points,
                tickColor = Color.DarkGray,
                axisColor = Color.DarkGray,
                lineColor = Color.Blue,
                fillColour = Color.Blue
            ),
            bottomAxisConfig = BottomAxisConfig(
                tickColor = Color.DarkGray,
                axisColor = Color.DarkGray
            )
        ),
        formatBottomAxisLabel = { index, x, point ->
            // x represents an epoch in milliseconds
            val date = Date(x.toLong())
            val dateFormatter = SimpleDateFormat("MMM d", Locale.ENGLISH)
            dateFormatter.format(date)
        }
    )
}


@Composable
private fun generateRandomIntegers(): ArrayList<ChartPoint> {
    // generate some data
    val randomX = List(10) { Random.nextInt(0,100) }
    val randomY = List(10) { Random.nextInt(100, 10000) }

    // create the chart points
    val points = ArrayList<ChartPoint>(10)
    for (i in 0..9) {
        points.add(
            i, ChartPoint(
                xValue = randomX[i].toFloat(),
                yValue = randomY[i].toFloat()
            )
        )
    }
    return points
}

@OptIn(ExperimentalTime::class)
@Composable
private fun generateTimeSeries(): ArrayList<ChartPoint> {

    val n = 8

    // get a 30 day date range
    val now = Date().time
    val then = now - (Duration.convert(30.0, DurationUnit.DAYS, DurationUnit.MILLISECONDS).toLong())

    // generate some data
    val randomX = List(n) { Random.nextLong(then,now) } // milliseconds on x axis
    val randomY = List(n) { Random.nextDouble(100.0, 125.0) }

    // create the chart points
    val points = ArrayList<ChartPoint>(n)
    for (i in 0..n-1) {
        points.add(
            i, ChartPoint(
                xValue = randomX[i].toFloat(),
                yValue = randomY[i].toFloat()
            )
        )
    }
    return points
}