package com.niallermoran.charteasysampleapp.charts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.niallermoran.charteasy.*
import com.niallermoran.charteasysampleapp.model.AppSettings
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
fun LineChartSampleMinimal(
    modifier: Modifier = Modifier,
    points: List<ChartPoint>,
    appSettings: AppSettings
) {

    /**
     * Creates a chart with minimum configuration.
     * Labels will be created for every point and the chart defaults to a line chart
     */
    Chart(
        modifier = modifier,
        leftAxisConfig = AxisConfig(
            dataPoints = points,
            showFillColour = appSettings.fillCharts
        )
    )
}

/**
 * Creates a very simple line chart with some formatting
 */
@Composable
fun LineChartSampleFormatted(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    points: List<ChartPoint>,
    minY: Float? = null,
    maxY: Float? = null
) {

    /**
     * Creates a chart with some configuration.
     * Labels will be created for every point and the chart defaults to a line chart
     * This chart defines the min and max to control the y-axis zoom for the chart
     */
    Chart(
        modifier = modifier,
        leftAxisConfig = AxisConfig(
            dataPoints = points,
            tickColor = Color.Gray,
            axisColor = Color.Gray,
            lineColor = Color.Blue,
            smoothLine = appSettings.smoothLineCharts,
            showFillColour = appSettings.fillCharts,
            fillBrush = Brush.verticalGradient(listOf(Color.Cyan, Color.Blue)),
            minY = minY,
            maxY = maxY
        ),
        bottomAxisConfig = BottomAxisConfig(
            tickColor = Color.Gray,
            axisColor = Color.Gray,
        )
    )
}

/**
 * Creates a very simple line chart with label formatting of time series data
 */
@Composable
fun LineChartTimeSeries(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    points: List<ChartPoint>,
    minY: Float? = null,
    maxY: Float? = null
) {

    /**
     * Creates a chart with configuration.
     * For the time series chart we are using a formatter to display the date in the format we want on the bottom axis
     */
    Chart(
        modifier = modifier.padding(end = 12.dp), // use some padding so last x-axis label is not clipped
        leftAxisConfig = AxisConfig(
            dataPoints = points,
            tickColor = Color.Gray,
            axisColor = Color.Gray,
            lineColor = Color.Blue,
            smoothLine = appSettings.smoothLineCharts,
            showFillColour = appSettings.fillCharts,
            fillBrush = Brush.verticalGradient(listOf(Color.Cyan, Color.Blue)),
            minY = minY,
            maxY = maxY
        ),
        bottomAxisConfig = BottomAxisConfig(
            tickColor = Color.Gray,
            axisColor = Color.Gray
        ),
        formatBottomAxisLabel = { _, x, _ ->
            // x represents an epoch in milliseconds
            val date = Date(x.toLong())
            val dateFormatter = SimpleDateFormat("MMM d", Locale.ENGLISH)
            dateFormatter.format(date)
        }
    )
}


/**
 * Creates a chart with configuration.
 * For the time series chart we are using a formatter to display the date in the format we want on the bottom axis
 * This chart also includes a right axis to display a second series of data as a line chart
 */
