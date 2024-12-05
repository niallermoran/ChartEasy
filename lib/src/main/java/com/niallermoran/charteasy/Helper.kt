import android.graphics.Color
import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.niallermoran.charteasy.AxisLabel
import com.niallermoran.charteasy.AxisType
import com.niallermoran.charteasy.BottomAxisArea
import com.niallermoran.charteasy.ChartPointCoordinates
import com.niallermoran.charteasy.Config
import com.niallermoran.charteasy.Dimensions
import com.niallermoran.charteasy.LeftAxisArea
import com.niallermoran.charteasy.OffsetDp
import com.niallermoran.charteasy.PlotArea
import com.niallermoran.charteasy.RightAxisArea
import com.niallermoran.charteasy.SizeDp
import com.niallermoran.charteasy.VerticalAxisConfig
import kotlin.math.ceil
import kotlin.random.Random

/**
 * Calculates all required dimensions for the chart to be built
 */
fun calculateDimensions(
    density: Density,
    textMeasurer: TextMeasurer,
    availableHeight: Dp,
    availableWidth: Dp,
    config: Config
): Dimensions {

    val dimensions = Dimensions()
    dimensions.chart.chartSize =
        SizeDp(width = availableWidth, height = availableHeight)

    val points = config.leftAxisConfig.dataPoints.sortedBy { it.xValue }
    val pointsWithRightValue = points.filter { it.yValueRightAxis != null }

    // calculate data specific dimensions
    dimensions.dataValues.points = points
    dimensions.dataValues.yMin =
        config.leftAxisConfig.minY ?: dimensions.dataValues.points.minOf { it.yValue }
    dimensions.dataValues.yMax =
        config.leftAxisConfig.maxY ?: dimensions.dataValues.points.maxOf { it.yValue }
    dimensions.dataValues.yMinRight = config.rightAxisConfig?.minY
        ?: if (pointsWithRightValue.isEmpty()) 0f else dimensions.dataValues.points.filter { it.yValueRightAxis != null }
            .minOf { it.yValueRightAxis!! }
    dimensions.dataValues.yMaxRight = config.rightAxisConfig?.maxY
        ?: if (pointsWithRightValue.isEmpty()) 0f else dimensions.dataValues.points.filter { it.yValueRightAxis != null }
            .maxOf { it.yValueRightAxis!! }
    dimensions.dataValues.xMin = dimensions.dataValues.points.minOf { it.xValue }
    dimensions.dataValues.xMax = dimensions.dataValues.points.maxOf { it.xValue }

    // calculate dimensions for chart axes and plot area
    calculateAxisDimensions(
        config = config,
        dimensions = dimensions,
        textMeasurer = textMeasurer,
        density = density
    )

    // calculate co-ordinates for each data point
    calculatePointCoOrdinates(
        config = config,
        dimensions = dimensions
    )

    calculateLabels(
        config = config,
        dimensions = dimensions,
        textMeasurer = textMeasurer
    )



    return dimensions

}

fun getRandomColour(): Int {
    val random = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
    return random
}

/**
 * calculate the co-ordinates on the inner plot area for every ChartPoint
 */
private fun calculatePointCoOrdinates(config: Config, dimensions: Dimensions) {
    val xMin = dimensions.dataValues.xMin
    val xMax = dimensions.dataValues.xMax
    val axisType = config.leftAxisConfig.type
    val innerPlotSize = SizeDp(
        width = dimensions.chart.plotArea.innerWidth,
        height = dimensions.chart.plotArea.innerHeight
    )
    val xDpBetweenBarCenters = innerPlotSize.width / (dimensions.dataValues.points.size - 1)

    dimensions.dataValues.points.forEachIndexed { index, chartPoint ->

        val x = chartPoint.xValue
        val y = chartPoint.yValue

        val offsetLeft = getPointCoOrdinates(
            axisType,
            innerPlotSize,
            x,
            xMin,
            xMax,
            dimensions.dataValues.yMax,
            y,
            dimensions.dataValues.yMin,
            index,
            xDpBetweenBarCenters
        )

        val yRight = chartPoint.yValueRightAxis
        val offsetRight: OffsetDp? = if( yRight == null) null else getPointCoOrdinates(
            axisType,
            innerPlotSize,
            x,
            xMin,
            xMax,
            dimensions.dataValues.yMaxRight,
            yRight,
            dimensions.dataValues.yMinRight,
            index,
            xDpBetweenBarCenters
        )

        dimensions.dataCoordinates.add(ChartPointCoordinates(
            chartPoint = chartPoint,
            offsetLeft = offsetLeft,
            offsetRight = offsetRight
        ))


    }
}

