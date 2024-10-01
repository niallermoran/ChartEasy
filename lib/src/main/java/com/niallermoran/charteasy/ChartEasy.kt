package com.niallermoran.charteasy

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.transition.MaterialSharedAxis.Axis
import java.util.Hashtable
import kotlin.math.sqrt


/**
 * The chart creates a cartesian co-ordinate chart with a bottom axis and a left axis.
 * The chart will display a line or bar depending on the type property of leftAxisConfig.
 * The chart will fill available space so make sure to wrap in a layout that defines size
 */
@Composable
fun Chart(
    chartConfig: ChartConfig = ChartConfig(),
    leftAxisConfig: AxisConfig,
    bottomAxisConfig: BottomAxisConfig = BottomAxisConfig(),
    formatBottomAxisLabel: ((Float) -> String)? = null,
    formatLeftAxisLabel: ((Float) -> String)? = null,
) {
    val density = LocalDensity.current
    val config = Config(
        chartConfig = chartConfig,
        leftAxisConfig = leftAxisConfig,
        bottomAxisConfig = bottomAxisConfig,
        formatLeftAxisLabel = formatLeftAxisLabel,
        formatBottomAxisLabel = formatBottomAxisLabel
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()

    )
    {
        // we can do nothing without a left axis config
        if (leftAxisConfig.dataPoints.size > 1) {

            val textMeasurer = rememberTextMeasurer()
            val dimensions = calculateDimensions(
                density = density,
                textMeasurer = textMeasurer,
                config = config,
                availableHeight = with(density) { constraints.maxHeight.toDp() },
                availableWidth = with(density) { constraints.maxWidth.toDp() }
            )

            if (config.leftAxisConfig.type == AxisType.Bar)
                BarPlot(dimensions, config.leftAxisConfig, textMeasurer)
            else if (config.leftAxisConfig.type == AxisType.Line)
                LinePlot(dimensions, config.leftAxisConfig, textMeasurer)

            LeftAxis(dimensions, textMeasurer, config)

            BottomAxis(dimensions, textMeasurer, config)

            CrossHairs(config = config, dimensions = dimensions )

        } else
            Text(text = "Not enough data", modifier = Modifier.align(Alignment.Center))
    }


}

@Composable
private fun CrossHairs(config: Config, dimensions: Dimensions)
{
    if( config.leftAxisConfig.crossHairsConfig.display ) {

        var points = config.leftAxisConfig.dataPoints
        if( config.leftAxisConfig.type == AxisType.Line)
            points = points.sortedBy { it.xValue }

        var verticalCrossHairX by rememberSaveable { mutableFloatStateOf(0f) }
        var horizontalCrossHairY by rememberSaveable { mutableFloatStateOf(0f) }
        var foundPointLeftIndex by rememberSaveable { mutableIntStateOf(0) }
        val textMeasurer = rememberTextMeasurer()

        val tapModifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        var deltaLeft = Float.MAX_VALUE
                        var foundPoint = points[0]

                        points.forEachIndexed { index, point ->

                            val px =
                                dimensions.chart.plotAreaWidth.toPx() * (point.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin)
                            val py =
                                dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))

                            val testDelta =
                                sqrt(((px - it.x) * (px - it.x)) + ((py - it.y) * (py - it.y)))

                            if (testDelta < deltaLeft) {
                                deltaLeft = testDelta
                                foundPoint = point
                                foundPointLeftIndex = index
                            }
                        }

                        verticalCrossHairX =
                            dimensions.chart.leftAxisWidth.toPx() + (dimensions.chart.plotAreaWidth.toPx() * (foundPoint.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin))
                        horizontalCrossHairY =
                            dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (foundPoint.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))
                    }
                )
            }

        val p = points[foundPointLeftIndex]

        Canvas(modifier = tapModifier) {

            drawLine(
                start = Offset( dimensions.chart.leftAxisWidth.toPx(), horizontalCrossHairY ),
                end = Offset(dimensions.chart.plotAreaWidth.toPx()+ dimensions.chart.leftAxisWidth.toPx(), horizontalCrossHairY),
                color = Color.Red
            )

            drawLine(
                start = Offset( verticalCrossHairX , dimensions.chart.plotAreaHeight.toPx()),
                end = Offset(verticalCrossHairX,0f ),
                color = Color.Red
            )


            val label = p.label ?: "(${p.xValue},${p.yValue})"

            val measure = textMeasurer.measure(
                label,
                config.leftAxisConfig.labelStyle,
                config.leftAxisConfig.labelOverflow,
                maxLines = 1
            )

            val maxIndex = points.size -  1
            var leftDelta = measure.size.width/2
            var topDelta = measure.size.height * 1.2f

            if( foundPointLeftIndex == maxIndex )
                leftDelta = measure.size.width

            if( foundPointLeftIndex == 0 )
                leftDelta =  0

            val left = verticalCrossHairX - leftDelta
            val top = horizontalCrossHairY - topDelta

            drawText(
                textLayoutResult = measure,
                color= Color.Black,
                topLeft = Offset(left, top)
            )


        }

    }


}

