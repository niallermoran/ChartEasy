package com.niallermoran.charteasy

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

class DataProvider {

    val samplePoints1: List<ChartPoint>
        get() {

            return listOf(
                ChartPoint(
                    1727680190.0,
                    106.16,
                    yValueRightAxis = 1076.06,
                    pointLabelRightAxis = "1km"
                ), // 30 sep
                ChartPoint(
                    1727613711.0,
                    109.87,
                    yValueRightAxis = 2217.55,
                    pointLabelRightAxis = "2.2km"
                ),
                ChartPoint(
                    1727514602.0,
                    111.56,
                    yValueRightAxis = 1981.097,
                    pointLabelRightAxis = "2km"
                ),
                ChartPoint(
                    1727461207.0,
                    112.66,
                    yValueRightAxis = 873.56,
                    pointLabelRightAxis = ".9km"
                ),
                ChartPoint(
                    1727111643.0,
                    114.92,
                    yValueRightAxis = 2441.08,
                    pointLabelRightAxis = "2.4km"
                ),
                ChartPoint(
                    1727002609.0,
                    116.85,
                    yValueRightAxis = 4626.87,
                    pointLabelRightAxis = "4.6km"
                ),
                ChartPoint(1726557332.0, 118.50, yValueRightAxis = 3626.87) //17 sep
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
                    xValue = randomX[i].toDouble(),
                    yValue = randomY[i].toDouble()
                )
            )
        }
        return points
    }

    @OptIn(ExperimentalTime::class)
    private fun generateTimeSeries(): ArrayList<ChartPoint> {

        val n = 20

        // get a 30 day date range
        val now = Clock.System.now().epochSeconds
        val then =
            now - (Duration.convert(30.0, DurationUnit.DAYS, DurationUnit.MILLISECONDS).toLong())

        // generate some data
        val randomX = List(n) { Random.nextLong(then, now) } // milliseconds on x axis
        val randomY = listOf(
            119.0, 120, 120.3, 120.4, 120.6, 120.8, 119.9, 119.8, 119.8, 119.7,
            119.6, 120, 120.3, 120.4, 120.6, 120.8, 119.9, 119.8, 119.8, 119.7, 119.7f
        )

        // create the chart points
        val points = ArrayList<ChartPoint>(n)
        for (i in 0..<n) {
            points.add(
                i, ChartPoint(
                    xValue = randomX[i].toDouble(),
                    yValue = randomY[i].toDouble()
                )
            )
        }
        return points
    }

    private fun generatePiePoints() = listOf<PiePoint>(
        PiePoint(
            label = "1 \n Jan",
            yValue = Random.nextDouble() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
            overflow = TextOverflow.Visible
        ),
        PiePoint(
            label = "2 \n Feb",
            yValue = Random.nextDouble() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "3 \n Mar",
            yValue = Random.nextDouble() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "4 \n Apr",
            yValue = Random.nextDouble() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "5 \n May",
            yValue = Random.nextDouble() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "6 \n Jun",
            yValue = Random.nextDouble() * 100,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        )
    )


    private fun generatePiePointsWithZeroValues() = listOf<PiePoint>(
        PiePoint(
            label = "1 \n Jan",
            yValue = 0.0,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
            overflow = TextOverflow.Visible
        ),
        PiePoint(
            label = "2 \n Feb",
            yValue = 0.0,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "3 \n Mar",
            yValue = 0.0,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "4 \n Apr",
            yValue = 0.0,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "5 \n May",
            yValue = 0.0,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        ),
        PiePoint(
            label = "6 \n Jun",
            yValue = 0.0,
            labelPosition = PieChartLabelPosition.INSIDE,
            maxLinesForLabel = 2,
            labelStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
        )
    )


}