private fun getPointCoOrdinates(
    axisType: AxisType,
    innerPlotSize: SizeDp,
    x: Float,
    xMin: Float,
    xMax: Float,
    yMax: Float,
    y: Float,
    yMin: Float,
    index: Int,
    xDpBetweenBarCenters: Dp
): OffsetDp {
    var xCoord = 0.dp
    var yCoord = 0.dp

    when (axisType) {
        AxisType.Line -> {
            xCoord = innerPlotSize.width * ((x - xMin) / (xMax - xMin))
            yCoord = innerPlotSize.height * ((yMax - y) / (yMax - yMin))
        }

        /**
         * For Bar types the points are plotted evenly spaced across X axis.
         * The coordinates represent the center top of the bar
         */
        AxisType.Bar -> {
            xCoord = index.dp * xDpBetweenBarCenters.value
            yCoord = innerPlotSize.height * ((yMax - y) / (yMax - yMin))
        }
    }




    val offset = OffsetDp(left = xCoord, top = yCoord)
    return offset
}

/**
 * Calculates the position for all labels and ticks for all axes in Dp
 */
private fun calculateLabels(
    config: Config,
    dimensions: Dimensions,
    textMeasurer: TextMeasurer
) {
    val points = dimensions.dataValues.points

    // get left axis labels/ticks
    if (config.leftAxisConfig.displayLabels || config.leftAxisConfig.displayTicks) {

        val axisConfig = config.leftAxisConfig
        val formatter = axisConfig.formatAxisLabel
        val labelCount =
            if (axisConfig.maxNumberOfLabelsToDisplay == null || axisConfig.maxNumberOfLabelsToDisplay > points.size) points.size else axisConfig.maxNumberOfLabelsToDisplay
        val distanceBetweenTicks = dimensions.chart.plotArea.innerHeight / (labelCount - 1)

        // add tick and label dimensions
        for (i: Int in 0..<labelCount) {

            val distanceAlongYAxis = i.dp * distanceBetweenTicks.value

            // get the value of x that would be represented by the label
            val yValue =
                dimensions.dataValues.yMin + ((dimensions.dataValues.yMax - dimensions.dataValues.yMin) * i / (labelCount - 1))

            val labelText = if (formatter == null) String.format(
                java.util.Locale.ENGLISH,
                "%.2f",
                yValue
            ) else formatter(yValue)

            val textMeasure = textMeasurer.measure(labelText, style = axisConfig.labelStyle)
            val label = AxisLabel(text = textMeasure, distanceAlongYAxis)

            dimensions.leftAxisLabels.add(label)
        }
    }

    // get right axis labels/ticks
    if (config.rightAxisConfig != null && (config.rightAxisConfig.displayLabels || config.rightAxisConfig.displayTicks)) {

        val axisConfig = config.rightAxisConfig
        val formatter = axisConfig.formatAxisLabel
        val labelCount =
            if (axisConfig.maxNumberOfLabelsToDisplay == null || axisConfig.maxNumberOfLabelsToDisplay > points.size) points.size else axisConfig.maxNumberOfLabelsToDisplay
        val distanceBetweenTicks = dimensions.chart.plotArea.innerHeight / (labelCount - 1)

        // add tick and label dimensions
        for (i: Int in 0..<labelCount) {

            val distanceAlongYAxis = i.dp * distanceBetweenTicks.value

            // get the value of x that would be represented by the label
            val yValue =
                dimensions.dataValues.yMinRight + ((dimensions.dataValues.yMaxRight - dimensions.dataValues.yMinRight) * i / (labelCount - 1))

            val labelText = if (formatter == null) String.format(
                java.util.Locale.ENGLISH,
                "%.2f",
                yValue
            ) else formatter(yValue)

            val textMeasure = textMeasurer.measure(labelText, style = axisConfig.labelStyle)
            val label = AxisLabel(text = textMeasure, distanceAlongYAxis)

            dimensions.rightAxisLabels.add(label)
        }

    }


    // get bottom axis labels/ticks
    if (config.bottomAxisConfig.displayLabels || config.bottomAxisConfig.displayTicks) {

        val bottomAxisFormatter = config.bottomAxisConfig.formatAxisLabel
        val bottomAxisLabelCount =
            if (config.bottomAxisConfig.maxNumberOfLabelsToDisplay > points.size
            ) points.size
            else config.bottomAxisConfig.maxNumberOfLabelsToDisplay

        val distanceBetweenTicks =
            dimensions.chart.plotArea.innerWidth / (bottomAxisLabelCount - 1)

        // if either vertical axis is a bar chart then the ticks and labels must
        // align with actual x values whereas for line charts the x-values can be equally dispersed
        if (config.leftAxisConfig.type == AxisType.Bar || (config.rightAxisConfig != null && config.rightAxisConfig.type == AxisType.Bar)) {
            // for bar chart each point gets a label as long as we don;t go over the max
            // each point gets a tick
            points.forEachIndexed { index, chartPoint ->

                // we can only show up to maxLabelsToDisplay, but we will alwyas show first and last
                var showLabel =
                    if (index == 0 || index == dimensions.dataValues.points.size - 1) true else false

                // calculate how many labels should be displayed for every data point
                val skip =
                    if (config.bottomAxisConfig.maxNumberOfLabelsToDisplay >= dimensions.dataValues.points.size) 1
                    else ceil(dimensions.dataValues.points.size.toDouble() / config.bottomAxisConfig.maxNumberOfLabelsToDisplay).toInt()

                if (index % skip == 0 && config.bottomAxisConfig.maxNumberOfLabelsToDisplay != 2)
                    showLabel = true

                if (showLabel == true) {
                    val distanceAlongXAxis = index.dp * distanceBetweenTicks.value
                    val labelText = if (bottomAxisFormatter == null) String.format(
                        java.util.Locale.ENGLISH,
                        "%.2f",
                        chartPoint.xValue
                    ) else bottomAxisFormatter(chartPoint.xValue)
                    val textMeasure =
                        textMeasurer.measure(labelText, style = config.bottomAxisConfig.labelStyle)
                    dimensions.bottomAxisLabels.add(
                        AxisLabel(
                            text = textMeasure,
                            centerDistanceAlongAxis = distanceAlongXAxis
                        )
                    )
                }
            }
        } else {
            // for lines the bottom axis labels represent values equally divided amongst width
            for (i: Int in 0..<bottomAxisLabelCount) {

                val distanceAlongXAxis = i.dp * distanceBetweenTicks.value

                // get the value of x that would be represented by the label
                val xValue =
                    dimensions.dataValues.xMin + ((dimensions.dataValues.xMax - dimensions.dataValues.xMin) * i / (bottomAxisLabelCount - 1))

                val labelText = if (bottomAxisFormatter == null) String.format(
                    java.util.Locale.ENGLISH,
                    "%.2f",
                    xValue
                ) else bottomAxisFormatter(xValue)

                val textMeasure =
                    textMeasurer.measure(labelText, style = config.bottomAxisConfig.labelStyle)
                val label = AxisLabel(text = textMeasure, distanceAlongXAxis)
                dimensions.bottomAxisLabels.add(label)
            }

        }
    }

}

