package com.niallermoran.charteasy

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.lifecycle.LiveData
import calculateDimensions
import com.google.android.material.transition.MaterialSharedAxis.Axis
import java.util.Hashtable
import kotlin.math.abs
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


            // left axis
            DrawLeftAxisArea(config = config, dimensions = dimensions)

            // bottom axis
            //DrawBox(topLeftOffset = dimensions.chart.bottomAxisArea.offset, size = dimensions.chart.bottomAxisArea.size, color = Color.Red)

            DrawBottomAxisArea(config = config, dimensions = dimensions)

            // plot area
            //DrawBox(topLeftOffset = dimensions.chart.plotArea.offset, size = dimensions.chart.plotArea.size, color = Color.Transparent)

            /*            if (config.leftAxisConfig.type == AxisType.Bar)
                            BarPlot(dimensions, config.leftAxisConfig, textMeasurer)
                        else if (config.leftAxisConfig.type == AxisType.Line)
                            LinePlot(dimensions, config.leftAxisConfig, textMeasurer)

                        LeftAxis(dimensions, textMeasurer, config)

                        BottomAxis(dimensions, textMeasurer, config)

                        CrossHairs(config = config, dimensions = dimensions)*/

        } else
            Text(text = "Not enough data", modifier = Modifier.align(Alignment.Center))
    }


}


@Composable
private fun DrawBottomAxisArea(config: Config, dimensions: Dimensions) {

    val topLeftOffset = dimensions.chart.bottomAxisArea.offset

    // all calcs are done in Dp as Float, make sure to convert to Pixels for drawing
    Canvas(
        modifier = Modifier
            .size(
                dimensions.chart.bottomAxisArea.size.width,
                dimensions.chart.bottomAxisArea.size.height
            )
            .offset(x = topLeftOffset.left, topLeftOffset.top)
            .background(Color.Magenta)
    ) {

        val widthPx = size.width
        val heightPx = size.height

        // draw axis line
        drawLine(
            color = config.bottomAxisConfig.axisColor,
            start = Offset(0f, 0f),
            end = Offset(widthPx, 0f)
        )

        // draw ticks and labels

        // draw ticks and labels
            val xPXBetweenTicks = widthPx / (dimensions.bottomAxisLabels.size - 1)

            dimensions.bottomAxisLabels.forEachIndexed { index, label ->

                val pxTick = xPXBetweenTicks * index

                drawLine(
                    color = config.bottomAxisConfig.tickColor,
                    start = Offset(pxTick, 0f),
                    end = Offset(
                        x = pxTick,
                        y = config.bottomAxisConfig.tickLength.toPx()
                    )
                )
                drawText(
                    textLayoutResult = label.text,
                    topLeft = Offset(
                        x = pxTick - (label.text.size.width / 2) ,
                        y = config.bottomAxisConfig.tickLength.toPx() + config.bottomAxisConfig.labelPadding.calculateTopPadding().toPx()
                    )
                )
            }




    }
}


@Composable
private fun DrawLeftAxisArea(config: Config, dimensions: Dimensions) {

    val topLeftOffset = dimensions.chart.leftAxisArea.topLeftOffset

    Canvas(
        modifier = Modifier
            .size(
                dimensions.chart.leftAxisArea.size.width,
                dimensions.chart.leftAxisArea.size.height
            )
            .offset(x = topLeftOffset.left, topLeftOffset.top)
            .background(Color.Cyan)
    ) {

        val widthPx = size.width
        val heightPx = size.height

        // draw axis line
        drawLine(
            color = config.leftAxisConfig.axisColor,
            start = Offset(widthPx, 0f),
            end = Offset(widthPx, heightPx)
        )

        // draw ticks and labels
        val yPxBetweenTicks = heightPx / (dimensions.leftAxisLabels.size - 1)

        dimensions.leftAxisLabels.forEachIndexed { index, leftAxisLabel ->

            val pxTick = size.height - (yPxBetweenTicks * (index))
            drawLine(
                color = config.leftAxisConfig.tickColor,
                start = Offset(widthPx, pxTick ),
                end = Offset(
                    x = widthPx - config.leftAxisConfig.tickLength.toPx(),
                    y = pxTick
                )
            )

            drawText(
                textLayoutResult = leftAxisLabel.text,
                topLeft = Offset(
                    x = widthPx
                            - config.leftAxisConfig.tickLength.toPx() - leftAxisLabel.text.size.width - config.leftAxisConfig.labelPadding.calculateRightPadding(
                        LayoutDirection.Ltr
                    ).toPx(),
                    y = pxTick - (leftAxisLabel.text.size.height / 2)
                )
            )
        }
    }


}


