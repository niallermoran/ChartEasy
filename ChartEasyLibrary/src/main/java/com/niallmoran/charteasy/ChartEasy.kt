package com.niallmoran.charteasy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Hashtable


@Composable
fun ChartEasy(
    modifier: Modifier = Modifier,
    config: Config,
    formatBottomAxisLabel: ((Int, Float, ChartPoint?) -> String)? = null,
    formatLeftAxisLabel: ((Int, Float) -> String)? = null,
    formatRightAxisLabel: ((Int, Float) -> String)? = null,
) {

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // take an x and y value to calculate the spans for the axis
    val x = config.leftAxisConfig.dataPoints.maxOf { it.xValue }
    val y = config.leftAxisConfig.dataPoints.maxOf { it.yValue }
    val z =
        if (config.rightAxisConfig != null) config.rightAxisConfig.dataPoints.maxOf { it.yValue } else 0f

    val yText = if (formatLeftAxisLabel != null) formatLeftAxisLabel(0, y) else y.toInt().toString()
    val zText = if (formatRightAxisLabel != null) formatRightAxisLabel(0, z) else z.toInt().toString()
    val xText = if (formatBottomAxisLabel != null) formatBottomAxisLabel(0, x, null) else x.toInt().toString()

    val bottomAxisHeight = with(density) {
        textMeasurer.measure(
            text = xText,
            style = config.bottomAxisConfig.labelStyle,
            maxLines = config.bottomAxisConfig.labelMaxLines
        ).size.height.toDp()
    } + config.bottomAxisConfig.tickLength

    val leftAxisWidth = with(density) {
        textMeasurer.measure(
            yText,
            style = config.leftAxisConfig.labelStyle,
            maxLines = 1
        ).size.width.toDp()
    }.plus(
        config.leftAxisConfig.tickLength + config.leftAxisConfig.labelPaddingStart + config.leftAxisConfig.labelPaddingEnd
    )

    val rightAxisWidth = if (config.rightAxisConfig != null)
        with(density) {
            textMeasurer.measure(
                zText,
                style = config.rightAxisConfig.labelStyle,
                maxLines = 1
            ).size.width.toDp()
        }.plus(
            config.rightAxisConfig.tickLength + config.rightAxisConfig.labelPaddingStart + config.rightAxisConfig.labelPaddingEnd
        )
    else 0.dp


    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(config.chartConfig.height)
    ) {

        val plotAreaHeightDP = with(density) { constraints.maxHeight.toDp() } - bottomAxisHeight

        Row()
        {

            /**
             * left axis line, ticks and labels
             */
            BoxWithConstraints(
                modifier = Modifier
                    .height(plotAreaHeightDP)
                    .width(leftAxisWidth)
            ) {
                val height = constraints.maxHeight
                val width = constraints.maxWidth

                // draw axis line
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawBehind {
                            drawLine(
                                color = config.leftAxisConfig.axisColor,
                                start = Offset(width.toFloat(), 0f),
                                end = Offset(width.toFloat(), height.toFloat()),
                                strokeWidth = config.leftAxisConfig.axisStrokeWidth.toPx()
                            )
                        }
                    }) {
                }

                YAxisTicksAndLabels(config.leftAxisConfig, formatLeftAxisLabel)

            }


            /**
             * Plot area and bottom axis
             */
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {

                Column(modifier = Modifier.fillMaxWidth())
                {
                    /**
                     * Plot area
                     */
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(config.chartConfig.plotAreaPadding)
                    )
                    {

                        if (config.leftAxisConfig.type == ChartType.Line)
                            DrawLineChart(config.leftAxisConfig, config.onPlotAreaTap)
                        else
                            DrawBarChart(config)

                        config.rightAxisConfig?.let {
                            if (it.type == ChartType.Line)
                                DrawLineChart(config.rightAxisConfig, config.onPlotAreaTap)
                            else
                                DrawBarChart(config)
                        }
                    }

                    /**
                     * Bottom axis line, ticks and labels
                     */
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bottomAxisHeight),
                    )
                    {

                        val width = with(density) { constraints.maxWidth.toDp() }

                        // draw axis line
                        Canvas(modifier = Modifier
                            .fillMaxSize()
                            .drawWithCache {
                                onDrawBehind {
                                    drawLine(
                                        color = config.bottomAxisConfig.tickColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(width.toPx(), 0f),
                                        strokeWidth = config.bottomAxisConfig.axisStrokeWidth.toPx()
                                    )
                                }
                            }) {
                        }


                        DrawBottomAxisTicksAndLabels(config, formatBottomAxisLabel)


                    }
                }
            }


            if (config.rightAxisConfig != null) {
                /**
                 * right axis line, ticks and labels
                 */
                BoxWithConstraints(
                    modifier = Modifier
                        .height(plotAreaHeightDP)
                        .width(rightAxisWidth)
                ) {
                    val height = constraints.maxHeight

                    // draw axis line
                    Canvas(modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            onDrawBehind {
                                drawLine(
                                    color = config.leftAxisConfig.lineColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, height.toFloat()),
                                    strokeWidth = config.rightAxisConfig.axisStrokeWidth.toPx()
                                )
                            }
                        }) {
                    }

                    YAxisTicksAndLabels(
                        config.rightAxisConfig,
                        formatRightAxisLabel,
                        axisType = AxisType.RIGHT
                    )

                }
            }
        }

    }


}