/**
 * Gets a list of labels for the appropriate axis and adds to dimensions
 */
private fun getVerticalAxisLabels(
    config: VerticalAxisConfig,
    dimensions: Dimensions,
    textMeasurer: TextMeasurer,
): ArrayList<AxisLabel> {

    val points = dimensions.dataValues.points
    val formatter = config.formatAxisLabel
    val labelCount =
        if (config.maxNumberOfLabelsToDisplay == null || config.maxNumberOfLabelsToDisplay > points.size) points.size else config.maxNumberOfLabelsToDisplay
    val labelsList: ArrayList<AxisLabel> = ArrayList()
    val distanceBetweenTicks = dimensions.chart.plotArea.innerHeight / (labelCount - 1)

    // add tick and label dimensions
    for (i: Int in 0..<labelCount) {

        val distanceAlongYAxis = i.dp * distanceBetweenTicks.value

        // get the value of x that would be represented by the label
        val yValue =
            dimensions.dataValues.yMin + ((dimensions.dataValues.yMax - dimensions.dataValues.xMin) * i / (labelCount - 1))

        val labelText = if (formatter == null) String.format(
            java.util.Locale.ENGLISH,
            "%.2f",
            yValue
        ) else formatter(yValue)

        val textMeasure = textMeasurer.measure(labelText, style = config.labelStyle)
        val label = AxisLabel(text = textMeasure, distanceAlongYAxis)

        labelsList.add(label)
    }

    return labelsList
}