/*

@Composable
private fun CrossHairs(config: Config, dimensions: Dimensions) {

    var verticalCrossHairX by rememberSaveable { mutableFloatStateOf(0f) }
    var horizontalCrossHairY by rememberSaveable { mutableFloatStateOf(0f) }
    var foundPointIndex by rememberSaveable {
        mutableStateOf(-1)
    }


    if (config.leftAxisConfig.crossHairsConfig.display) {

        var points = config.leftAxisConfig.dataPoints
        if (config.leftAxisConfig.type == AxisType.Line)
            points = points.sortedBy { it.xValue }

        val textMeasurer = rememberTextMeasurer()

        val tapModifier = Modifier
            .width(dimensions.chart.plotAreaWidth)
            .height(dimensions.chart.plotAreaHeight)
            .offset(dimensions.chart.leftAxisWidth)
            .background(color = Color.Transparent)
            .pointerInput(Unit) {
                Log.d("Chart", "tap")
                detectTapGestures(
                    onTap = {
                        var deltaLeft = Float.MAX_VALUE

                        if( config.leftAxisConfig.type == AxisType.Bar) {

                            Log.d("TappedPixel", it.x.toString())
                            Log.d("StartPlotArea",  dimensions.chart.plotAreaInnerStart.toPx().toString())
                            Log.d("TappedPixel Adjusted", it.x.toString())

                            dimensions.bottomAxisLabels.forEachIndexed { index, bottomAxisLabel ->
                                val px = bottomAxisLabel.xPixels
                                Log.d("pixels", "index: $index ${px.toString()}")

                                val testDelta = abs(px - it.x)

                                if (testDelta < deltaLeft) {
                                    deltaLeft = testDelta
                                    foundPointIndex = index
                                }
                            }


                            if (foundPointIndex > -1) {

                                Log.d("FoundIndex", foundPointIndex.toString())

                                val foundPoint = points[foundPointIndex]
                                verticalCrossHairX =
                                    dimensions.chart.leftAxisWidth.toPx() + (dimensions.chart.plotAreaWidth.toPx() * (foundPoint.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin))
                                horizontalCrossHairY =
                                    dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (foundPoint.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))
                            }

                        }


                        if( config.leftAxisConfig.type == AxisType.Line) {
                            points.forEachIndexed { index, point ->

                                val px =
                                    dimensions.chart.plotAreaWidth.toPx() * (point.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin)
                                val py =
                                    dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (point.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))

                                val testDelta =
                                    sqrt(((px - it.x) * (px - it.x)) + ((py - it.y) * (py - it.y)))

                                if (testDelta < deltaLeft) {
                                    deltaLeft = testDelta
                                    foundPointIndex = index
                                }
                            }

                            if (foundPointIndex > -1) {

                                Log.d("FoundIndex", foundPointIndex.toString())

                                val foundPoint = points[foundPointIndex]
                                verticalCrossHairX =
                                    dimensions.chart.leftAxisWidth.toPx() + (dimensions.chart.plotAreaWidth.toPx() * (foundPoint.xValue - dimensions.xMin) / (dimensions.xMax - dimensions.xMin))
                                horizontalCrossHairY =
                                    dimensions.chart.plotAreaHeight.toPx() - (dimensions.chart.plotAreaHeight.toPx() * (foundPoint.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin))
                            }

                        }

                    }
                )
            }




        Canvas(modifier = tapModifier) {
            if( foundPointIndex > -1){

                val foundPoint = points[foundPointIndex]

                drawLine(
                    start = Offset(dimensions.chart.leftAxisWidth.toPx(), horizontalCrossHairY),
                    end = Offset(
                        dimensions.chart.plotAreaWidth.toPx() + dimensions.chart.leftAxisWidth.toPx(),
                        horizontalCrossHairY
                    ),
                    color = Color.Red
                )

                drawLine(
                    start = Offset(verticalCrossHairX, dimensions.chart.plotAreaHeight.toPx()),
                    end = Offset(verticalCrossHairX, 0f),
                    color = Color.Red
                )


                val label = foundPoint.label ?: "(${foundPoint.xValue},${foundPoint.yValue})"

                val measure = textMeasurer.measure(
                    label,
                    config.leftAxisConfig.labelStyle,
                    config.leftAxisConfig.labelOverflow,
                    maxLines = 1
                )

                val maxIndex = points.size - 1
                var leftDelta = measure.size.width / 2
                var topDelta = measure.size.height * 1.2f

                if (foundPointIndex == maxIndex)
                    leftDelta = measure.size.width

                if (foundPointIndex == 0) {
                    leftDelta = 0

                    val deltaFromMinY =
                        100 * (foundPoint.yValue - dimensions.yMin) / (dimensions.yMax - dimensions.yMin)
                    if (deltaFromMinY < 5)
                        topDelta = measure.size.height * 2f

                    if (deltaFromMinY > 90)
                        topDelta = 0f
                }

                val left = verticalCrossHairX - leftDelta
                val top = horizontalCrossHairY - topDelta

                drawText(
                    textLayoutResult = measure,
                    color = Color.Black,
                    topLeft = Offset(left, top)
                )
            }
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
            .background(Color.Transparent)
            .fillMaxSize()
    ) {

        dimensions.bottomAxisLabels.forEach { bottomAxisLabel ->

            val labelText = bottomAxisLabel.label
            val xPixels = bottomAxisLabel.xPixels + dimensions.chart.plotAreaInnerStart.toPx()

            val measure = textMeasurer.measure(
                labelText,
                config.bottomAxisConfig.labelStyle,
                config.bottomAxisConfig.labelOverflow,
                maxLines = 1
            )

            if (config.bottomAxisConfig.displayTicks) {
                drawLine(
                    color = config.bottomAxisConfig.tickColor,
                    start = Offset(xPixels, dimensions.chart.plotAreaHeight.toPx()),
                    end = Offset(
                        xPixels,
                        config.bottomAxisConfig.tickLength.toPx()+ dimensions.chart.plotAreaHeight.toPx()
                    ),
                    strokeWidth = config.bottomAxisConfig.tickStrokeWidth.toPx()
                )
            }

            if (config.bottomAxisConfig.displayLabels) {

                drawText(
                    topLeft = Offset(
                        x = (xPixels - (measure.size.width / 2)),
                        y = config.bottomAxisConfig.tickLength.toPx()+ dimensions.chart.plotAreaHeight.toPx()
                    ),
                    textLayoutResult = measure,
                    color = config.bottomAxisConfig.labelStyle.color
                )
            }
        }

    }
}


 */