@Composable
private fun LeftAxis(
    dimensions: Dimensions,
    textMeasurer: TextMeasurer,
    config: Config
) {

    Canvas(modifier = Modifier.fillMaxSize()) {

        val axisPath = Path()
        axisPath.moveTo(x = dimensions.chart.leftAxisWidth.toPx(), y = 0f)
        axisPath.lineTo(
            x = dimensions.chart.leftAxisWidth.toPx(),
            y = dimensions.chart.plotAreaHeight.toPx()
        )
        drawPath(
            path = axisPath,
            color = config.leftAxisConfig.axisColor,
            style = Stroke(width = config.leftAxisConfig.axisStrokeWidth.toPx())
        )
    }



    Canvas(modifier = Modifier.fillMaxSize()) {

        dimensions.leftAxisLabelMap?.forEach { y, labelText ->

            val measure = textMeasurer.measure(
                labelText,
                config.leftAxisConfig.labelStyle,
                config.leftAxisConfig.labelOverflow,
                maxLines = 1
            )

            if (config.leftAxisConfig.displayTicks) {
                drawLine(
                    color = config.leftAxisConfig.tickColor,
                    start = Offset(
                        dimensions.chart.leftAxisWidth.toPx(),
                        y.toPx()
                    ),
                    end = Offset(
                        dimensions.chart.leftAxisWidth.toPx() - config.leftAxisConfig.tickLength.toPx(),
                        y.toPx()
                    ),
                    strokeWidth = config.bottomAxisConfig.tickStrokeWidth.toPx()
                )
            }

            if (config.leftAxisConfig.displayLabels) {
                drawText(
                    topLeft = Offset(
                        x = dimensions.chart.leftAxisWidth.toPx() - config.leftAxisConfig.tickLength.toPx() - measure.size.width
                            .toDp()
                            .toPx(),
                        y = y.toPx() - measure.size.height / 2
                    ),
                    textLayoutResult = measure,
                    color = config.leftAxisConfig.labelStyle.color
                )
            }
        }

    }
}

