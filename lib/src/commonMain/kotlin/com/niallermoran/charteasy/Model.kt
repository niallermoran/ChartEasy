package com.niallermoran.charteasy

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import getRandomColour
import kotlin.math.abs


data class LeftAxisArea(var topLeftOffset: OffsetDp = OffsetDp(), var size: SizeDp = SizeDp())
data class RightAxisArea(var topLeftOffset: OffsetDp = OffsetDp(), var size: SizeDp = SizeDp())

data class BottomAxisArea(var offset: OffsetDp = OffsetDp(), var size: SizeDp = SizeDp())
data class PlotArea(
    var offset: OffsetDp = OffsetDp(),
    var size: SizeDp = SizeDp(),
    var padding: PaddingValues = PaddingValues(0.dp),
    var innerTopLeftOffset: OffsetDp = OffsetDp(),
    var barWidth: Dp = 0.dp
) {
    val innerWidth: Dp
        get() = size.width - padding.calculateLeftPadding(LayoutDirection.Ltr) - padding.calculateRightPadding(
            LayoutDirection.Ltr
        )

    val innerHeight: Dp
        get() = size.height - padding.calculateTopPadding() - padding.calculateBottomPadding()


}


data class AxisLabel(val text: TextLayoutResult, val centerDistanceAlongAxis: Dp)

data class OffsetDp(var left: Dp = 0.dp, var top: Dp = 0.dp)
data class SizeDp(val width: Dp = 0.dp, val height: Dp = 0.dp)