@Composable
private fun YAxisTicksAndLabels(
    config: AxisConfig,
    formatLabel: ((Int, Float) -> String)? = null,
    axisType: AxisType = AxisType.LEFT,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {

        val height = constraints.maxHeight
        val width = constraints.maxWidth

        // get min and max values to use for ticks
        val textMeasurer = rememberTextMeasurer()
        val points = config.dataPoints

        val minY = config.minY ?: points.minOf { it.yValue }
        val maxY = config.maxY ?: points.maxOf { it.yValue }

        // get a count of labels to display
        val labelCount =
            when (config.maxNumberOfLabelsToDisplay) {
                null -> points.size
                else -> config.maxNumberOfLabelsToDisplay
            }

        // gap between ticks on y axis
        val heightDelta = height / (labelCount - 1)

        // gap between y values on y axis
        val yDelta = (maxY - minY) / (labelCount - 1)

        // create a table of y co-ordinates and strings to draw the labels and ticks
        val labelMap = Hashtable<Float, String>()

        for (i in 1..labelCount) {
            val y = (yDelta * (i - 1)) + minY
            val yCoOrd = height - (heightDelta.toFloat() * (i - 1))
            val labelText = if (formatLabel == null) y.toInt().toString() else formatLabel(i, y)
            labelMap[yCoOrd] = labelText
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .drawWithCache {

                onDrawWithContent {

                    labelMap.forEach { y, labelText ->

                        val measure = textMeasurer.measure(
                            labelText,
                            config.labelStyle,
                            config.labelOverflow,
                            maxLines = 1
                        )

                        val start = when (axisType) {
                            AxisType.LEFT -> Offset(
                                width.toFloat() - config.tickLength.toPx(),
                                y
                            )
                            AxisType.RIGHT -> Offset(0f, y)
                        }

                        val end = when (axisType) {
                            AxisType.LEFT -> Offset(width.toFloat(), y)
                            AxisType.RIGHT -> Offset(config.tickLength.toPx(), y)
                        }

                        val topLeft = when (axisType) {
                            AxisType.LEFT -> Offset(
                                width.toFloat() - (measure.size.width) - config.labelPaddingEnd.toPx() - config.tickLength.toPx(),
                                y - measure.size.height.toFloat() / 2
                            )
                            AxisType.RIGHT -> Offset(
                                config.tickLength.toPx() + config.labelPaddingStart.toPx(),
                                y - measure.size.height.toFloat() / 2
                            )
                        }

                        drawLine(
                            color = config.tickColor,
                            start = start,
                            end = end,
                            strokeWidth = config.tickStrokeWidth.toPx()
                        )

                        drawText(
                            topLeft = topLeft,
                            textLayoutResult = measure,
                            color = config.tickColor
                        )
                    }

                }

            }
        ) {

        }

    }
}

