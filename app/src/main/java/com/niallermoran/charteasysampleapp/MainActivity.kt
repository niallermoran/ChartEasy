package com.niallermoran.charteasysampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.niallermoran.charteasysampleapp.ui.theme.ChartEasySampleAppTheme
import com.niallermoran.charteasysampleapp.charts.LineChartSampleFormatted
import com.niallermoran.charteasysampleapp.charts.LineChartSampleMinimal
import com.niallermoran.charteasysampleapp.charts.LineChartTimeSeries

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    )
    {


        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
        ) {
            Text(
                text = "Minimal Config and Random Values", modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth(), textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.LightGray))

            LineChartSampleMinimal(modifier = Modifier.padding(12.dp))
        }

        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
        ) {
            Text(
                text = "Formatted and Random Values", modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth(), textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.LightGray))

            LineChartSampleFormatted(modifier = Modifier.padding(12.dp))
        }

        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
        ) {
            Text(
                text = "Time Series with Formatted Values", modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth(), textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.LightGray))

            LineChartTimeSeries(modifier = Modifier.padding(12.dp))
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