package com.niallermoran.charteasy

import androidx.compose.ui.graphics.Color
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
    private fun generateTimeSeries(): ArrayList<ChartPoint> {

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

    private fun generatePiePoints() = listOf<PiePoint>(
        PiePoint(
            label = "Jan",
            yValue = Random.nextFloat() * 100,
            colour = Color.Red,
            labelPosition = PieChartLabelPosition.INSIDE
        ),
        PiePoint(
            label = "Feb",
            yValue = Random.nextFloat() * 100,
            colour = Color.Blue,
            labelPosition = PieChartLabelPosition.INSIDE
        ),
        PiePoint(
            label = "Mar",
            yValue = Random.nextFloat() * 100,
            colour = Color.Green,
            labelPosition = PieChartLabelPosition.INSIDE
        ),
        PiePoint(
            label = "Apr",
            yValue = Random.nextFloat() * 100,
            colour = Color.Yellow,
            labelPosition = PieChartLabelPosition.INSIDE
        ),
        PiePoint(
            label = "May",
            yValue = Random.nextFloat() * 100,
            colour = Color.Cyan,
            labelPosition = PieChartLabelPosition.INSIDE
        ),
        PiePoint(
            label = "Jun",
            yValue = Random.nextFloat() * 100,
            colour = Color.Magenta,
            labelPosition = PieChartLabelPosition.INSIDE
        )
    )


}