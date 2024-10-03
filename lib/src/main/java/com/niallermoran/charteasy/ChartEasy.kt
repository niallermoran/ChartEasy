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
import androidx.compose.ui.graphics.drawscope.DrawScope
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
    leftAxisConfig: VerticalAxisConfig,
    bottomAxisConfig: BottomAxisConfig = BottomAxisConfig(),
    rightAxisConfig: VerticalAxisConfig = VerticalAxisConfig(),
) {
    val density = LocalDensity.current
    val config = Config(
        chartConfig = chartConfig,
        leftAxisConfig = leftAxisConfig,
        bottomAxisConfig = bottomAxisConfig,
        rightAxisConfig = rightAxisConfig
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        // we can do nothing without a left axis config and more than one data point
        if (leftAxisConfig.dataPoints.size > 1) {

            val textMeasurer = rememberTextMeasurer()
            val dimensions = calculateDimensions(
                density = density,
                textMeasurer = textMeasurer,
                config = config,
                availableHeight = with(density) { constraints.maxHeight.toDp() },
                availableWidth = with(density) { constraints.maxWidth.toDp() }
            )

            DrawLeftAxisArea(config = config, dimensions = dimensions)
            DrawRightAxisArea(config = config, dimensions = dimensions)
            DrawBottomAxisArea(config = config, dimensions = dimensions)
            DrawPlotArea(config = config, dimensions = dimensions)



        } else
            Text(text = "Not enough data", modifier = Modifier.align(Alignment.Center))
    }


}

@Composable
private fun DrawPlotArea(config: Config, dimensions: Dimensions)
{
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .size(
                dimensions.chart.plotArea.innerWidth,
                dimensions.chart.plotArea.innerHeight
            )
            .offset(x = dimensions.chart.plotArea.innerTopLeftOffset.left,
                dimensions.chart.plotArea.innerTopLeftOffset.top
            )
    ) {

        if( config.leftAxisConfig.type == AxisType.Line )
            drawLinePlot( config, dimensions, false, textMeasurer)

        if( config.rightAxisConfig.type == AxisType.Line )
            drawLinePlot( config, dimensions, true, textMeasurer)

        if( config.leftAxisConfig.type == AxisType.Bar )
            drawBarPlot( config, dimensions, false, textMeasurer)

        if( config.rightAxisConfig.type == AxisType.Bar )
            drawBarPlot( config, dimensions, true, textMeasurer)

    }
}



private fun DrawScope.drawBarPlot(
    config: Config,
    dimensions: Dimensions,
    rightAxis: Boolean = false,
    textMeasurer: TextMeasurer
) {
    // x,y values
    val points = dimensions.dataValues.points
    val xMin = dimensions.dataValues.xMin
    val yMin = if( rightAxis ) dimensions.dataValues.yMinRight else dimensions.dataValues.yMin
    val xMax = dimensions.dataValues.xMax
    val yMax = if( rightAxis ) dimensions.dataValues.yMaxRight else dimensions.dataValues.yMax

    val plotAreaWidth = dimensions.chart.plotArea.innerWidth
    val plotAreaHeight = dimensions.chart.plotArea.innerHeight

    val lineColor = if( rightAxis ) config.rightAxisConfig.lineColor else config.leftAxisConfig.lineColor
    //val fillBrush = if( rightAxis ) config.rightAxisConfig.fillBrush else config.leftAxisConfig.lineColor

    val spaceBetweenBarCenters = plotAreaWidth / ( points.size - 1)
    points.forEachIndexed { index, chartPoint ->
        val barCenterDistanceAlongXAxis = spaceBetweenBarCenters * (index)
        val barWidth = dimensions.chart.plotArea.barWidth
        val yValue = if(rightAxis) chartPoint.yValueRightAxis else chartPoint.yValue
        if( yValue != null )
        {
            val barLeftX = (barCenterDistanceAlongXAxis - barWidth/2).toPx()
            val barRightX = (barCenterDistanceAlongXAxis + barWidth/2).toPx()
            val barTopY = (plotAreaHeight - ( plotAreaHeight * (yValue - yMin)/(yMax - yMin) )).toPx()
            val barBottomY = plotAreaHeight.toPx()

            // draw bar
            val path=Path()
            path.moveTo( barLeftX,  barBottomY)
            path.lineTo( barLeftX,  barTopY )
            path.lineTo( barRightX, barTopY)
            path.lineTo( barRightX, barBottomY)

            drawPath(path, color= lineColor )
        }
    }


    val lambda = ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).formatPointLabel
    if (lambda != null) {
        for (i in 1..points.size) {
            val point = points[i - 1]
            val text = lambda(i, point)
            val measure = textMeasurer.measure(
                text,
                ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).pointLabelStyle,
                ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).labelOverflow,
                maxLines = 1
            )

            val x =
                (plotAreaWidth.toPx() * (point.xValue - xMin) / (xMax - xMin)) - (measure.size.width / 2)
            val y =
                plotAreaHeight.toPx() - (plotAreaHeight.toPx() * (point.yValue - yMin) / (yMax - yMin)) - ((measure.size.height) * 1.5f)


            drawText(
                textLayoutResult = measure,
                topLeft = Offset(x, y)
            )
        }
    }
}



