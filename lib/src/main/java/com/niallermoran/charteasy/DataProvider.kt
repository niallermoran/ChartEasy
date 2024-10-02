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
                ChartPoint(1.1f, 40f, label = "Dur.: 1.1 - 50 bpm"),
                ChartPoint(3.2f, 10f, label = "Dur.: 3.2 - 10 bpm"),
                ChartPoint(3.3f, 30.8f, label = "Dur.: 3.3 - 31 bpm"),
                ChartPoint(3.4f, 12f, label = "Dur.: 3.4 - 2 bpm"),
                ChartPoint(1.6f, 26f, label = "Dur.: 1.6 - 26 bpm"),
                ChartPoint(9f, 32.5f, label = "Dur.: 9 - 33 bpm"),
                ChartPoint(2.3f, 17.89f, label = "Dur.: 2.3 - 8 bpm")
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