/**
 * Calculates the width of the left axis and the height of the bottom axis in Dp
 */
private fun calculateAxisDimensions(
    density: Density,
    config: Config,
    dimensions: Dimensions,
    textMeasurer: TextMeasurer
) {
    val formatLeftAxisLabelLambda = config.leftAxisConfig.formatAxisLabel
    val formatRightAxisLabelLambda = config.rightAxisConfig?.formatAxisLabel
    val formatBottomAxisLabelLambda = config.bottomAxisConfig.formatAxisLabel

    val yTextLeft = if (formatLeftAxisLabelLambda != null) formatLeftAxisLabelLambda(
        dimensions.dataValues.yMax
    ) else String.format(java.util.Locale.ENGLISH, "%.2f", dimensions.dataValues.yMax)

    val yTextRight = if (formatRightAxisLabelLambda != null) formatRightAxisLabelLambda(
        dimensions.dataValues.yMax
    ) else String.format(java.util.Locale.ENGLISH, "%.2f", dimensions.dataValues.yMaxRight)

    val xText = if (formatBottomAxisLabelLambda != null) formatBottomAxisLabelLambda(
        dimensions.dataValues.xMax
    ) else String.format(java.util.Locale.ENGLISH, "%.2f", dimensions.dataValues.xMax)

    // calculate bottom axis height
    var bottomAxisHeight =
        config.bottomAxisConfig.labelPadding.calculateBottomPadding().value + config.bottomAxisConfig.labelPadding.calculateTopPadding().value

    if (config.bottomAxisConfig.displayTicks) {
        bottomAxisHeight += config.bottomAxisConfig.tickLength.value
    }

    if (config.bottomAxisConfig.displayLabels) {
        // text measures return pixels
        with(density) {
            bottomAxisHeight += textMeasurer.measure(
                text = xText,
                style = config.bottomAxisConfig.labelStyle,
                maxLines = config.bottomAxisConfig.labelMaxLines
            ).size.height.toDp().value
        }
    }

    // calculate the vertical axis widths
    var leftAxisWidth = 0.dp
    if (config.leftAxisConfig.display) {
        config.leftAxisConfig.labelPadding.calculateLeftPadding(LayoutDirection.Ltr) + config.leftAxisConfig.labelPadding.calculateRightPadding(
            LayoutDirection.Ltr
        )

        if (config.leftAxisConfig.displayLabels) {
            with(density) {
                val labelWidth = textMeasurer.measure(
                    yTextLeft,
                    style = config.leftAxisConfig.labelStyle,
                    maxLines = config.leftAxisConfig.labelMaxLines
                ).size.width.toDp()

                leftAxisWidth += labelWidth
            }
        }

        if (config.leftAxisConfig.displayTicks) {
            leftAxisWidth += config.leftAxisConfig.tickLength
        }

    }

    var rightAxisWidth = 0.dp


    if (config.rightAxisConfig != null && config.rightAxisConfig.display) {
        config.rightAxisConfig.labelPadding.calculateLeftPadding(LayoutDirection.Ltr) + config.rightAxisConfig.labelPadding.calculateRightPadding(
            LayoutDirection.Ltr
        )

        if (config.rightAxisConfig.displayLabels) {
            with(density) {
                val labelWidth = textMeasurer.measure(
                    yTextRight,
                    style = config.rightAxisConfig.labelStyle,
                    maxLines = config.rightAxisConfig.labelMaxLines
                ).size.width.toDp()

                rightAxisWidth += labelWidth
            }
        }
        if (config.rightAxisConfig.displayTicks) {
            rightAxisWidth += config.rightAxisConfig.tickLength
        }
    }


    dimensions.chart.bottomAxisArea = BottomAxisArea(
        size = SizeDp(
            width = dimensions.chart.chartSize.width - leftAxisWidth.value.dp - rightAxisWidth.value.dp,
            height = bottomAxisHeight.dp
        ),
        offset = OffsetDp(
            leftAxisWidth.value.dp,
            dimensions.chart.chartSize.height - bottomAxisHeight.dp
        )
    )

    dimensions.chart.leftAxisArea = LeftAxisArea(
        size = SizeDp(
            width = leftAxisWidth.value.dp,
            height = dimensions.chart.chartSize.height - bottomAxisHeight.dp
        ),
        topLeftOffset = OffsetDp(0.dp, 0.dp)
    )

    dimensions.chart.rightAxisArea = RightAxisArea(
        size = SizeDp(
            width = rightAxisWidth.value.dp,
            height = dimensions.chart.chartSize.height - bottomAxisHeight.dp
        ),
        topLeftOffset = OffsetDp(
            dimensions.chart.chartSize.width.value.dp - leftAxisWidth - rightAxisWidth,
            0.dp
        )
    )

    val plotAreaOuterWidth = dimensions.chart.chartSize.width - leftAxisWidth - rightAxisWidth
    val plotAreaOuterHeight = dimensions.chart.chartSize.height - bottomAxisHeight.dp

    // calculate padding for plot area
    var padding = config.chartConfig.plotAreaPadding ?: PaddingValues(0.dp)
    var barWidth = 0.dp


    // calculate bar widths for left and/or right axis
    if (config.leftAxisConfig.type == AxisType.Bar || config.rightAxisConfig?.type == AxisType.Bar) {

        // calculate the bar width allowing for half a width padding at start and right
        val numberOfBars = dimensions.dataValues.points.size
        val barWidthFraction = config.chartConfig.barChartFraction

        // calculate auto padding
        if (config.chartConfig.plotAreaPadding == null) {
            val divisor = (numberOfBars / barWidthFraction) + (numberOfBars + 1)
            val space = plotAreaOuterWidth / divisor
            padding = PaddingValues(
                start = space,
                end = space
            )
            barWidth = space / barWidthFraction
        }

        // calculate padding when defined
        if (config.chartConfig.plotAreaPadding != null) {
            val innerWidth =
                plotAreaOuterWidth - config.chartConfig.plotAreaPadding.calculateLeftPadding(
                    LayoutDirection.Ltr
                ) - config.chartConfig.plotAreaPadding.calculateRightPadding(LayoutDirection.Ltr)
            val divisor = (numberOfBars / barWidthFraction) + numberOfBars.toFloat() - 1
            val space = innerWidth / divisor
            padding = PaddingValues(
                start = config.chartConfig.plotAreaPadding.calculateLeftPadding(LayoutDirection.Ltr),
                end = config.chartConfig.plotAreaPadding.calculateRightPadding(LayoutDirection.Ltr)
            )
            barWidth = space / barWidthFraction
        }
    }

    dimensions.chart.plotArea = PlotArea(
        size = SizeDp(
            width = plotAreaOuterWidth,
            height = plotAreaOuterHeight
        ),
        offset = OffsetDp(
            leftAxisWidth,
            0.dp
        ),
        padding = padding,
        innerTopLeftOffset = OffsetDp(
            leftAxisWidth + padding.calculateLeftPadding(LayoutDirection.Ltr).value.dp,
            padding.calculateTopPadding().value.dp
        ),
        barWidth = barWidth
    )


}
