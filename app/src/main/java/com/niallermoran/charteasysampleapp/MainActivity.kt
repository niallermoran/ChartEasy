package com.niallermoran.charteasysampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.niallermoran.charteasy.AxisType
import com.niallermoran.charteasy.BottomAxisConfig
import com.niallermoran.charteasy.Chart
import com.niallermoran.charteasy.ChartConfig
import com.niallermoran.charteasy.ChartPoint
import com.niallermoran.charteasy.DataProvider
import com.niallermoran.charteasy.OffsetDp
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

                var caption by rememberSaveable { mutableStateOf("Tap chart area") }

                // A surface container using the 'background' color from the theme
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text( modifier = Modifier.padding(6.dp)
                        .fillMaxWidth(),
                        text = caption,
                        textAlign = TextAlign.Center
                    )
                    Box(modifier = Modifier.padding(12.dp)) {
                        Chart( onTapped = { point, offset ->
                            caption = "(${point.xValue},${point.yValue}) - (${offset.left},${offset.top})"
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun Chart(onTapped: (point:ChartPoint, chartOffset: OffsetDp) ->Unit ) {

    val data = DataProvider()
    val points = data.samplePoints2
    val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    Box(modifier = Modifier.padding(12.dp)) {
        Chart(
            chartConfig = ChartConfig(
                onTap = onTapped,
                onDraw = { dimensions ->

                    // draw a box with some semi-transparent background
                    val path = Path()
                    path.moveTo( 0f, 0f )
                    path.lineTo( size.height, size.width)

                    drawRect(
                        topLeft = Offset(
                            x = dimensions.plotArea.offset.left.toPx(),
                            y = dimensions.plotArea.innerHeight.toPx() * 3 / 8
                        ),
                        size = Size(
                            width = dimensions.plotArea.size.width.toPx(),
                            height = dimensions.plotArea.innerHeight.toPx()/4
                        ),
                        color = Color(
                            red = 0f,
                            green = 0f,
                            blue = 0f,
                            alpha = .1f
                        ),
                        style = Fill
                    )


                }
            ),
            leftAxisConfig = VerticalAxisConfig(
                type = AxisType.Bar,
                dataPoints = points,
                formatAxisLabel = { y ->
                    String.format( locale = Locale.ENGLISH, "%.1f", y)
                },
                maxY = 120f,
                maxNumberOfLabelsToDisplay = 3,
                minY = 100f
            ),
            bottomAxisConfig = BottomAxisConfig(
                maxNumberOfLabelsToDisplay = 5,
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
        Chart(onTapped = { point, offset ->

        })
    }
}