@Composable
private fun LinePlot(
    dimensions: Dimensions,
    config: AxisConfig,
    textMeasurer: TextMeasurer
) {

    Canvas(
        modifier = Modifier
            .offset(x = dimensions.chart.plotAreaInnerStart, y = dimensions.chart.plotAreaInnerTop)
            .width(dimensions.chart.plotAreaInnerWidth)
            .height(dimensions.chart.plotAreaInnerHeight)

    )
    {
        val points = config.dataPoints.sortedBy { it.xValue }
        val linePoints = ArrayList<PointF>(points.size)

        for (point in points) {
            // actual point add to path
            val x =
                dimensions.chart.plotAreaWidth * (point.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin)
            val y =
                dimensions.chart.plotAreaHeight - (dimensions.chart.plotAreaHeight * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))
            linePoints.add(PointF(x.toPx(), y.toPx()))
        }

        // get a list of control points for Bezier curve
        val splinePoints1 = ArrayList<PointF>()
        val splinePoints2 = ArrayList<PointF>()
        // splinePoints1.add(PointF(linePoints[0].x, linePoints[0].y))

        points.forEachIndexed { index, chartPoint ->
            val i = index + 1
            if (i < linePoints.size) {
                splinePoints1.add(
                    PointF(
                        (linePoints[i].x + linePoints[i - 1].x) / 2,
                        linePoints[i - 1].y
                    )
                )
                splinePoints2.add(
                    PointF(
                        (linePoints[i].x + linePoints[i - 1].x) / 2,
                        linePoints[i].y
                    )
                )
            }
        }


        // path for shape fill
        val pathFill = Path()
        pathFill.moveTo(0f, dimensions.chart.plotAreaHeight.toPx())

        // path for line
        val pathLine = Path()

        // path for spline
        val pathSpline = Path()

        // draw line and fill
        if (config.smoothLine) {
            pathFill.lineTo(linePoints.first().x, linePoints.first().y)
            pathSpline.moveTo(linePoints.first().x, linePoints.first().y)

            //for (i in 1 until linePoints.size)
            linePoints.forEachIndexed { i, linePoint ->
                if (i > 0) {
                    pathSpline.cubicTo(
                        splinePoints1[i - 1].x, splinePoints1[i - 1].y,
                        splinePoints2[i - 1].x, splinePoints2[i - 1].y,
                        linePoints[i].x, linePoints[i].y
                    )

                    pathFill.cubicTo(
                        splinePoints1[i - 1].x, splinePoints1[i - 1].y,
                        splinePoints2[i - 1].x, splinePoints2[i - 1].y,
                        linePoints[i].x, linePoints[i].y
                    )
                }
            }

            pathFill.lineTo(
                dimensions.chart.plotAreaWidth.toPx(),
                dimensions.chart.plotAreaHeight.toPx()
            )
        } else {
            pathLine.moveTo(linePoints.first().x, linePoints.first().y)

            linePoints.forEach { pointF ->
                pathLine.lineTo(pointF.x, pointF.y)
                pathFill.lineTo(pointF.x, pointF.y)
            }

            pathFill.lineTo(
                dimensions.chart.plotAreaWidth.toPx(),
                dimensions.chart.plotAreaHeight.toPx()
            )
        }


        drawPath(
            path = pathLine,
            color = config.lineColor,
            style = Stroke(width = config.lineStrokeWidth.value)
        )

        if (points.size > 2 && config.smoothLine) {
            drawPath(
                path = pathSpline,
                color = config.lineColor,
                style = Stroke(width = config.lineStrokeWidth.value)
            )
        }

        if (config.showFillColour) {
            drawPath(
                path = pathFill,
                brush = config.fillBrush,
                style = Fill,
                alpha = 0.3f,
            )
        }

        if (config.showCircles) {

            if (points.size == 1) {
                drawCircle(
                    color = config.circleColor,
                    center = Offset(
                        dimensions.chart.plotAreaWidth.toPx(),
                        dimensions.chart.plotAreaHeight.toPx()
                    ),
                    radius = config.circleRadius.value,

                    )
            } else {
                for (i in 1..points.size) {
                    val point = points[i - 1]
                    val x =
                        dimensions.chart.plotAreaWidth.toPx() * (point.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin)
                    val y =
                        dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))

                    drawCircle(
                        color = config.circleColor,
                        center = Offset(x, y),
                        radius = config.circleRadius.value,

                        )
                }
            }
        }

        val lambda = config.formatPointLabel
        if (lambda != null) {
            for (i in 1..points.size) {
                val point = points[i - 1]
                val text = lambda(i, point)
                val measure = textMeasurer.measure(
                    text,
                    config.pointLabelStyle,
                    config.labelOverflow,
                    maxLines = 1
                )

                val x =
                    (dimensions.chart.plotAreaWidth.toPx() * (point.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin)) - (measure.size.width / 2)
                val y =
                    dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin)) - ((measure.size.height) * 1.5f)


                drawText(
                    textLayoutResult = measure,
                    topLeft = Offset(x, y)
                )
            }
        }


    }
}

@Composable
/**
 * Draws the bars for AxisType Bar within the inner plot area
 */