private fun DrawScope.drawLinePlot(
    config: Config,
    dimensions: Dimensions,
    rightAxis: Boolean = false,
    textMeasurer: TextMeasurer
) {
        // x,y values
        val points = dimensions.dataValues.points
        val xMin = dimensions.dataValues.xMin
        val yMin = if( rightAxis ) dimensions.dataValues.yMinRight else dimensions.dataValues.yMin
        val xMax = dimensions.dataValues.xMax
        val yMax = if( rightAxis ) dimensions.dataValues.yMaxRight else dimensions.dataValues.yMax

        // x,y pixel points
        val linePoints = ArrayList<PointF>(points.size)

        val plotAreaWidth = dimensions.chart.plotArea.innerWidth
        val plotAreaHeight = dimensions.chart.plotArea.innerHeight

        // get the pixle co-ordniates for every point
        for (point in points) {
            // actual point add to path
            val x = plotAreaWidth * (point.xValue -  dimensions.dataValues.xMin) / (xMax - xMin)
            val yValue = if(rightAxis) point.yValueRightAxis else point.yValue

            if( yValue != null ) {
                val y = plotAreaHeight - ( plotAreaHeight * (yValue - yMin) / (yMax - yMin))
                linePoints.add(PointF(x.toPx(), y.toPx()))
            }
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
        pathFill.moveTo(0f, plotAreaHeight.toPx())

        // path for line
        val pathLine = Path()

        // path for spline
        val pathSpline = Path()

        // draw line and fill
        val smooth = if( rightAxis ) config.rightAxisConfig.smoothLine else config.leftAxisConfig.smoothLine
        val showFillColour = if( rightAxis ) config.rightAxisConfig.showFillColour else config.leftAxisConfig.showFillColour

        if (smooth) {
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
                plotAreaWidth.toPx(),
                plotAreaHeight.toPx()
            )
        } else {
            pathLine.moveTo(linePoints.first().x, linePoints.first().y)

            linePoints.forEach { pointF ->
                pathLine.lineTo(pointF.x, pointF.y)
                pathFill.lineTo(pointF.x, pointF.y)
            }

            pathFill.lineTo(
                plotAreaWidth.toPx(),
                plotAreaHeight.toPx()
            )
        }


        drawPath(
            path = pathLine,
            color = if( rightAxis) config.rightAxisConfig.lineColor else config.leftAxisConfig.lineColor,
            style = Stroke(width = if( rightAxis ) config.rightAxisConfig.lineStrokeWidth.toPx() else config.leftAxisConfig.lineStrokeWidth.toPx())
        )

        if (points.size > 2 && smooth) {
            drawPath(
                path = pathSpline,
                color = if( rightAxis) config.rightAxisConfig.lineColor else config.leftAxisConfig.lineColor,
                style = Stroke(width = if( rightAxis ) config.rightAxisConfig.lineStrokeWidth.toPx() else config.leftAxisConfig.lineStrokeWidth.toPx())
            )
        }

        if (showFillColour) {
            drawPath(
                path = pathFill,
                brush = ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).fillBrush,
                style = Fill,
                alpha = 0.3f,
            )
        }

        if (( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).showCircles) {

            if (points.size == 1) {
                drawCircle(
                    color = ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).circleColor,
                    center = Offset(
                        plotAreaWidth.toPx(),
                        plotAreaHeight.toPx()
                    ),
                    radius = ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).circleRadius.value,

                    )
            } else {
                for (i in 1..points.size) {
                    val point = points[i - 1]

                    val yValue = if(rightAxis) point.yValueRightAxis else point.yValue
                    if( yValue != null) {

                        val x =
                        plotAreaWidth.toPx() * (point.xValue - xMin) / (xMax - xMin)
                    val y =
                        plotAreaHeight.toPx() - (plotAreaHeight.toPx() * (yValue - yMin) / (yMax - yMin))

                        drawCircle(
                            color = (if (rightAxis) config.rightAxisConfig else config.leftAxisConfig).circleColor,
                            center = Offset(x, y),
                            radius = (if (rightAxis) config.rightAxisConfig else config.leftAxisConfig).circleRadius.value,

                            )
                    }
                }
            }
        }

        val lambda = ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).formatPointLabel
        if (lambda != null) {
            for (i in 1..points.size) {
                val point = points[i - 1]
                val text = lambda(i, point)
                val measure = textMeasurer.measure(
                    text,
                    ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).pointLabelStyle,
                    ( if( rightAxis ) config.rightAxisConfig else config.leftAxisConfig).labelOverflow,
                    maxLines = 1
                )

                val x =
                    (plotAreaWidth.toPx() * (point.xValue - xMin) / (xMax - xMin)) - (measure.size.width / 2)
                val y =
                    plotAreaHeight.toPx() - (plotAreaHeight.toPx() * (point.yValue - yMin) / (yMax - yMin)) - ((measure.size.height) * 1.5f)


                drawText(
                    textLayoutResult = measure,
                    topLeft = Offset(x, y)
                )
            }
        }
}



