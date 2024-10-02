import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.niallermoran.charteasy.AxisType
import com.niallermoran.charteasy.BottomAxisArea
import com.niallermoran.charteasy.BottomAxisLabel
import com.niallermoran.charteasy.ChartDimensions
import com.niallermoran.charteasy.Config
import com.niallermoran.charteasy.Dimensions
import com.niallermoran.charteasy.LeftAxisArea
import com.niallermoran.charteasy.LeftAxisLabel
import com.niallermoran.charteasy.OffsetDp
import com.niallermoran.charteasy.PlotArea
import com.niallermoran.charteasy.SizeDp
import java.util.Hashtable

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
    with(density) {
        dimensions.chart.chartSize =
            Size(width = availableWidth.toPx(), height = availableHeight.toPx())

        // calculate data specific dimensions
        dimensions.dataValues.points =
            if (config.leftAxisConfig.type == AxisType.Bar) config.leftAxisConfig.dataPoints else config.leftAxisConfig.dataPoints.sortedBy { it.xValue } //.sortedBy { it.xValue }
        dimensions.dataValues.yMin =
            config.leftAxisConfig.minY ?: dimensions.dataValues.points.minOf { it.yValue }
        dimensions.dataValues.yMax =
            config.leftAxisConfig.maxY ?: dimensions.dataValues.points.maxOf { it.yValue }
        dimensions.dataValues.xMin = dimensions.dataValues.points.minOf { it.xValue }
        dimensions.dataValues.xMax = dimensions.dataValues.points.maxOf { it.xValue }

        // calculate dimensions for chart axes and plot area
        calculateAxisDimensions(
            config = config,
            dimensions = dimensions,
            textMeasurer = textMeasurer,
            density = density
        )

        calculateLabels(
            config = config,
            dimensions = dimensions,
            textMeasurer = textMeasurer
        )

    }

    return dimensions

}

/**
 * Calculates the position for all labels and ticks for all axes
 */