private fun BarPlot(
    dimensions: Dimensions,
    config: AxisConfig,
    textMeasurer: TextMeasurer
) {


    Canvas(
        modifier = Modifier
            .offset(x = dimensions.chart.plotAreaInnerStart, y = dimensions.chart.plotAreaInnerTop)
            .width(dimensions.chart.plotAreaInnerWidth)
            .height(dimensions.chart.plotAreaInnerHeight)

    )
    {

        val points = config.dataPoints
        val barPaths = HashMap<ChartPoint, Path>(points.size)
        val barWidth = dimensions.barWidth

        dimensions.bottomAxisLabels.onEachIndexed { index, bottomAxisLabel ->

            val point = points[index]
            val pointHeightPx =
                dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))
            val barPath = Path()
            barPath.moveTo(
                bottomAxisLabel.xPixels - (barWidth.toPx() / 2),
                dimensions.chart.plotAreaHeight.toPx()
            )
            barPath.lineTo(bottomAxisLabel.xPixels - (barWidth.toPx() / 2), pointHeightPx)
            barPath.lineTo(bottomAxisLabel.xPixels + (barWidth.toPx() / 2), pointHeightPx)
            barPath.lineTo(
                bottomAxisLabel.xPixels + (barWidth.toPx() / 2),
                dimensions.chart.plotAreaHeight.toPx()
            )
            barPaths[point] = barPath
        }



        barPaths.onEachIndexed { index, hashMap ->
            val point = hashMap.key
            val path = hashMap.value
            drawPath(
                path = path,
                color = config.lineColor
            )

            val lambda = config.formatPointLabel
            if (lambda != null) {
                val text = lambda(index, point)
                val measure = textMeasurer.measure(
                    text,
                    config.pointLabelStyle,
                    config.labelOverflow,
                    maxLines = 1
                )

                val x =
                    (dimensions.chart.plotAreaWidth.toPx() * (point.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin)) - (measure.size.width / 2)
                val y =
                    dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin)) - ((measure.size.height) * 1.5f)


                drawText(
                    textLayoutResult = measure,
                    topLeft = Offset(x, y)
                )

            }
        }


        /*

                    for (point in points) {
                        // actual point add to path
                        val x = dimensions.chart.plotAreaWidth * (point.xValue - xMin) / (xMax - xMin)
                        val y =
                            dimensions.chart.plotAreaHeight - (dimensions.chart.plotAreaHeight * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))
                        linePoints.add(PointF(x.toPx(), y.toPx()))
                    }

                    // get a list of control points for Bezier curve
                    val splinePoints1 = ArrayList<PointF>()
                    val splinePoints2 = ArrayList<PointF>()
                    // splinePoints1.add(PointF(linePoints[0].x, linePoints[0].y))
                    for (i in 1 until points.size) {
                        splinePoints1.add(
                            PointF(
                                (linePoints[i].x + linePoints[i - 1].x) / 2,
                                linePoints[i - 1].y
                            )
                        )
                        splinePoints2.add(
                            PointF(
                                (linePoints[i].x + linePoints[i - 1].x) / 2,
                                linePoints[i].y
                            )
                        )
                    }

                    // path for shape fill
                    val pathFill = Path()
                    pathFill.moveTo(0f, dimensions.chart.plotAreaHeight.toPx())

                    // path for line
                    val pathLine = Path()

                    // path for spline
                    val pathSpline = Path()

                    // draw line and fill
                    if (config.leftAxisConfig.smoothLine) {
                        pathFill.lineTo(linePoints.first().x, linePoints.first().y)
                        pathSpline.moveTo(linePoints.first().x, linePoints.first().y)

                        for (i in 1 until linePoints.size) {
                            pathSpline.cubicTo(
                                splinePoints1[i - 1].x, splinePoints1[i - 1].y,
                                splinePoints2[i - 1].x, splinePoints2[i - 1].y,
                                linePoints[i].x, linePoints[i].y
                            )

                            pathFill.cubicTo(
                                splinePoints1[i - 1].x, splinePoints1[i - 1].y,
                                splinePoints2[i - 1].x, splinePoints2[i - 1].y,
                                linePoints[i].x, linePoints[i].y
                            )
                        }

                        pathFill.lineTo(
                            dimensions.chart.plotAreaWidth.toPx(),
                            dimensions.chart.plotAreaHeight.toPx()
                        )
                    } else {
                        pathLine.moveTo(linePoints.first().x, linePoints.first().y)

                        linePoints.forEach { pointF ->
                            pathLine.lineTo(pointF.x, pointF.y)
                            pathFill.lineTo(pointF.x, pointF.y)
                        }

                        pathFill.lineTo(
                            dimensions.chart.plotAreaWidth.toPx(),
                            dimensions.chart.plotAreaHeight.toPx()
                        )
                    }



                    return onDrawBehind {

                        drawPath(
                            path = pathLine,
                            color = config.leftAxisConfig.lineColor,
                            style = Stroke(width = config.leftAxisConfig.lineStrokeWidth.value)
                        )

                        if (points.size > 2) {
                            drawPath(
                                path = pathSpline,
                                color = config.leftAxisConfig.lineColor,
                                style = Stroke(width = config.leftAxisConfig.lineStrokeWidth.value)
                            )
                        }

                        if (config.leftAxisConfig.showFillColour) {
                            drawPath(
                                path = pathFill,
                                brush = config.leftAxisConfig.fillBrush,
                                style = Fill,
                                alpha = 0.3f,
                            )
                        }

                        if (config.leftAxisConfig.showCircles) {

                            if (points.size == 1) {
                                drawCircle(
                                    color = config.leftAxisConfig.circleColor,
                                    center = Offset(
                                        dimensions.chart.plotAreaWidth.toPx(),
                                        dimensions.chart.plotAreaHeight.toPx()
                                    ),
                                    radius = config.leftAxisConfig.circleRadius.value,

                                    )
                            } else {
                                for (i in 1..points.size) {
                                    val point = points[i - 1]
                                    val x =
                                        dimensions.chart.plotAreaWidth.toPx() * (point.xValue - xMin) / (xMax - xMin)
                                    val y =
                                        dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))

                                    drawCircle(
                                        color = config.leftAxisConfig.circleColor,
                                        center = Offset(x, y),
                                        radius = config.leftAxisConfig.circleRadius.value,

                                        )
                                }
                            }
                        }

                        val lambda = config.leftAxisConfig.formatPointLabel
                        if (lambda != null) {
                            for (i in 1..points.size) {
                                val point = points[i - 1]
                                val text = lambda(i, point)
                                val measure = textMeasurer.measure(
                                    text,
                                    config.leftAxisConfig.pointLabelStyle,
                                    config.leftAxisConfig.labelOverflow,
                                    maxLines = 1
                                )

                                val x =
                                    (dimensions.chart.plotAreaWidth.toPx() * (point.xValue - xMin) / (xMax - xMin)) - (measure.size.width / 2)
                                val y =
                                    dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin)) - ((measure.size.height) * 1.5f)


                                drawText(
                                    textLayoutResult = measure,
                                    topLeft = Offset(x, y)
                                )
                            }
                        }

                    }


                 */
    }
}

