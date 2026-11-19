package com.wallet.mywallet.ui.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.wallet.mywallet.R

/**
 * A small hand-drawn bar chart: two bars (income green, expense red) for each month.
 * Drawing it ourselves keeps the app free of chart libraries.
 */
class TrendChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var points: List<TrendPoint> = emptyList()

    private val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.income) }
    private val expensePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.expense) }
    private val baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.md_outlineVariant)
        strokeWidth = resources.displayMetrics.density
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.md_onSurfaceVariant)
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
        textAlign = Paint.Align.CENTER
    }

    private val barRect = RectF()

    fun setPoints(newPoints: List<TrendPoint>) {
        points = newPoints
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) return

        val density = resources.displayMetrics.density
        val labelHeight = labelPaint.textSize + 8 * density
        val chartBottom = height - labelHeight
        val chartHeight = chartBottom - 8 * density
        val slotWidth = width.toFloat() / points.size
        val barWidth = slotWidth * 0.26f
        val corner = 3 * density
        val maxValue = points.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1L)

        canvas.drawLine(0f, chartBottom, width.toFloat(), chartBottom, baselinePaint)

        points.forEachIndexed { index, point ->
            val centerX = slotWidth * index + slotWidth / 2
            drawBar(canvas, centerX - barWidth - 1 * density, barWidth, point.income, maxValue, chartHeight, chartBottom, corner, incomePaint)
            drawBar(canvas, centerX + 1 * density, barWidth, point.expense, maxValue, chartHeight, chartBottom, corner, expensePaint)
            canvas.drawText(point.label, centerX, height - 4 * density, labelPaint)
        }
    }

    private fun drawBar(
        canvas: Canvas, left: Float, barWidth: Float, value: Long, maxValue: Long,
        chartHeight: Float, chartBottom: Float, corner: Float, paint: Paint
    ) {
        if (value <= 0) return
        val barHeight = (chartHeight * value / maxValue).coerceAtLeast(2f)
        barRect.set(left, chartBottom - barHeight, left + barWidth, chartBottom)
        canvas.drawRoundRect(barRect, corner, corner, paint)
    }
}