private fun calculateLabels(
    config: Config,
    dimensions: Dimensions,
    textMeasurer: TextMeasurer
) {
    val points = dimensions.dataValues.points

    // get left axis labels/ticks
    if (config.leftAxisConfig.displayLabels || config.leftAxisConfig.displayTicks) {
        val leftAxisFormatter = config.formatLeftAxisLabel
        val maxCount = config.leftAxisConfig.maxNumberOfLabelsToDisplay
        val leftAxisLabelCount =
            if (maxCount == null || maxCount > points.size) points.size else maxCount
        val divisor = leftAxisLabelCount - 1

        val dpBetweenTicks =
            if (divisor <= 0f) dimensions.chart.leftAxisArea.size.height else dimensions.chart.leftAxisArea.size.height / divisor

        // add tick and label dimensions
        for (i: Int in 1..leftAxisLabelCount) {
            // get the value of Y that would be represented by the label
            val yValue =
                dimensions.dataValues.yMin + ((dimensions.dataValues.yMax - dimensions.dataValues.yMin) * (i - 1) / divisor)
            val labelText = if (leftAxisFormatter == null) String.format(
                java.util.Locale.ENGLISH,
                "%.2f",
                yValue
            ) else leftAxisFormatter(yValue)
            val textMeasure =
                textMeasurer.measure(labelText, style = config.leftAxisConfig.labelStyle)
            val label = LeftAxisLabel(
                text = textMeasure,
                tickStartOffset = OffsetDp(
                     dimensions.chart.leftAxisArea.size.width,
                     dimensions.chart.leftAxisArea.size.height - ( dpBetweenTicks * (i - 1) )
                ),
                labelTopLeftOffset = OffsetDp(
                    dimensions.chart.leftAxisArea.size.width - config.leftAxisConfig.tickLength - config.leftAxisConfig.labelPadding.calculateRightPadding(
                        LayoutDirection.Ltr
                    ) - textMeasure.size.width.dp,
                    dimensions.chart.leftAxisArea.size.height - ( dpBetweenTicks * (i - 1) ) - (textMeasure.size.height.dp / 2)
                )
            )
            dimensions.leftAxisLabels.add(label)
        }
    }

    // get bottom axis labels/ticks
    if (config.bottomAxisConfig.displayLabels || config.bottomAxisConfig.displayTicks) {
        val bottomAxisFormatter = config.formatBottomAxisLabel
        val maxCount = config.bottomAxisConfig.maxNumberOfLabelsToDisplay
        val bottomAxisLabelCount =
            if (maxCount == null || maxCount > points.size) points.size else maxCount
        val divisor = bottomAxisLabelCount - 1
        val dpBetweenTicks =
            if (divisor <= 0f) dimensions.chart.bottomAxisArea.size.width else dimensions.chart.bottomAxisArea.size.width / divisor

        // add tick and label dimensions
        for (i: Int in 1..bottomAxisLabelCount) {
            // get the value of Y that would be represented by the label
            val xValue =
                dimensions.dataValues.xMin + ((dimensions.dataValues.xMax - dimensions.dataValues.xMin) * (i - 1) / divisor)
            val labelText = if (bottomAxisFormatter == null) String.format(
                java.util.Locale.ENGLISH,
                "%.2f",
                xValue
            ) else bottomAxisFormatter(xValue)
            val textMeasure =
                textMeasurer.measure(labelText, style = config.bottomAxisConfig.labelStyle)
            val label = BottomAxisLabel(
                text = textMeasure,
                tickStartOffset = OffsetDp(
                    dimensions.chart.bottomAxisArea.offset.left + ( dpBetweenTicks.times (i - 1)),
                    dimensions.chart.bottomAxisArea.offset.left
                ),
                labelTopLeftOffset = OffsetDp(
                     dimensions.chart.bottomAxisArea.offset.left + ( dpBetweenTicks * (i - 1) ) - textMeasure.size.width.dp / 2,
                     dimensions.chart.bottomAxisArea.offset.top + config.bottomAxisConfig.tickLength + config.bottomAxisConfig.labelPadding.calculateTopPadding()
                )
            )
            dimensions.bottomAxisLabels.add(label)
        }
    }

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
    val formatLeftAxisLabelLambda = config.formatLeftAxisLabel
    val formatBottomAxisLabelLambda = config.formatBottomAxisLabel

    val yText = if (formatLeftAxisLabelLambda != null) formatLeftAxisLabelLambda(
        dimensions.dataValues.yMax
    ) else String.format(java.util.Locale.ENGLISH, "%.2f", dimensions.dataValues.yMax)

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
        bottomAxisHeight += textMeasurer.measure(
            text = xText,
            style = config.bottomAxisConfig.labelStyle,
            maxLines = config.bottomAxisConfig.labelMaxLines
        ).size.height
    }

    Log.d("BottomAxisHeightDPFloat", bottomAxisHeight.toString())

    // calculate the left axis width
    var leftAxisWidth =
        config.leftAxisConfig.labelPadding.calculateLeftPadding(LayoutDirection.Ltr) + config.leftAxisConfig.labelPadding.calculateRightPadding(
            LayoutDirection.Ltr
        )

    Log.d("LeftAxisWidth", leftAxisWidth.toString())

    if (config.leftAxisConfig.displayLabels) {
        leftAxisWidth += textMeasurer.measure(
            yText,
            style = config.leftAxisConfig.labelStyle,
            maxLines = config.leftAxisConfig.labelMaxLines
        ).size.width.dp
    }

    Log.d("LeftAxisWidth", leftAxisWidth.toString())


    if (config.leftAxisConfig.displayTicks) {
        leftAxisWidth += config.leftAxisConfig.tickLength
    }


    Log.d("LeftAxisWidth", leftAxisWidth.toString())

    with(density)
    {
        dimensions.chart.bottomAxisArea = BottomAxisArea(
            size = SizeDp(
                width = dimensions.chart.chartSize.width.toDp() - leftAxisWidth.value.toDp(),
                height = bottomAxisHeight.t
            ),
            offset = Offset(
                x = leftAxisWidth.value.toDp().toPx(),
                y = dimensions.chart.chartSize.height.toDp().toPx() - bottomAxisHeight.toDp().toPx()
            )
        )

        dimensions.chart.leftAxisArea = LeftAxisArea(
            size = Size(
                width = leftAxisWidth.value,
                height = dimensions.chart.chartSize.height - bottomAxisHeight
            ),
            offset = Offset(
                x = 0f,
                y = 0f
            )
        )
    }

    val plotAreaOuterWidth = dimensions.chart.chartSize.width - leftAxisWidth.value
    val plotAreaOuterHeight = dimensions.chart.chartSize.height - bottomAxisHeight

    // calculate padding for plot area
    var padding = config.chartConfig.plotAreaPadding ?: PaddingValues(0.dp)
    var barWidth = 0f

    // if it's bar chart and no padding is set, calculate it
    if (config.leftAxisConfig.type == AxisType.Bar) {
        // calculate the bar width allowing for half a width padding at start and right
        val numberOfBars = dimensions.dataValues.points.size
        val barWidthFraction = config.chartConfig.barChartFraction

        // calculate auto padding
        if (config.chartConfig.plotAreaPadding == null) {
            val divisor = numberOfBars + ((numberOfBars.toFloat() + 1) * barWidthFraction)
            barWidth = plotAreaOuterWidth / divisor
            padding = PaddingValues(
                start = (barWidth * barWidthFraction).dp,
                end = (barWidth * barWidthFraction).dp
            )
        }

        // calculate padding when defined
        if (config.chartConfig.plotAreaPadding != null) {
            val divisor = numberOfBars + ((numberOfBars.toFloat() - 1) * barWidthFraction)
            barWidth =
                (plotAreaOuterWidth - config.chartConfig.plotAreaPadding.calculateLeftPadding(
                    LayoutDirection.Ltr
                ).value - config.chartConfig.plotAreaPadding.calculateRightPadding(LayoutDirection.Ltr).value) / divisor
            padding = PaddingValues(
                start = config.chartConfig.plotAreaPadding.calculateLeftPadding(LayoutDirection.Ltr),
                end = config.chartConfig.plotAreaPadding.calculateRightPadding(LayoutDirection.Ltr)
            )
        }

    }

    dimensions.chart.plotArea = PlotArea(
        size = Size(
            width = plotAreaOuterWidth,
            height = plotAreaOuterHeight
        ),
        offset = Offset(
            x = leftAxisWidth.value,
            y = 0f
        ),
        padding = padding,
        innerOffset = Offset(
            x = leftAxisWidth.value + padding.calculateLeftPadding(LayoutDirection.Ltr).value,
            y = padding.calculateTopPadding().value
        ),
        barWidth = barWidth
    )

}