@Composable
private fun BottomAxis(
    dimensions: Dimensions,
    textMeasurer: TextMeasurer,
    config: Config
) {

    // draw the axis line
    Canvas(modifier = Modifier.fillMaxSize()) {

        val axisPath = Path()
        axisPath.moveTo(
            x = dimensions.chart.leftAxisWidth.toPx(),
            y = dimensions.chart.plotAreaHeight.toPx()
        )
        axisPath.lineTo(
            x = dimensions.chart.plotAreaWidth.toPx() + dimensions.chart.leftAxisWidth.toPx(),
            y = dimensions.chart.plotAreaHeight.toPx()
        )
        drawPath(
            path = axisPath,
            color = config.bottomAxisConfig.axisColor,
            style = Stroke(width = config.bottomAxisConfig.axisStrokeWidth.toPx())
        )
    }

    /**
     * Draw the bottom axis labels and ticks
     */
    Canvas(
        modifier = Modifier
            .width(dimensions.chart.plotAreaInnerWidth)
            .height(dimensions.chart.bottomAxisHeight)
            .offset(
                x = dimensions.chart.plotAreaInnerStart,
                y = dimensions.chart.plotAreaInnerTop + dimensions.chart.plotAreaInnerHeight
            )
    ) {

        dimensions.bottomAxisLabels.forEach { bottomAxisLabel ->

            val labelText = bottomAxisLabel.label
            val xPixels = bottomAxisLabel.xPixels

            val measure = textMeasurer.measure(
                labelText,
                config.bottomAxisConfig.labelStyle,
                config.bottomAxisConfig.labelOverflow,
                maxLines = 1
            )

            if (config.bottomAxisConfig.displayTicks) {
                drawLine(
                    color = config.bottomAxisConfig.tickColor,
                    start = Offset(xPixels, 0f),
                    end = Offset(
                        xPixels,
                        config.bottomAxisConfig.tickLength.toPx()
                    ),
                    strokeWidth = config.bottomAxisConfig.tickStrokeWidth.toPx()
                )
            }

            if (config.bottomAxisConfig.displayLabels) {

                drawText(
                    topLeft = Offset(
                        x = (xPixels - (measure.size.width / 2)),
                        y = config.bottomAxisConfig.tickLength.toPx()
                    ),
                    textLayoutResult = measure,
                    color = config.bottomAxisConfig.labelStyle.color
                )
            }
        }

    }
}

data class BottomAxisConfig(

    val axisColor: Color = Color.Black,
    val axisStrokeWidth: Dp = 2.dp,
    val tickStrokeWidth: Dp = 2.dp,
    val tickColor: Color = Color.Black,
    val tickLength: Dp = 10.dp,
    val labelStyle: TextStyle = TextStyle(
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        background = Color.Transparent
    ),
    val labelOverflow: TextOverflow = TextOverflow.Visible,

    /**
     * Defines the maximum number of ticks and labels to display on the bottom axis.
     * If left empty then all points will have a tick and label shown
     */
    val maxNumberOfLabelsToDisplay: Int? = null,

    val labelMaxLines: Int = 1,

    /**
     * Use this value to hide or show the bottom axis ticks
     */
    val displayTicks: Boolean = true,

    /**
     * Use this value to hide or show the bottom axis labels
     */
    val displayLabels: Boolean = true,

    /**
     * Use this value to hide or show the bottom axis
     */
    val display: Boolean = true
)


