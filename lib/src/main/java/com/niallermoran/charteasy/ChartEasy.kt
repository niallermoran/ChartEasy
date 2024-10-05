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
import androidx.compose.ui.graphics.drawscope.DrawStyle
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
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
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

            if (config.leftAxisConfig.display)
                DrawLeftAxisArea(config = config, dimensions = dimensions)

            if (config.rightAxisConfig.display)
                DrawRightAxisArea(config = config, dimensions = dimensions)

            DrawBottomAxisArea(config = config, dimensions = dimensions)
            DrawPlotArea(config = config, dimensions = dimensions)
            DrawCrossHairs(config = config, dimensions = dimensions)

            val lambda = config.chartConfig.onDraw
            if( lambda != null )
                DrawUserObjects(
                    dimensions = dimensions,
                    userLambda = lambda
                )

        } else
            Text(text = "Not enough data", modifier = Modifier.align(Alignment.Center))
    }


}

@Composable
private fun DrawUserObjects(  dimensions: Dimensions, userLambda: DrawScope.( chartDimension: ChartDimensions ) -> Unit)
{
    // create a canvas which maps to the inside plot area and
    // provide access to the drawscope so that the user can draw custom objects
    Canvas(modifier = Modifier
        .width(dimensions.chart.chartSize.width)
        .height(dimensions.chart.chartSize.height)
    )
    {
        userLambda( dimensions.chart )
    }
}

