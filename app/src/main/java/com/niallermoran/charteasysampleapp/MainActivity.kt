package com.niallermoran.charteasysampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.niallermoran.charteasy.PieChartLabelPosition
import com.niallermoran.charteasy.PiePoint
import com.niallermoran.charteasysampleapp.charts.BarChartTimeSeries
import com.niallermoran.charteasysampleapp.ui.theme.ChartEasySampleAppTheme
import com.niallermoran.charteasysampleapp.charts.LineChartSampleFormatted
import com.niallermoran.charteasysampleapp.charts.LineChartSampleMinimal
import com.niallermoran.charteasysampleapp.charts.LineChartTimeSeries
import com.niallermoran.charteasysampleapp.charts.MixedChartTimeSeries
import com.niallermoran.charteasysampleapp.charts.PieChart
import com.niallermoran.charteasysampleapp.charts.generateRandomIntegers
import com.niallermoran.charteasysampleapp.charts.generateTimeSeries
import com.niallermoran.charteasysampleapp.layouts.EasyCard
import com.niallermoran.charteasysampleapp.model.AppSettings
import kotlin.random.Random

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

    val points = generateRandomIntegers()
    val timeSeries = generateTimeSeries()
    val piePoints = listOf<PiePoint>(
        PiePoint(label = "Jan", yValue = Random.nextFloat() * 100, colour = Color.Red, labelPosition = PieChartLabelPosition.INSIDE ),
        PiePoint(label = "Feb", yValue = Random.nextFloat() * 100, colour = Color.Blue, labelPosition = PieChartLabelPosition.INSIDE ),
        PiePoint(label = "Mar", yValue = Random.nextFloat() * 100, colour = Color.Green, labelPosition = PieChartLabelPosition.INSIDE ),
        PiePoint(label = "Apr", yValue = Random.nextFloat() * 100, colour = Color.Yellow, labelPosition = PieChartLabelPosition.INSIDE ),
        PiePoint(label = "May", yValue = Random.nextFloat() * 100, colour = Color.Cyan, labelPosition = PieChartLabelPosition.INSIDE ),
        PiePoint(label = "Jun", yValue = Random.nextFloat() * 100, colour = Color.Magenta, labelPosition = PieChartLabelPosition.INSIDE )
    )



    /**
     * Creat a min and max value for the y-axis so we have some buffer
     */
    val minY = points.minOf { it.yValue } * 0.95f
    val maxY = points.maxOf { it.yValue } * 1.05f

    /**
     * Creat a min and max value for the y-axis so we have some buffer
     */
    val minYTimeSeries = timeSeries.minOf { it.yValue } * 0.95f
    val maxYTimeSeries = timeSeries.maxOf { it.yValue } * 1.05f

    var smoothLineCharts by rememberSaveable(){ mutableStateOf(true)}
    var fillCharts by rememberSaveable(){ mutableStateOf(true)}

    val settings = AppSettings(smoothLineCharts = smoothLineCharts, fillCharts = fillCharts)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .verticalScroll(rememberScrollState())
            .padding(6.dp)
    )
    {

        EasyCard(title="Options") {

            Column() {
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
            }
        }

        EasyCard(title="Pie Chart with Custom Labels and Random Data") {
            PieChart(modifier = Modifier.padding(12.dp), piePoints )
        }

        EasyCard(title="Minimal Configuration with Random Data") {
            LineChartSampleMinimal(modifier = Modifier.padding(12.dp), appSettings = settings, points = points)
        }

        EasyCard(title="Formatted with Random Data") {
            LineChartSampleFormatted(modifier = Modifier.padding(12.dp), appSettings = settings, points, minY = minY, maxY= maxY)
        }

        EasyCard(title="Time Series with Custom Labels and Random Data") {
            LineChartTimeSeries(modifier = Modifier.padding(12.dp),appSettings = settings, timeSeries, minY = minYTimeSeries, maxY= maxYTimeSeries)
        }

        EasyCard(title="Time Series Bar Chart with Custom Labels and Random Data") {
            BarChartTimeSeries(modifier = Modifier.padding(12.dp),appSettings = settings, points= timeSeries, minY = minYTimeSeries, maxY= maxYTimeSeries)
        }

        EasyCard(title="Mixed Series Bar Chart with Custom Labels and Random Data") {
            MixedChartTimeSeries(modifier = Modifier.padding(12.dp),appSettings = settings, points = timeSeries, minY = minYTimeSeries, maxY= maxYTimeSeries)
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