data class AxisConfig(

    val showCircles: Boolean = true,

    /**
     * Use this value to hide or show the bottom axis ticks
     */
    val displayTicks: Boolean = true,

    /**
     * Use this value to hide or show the bottom axis labels
     */
    val displayLabels: Boolean = true,

    /**
     * Use this value to hide or show the bottom axis
     */
    val display: Boolean = true,

    val showFillColour: Boolean = true,
    val fillBrush: Brush = Brush.verticalGradient(listOf(Color.LightGray, Color.Gray)),

    val lineColor: Color = Color.DarkGray,
    val circleColor: Color = Color.Gray,
    val circleRadius: Dp = 8.dp,

    /**
     * If true draws a bezier curve through the points to smooth corners
     */
    val smoothLine: Boolean = true,

    /**
     * Defines the maximum number of labels to display, including the origin and max values
     * Leave undefined to display a tick and label for every point
     */
    val maxNumberOfLabelsToDisplay: Int? = null,

    /**
     * A list of labels that will be displayed on the (Y) axis with lower indexes starting on the bottom
     * The left axis will be divided equally to represent the labels.
     */
    //var labels: List<ChartAxisLabel>,

    /**
     * The data points used to plot a chart.
     * For bar charts the number of data points must match the number of bottom labels
     */
    var dataPoints: List<ChartPoint> = ArrayList(),

    val axisColor: Color = Color.Black,
    val axisStrokeWidth: Dp = 2.dp,
    val tickStrokeWidth: Dp = 2.dp,
    val lineStrokeWidth: Dp = 8.dp,


    /**
     * Set the type of chart to draw for the left axis values
     */
    val type: AxisType = AxisType.Line,
    val tickColor: Color = Color.Black,
    val tickLength: Dp = 10.dp,

    /**
     * Set the minimum value used for the Y axis to control how much of the plot area the graph fills
     * To use the min value from your data points do not set this or set to zero, which will force the chart to fill the full plot area
     */
    val minY: Float? = null,

    /**
     * Set the maximum value used for the Y axis to control how much of the plot area the graph fills
     * To use the max value from your data points do not set this or set to zero, , which will force the chart to fill the full plot area
     */
    val maxY: Float? = null,

    /**
     * Set the padding to the right of the tick label on the left axis
     */
    val labelPaddingEnd: Dp = 6.dp,

    /**
     * Set the padding to the left of the tick label on the left axis
     */
    val labelPaddingStart: Dp = 2.dp,

    val labelStyle: TextStyle = TextStyle(
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        background = Color.Transparent,
    ),

    val formatPointLabel: ((Int, ChartPoint) -> String)? = null,

    /**
     * Defines the style for point labels when formatPointLabel is defined
     */
    val pointLabelStyle: TextStyle = TextStyle(
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        background = Color.Transparent,
    ),

    val labelOverflow: TextOverflow = TextOverflow.Ellipsis,
    val labelMaxLines: Int = 1,

    /**
     * Configure the cross hairs when a user taps a point on the screen
     */
    val crossHairsConfig: CrossHairs = CrossHairs()
)

data class ChartConfig(

    /**
     * Add padding to the plot area if you need more spacing around your chart
     * Leaving blank will create automatic padding, set to zero to undo this
     * */
    val plotAreaPadding: PaddingValues? = null,

    /**
     * This is the proportion of space a bar takes up on a bar chart.
     * e.g. 0.5 means the space between bars will be the same width as the bars
     * the default is 0.75, so the bar width will be three times the space
     */
    val barChartFraction: Float = 0.75f,
)


/**
 * Represents a data point to be plotted
 * @param xValue the value to use for the xAxis
 * @param yValue the value to use for the y axis
 * @param data is any data you want to attach to the point
 */
data class ChartPoint(
    val xValue: Float,
    val yValue: Float,
    val data: Any? = null,
    val key: String? = null,
    val label: String? = null
)

data class CrossHairs( val display:Boolean = true, val displayCoordinates: Boolean = true, val textStyle: TextStyle = TextStyle(color = Color.Black, fontSize = 10.sp), val lineColor:Color = Color.LightGray, val lineStrokeWidth: Dp = 1.dp)

private data class Config(
    val chartConfig: ChartConfig = ChartConfig(),
    val leftAxisConfig: AxisConfig = AxisConfig(),
    val bottomAxisConfig: BottomAxisConfig = BottomAxisConfig(),
    val formatBottomAxisLabel: ((Float) -> String)? = null,
    val formatLeftAxisLabel: ((Float) -> String)? = null,
)

