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
            return generateRandomIntegers()
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

        val n = 0

        // generate some data
        val randomX = List(n) { Random.nextInt(0, 100) }
        val randomY = List(n) { Random.nextInt(100, 10000) }

        // create the chart points
        val points = ArrayList<ChartPoint>(n)
        for (i in 0..n) {
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

        val n = 0

        // get a 30 day date range
        val now = Date().time
        val then = now - (Duration.convert(30.0, DurationUnit.DAYS, DurationUnit.MILLISECONDS).toLong())

        // generate some data
        val randomX = List(n) { Random.nextLong(then, now) } // milliseconds on x axis
        val randomY = List(n) { Random.nextDouble(100.0, 125.0) }

        // create the chart points
        val points = ArrayList<ChartPoint>(n)
        for (i in 0..n) {
            points.add(
                i, ChartPoint(
                    xValue = randomX[i].toFloat(),
                    yValue = randomY[i].toFloat()
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