package com.niallermoran.charteasy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    config: PieChartConfig
) {
    DrawPieChart(config = config, modifier = modifier)
}


@Composable
private fun DrawPieChart(config: PieChartConfig, modifier: Modifier = Modifier) {

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier
            .height(config.chartHeight)
            .fillMaxWidth()
            .padding(config.padding),
        contentAlignment = Alignment.Center
    ) {

        val height = constraints.maxHeight
        val width = constraints.maxWidth
        val diameter = if (width < height) width.toFloat() else height.toFloat()
        val radius = diameter / 2
        val points = config.dataPoints.sortedBy { it.yValue }.filter { it.yValue > 0 }
        val sumOfY = points.sumOf { it.yValue.toDouble() }.toFloat()

        if( sumOfY == 0.0f)
        {
            Box(modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                ) {
                Text("No Data", modifier = Modifier.align(Alignment.Center))
            }
        }
        else {

            Canvas(modifier = Modifier
                .align(Alignment.Center)
                .shadow(12.dp, shape = CircleShape)
                .size(with(density) { diameter.toDp() }, with(density) { diameter.toDp() })
                //        .background(Color.Red)
                .drawWithCache {
                    onDrawBehind {

                        drawArc(
                            color = Color.Black,
                            topLeft = Offset(x = 0f, y = 0f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = true,
                        )

                        var startAngle = 0f

                        points.forEachIndexed { _, chartPoint ->

                            // calculate the angle of sweep
                            val sweepAngle = 360f * (chartPoint.yValue / sumOfY)

                            // draw the arc
                            drawArc(
                                color = chartPoint.colour,
                                alpha = chartPoint.alpha,
                                topLeft = Offset(x = 0f, y = 0f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = Size(diameter, diameter)
                            )

                            // cut the arc in two with a line half radius long.
                            // at the endpoint of this bisector drop to x and y axis to find deltas
                            // to calculate the offset for the label
                            // angleA is the angle between the bisector and the x axis
                            val angleA = startAngle + (sweepAngle / 2)
                            val angleB = 90 - angleA
                            val labelXOffset =
                                radius + (radius * sin(angleB.toDouble() * 0.0174533) / 2).toFloat()
                            val labelYOffset =
                                radius + (radius * sin(angleA.toDouble() * 0.0174533) / 2).toFloat()


                            val measure = textMeasurer.measure(
                                text = chartPoint.label,
                                style = chartPoint.labelStyle,
                                overflow = chartPoint.overflow,
                                maxLines = chartPoint.maxLinesForLabel
                            )

                            val labelXOffsetOutside =
                                radius + ((measure.size.width + radius) * sin(angleB.toDouble() * 0.0174533)).toFloat()
                            val labelYOffsetOutside =
                                radius + ((measure.size.height + radius) * sin(angleA.toDouble() * 0.0174533)).toFloat()

                            val labelXOffsetEdge =
                                radius + (radius * sin(angleB.toDouble() * 0.0174533)).toFloat()
                            val labelYOffsetEdge =
                                radius + (radius * sin(angleA.toDouble() * 0.0174533)).toFloat()

                            when (chartPoint.labelPosition) {

                                PieChartLabelPosition.INSIDE -> {
                                    // draw the label
                                    drawText(
                                        topLeft = Offset(
                                            labelXOffset - (measure.size.width / 2),
                                            labelYOffset - (measure.size.height / 2)
                                        ),
                                        textLayoutResult = measure
                                    )
                                }

                                PieChartLabelPosition.OUTSIDE -> {
                                    drawText(
                                        topLeft = Offset(
                                            labelXOffsetOutside - (measure.size.width / 2),
                                            labelYOffsetOutside - (measure.size.height / 2)
                                        ),
                                        textLayoutResult = measure
                                    )
                                }

                                PieChartLabelPosition.EDGE -> {
                                    drawText(
                                        topLeft = Offset(
                                            labelXOffsetEdge - (measure.size.width / 2),
                                            labelYOffsetEdge - (measure.size.height / 2)
                                        ),
                                        textLayoutResult = measure
                                    )
                                }

                                PieChartLabelPosition.INVISIBLE -> {}
                            }


                            startAngle += sweepAngle

                        }


                    }
                }) {
            }
        }
    }

}

data class PieChartConfig(

    /**
     * The data points used to plot a chart.
     */
    var dataPoints: List<PiePoint>,

    /**
     * The padding around the pie-chart. Use this to ensure your labels display as expected
     */
    var padding: PaddingValues = PaddingValues(12.dp),

    /**
     * The height to use for the pie-chart
     */
    var chartHeight:Dp = 300.dp
)


enum class PieChartLabelPosition {
    INSIDE, OUTSIDE, EDGE, INVISIBLE
}


data class PiePoint(
    val yValue: Float,
    val label: String = yValue.toString(),
    val labelPosition: PieChartLabelPosition = PieChartLabelPosition.INSIDE,
    val colour: Color =  Color ( red = Random.nextInt(256),  Random.nextInt(256), Random.nextInt(256), Random.nextInt(256) ),
    val labelStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
        background = Color.Transparent,
    ),
    val overflow: TextOverflow = TextOverflow.Visible,
    val maxLinesForLabel: Int = 1,
    val alpha:Float = 0.9f
)