data class BottomAxisConfig(

    /**
     * Configure the cross hairs when a user taps a point on the screen
     */
    val crossHairsConfig: CrossHairs = CrossHairs(),

    val gridLines: GridLines = GridLines(),
    val formatAxisLabel: ((Double) -> String)? = null,
    val axisColor: Color = Color.LightGray,
    val axisStrokeWidth: Dp = 1.dp,
    val tickStrokeWidth: Dp = 1.dp,
    val tickColor: Color = Color.LightGray,
    val tickLength: Dp = 10.dp,
    val labelStyle: TextStyle = TextStyle(
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        background = Color.Transparent,
        color = Color.Gray
    ),
    val labelOverflow: TextOverflow = TextOverflow.Ellipsis,
    val labelPadding: PaddingValues = PaddingValues(0.dp),

    /**
     * Defines the maximum number of ticks and labels to display on the bottom axis.
     * If left empty then all points will have a tick and label shown
     */
    val maxNumberOfLabelsToDisplay: Int = 20,

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

data class GridLines(
    var display: Boolean = true,
    var color: Color = Color.LightGray,
    var strokeWidth: Double = 0.1
)

data class VerticalAxisConfig(

    val formatAxisLabel: ((Double) -> String)? = null,

    val gridLines: GridLines = GridLines(),

    val showCircles: Boolean = true,

    /**
     * Use this value to hide or show the bottom axis ticks
     */
    val displayTicks: Boolean = true,

    /**
     * Use this value to hide or show the axis labels
     */
    val displayLabels: Boolean = true,

    /**
     * Use this value to hide or show the axis completely
     */
    val display: Boolean = true,

    val showFillColour: Boolean = true,


    /**
     * Define a fill brush to use to fill the bars or line
     */
    val fillBrush: Brush = Brush.verticalGradient(listOf(Color.White, Color(getRandomColour()))),

    /**
     * Apply transparency
     */
    val fillAlpha: Double = 1.0,

    val lineColor: Color = Color(getRandomColour()),
    val circleColor: Color = Color(getRandomColour()),
    val circleRadius: Dp = 4.dp,

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
     * The data points used to plot a chart.
     * For bar charts the number of data points must match the number of bottom labels
     */
    var dataPoints: List<ChartPoint> = ArrayList(),

    val axisColor: Color = Color.LightGray,
    val axisStrokeWidth: Dp = 1.dp,
    val tickStrokeWidth: Dp = 1.dp,
    val lineStrokeWidth: Dp = 1.dp,


    /**
     * Set the type of chart to draw for the left axis values
     */
    val type: AxisType = AxisType.Line,
    val tickColor: Color = Color.LightGray,
    val tickLength: Dp = 10.dp,

    /**
     * Set the minimum value used for the left Y axis to control how much of the plot area the graph fills
     * To use the min value from your data points do not set this or set to zero, which will force the chart to fill the full plot area
     */
    val minY: Double? = null,


    /**
     * Set the maximum value used for the left Y axis to control how much of the plot area the graph fills
     * To use the max value from your data points do not set this or set to zero, , which will force the chart to fill the full plot area
     */
    val maxY: Double? = null,

    /**
     * Defines th epadding around the labels
     */
    val labelPadding: PaddingValues = PaddingValues(start = 2.dp, end = 6.dp),

    val labelStyle: TextStyle = TextStyle(
        color = Color.Gray,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        background = Color.Transparent,
    ),

    /**
     * Indicates if labels should be shown alongside points on the chart
     */
    val showPointLabels: Boolean = true,

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
) {
    fun getPointLabelText(
        chartPoint: ChartPoint,
        rightAxis: Boolean,
        textMeasurer: TextMeasurer
    ): TextLayoutResult? {
        val label = if (rightAxis) chartPoint.pointLabelRightAxis else chartPoint.pointLabel

        if (label != null) {
            val measure = textMeasurer.measure(
                label,
                pointLabelStyle,
                labelOverflow,
                maxLines = 1
            )

            return measure
        }

        return null
    }
}

data class ChartConfig(

    /**
     * Add padding to the plot area if you need more spacing around your chart
     * Leaving blank will create automatic padding, set to zero to undo this
     * NOTE: Top and Bottom padding is ignored. Use this to provide padding on left and right of plot area only
     * */
    val plotAreaPadding: PaddingValues? = null,

    /**
     * This is the proportion of barwidth left as space between bars and padding at start and end if no padding values are set
     * e.g. 1.0 means the space between bars will be the width of a bar
     * the default is 0.5, so the space would be half the bar width
     */
    val barChartFraction: Double = 0.5,

    /**
     * Provide a lambda to respond to user tapping the plot area
     * @param chartPointCoordinates returns the co-ordinates of the left and right values as well as the data values
     * @param offset is the position of the point (in Dp) relative to the chart canvas top left corner
     */
    val onTap: ((chartPointCoordinates: ChartPointCoordinates) -> Unit)? = null,

    /**
     * Provide a lambda which can be used to draw custom elements on the chart
     * The lambda will be passed a ChartDimensions object containing all of the dimensions of the chart
     * so you can position your custom elements correctly
     */
    val onDraw: (DrawScope.(dimensions: Dimensions) -> Unit)? = null
)


/**
 * Represents a data point to be plotted. The ChartPoint must contain values representing the left axis
 * and can contain points for the right axis
 * @param xValue the value to use for the xAxis
 * @param yValue the value to use for the left y axis
 * @param yValueRightAxis the value to use for the right y axis
 * @param pointLabelRightAxis the text used to display along side the point on the chart for the yValueRightAxis
 * @param pointLabel the text used to display along side the point on the chart for the yValue
 * @param data is any data you want to attach to the point
 */
data class ChartPoint(
    val xValue: Double,
    val yValue: Double,
    val yValueRightAxis: Double? = null,
    val data: Any? = null,
    val pointLabel: String? = null,
    val pointLabelRightAxis: String? = null,
)


data class CrossHairs(
    val display: Boolean = true,
    val lineColor: Color = Color.LightGray,
    val lineStrokeWidth: Dp = 1.dp,
)

data class Config(
    val chartConfig: ChartConfig = ChartConfig(),
    val leftAxisConfig: VerticalAxisConfig = VerticalAxisConfig(),
    val bottomAxisConfig: BottomAxisConfig = BottomAxisConfig(),
    val rightAxisConfig: VerticalAxisConfig? = null,

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
data class Dimensions(
    val chart: ChartDimensions = ChartDimensions(),
    var rightAxisLabels: ArrayList<AxisLabel> = ArrayList(),
    var leftAxisLabels: ArrayList<AxisLabel> = ArrayList(),
    val bottomAxisLabels: ArrayList<AxisLabel> = ArrayList(),
    val dataValues: DataValues = DataValues(),

    /**
     * The co-ordinates of every point on the inner plot area
     */
    val dataCoordinates: ArrayList<ChartPointCoordinates> = ArrayList(),

) {
    /**
     * Based on x and y Dp values on the inner plot area canvas, find the closest chartPoint
     */
    fun getClosestChartPointToCoordinate(
        innerPlotAreaCoordinate: OffsetDp
    ): ChartPointCoordinates {
        var distanceBetweenPoints = Float.MAX_VALUE
        var closestPoint: ChartPointCoordinates = dataCoordinates[0]
        var closestIndex = 0



        dataCoordinates.forEachIndexed { index, chartPointCoordinates ->


            val distanceDp =
                abs(innerPlotAreaCoordinate.left.value - chartPointCoordinates.offsetLeft.left.value)

            if (distanceDp < distanceBetweenPoints) {
                distanceBetweenPoints = distanceDp
                closestPoint = chartPointCoordinates
                closestIndex = index
            }
        }

        return closestPoint
    }
}

/**
 * An object that represents a chart point value with cooordinates for left and right values
 */
data class ChartPointCoordinates(val chartPoint: ChartPoint, val offsetLeft: OffsetDp, val offsetRight: OffsetDp? = null)


/**
 * This object contains all of the calculated dimensions required to position elements outside of the chart. This object wilkl be returned to the user when the plot area is tapped
 */
data class ChartDimensions(
    var leftAxisArea: LeftAxisArea = LeftAxisArea(),
    var bottomAxisArea: BottomAxisArea = BottomAxisArea(),
    var rightAxisArea: RightAxisArea = RightAxisArea(),
    var plotArea: PlotArea = PlotArea(),
    var chartSize: SizeDp = SizeDp(0.dp, 0.dp),

    )

data class DataValues(
    var xMin: Double = 0.0,
    var yMin: Double = 0.0,
    var xMax: Double = 0.0,
    var yMax: Double = 0.0,
    var yMinRight: Double = 0.0,
    var yMaxRight: Double = 0.0,
    var points: List<ChartPoint> = ArrayList()
)