@Composable
private fun DrawBottomAxisTicksAndLabels(
    config: Config,
    formatBottomAxisLabel: ((Int, Float, ChartPoint?) -> String)? = null,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )
    {

        // get min and max values to use for ticks
        val textMeasurer = rememberTextMeasurer()
        val points = config.leftAxisConfig.dataPoints
        val minX = points.minOf { it.xValue }
        val maxX = points.maxOf { it.xValue }
        val rangeX = maxX - minX

        // the label count for bar charts is the same as the number of bars
        val labelCount = if (config.leftAxisConfig.type == ChartType.Bar) points.size else {
            when (config.bottomAxisConfig.maxNumberOfLabelsToDisplay) {
                null -> if (config.leftAxisConfig.type == ChartType.Bar) points.size else points.size - 1
                else -> config.bottomAxisConfig.maxNumberOfLabelsToDisplay
            }
        }

        val evenX = rangeX / (labelCount - 1)
        val width = constraints.maxWidth.toFloat()
        val evenSpace = width / (labelCount - 1)
        val labelMap = Hashtable<Float, String>()

        // get bar width for bar chart
        val barWidth =
            width * config.chartConfig.barChartFraction / (points.size - 1 + config.chartConfig.barChartFraction)

        points.forEachIndexed { index, dataPoint ->

                val labelText =  when (config.leftAxisConfig.type) {
                    ChartType.Bar ->  if( formatBottomAxisLabel == null ) dataPoint.xValue.toInt().toString() else  formatBottomAxisLabel(index, dataPoint.xValue, dataPoint)
                    ChartType.Line -> if( formatBottomAxisLabel == null ) (minX + evenX * index).toInt().toString() else formatBottomAxisLabel(index, minX + evenX * index, dataPoint)
                }

                if (labelText != "") {
                    var xDistance = evenSpace * index
                    if (config.leftAxisConfig.type == ChartType.Bar) {
                        xDistance =
                            if (index == 0)
                                barWidth / 2
                            else
                                (index * barWidth / config.chartConfig.barChartFraction) + (barWidth / 2)
                    }

                    labelMap[xDistance] = labelText
                }

        }

        Box(modifier = Modifier
            .fillMaxSize()
            .drawWithCache {

                onDrawWithContent {

                    labelMap.forEach { x, labelText ->

                        val measure = textMeasurer.measure(
                            labelText,
                            config.bottomAxisConfig.labelStyle,
                            config.bottomAxisConfig.labelOverflow,
                            maxLines = config.bottomAxisConfig.labelMaxLines
                        )

                        drawLine(
                            color = config.bottomAxisConfig.tickColor,
                            start = Offset(x, 0f),
                            end = Offset(x, config.bottomAxisConfig.tickLength.toPx()),
                            strokeWidth = config.bottomAxisConfig.tickStrokeWidth.toPx()
                        )

                        drawText(
                            topLeft = Offset(
                                x - (measure.size.width / 2),
                                config.bottomAxisConfig.tickLength.toPx()
                            ),
                            textLayoutResult = measure,
                            color = config.bottomAxisConfig.tickColor
                        )
                    }

                }

            }
        ) {

        }
    }
}


@Composable
private fun DrawBarChart(config: Config) {

    val density = LocalDensity.current
    config.leftAxisConfig.dataPoints.let { points ->

        // get min and max values to use for ticks
        val yMin = config.leftAxisConfig.minY ?: points.minOf { it.yValue }
        val yMax = config.leftAxisConfig.maxY ?: points.maxOf { it.yValue }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            val height = constraints.maxHeight.toFloat()
            val width = constraints.maxWidth.toFloat()
            val heightDP = with(density) { height.toDp() }

            // get bar width for bar chart
            val barWidth =
                width * config.chartConfig.barChartFraction / (points.size - 1 + config.chartConfig.barChartFraction)
            val barWidthDP = with(density) { barWidth.toDp() }

            // space between bars
            val space = (barWidth / config.chartConfig.barChartFraction) - barWidth
            val spaceDP = with(density) { space.toDp() }

            // stack canvases to the right to draw the bars
            Row {

                points.forEachIndexed { index, chartPoint ->

                    // add the space between bars
                    if (index > 0)
                        Spacer(modifier = Modifier.size(width = spaceDP, height = heightDP))

                    // create a path to draw the bar on the canvas
                    val path = Path()

                    // calculate the bar height relative to box
                    val y = height - (height * (chartPoint.yValue - yMin) / (yMax - yMin))

                    path.moveTo(0f, height)
                    path.lineTo(0f, y)
                    path.lineTo(barWidth, y)
                    path.lineTo(barWidth, height)

                    // create a canvas to draw the bar
                    Canvas(
                        modifier = Modifier
                            .width(barWidthDP)
                            .fillMaxHeight()
                            .pointerInput(key1 = Unit) {
                                detectTapGestures(
                                    onTap = {
                                        val lambda = config.onPlotAreaTap
                                        if (lambda != null)
                                            lambda(chartPoint)
                                    }
                                )
                            }
                            .drawWithCache {

                                onDrawBehind {
                                    drawPath(
                                        path = path,
                                        color = config.leftAxisConfig.lineColor,
                                        style = Stroke(width = config.leftAxisConfig.axisStrokeWidth.value)
                                    )

                                    if (config.leftAxisConfig.showFillColour) {
                                        drawPath(
                                            path = path,
                                            color = config.leftAxisConfig.fillColour,
                                            style = Fill,
                                            alpha = 0.3f,
                                        )
                                    }

                                }
                            }
                    ) {

                    }

                }
            }


        }

    }

}