@Composable
fun MixedChartTimeSeries(
    modifier: Modifier = Modifier, appSettings: AppSettings,
    points: List<ChartPoint>,
    minY: Float? = null, maxY: Float? = null
) {

    /**
     * Creates a chart with minimum configuration.
     * Labels will be created for every point and the chart defaults to a line chart
     */
    MixedChart(
        modifier = modifier.padding(end = 12.dp), // use some padding so last x-axis label is not clipped
        leftAxisConfig = AxisConfig(
            dataPoints = points,
            tickColor = Color.Gray,
            axisColor = Color.Gray,
            lineColor = Color.Blue,
            smoothLine = appSettings.smoothLineCharts,
            showFillColour = appSettings.fillCharts,
            fillBrush = Brush.verticalGradient(listOf(Color.Cyan, Color.Blue)),
            type = AxisType.Bar,
            minY = minY,
            maxY = maxY
        ),
        rightAxisConfig = AxisConfig(
            dataPoints = points,
            tickColor = Color.Gray,
            axisColor = Color.Gray,
            lineColor = Color.Blue,
            smoothLine = appSettings.smoothLineCharts,
            showFillColour = false,
            type = AxisType.Line,
            minY = minY,
            maxY = maxY
        ),
        bottomAxisConfig = BottomAxisConfig(
            tickColor = Color.Gray,
            axisColor = Color.Gray
        ),
        formatBottomAxisLabel = { _, x, _ ->
            // x represents an epoch in milliseconds
            val date = Date(x.toLong())
            val dateFormatter = SimpleDateFormat("MMM d", Locale.ENGLISH)
            dateFormatter.format(date)
        }
    )
}


/**
 * Creates a very simple pie chart with label formatting of time series data
 */
@Composable
fun PieChart(modifier: Modifier = Modifier, points: List<PiePoint>) {

    /**
     * Creates a chart with minimum configuration.
     * Labels will be created for every point and the chart defaults to a line chart
     */
    PieChart(
        modifier = modifier.padding(end = 12.dp), // use some padding so last x-axis label is not clipped
        config = PieChartConfig(
            dataPoints = points,
            chartHeight = 300.dp,
            padding = PaddingValues(6.dp)
        ),
    )
}

/**
 * Creates a chart with configuration.
 * For the time series chart we are using a formatter to display the date in the format we want on the bottom axis
 * This chart also includes a right axis to display a second series of data as a line chart
 */
@Composable
fun BarChartTimeSeries(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    points: List<ChartPoint>,
    minY: Float? = null,
    maxY: Float? = null
) {

    /**
     * Creates a chart with minimum configuration.
     * Labels will be created for every point and the chart defaults to a line chart
     */
    Chart(
        modifier = modifier.padding(end = 12.dp), // use some padding so last x-axis label is not clipped
        leftAxisConfig = AxisConfig(
            dataPoints = points,
            tickColor = Color.Gray,
            axisColor = Color.Gray,
            lineColor = Color.Blue,
            smoothLine = appSettings.smoothLineCharts,
            showFillColour = appSettings.fillCharts,
            fillBrush = Brush.verticalGradient(listOf(Color.Cyan, Color.Blue)),
            type = AxisType.Bar,
            minY = minY,
            maxY = maxY
        ),
        bottomAxisConfig = BottomAxisConfig(
            tickColor = Color.Gray,
            axisColor = Color.Gray
        ),
        formatBottomAxisLabel = { _, x, _ ->
            // x represents an epoch in milliseconds
            val date = Date(x.toLong())
            val dateFormatter = SimpleDateFormat("MMM d", Locale.ENGLISH)
            dateFormatter.format(date)
        }
    )
}


@Composable
fun generateRandomIntegers(): ArrayList<ChartPoint> {

    val n = 6

    // generate some data
    val randomX = List(n) { Random.nextInt(0, 100) }
    val randomY = List(n) { Random.nextInt(100, 10000) }

    // create the chart points
    val points = ArrayList<ChartPoint>(n)
    for (i in 0..<n) {
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
fun generateTimeSeries(): ArrayList<ChartPoint> {

    val n = 6

    // get a 30 day date range
    val now = Date().time
    val then = now - (Duration.convert(30.0, DurationUnit.DAYS, DurationUnit.MILLISECONDS).toLong())

    // generate some data
    val randomX = List(n) { Random.nextLong(then, now) } // milliseconds on x axis
    val randomY = List(n) { Random.nextDouble(100.0, 125.0) }

    // create the chart points
    val points = ArrayList<ChartPoint>(n)
    for (i in 0..<n) {
        points.add(
            i, ChartPoint(
                xValue = randomX[i].toFloat(),
                yValue = randomY[i].toFloat()
            )
        )
    }
    return points
}
