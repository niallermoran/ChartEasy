package com.niallermoran.charteasysampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.niallermoran.charteasy.AxisType
import com.niallermoran.charteasy.BottomAxisConfig
import com.niallermoran.charteasy.Chart
import com.niallermoran.charteasy.ChartConfig
import com.niallermoran.charteasy.DataProvider
import com.niallermoran.charteasy.GridLines

import com.niallermoran.charteasy.PieChart
import com.niallermoran.charteasy.PieChartConfig
import com.niallermoran.charteasy.VerticalAxisConfig
import com.niallermoran.charteasysampleapp.ui.theme.ChartEasySampleAppTheme
import com.niallermoran.charteasysampleapp.layouts.EasyCard
import com.niallermoran.charteasysampleapp.model.AppSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChartEasySampleAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SamplesCharts()
                }
            }
        }
    }
}

@Composable
fun SamplesCharts() {

    val data = DataProvider()

    val points = data.points
    val piePoints = data.piePoints
    val timeSeries1 = data.timeSeries
    val timeSeries2 = data.timeSeries

    /**
     * Create a min and max value for the y-axis so we have some buffer
     */
    val minY = if(points.size>0) points.minOf { it.yValue } * 0.99f else 0.0f
    val maxY = if(points.size>0) points.maxOf { it.yValue } * 1.01f else 0.0f

    /**
     * Create a min and max value for the y-axis so we have some buffer
     */
    val minYTimeSeries1 = timeSeries1.minOf { it.yValue }
    val maxYTimeSeries1 = timeSeries1.maxOf { it.yValue }
    val minYTimeSeries2 = timeSeries2.minOf { it.yValue }
    val maxYTimeSeries2 = timeSeries2.maxOf { it.yValue }

    var smoothLineCharts by rememberSaveable { mutableStateOf(true) }
    var fillCharts by rememberSaveable { mutableStateOf(true) }

    var showAxes by rememberSaveable { mutableStateOf(true) }
    var showTicks by rememberSaveable { mutableStateOf(true) }
    var showLabels by rememberSaveable { mutableStateOf(true) }
    var lineThickness by rememberSaveable { mutableFloatStateOf(4f) }

    var displayText by rememberSaveable { mutableStateOf("tap something to see text") }

    val settings = AppSettings(smoothLineCharts = smoothLineCharts, fillCharts = fillCharts)


    Box(modifier = Modifier.padding(12.dp)) {
        Chart(
            chartConfig = ChartConfig(
                //plotAreaPadding = PaddingValues(start = 0.dp, end = 0.dp)
            ),
            rightAxisConfig = VerticalAxisConfig(
                type = AxisType.Bar,
                dataPoints = points,
                showFillColour = settings.fillCharts,
                display = showAxes,
                displayLabels = showLabels,
                displayTicks = showTicks,
                lineStrokeWidth = lineThickness.dp,
                labelStyle = TextStyle( fontSize = 14.sp ),
                labelPadding = PaddingValues(end=4.dp, start = 4.dp),
                tickLength = 10.dp
                //maxNumberOfLabelsToDisplay = 4
            ),
            leftAxisConfig = VerticalAxisConfig(
                type = AxisType.Line,
                dataPoints = points,
                showFillColour = settings.fillCharts,
                display = showAxes,
                displayLabels = showLabels,
                displayTicks = showTicks,
                lineStrokeWidth = lineThickness.dp,
                minY = 0f,
                labelStyle = TextStyle( fontSize = 14.sp ),
                labelPadding = PaddingValues(end=4.dp, start = 4.dp),
                tickLength = 10.dp
                //maxNumberOfLabelsToDisplay = 4
            ),
            bottomAxisConfig = BottomAxisConfig(
                display = showAxes,
                displayLabels = showLabels,
                displayTicks = showTicks,
               //   maxNumberOfLabelsToDisplay = 3,
                labelPadding = PaddingValues(10.dp),
                tickLength = 10.dp
            ),

            )
    }

    return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(6.dp)
    )
    {

        EasyCard(title = "Options", onCardTap = {displayText = "options card tapped"}) {

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = smoothLineCharts,
                        onCheckedChange = {
                            smoothLineCharts = it
                        })
                    Text(text = "Smooth lines", modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = fillCharts,
                        onCheckedChange = {
                            fillCharts = it
                        })
                    Text(text = "Show Fills", modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showLabels,
                        onCheckedChange = {
                            showLabels = it
                        })
                    Text(text = "Show Labels", modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showTicks,
                        onCheckedChange = {
                            showTicks = it
                        })
                    Text(text = "Show Tick", modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showAxes,
                        onCheckedChange = {
                            showAxes = it
                        })
                    Text(text = "Show Axes", modifier = Modifier.weight(1f))
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "Line Thickness")
                    Slider(value = lineThickness,
                        valueRange = 2f..24f,
                        onValueChange = {
                            lineThickness = it
                        })
                }

                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = displayText)
                }

            }
        }

        EasyCard(title = "Minimal Configuration") {

            val lineColor = Color.Magenta
            val x = 0f
            val height = 0f

            Box {

                Chart(
                    chartConfig = ChartConfig(
                        plotAreaPadding = PaddingValues(start = 10.dp, end=10.dp)
                    ),
                    leftAxisConfig = VerticalAxisConfig(
                        type = AxisType.Bar,
                        dataPoints = points,
                        showFillColour = settings.fillCharts,
                        display = showAxes,
                        displayLabels = showLabels,
                        displayTicks = showTicks,
                        lineStrokeWidth = lineThickness.dp,
                        minY = 0f
                    ),
                    bottomAxisConfig = BottomAxisConfig(
                        display = showAxes,
                        displayLabels = showLabels,
                        displayTicks = showTicks,
                     //  maxNumberOfLabelsToDisplay = 3
                    ),

                )
            }
        }

        EasyCard(title = "Formatted with Random Data") {
            Chart(

                leftAxisConfig = VerticalAxisConfig(
                    dataPoints = points,
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    lineColor = Color.Blue,
                    smoothLine = settings.smoothLineCharts,
                    showFillColour = settings.fillCharts,
                    fillBrush = Brush.verticalGradient(listOf(Color.Cyan, Color.Blue)),
                    minY = minY,
                    maxY = maxY,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    lineStrokeWidth = lineThickness.dp,
                    labelStyle = TextStyle(color = Color.Red),
                    pointLabelStyle = TextStyle(color = Color.Red, fontSize = 9.sp),
                    formatPointLabel = { index, point->
                        val rounded = point.yValue.toInt().toString()
                        rounded
                    }
                ),
                bottomAxisConfig = BottomAxisConfig(
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    labelStyle = TextStyle(color = Color.Red)
                )
            )
        }

        EasyCard(title = "Time Series with Custom Labels and Random Data") {

            /**
             * Creates a chart with configuration.
             * For the time series chart we are using a formatter to display the date in the format we want on the bottom axis
             */
            Chart(

                leftAxisConfig = VerticalAxisConfig(
                    dataPoints = timeSeries1,
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    lineColor = Color.Blue,
                    smoothLine = settings.smoothLineCharts,
                    showFillColour = settings.fillCharts,
                    fillBrush = Brush.verticalGradient(listOf(Color.Cyan, Color.Blue)),
                    minY = minYTimeSeries1,
                    maxY = maxYTimeSeries1,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    lineStrokeWidth = lineThickness.dp
                ),
                bottomAxisConfig = BottomAxisConfig(
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    maxNumberOfLabelsToDisplay = 5,
                    formatAxisLabel = { x ->
                        // x represents an epoch in milliseconds
                        val date = Date(x.toLong())
                        val dateFormatter = SimpleDateFormat("MMM d", Locale.ENGLISH)
                        dateFormatter.format(date)
                    }
                ),

            )
        }

        EasyCard(title = "Time Series Bar Chart with Custom Labels and Random Data") {
            /**
             * Creates a chart with minimum configuration.
             * Labels will be created for every point and the chart defaults to a line chart
             */
            Chart(

                leftAxisConfig = VerticalAxisConfig(
                    dataPoints = timeSeries1,
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    lineColor = Color.Blue,
                    smoothLine = settings.smoothLineCharts,
                    showFillColour = settings.fillCharts,
                    fillBrush = Brush.verticalGradient(listOf(Color.Cyan, Color.Blue)),
                    type = AxisType.Bar,
                    minY = minYTimeSeries1,
                    maxY = maxYTimeSeries1,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    lineStrokeWidth = lineThickness.dp,
                            pointLabelStyle = TextStyle(color = Color.Red, fontSize = 9.sp),
                    formatPointLabel = { index, point->
                        val y = point.yValue* 10f.toInt()/10f
                        y.toString()
                    }
                ),
                bottomAxisConfig = BottomAxisConfig(
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    maxNumberOfLabelsToDisplay = 5,
                    formatAxisLabel = { x ->
                        // x represents an epoch in milliseconds
                        val date = Date(x.toLong())
                        val dateFormatter = SimpleDateFormat("MMM d", Locale.ENGLISH)
                        dateFormatter.format(date)
                    }
                ),

            )
        }

        EasyCard(title = "Mixed Series Bar Chart with Custom Labels and Random Data") {
            /**
             * Creates a chart with minimum configuration.
             * Labels will be created for every point and the chart defaults to a line chart
             */
       /*     MixedChart(
                modifier = Modifier
                    .padding(12.dp)
                    .height(300.dp), // use some padding so last x-axis label is not clipped
                leftAxisConfig = AxisConfig(
                    dataPoints = timeSeries1,
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    lineColor = Color.Blue,
                    smoothLine = settings.smoothLineCharts,
                    showFillColour = settings.fillCharts,
                    fillBrush = Brush.verticalGradient(listOf(Color.Cyan, Color.Blue)),
                    type = AxisType.Bar,
                    minY = minYTimeSeries1,
                    maxY = maxYTimeSeries1,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    lineStrokeWidth = lineThickness.dp,
                    formatPointLabel = { index, point ->
                        val y = point.yValue* 10f.toInt()/10f
                        y.toString()
                    }
                ),
                rightAxisConfig = AxisConfig(
                    dataPoints = timeSeries2,
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    lineColor = Color.Blue,
                    smoothLine = settings.smoothLineCharts,
                    showFillColour = false,
                    type = AxisType.Line,
                    minY = minYTimeSeries2,
                    maxY = maxYTimeSeries2,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    lineStrokeWidth = lineThickness.dp,
                    formatPointLabel = { index, point ->
                        val y = point.yValue* 10f.toInt()/10f
                        y.toString()
                    }
                ),
                bottomAxisConfig = BottomAxisConfig(
                    tickColor = Color.Gray,
                    axisColor = Color.Gray,
                    display = showAxes,
                    displayLabels = showLabels,
                    displayTicks = showTicks,
                    labelStyle = TextStyle(fontSize = 10.sp),
                    labelMaxLines = 2,
                    labelOverflow = TextOverflow.Visible,
                    maxNumberOfLabelsToDisplay = 5
                ),
                formatBottomAxisLabel = { _, x, _ ->
                    // x represents an epoch in milliseconds
                    val date = Date(x.toLong())
                    val dateFormatter = SimpleDateFormat("MMM d", Locale.ENGLISH)
                    dateFormatter.format(date)
                }
            )*/
        }

        EasyCard(title = "Pie Chart with Custom Labels and Random Data") {

            PieChart(
                modifier = Modifier
                    .padding(12.dp)
                    .height(300.dp),
                config = PieChartConfig(
                    dataPoints = piePoints,
                    chartHeight = 300.dp,
                    padding = PaddingValues(6.dp)
                ),
            )
        }


    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChartEasySampleAppTheme {
        SamplesCharts()
    }
}