@Composable
private fun DrawPlotArea(config: Config, dimensions: Dimensions) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .size(
                dimensions.chart.plotArea.innerWidth,
                dimensions.chart.plotArea.innerHeight
            )
            .offset(
                x = dimensions.chart.plotArea.innerTopLeftOffset.left,
                dimensions.chart.plotArea.innerTopLeftOffset.top
            )
            .background(Color.Transparent)
    ) {

        if (config.leftAxisConfig.display) {
            if (config.leftAxisConfig.type == AxisType.Line)
                drawLinePlot(config, dimensions, false, textMeasurer)

            if (config.leftAxisConfig.type == AxisType.Bar)
                drawBarPlot(config, dimensions, false, textMeasurer)
        }

        if (config.rightAxisConfig.display) {
            if (config.rightAxisConfig.type == AxisType.Line)
                drawLinePlot(config, dimensions, true, textMeasurer)


            if (config.rightAxisConfig.type == AxisType.Bar)
                drawBarPlot(config, dimensions, true, textMeasurer)
        }
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
    val yMin = if (rightAxis) dimensions.dataValues.yMinRight else dimensions.dataValues.yMin
    val xMax = dimensions.dataValues.xMax
    val yMax = if (rightAxis) dimensions.dataValues.yMaxRight else dimensions.dataValues.yMax

    val plotAreaWidth = dimensions.chart.plotArea.innerWidth
    val plotAreaHeight = dimensions.chart.plotArea.innerHeight

    val lineColor =
        if (rightAxis) config.rightAxisConfig.lineColor else config.leftAxisConfig.lineColor

    val spaceBetweenBarCenters = plotAreaWidth / (points.size - 1)
    points.forEachIndexed { index, chartPoint ->
        val barCenterDistanceAlongXAxis = spaceBetweenBarCenters * (index)
        val barWidth = dimensions.chart.plotArea.barWidth
        val yValue = if (rightAxis) chartPoint.yValueRightAxis else chartPoint.yValue
        val xValue = chartPoint.xValue

        if (yValue != null) {
            val barLeftX = (barCenterDistanceAlongXAxis - barWidth / 2).toPx()
            val barRightX = (barCenterDistanceAlongXAxis + barWidth / 2).toPx()
            val barTopY =
                (plotAreaHeight - (plotAreaHeight * (yValue - yMin) / (yMax - yMin))).toPx()
            val barBottomY = plotAreaHeight.toPx()

            // draw bar
            val path = Path()
            path.moveTo(barLeftX, barBottomY)
            path.lineTo(barLeftX, barTopY)
            path.lineTo(barRightX, barTopY)
            path.lineTo(barRightX, barBottomY)

            val axisConfig = if (rightAxis) config.rightAxisConfig else config.leftAxisConfig

            drawPath(
                path = path,
                color = axisConfig.lineColor,
                style = Stroke(width = axisConfig.lineStrokeWidth.toPx())
            )

            if (axisConfig.showFillColour) {
                drawPath(
                    path = path,
                    brush = axisConfig.fillBrush,
                    style = Fill,
                    alpha = axisConfig.fillAlpha
                )
            }

            // draw point label text
            val text = axisConfig.getPointLabelText(chartPoint, rightAxis, textMeasurer)
            if (text != null) {
                drawText(
                    textLayoutResult = text,
                    topLeft = Offset(
                        x = barCenterDistanceAlongXAxis.toPx() - (text.size.width / 2),
                        y = barTopY - text.size.height
                    )
                )
            }
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
    val yMin = if (rightAxis) dimensions.dataValues.yMinRight else dimensions.dataValues.yMin
    val xMax = dimensions.dataValues.xMax
    val yMax = if (rightAxis) dimensions.dataValues.yMaxRight else dimensions.dataValues.yMax

    // x,y pixel points
    val linePoints = ArrayList<PointF>(points.size)

    val plotAreaWidth = dimensions.chart.plotArea.innerWidth
    val plotAreaHeight = dimensions.chart.plotArea.innerHeight

    // get the pixle co-ordniates for every point
    for (point in points) {
        // actual point add to path
        val x = plotAreaWidth * (point.xValue - dimensions.dataValues.xMin) / (xMax - xMin)
        val yValue = if (rightAxis) point.yValueRightAxis else point.yValue

        if (yValue != null) {
            val y = plotAreaHeight - (plotAreaHeight * (yValue - yMin) / (yMax - yMin))
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
    val smooth =
        if (rightAxis) config.rightAxisConfig.smoothLine else config.leftAxisConfig.smoothLine
    val showFillColour =
        if (rightAxis) config.rightAxisConfig.showFillColour else config.leftAxisConfig.showFillColour

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
        color = if (rightAxis) config.rightAxisConfig.lineColor else config.leftAxisConfig.lineColor,
        style = Stroke(width = if (rightAxis) config.rightAxisConfig.lineStrokeWidth.toPx() else config.leftAxisConfig.lineStrokeWidth.toPx())
    )

    if (points.size > 2 && smooth) {
        drawPath(
            path = pathSpline,
            color = if (rightAxis) config.rightAxisConfig.lineColor else config.leftAxisConfig.lineColor,
            style = Stroke(width = if (rightAxis) config.rightAxisConfig.lineStrokeWidth.toPx() else config.leftAxisConfig.lineStrokeWidth.toPx())
        )
    }

    if (showFillColour) {
        drawPath(
            path = pathFill,
            brush = (if (rightAxis) config.rightAxisConfig else config.leftAxisConfig).fillBrush,
            style = Fill,
            alpha = (if (rightAxis) config.rightAxisConfig else config.leftAxisConfig).fillAlpha,
        )
    }

    if ((if (rightAxis) config.rightAxisConfig else config.leftAxisConfig).showCircles) {

        if (points.size == 1) {
            drawCircle(
                color = (if (rightAxis) config.rightAxisConfig else config.leftAxisConfig).circleColor,
                center = Offset(
                    plotAreaWidth.toPx(),
                    plotAreaHeight.toPx()
                ),
                radius = (if (rightAxis) config.rightAxisConfig else config.leftAxisConfig).circleRadius.value,

                )
        } else {
            for (i in 1..points.size) {
                val point = points[i - 1]

                val yValue = if (rightAxis) point.yValueRightAxis else point.yValue
                if (yValue != null) {

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




    for (i in 1..points.size) {
        val point = points[i - 1]

        // draw point label text
        val axisConfig = if (rightAxis) config.rightAxisConfig else config.leftAxisConfig
        val text = axisConfig.getPointLabelText(point, rightAxis, textMeasurer)

        if (text != null) {
            val x =
                (plotAreaWidth.toPx() * (point.xValue - xMin) / (xMax - xMin)) - (text.size.width / 2)
            val y =
                plotAreaHeight.toPx() - (plotAreaHeight.toPx() * (point.yValue - yMin) / (yMax - yMin)) - ((text.size.height) * 1.5f)

            drawText(
                textLayoutResult = text,
                topLeft = Offset(
                    x = x,
                    y = y
                )
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


        val widthPx =
            dimensions.chart.plotArea.size.width.toPx() - dimensions.chart.plotArea.padding.calculateLeftPadding(
                LayoutDirection.Ltr
            ).toPx() - dimensions.chart.plotArea.padding.calculateRightPadding(LayoutDirection.Ltr)
                .toPx()

        // draw axis line
        drawLine(
            strokeWidth = config.bottomAxisConfig.axisStrokeWidth.toPx(),
            color = config.bottomAxisConfig.axisColor,
            start = Offset(0f, 0f),
            end = Offset(dimensions.chart.plotArea.size.width.toPx(), 0f)
        )

        // draw ticks and labels
        val xPXBetweenTicks = widthPx / (dimensions.bottomAxisLabels.size - 1)

        dimensions.bottomAxisLabels.forEachIndexed { index, label ->

            val pxTick =
                xPXBetweenTicks * index + dimensions.chart.plotArea.padding.calculateLeftPadding(
                    LayoutDirection.Ltr
                ).toPx()

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

            if (gridLines.display) {
                drawLine(
                    strokeWidth = gridLines.strokeWidth,
                    color = gridLines.color,
                    start = Offset(pxTick, 0f),
                    end = Offset(
                        x = pxTick,
                        y = -dimensions.chart.plotArea.size.height.toPx()
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

            if (gridLines.display) {
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

            Log.d("strokewidth", gridLines.strokeWidth.toString())
            if (gridLines.display) {
                drawLine(
                    strokeWidth = gridLines.strokeWidth,
                    color = gridLines.color,
                    start = Offset(0f, pxTick),
                    end = Offset(
                        x = -dimensions.chart.plotArea.size.width.toPx(),
                        y = pxTick
                    )
                )
            }
        }
    }


}


@Composable
private fun DrawCrossHairs(config: Config, dimensions: Dimensions) {

    var verticalCrossHairXPixels by rememberSaveable { mutableStateOf(0f) }
    var horizontalCrossHairYPixels by rememberSaveable { mutableStateOf(0f) }
    var horizontalCrossHairYRightAxisPixels by rememberSaveable { mutableStateOf(0f) }

    // get offsets to inner ploat area
    val xOffset = dimensions.chart.plotArea.innerTopLeftOffset.left
    val yOffset = dimensions.chart.plotArea.innerTopLeftOffset.top
    var modifier = Modifier.fillMaxSize()

    // the navas for the cross hairs responds to tap, so use the full plot area width (not just inner)
    modifier = modifier
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {

                    // translate the tapped point to an equivalent point on the inner plot area
                    val innerTappedDp = OffsetDp(
                        left = it.x.toDp() - xOffset,
                        top = it.y.toDp() - yOffset
                    )

                    // if we tapped outside of the inner plot area, just zero the relevant co-ordinate
                    if( innerTappedDp.left.value < 0f ) innerTappedDp.left = 0.dp
                    if( innerTappedDp.top.value < 0f ) innerTappedDp.top = 0.dp

                    // get the closest point to where the user tapped
                    val point = dimensions.chart.plotArea.getClosestPoint( innerTappedDp, dimensions)
                    val pointDp = dimensions.chart.plotArea.getPlotAreaInnerOffsetForChartPoint(point, dimensions)
                    val chartOffsetForPoint = dimensions.chart.getChartOffsetForChartPoint(chartPoint = point, dimensions = dimensions)

                    // call the user's lambda if it exists
                    val lambda = config.chartConfig.onTap
                    if( lambda != null)
                        lambda( point, chartOffsetForPoint )

                    Log.d("crosshairs-found", "(${point.xValue},${point.yValue})")
                    Log.d("crosshairs-pointdp", "(${pointDp.left},${pointDp.top})")
                    Log.d("crosshairs-tapped", "(${innerTappedDp.left},${innerTappedDp.top})")
                    //  Log.d("TappedPixels", "(${pixels.left},${pixels.top})")

                    // reposition cross hairs, accounting for padding
                    horizontalCrossHairYPixels = pointDp.top.toPx() + yOffset.toPx()
                    verticalCrossHairXPixels = pointDp.left.toPx() + xOffset.toPx()

                }
            )
        }

    // draw the cross hairs
    Canvas(modifier = modifier) {

        // draw vertical line
        if (config.bottomAxisConfig.crossHairsConfig.display && (verticalCrossHairXPixels + horizontalCrossHairYPixels) > 0f) {
            drawLine(
                color = config.bottomAxisConfig.crossHairsConfig.lineColor,
                strokeWidth = config.bottomAxisConfig.crossHairsConfig.lineStrokeWidth.toPx(),
                start = Offset(verticalCrossHairXPixels, 0f),
                end = Offset(verticalCrossHairXPixels, dimensions.chart.plotArea.innerHeight.toPx())
            )
        }

        // draw horizontal line
        if (config.leftAxisConfig.crossHairsConfig.display && (verticalCrossHairXPixels + horizontalCrossHairYPixels) > 0f) {
            drawLine(
                color = config.bottomAxisConfig.crossHairsConfig.lineColor,
                strokeWidth = config.bottomAxisConfig.crossHairsConfig.lineStrokeWidth.toPx(),
                start = Offset(0f, horizontalCrossHairYPixels),
                end = Offset(size.width,  horizontalCrossHairYPixels)
            )
        }
    }


}
