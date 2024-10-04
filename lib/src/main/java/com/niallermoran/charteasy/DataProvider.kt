package com.niallermoran.charteasy

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import java.util.Date
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

class DataProvider {

    val points: List<ChartPoint>
        get() {

            return listOf(
                ChartPoint(1727680190.0f, 106.15888928842716f, yValueRightAxis = 1076.06f, pointLabelRightAxis = "1km"), // 30 sep
                ChartPoint(1727613711.0f, 109.87542523490303f,  yValueRightAxis = 2217.55f, pointLabelRightAxis = "2.2km"),
                ChartPoint(1727514602.0f, 111.566732567526f,  yValueRightAxis = 1981.097f, pointLabelRightAxis = "2km"),
                ChartPoint(1727461207.0f, 112.66887205564919f,  yValueRightAxis = 873.56f, pointLabelRightAxis = ".9km"),
                ChartPoint(1727111643.0f, 114.92946548369882f, yValueRightAxis = 2441.08f, pointLabelRightAxis = "2.4km"),
                ChartPoint(1727002609.0f, 116.85154599391255f,  yValueRightAxis = 4626.87f, pointLabelRightAxis = "4.6km"),
                ChartPoint(1726557332.0f, 118.50448343984388f, yValueRightAxis = 3626.87f,) //17 sep
            )

        }

    val piePoints: List<PiePoint>
        get() {
            return generatePiePoints()
        }


    val timeSeries: List<ChartPoint>
        get() {
            return generateTimeSeries()
        }



    private fun generateRandomIntegers(): ArrayList<ChartPoint> {

        val n = 10

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
    private fun generateTimeSeries(): ArrayList<ChartPoint> {

        val n = 20

        // get a 30 day date range
        val now = Date().time
        val then = now - (Duration.convert(30.0, DurationUnit.DAYS, DurationUnit.MILLISECONDS).toLong())

        // generate some data
        val randomX = List(n) { Random.nextLong(then, now) } // milliseconds on x axis
        val randomY = listOf (  119.0f, 120f, 120.3f, 120.4f, 120.6f, 120.8f, 119.9f, 119.8f, 119.8f, 119.7f,
            119.6f, 120f, 120.3f, 120.4f, 120.6f, 120.8f, 119.9f, 119.8f, 119.8f, 119.7f, 119.7f )

        // create the chart points
        val points = ArrayList<ChartPoint>(n)
        for (i in 0..< n) {
            points.add(
                i, ChartPoint(
                    xValue = randomX[i].toFloat(),
                    yValue = randomY[i]
                )
            )
        }
        return points
    }

    private fun generatePiePoints() = listOf<PiePoint>(
        PiePoint(
            label = "1 \n Jan",
            yValue = Random.nextFloat() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
            overflow = TextOverflow.Visible
        ),
        PiePoint(
            label = "2 \n Feb",
            yValue = Random.nextFloat() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "3 \n Mar",
            yValue = Random.nextFloat() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "4 \n Apr",
            yValue = Random.nextFloat() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "5 \n May",
            yValue = Random.nextFloat() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "6 \n Jun",
            yValue = Random.nextFloat() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        )
    )


    private fun generatePiePointsWithZeroValues() = listOf<PiePoint>(
        PiePoint(
            label = "1 \n Jan",
            yValue = 0.0f,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
            overflow = TextOverflow.Visible
        ),
        PiePoint(
            label = "2 \n Feb",
            yValue =0.0f,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "3 \n Mar",
            yValue =0.0f,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "4 \n Apr",
            yValue =0.0f,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "5 \n May",
            yValue =0.0f,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "6 \n Jun",
            yValue =0.0f,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color= Color.White, textAlign = TextAlign.Center),
        )
    )


}