@Composable
private fun DrawLineChart(config: AxisConfig, onPlotAreaTap: ((ChartPoint) -> Unit)?) {

    val pointsSortedByX = config.dataPoints.sortedBy { it.xValue }

    pointsSortedByX.let { points ->

        // get min and max values to use for ticks
        val yMin = config.minY ?: points.minOf { it.yValue }
        val yMax = config.maxY ?: points.maxOf { it.yValue }
        val xMin = points.minOf { it.xValue }
        val xMax = points.maxOf { it.xValue }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            val height = constraints.maxHeight.toFloat()
            val width = constraints.maxWidth.toFloat()

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onTap = {
                                if (onPlotAreaTap != null) {

                                    //TODO
                                    /*      val x = it.x
                                          var pointIndex = Math.round(points.size * (x / width)) - 1
                                          if( pointIndex < 0) pointIndex = 1
                                          if( pointIndex > points.size -1) pointIndex = points.size -1
                                              val point = points[pointIndex]
                                              val lambda = onPlotAreaTap
                                                  lambda(point)*/
                                }
                            }
                        )
                    }
                    .drawWithCache {

                        // path for line
                        val path = Path()

                        // path for shape fill
                        val fillPath = Path()
                        fillPath.moveTo(0f, height)

                        for (i in 1..points.size) {
                            val point = points[i - 1]
                            val x = width * (point.xValue - xMin) / (xMax - xMin)
                            val y = height - (height * (point.yValue - yMin) / (yMax - yMin))

                            fillPath.lineTo(x, y)

                            when (i) {
                                1 -> {
                                    path.moveTo(x, y)
                                }
                                points.size -> {
                                    path.lineTo(x, y)
                                    fillPath.lineTo(width, height)
                                }
                                else -> {
                                    path.lineTo(x, y)
                                }
                            }
                        }

                        onDrawBehind {

                            drawPath(
                                path = path,
                                color = config.lineColor,
                                style = Stroke(width = config.axisStrokeWidth.value)
                            )

                            if (config.showFillColour) {
                                drawPath(
                                    path = fillPath,
                                    color = config.fillColour,
                                    style = Fill,
                                    alpha = 0.3f,
                                )
                            }

                            if (config.showCircles) {
                                for (i in 1..points.size) {
                                    val point = points[i - 1]
                                    val x = width * (point.xValue - xMin) / (xMax - xMin)
                                    val y =
                                        height - (height * (point.yValue - yMin) / (yMax - yMin))

                                    drawCircle(
                                        color = config.circleColor,
                                        center = Offset(x, y),
                                        radius = config.circleRadius.value,

                                        )
                                }
                            }

                        }
                    }
            ) {

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
)


data class AxisConfig(

    val showCircles: Boolean = true,
    val showFillColour: Boolean = true,
    val fillColour: Color = Color.Gray,
    val lineColor: Color = Color.DarkGray,
    val circleColor: Color = Color.Gray,
    val circleRadius: Dp = 8.dp,

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
    var dataPoints: List<ChartPoint>,

    val axisColor: Color = Color.Black,
    val axisStrokeWidth: Dp = 2.dp,
    val tickStrokeWidth: Dp = 2.dp,


    /**
     * Set the type of chart to draw for the left axis values
     */
    val type: ChartType = ChartType.Line,
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

    val labelOverflow: TextOverflow = TextOverflow.Ellipsis,
    val labelMaxLines: Int = 1,
)

data class ChartConfig(

    val height: Dp = 300.dp,
    val plotAreaPadding: PaddingValues = PaddingValues(start = 0.dp, end = 0.dp),

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
)


data class Config(
    val chartConfig: ChartConfig = ChartConfig(),
    val leftAxisConfig: AxisConfig,
    val rightAxisConfig: AxisConfig? = null,
    val bottomAxisConfig: BottomAxisConfig = BottomAxisConfig(),
    val onPlotAreaTap: ((ChartPoint) -> Unit)? = null,
)

enum class ChartType {
    Line, Bar
}

private enum class AxisType {
    LEFT, RIGHT
}