/**
 * The type of axis to use for left or right axis on a line/bar chart
 */
enum class AxisType {
    Line, Bar
}


/**
 * This object contains all of the calculated dimensions required to position chart elements
 */
private data class Dimensions(
    val chart: ChartDimensions,
    val leftAxisLabelMap: Hashtable<Dp, String>? = null,
    val bottomAxisLabels: ArrayList<BottomAxisLabel> = ArrayList(),
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float,
    val barWidth: Dp
)

private data class BottomAxisLabel(
    val index: Int,
    val xValue: Float,
    val label: String,
    val xPixels: Float
)

/**
 * This object contains all of the calculated dimensions required to position elements outside of the chart. This object wilkl be returned to the user when the plot area is tapped
 */
data class ChartDimensions(
    val plotAreaWidth: Dp,
    val plotAreaHeight: Dp,
    val plotAreaInnerWidth: Dp,
    val plotAreaInnerHeight: Dp, // height taking account of any padding
    val plotAreaInnerStart: Dp,
    val plotAreaInnerTop: Dp,
    val leftAxisWidth: Dp,
    val bottomAxisHeight: Dp
)


/**
 * Calculates all required dimensions for the chart to be built
 */
private fun calculateDimensions(
    density: Density,
    textMeasurer: TextMeasurer,
    availableHeight: Dp,
    availableWidth: Dp,
    config: Config
): Dimensions {

    // plot area padding will affect plot area size
    var plotAreaPadding = PaddingValues(0.dp)

    var barWidth = 0.dp
    var bottomAxisHeight = 0.dp
    var leftAxisWidth = 0.dp
    var plotAreaInnerWidth = 0.dp
    var plotAreaInnerHeight = 0.dp
    var plotAreaInnerStart = 0.dp
    var plotAreaInnerTop = 0.dp

    var leftAxisLabelMap: Hashtable<Dp, String>? = null
    val bottomAxisLabels: ArrayList<BottomAxisLabel> = ArrayList()

    // calculate data specific dimensions
    val points =if( config.leftAxisConfig.type ==AxisType.Bar ) config.leftAxisConfig.dataPoints else config.leftAxisConfig.dataPoints.sortedBy { it.xValue }
    val minY = config.leftAxisConfig.minY ?: points.minOf { it.yValue }
    val maxY = config.leftAxisConfig.maxY ?: points.maxOf { it.yValue }
    val minX = points.minOf { it.xValue }
    val maxX = points.maxOf { it.xValue }

    /**
     * Calculate the width required for the left axis
     */
    config.leftAxisConfig.apply {

        val formatLeftAxisLabelLambda = config.formatLeftAxisLabel
        val formatBottomAxisLabelLambda = config.formatBottomAxisLabel

        val yText = if (formatLeftAxisLabelLambda != null) formatLeftAxisLabelLambda(
            this.dataPoints.maxOf { it.yValue }) else this.dataPoints.maxOf { it.yValue }
            .toInt()
            .toString()
        val xText = if (formatBottomAxisLabelLambda != null) formatBottomAxisLabelLambda(
            this.dataPoints.maxOf { it.xValue }
        ) else this.dataPoints.maxOf { it.xValue }.toInt().toString()

        bottomAxisHeight = with(density) {
            textMeasurer.measure(
                text = xText,
                style = config.bottomAxisConfig.labelStyle,
                maxLines = config.bottomAxisConfig.labelMaxLines
            ).size.height.toDp()
        } + config.bottomAxisConfig.tickLength

        leftAxisWidth =
            with(density) {

                val textWidth = if (config.leftAxisConfig.displayLabels) {
                    textMeasurer.measure(
                        yText,
                        style = config.leftAxisConfig.labelStyle,
                        maxLines = 1
                    ).size.width.toDp()
                        .plus(config.leftAxisConfig.labelPaddingStart + config.leftAxisConfig.labelPaddingEnd)
                } else 0.dp

                val ticksWidth =
                    if (config.leftAxisConfig.displayTicks) config.leftAxisConfig.tickLength else 0.dp

                config.leftAxisConfig.axisStrokeWidth + ticksWidth + textWidth
            }

        val barDivisor = points.size - 1 + config.chartConfig.barChartFraction
        barWidth = if (barDivisor == 0.0f) 0.dp else {
            availableWidth * config.chartConfig.barChartFraction / barDivisor
        }

        val autoPadding = if (config.leftAxisConfig.type == AxisType.Line) PaddingValues(
            start = 0.dp,
            end = 0.dp
        ) else
            PaddingValues(
                start = barWidth / 2,
                end = barWidth / 2
            )

        plotAreaPadding = config.chartConfig.plotAreaPadding ?: autoPadding

        // calculate the inner plot area widths
        plotAreaInnerWidth =
            availableWidth - leftAxisWidth - plotAreaPadding.calculateLeftPadding(LayoutDirection.Ltr) - plotAreaPadding.calculateRightPadding(
                LayoutDirection.Ltr
            )
        plotAreaInnerHeight =
            availableHeight - bottomAxisHeight - plotAreaPadding.calculateTopPadding() - plotAreaPadding.calculateBottomPadding()

        plotAreaInnerTop = plotAreaPadding.calculateTopPadding()
        plotAreaInnerStart =
            leftAxisWidth + plotAreaPadding.calculateLeftPadding(LayoutDirection.Ltr)


        // get a count of labels to display
        var yAxislabelCount =
            when (this.maxNumberOfLabelsToDisplay) {
                null -> points.size
                else -> this.maxNumberOfLabelsToDisplay
            }

        // fix for divide by zero bug
        if (yAxislabelCount < 2)
            yAxislabelCount = 2

        // gap between ticks on y axis in Dp
        val heightDelta = (availableHeight - bottomAxisHeight) / (yAxislabelCount - 1)

        // gap between y values on y axis in y values
        val yDelta = (maxY - minY) / (yAxislabelCount - 1)


        // add axes labels text and positioning

        // create a table of y values in Dp and strings to draw the labels and ticks
        leftAxisLabelMap = Hashtable<Dp, String>()

        for (i in 1..yAxislabelCount) {
            val y = (yDelta * (i - 1)) + minY
            val yCoOrd = availableHeight - bottomAxisHeight - (heightDelta * (i - 1))
            val labelText = if (formatLeftAxisLabelLambda == null) y.toInt()
                .toString() else formatLeftAxisLabelLambda(y)
            leftAxisLabelMap!![yCoOrd] = labelText
        }


        config.bottomAxisConfig.apply {


            // for bars the ticks align with x values so they are with the bar
            if (config.leftAxisConfig.type == AxisType.Bar) {
                val divisor = points.size - 1
                val deltaDp = if (divisor == 0) 0.dp else plotAreaInnerWidth / divisor

                // for bar charts the tick and label maps directly to a point
                points.forEachIndexed { index, chartPoint ->
                    val xValue = chartPoint.xValue
                    val xDp = deltaDp * index
                    val text = if (formatBottomAxisLabelLambda == null) xValue
                        .toString() else formatBottomAxisLabelLambda(xValue)
                    val pixels = with(density) { xDp.toPx() }
                    bottomAxisLabels.add(BottomAxisLabel(index, xValue, text, pixels))
                }
            }
            // for line chart the labels can be evenlyu distributed based on the number of ticvks wanted
            else {

                val numberOfTicks =
                    config.bottomAxisConfig.maxNumberOfLabelsToDisplay ?: points.size

                val divisor = numberOfTicks - 1
                val deltaDp = if (divisor == 0) 0.dp else plotAreaInnerWidth / divisor
                val deltaX = if( divisor == 0) 0f else (maxX - minX) / divisor

                points.forEachIndexed { index, chartPoint ->
                    val xValue = minX  + ( deltaX * index )
                    val xDp = deltaDp * index
                    val text = if (formatBottomAxisLabelLambda == null) xValue
                        .toString() else formatBottomAxisLabelLambda(xValue)
                    val pixels = with(density) { xDp.toPx() }
                    bottomAxisLabels.add(BottomAxisLabel(index, xValue, text, pixels))
                }

            }

        }


    }



    return Dimensions(
        chart = ChartDimensions(
            plotAreaWidth = availableWidth - leftAxisWidth,
            plotAreaHeight = availableHeight - bottomAxisHeight,
            plotAreaInnerHeight = plotAreaInnerHeight,
            plotAreaInnerWidth = plotAreaInnerWidth,
            plotAreaInnerTop = plotAreaInnerTop,
            plotAreaInnerStart = plotAreaInnerStart,
            leftAxisWidth = leftAxisWidth,
            bottomAxisHeight = bottomAxisHeight
        ),
        leftAxisLabelMap = leftAxisLabelMap,
        bottomAxisLabels = bottomAxisLabels,
        xMax = maxX,
        yMax = maxY,
        xMin = minX,
        yMin = minY,
        barWidth = barWidth
    )


}