@Composable
private fun DrawBottomAxisArea(config: Config, dimensions: Dimensions) {

    val topLeftOffset = dimensions.chart.bottomAxisArea.offset
    val gridLines = config.bottomAxisConfig.gridLines


    // all calcs are done in Dp as Float, make sure to convert to Pixels for drawing
    Canvas(
        modifier = Modifier
            .size(
                dimensions.chart.bottomAxisArea.size.width,
                dimensions.chart.bottomAxisArea.size.height
            )
            .offset(x = topLeftOffset.left, topLeftOffset.top)
    ) {


        val widthPx =  dimensions.chart.plotArea.size.width.toPx() - dimensions.chart.plotArea.padding.calculateLeftPadding(LayoutDirection.Ltr).toPx() - dimensions.chart.plotArea.padding.calculateRightPadding(LayoutDirection.Ltr).toPx()
        val heightPx = dimensions.chart.plotArea.size.height.toPx() - dimensions.chart.plotArea.padding.calculateTopPadding().toPx() - dimensions.chart.plotArea.padding.calculateBottomPadding().toPx()

        // draw axis line
        drawLine(
            color = config.bottomAxisConfig.axisColor,
            start = Offset(0f, 0f),
            end = Offset( dimensions.chart.chartSize.width.toPx() - dimensions.chart.leftAxisArea.size.width.toPx(), 0f)
        )

        // draw ticks and labels

        // draw ticks and labels
        val xPXBetweenTicks = widthPx / (dimensions.bottomAxisLabels.size - 1)

        dimensions.bottomAxisLabels.forEachIndexed { index, label ->

            val pxTick = xPXBetweenTicks * index + dimensions.chart.plotArea.padding.calculateLeftPadding(LayoutDirection.Ltr).toPx()

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
                    x = pxTick - (label.text.size.width / 2),
                    y = config.bottomAxisConfig.tickLength.toPx() + config.bottomAxisConfig.labelPadding.calculateTopPadding()
                        .toPx()
                )
            )

            if(gridLines.display)
            {
                drawLine(
                    strokeWidth = gridLines.strokeWidth,
                    color = gridLines.color,
                    start = Offset(pxTick, 0f),
                    end = Offset(
                        x = pxTick,
                        y = - dimensions.chart.plotArea.size.height.toPx()
                    )
                )
            }
        }


    }
}


