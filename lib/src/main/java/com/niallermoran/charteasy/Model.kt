package com.niallermoran.charteasy

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class LeftAxisArea( var topLeftOffset: OffsetDp = OffsetDp(), var size: SizeDp = SizeDp() )
data class RightAxisArea(var topLeftOffset: OffsetDp = OffsetDp(), var size: SizeDp = SizeDp())

data class BottomAxisArea(var offset: OffsetDp = OffsetDp(), var size: SizeDp = SizeDp() )
data class PlotArea(var offset: OffsetDp = OffsetDp(), var size: SizeDp = SizeDp(), var padding:PaddingValues = PaddingValues(0.dp),
                    var innerOffset: OffsetDp = OffsetDp(), var barWidth: Dp = 0.dp)

data class AxisLabel( val text: TextLayoutResult )

data class OffsetDp( val left:Dp = 0.dp, val top: Dp = 0.dp )
data class SizeDp( val width:Dp = 0.dp, val height: Dp = 0.dp )

data class BottomAxisConfig(

    val gridLines: GridLines = GridLines(),

    val formatAxisLabel: ((Float) -> String)? = null,
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
    val labelPadding: PaddingValues = PaddingValues(0.dp),

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


data class GridLines( var display: Boolean = true, var color: Color = Color.LightGray, var strokeWidth: Float = 1f )

data class VerticalAxisConfig(

    val formatAxisLabel: ((Float) -> String)? = null,

    val gridLines: GridLines =  GridLines(),

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
     * Set the minimum value used for the left Y axis to control how much of the plot area the graph fills
     * To use the min value from your data points do not set this or set to zero, which will force the chart to fill the full plot area
     */
    val minY: Float? = null,

    /**
     * Set the minimum value used for the right Y axis to control how much of the plot area the graph fills
     * To use the min value from your data points do not set this or set to zero, which will force the chart to fill the full plot area
     */
    val minYRight: Float? = null,

    /**
     * Set the maximum value used for the right Y axis to control how much of the plot area the graph fills
     * To use the max value from your data points do not set this or set to zero, , which will force the chart to fill the full plot area
     */
    val maxYRight: Float? = null,

    /**
     * Set the maximum value used for the left Y axis to control how much of the plot area the graph fills
     * To use the max value from your data points do not set this or set to zero, , which will force the chart to fill the full plot area
     */
    val maxY: Float? = null,

    /**
     * Defines th epadding around the labels
     */
    val labelPadding: PaddingValues = PaddingValues(start=2.dp, end=6.dp),

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
     * NOTE: Top and Bottom padding is ignored. Use this to provide padding on left and right of plot area only
     * */
    val plotAreaPadding: PaddingValues? = null,

    /**
     * This is the proportion of barwidth left as space between bars and padding at start and end if no padding values are set
     * e.g. 1.0 means the space between bars will be the width of a bar
     * the default is 0.5, so the space would be half the bar width
     */
    val barChartFraction: Float = 0.5f,
)


/**
 * Represents a data point to be plotted. The ChartPoint must contain values representing the left axis
 * and can contain points for the right axis
 * @param xValue the value to use for the xAxis
 * @param yValue the value to use for the left y axis
 * @param yValueRightAxis the value to use for the right y axis
 * @param labelRightAxis the text used to display along side the point on the chart for the yValueRightAxis
 * @param label the text used to display along side the point on the chart for the yValue
 * @param data is any data you want to attach to the point
 */
data class ChartPoint(
    val xValue: Float,
    val yValue: Float,
    val data: Any? = null,
    val label: String? = null,
    val yValueRightAxis: Float? = null,
    val labelRightAxis: String? = null,
)


data class CrossHairs(
    val display: Boolean = true,
    val displayCoordinates: Boolean = true,
    val textStyle: TextStyle = TextStyle(color = Color.Black, fontSize = 10.sp),
    val lineColor: Color = Color.LightGray,
    val lineStrokeWidth: Dp = 1.dp
)

data class Config(
    val chartConfig: ChartConfig = ChartConfig(),
    val leftAxisConfig: VerticalAxisConfig = VerticalAxisConfig(),
    val bottomAxisConfig: BottomAxisConfig = BottomAxisConfig(),
    val rightAxisConfig: VerticalAxisConfig = VerticalAxisConfig(),

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
    val dataValues: DataValues = DataValues()
)

/**
 * This object contains all of the calculated dimensions required to position elements outside of the chart. This object wilkl be returned to the user when the plot area is tapped
 */
data class ChartDimensions(
    var leftAxisArea:LeftAxisArea = LeftAxisArea(),
    var bottomAxisArea:BottomAxisArea= BottomAxisArea(),
    var rightAxisArea:RightAxisArea = RightAxisArea(),
    var plotArea:PlotArea = PlotArea(),
    var chartSize: SizeDp = SizeDp(0.dp, 0.dp)
)

data class DataValues(var xMin: Float = 0f, var yMin:Float = 0f, var xMax: Float = 0f, var yMax:Float = 0f, var yMinRight: Float = 0f, var yMaxRight: Float = 0f,  var points: List<ChartPoint> = ArrayList() )

