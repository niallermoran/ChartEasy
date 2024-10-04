package com.niallermoran.charteasysampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.niallermoran.charteasy.AxisType
import com.niallermoran.charteasy.BottomAxisConfig
import com.niallermoran.charteasy.Chart
import com.niallermoran.charteasy.DataProvider
import com.niallermoran.charteasy.VerticalAxisConfig
import com.niallermoran.charteasysampleapp.ui.theme.ChartEasySampleAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChartEasySampleAppTheme {
                // A surface container using the 'background' color from the theme
                Box(
                    modifier = Modifier.fillMaxSize().padding(12.dp )
                ) {
                   Chart()
                }
            }
        }
    }
}

@Composable
fun Chart() {

    val data = DataProvider()
    val points = data.points
    val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    Box(modifier = Modifier.padding(12.dp)) {
        Chart(
            rightAxisConfig = VerticalAxisConfig(
                display = true,
                type = AxisType.Bar,
                dataPoints = points,
                formatAxisLabel = { y ->
                    "${String.format( locale = Locale.ENGLISH, "%.1f", y / 1000)} km"
                }
            ),
            leftAxisConfig = VerticalAxisConfig(
                type = AxisType.Line,
                dataPoints = points,
                formatAxisLabel = { y ->
                    String.format( locale = Locale.ENGLISH, "%.1f", y)
                },
                maxY = 120f
            ),
            bottomAxisConfig = BottomAxisConfig(
                formatAxisLabel = { x->
                    val date = Date( (x*1000).toLong() )
                    dateFormatter.format(date)
                }
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChartEasySampleAppTheme {
        Chart()
    }
}