@Composable
private fun DrawLeftAxisArea(config: Config, dimensions: Dimensions) {

    val topLeftOffset = dimensions.chart.leftAxisArea.topLeftOffset
    val gridLines = config.leftAxisConfig.gridLines

    Canvas(
        modifier = Modifier
            .size(
                dimensions.chart.leftAxisArea.size.width,
                dimensions.chart.leftAxisArea.size.height
            )
            .offset(x = topLeftOffset.left, topLeftOffset.top)
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
                start = Offset(widthPx, pxTick),
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

            if(gridLines.display)
            {
                drawLine(
                    strokeWidth = gridLines.strokeWidth,
                    color = gridLines.color,
                    start = Offset(widthPx, pxTick),
                    end = Offset(
                        x = widthPx + dimensions.chart.plotArea.size.width.toPx(),
                        y = pxTick
                    )
                )
            }
        }
    }


}


@Composable
private fun DrawRightAxisArea(config: Config, dimensions: Dimensions) {

    val topLeftOffset =
        OffsetDp(dimensions.chart.chartSize.width - dimensions.chart.rightAxisArea.size.width, 0.dp)

    val gridLines = config.rightAxisConfig.gridLines

    Canvas(
        modifier = Modifier
            .size(
                dimensions.chart.rightAxisArea.size.width,
                dimensions.chart.rightAxisArea.size.height
            )
            .offset(x = topLeftOffset.left, topLeftOffset.top)
    ) {

        val widthPx = size.width
        val heightPx = size.height

        // draw axis line
        drawLine(
            color = config.rightAxisConfig.axisColor,
            start = Offset(0f, 0f),
            end = Offset(0f, heightPx)
        )

        // draw ticks and labels
        val yPxBetweenTicks = heightPx / (dimensions.rightAxisLabels.size - 1)

        dimensions.rightAxisLabels.forEachIndexed { index, label ->

            val pxTick = size.height - (yPxBetweenTicks * (index))
            drawLine(
                color = config.rightAxisConfig.tickColor,
                start = Offset(0f, pxTick),
                end = Offset(
                    x = config.rightAxisConfig.tickLength.toPx(),
                    y = pxTick
                )
            )

            drawText(
                textLayoutResult = label.text,
                topLeft = Offset(
                    x = config.rightAxisConfig.tickLength.toPx() + config.rightAxisConfig.labelPadding.calculateLeftPadding(
                        LayoutDirection.Ltr
                    ).toPx(),
                    y = pxTick - (label.text.size.height / 2)
                )
            )

            Log.d( "strokewidth", gridLines.strokeWidth.toString())
            if(gridLines.display)
            {
                drawLine(
                    strokeWidth = gridLines.strokeWidth,
                    color = gridLines.color,
                    start = Offset(0f, pxTick),
                    end = Offset(
                        x = - dimensions.chart.plotArea.size.width.toPx(),
                        y = pxTick
                    )
                )
            }
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