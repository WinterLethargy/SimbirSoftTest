package com.example.simbirsofttest.feature.deal.recyclerview.customCells

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import androidx.core.graphics.withSave
import com.example.simbirsofttest.core.utils.dpToPx


class DealCellView(
    context: Context,
) : View(context) {

    var hourStart: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var duration: Int = 0
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var dealName: String? = null
        set(value) {
            field = value
            setupStaticLayout()
            invalidate()
        }

    private val hourTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
    }

    private val dealBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val emptyDealBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
    }

    private var staticLayout: StaticLayout? = null

    private val dealTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 50f
    }

    private val HOUR_HEIGHT_PX = HOUR_HEIGHT_DP.dpToPx
    private val MARGIN_PX = MARGIN_DP.dpToPx
    private val HOUR_TEXT_WIDTH_PX = hourTextPaint.measureText("00:00 - 00:00")
    private val HOUR_TO_DEAL_SPACE_PX = HOUR_TO_DEAL_SPACE_DP.dpToPx
    private val DEAL_VERTICAL_MARGIN_PX = DEAL_VERTICAL_MARGIN_DP.dpToPx
    private val CORNER_RADIUS_PX = CORNER_RADIUS_DP.dpToPx
    private val EMPTY_DEAL_CORNER_RADIUS_PX = EMPTY_DEAL_CORNER_RADIUS_DP.dpToPx
    private val DEAL_LEFT_PX = MARGIN_PX + HOUR_TEXT_WIDTH_PX + HOUR_TO_DEAL_SPACE_PX
    private val DEAL_TOP_PX get() = DEAL_VERTICAL_MARGIN_PX
    private var DEAL_RIGHT_PX = 0f
    private var DEAL_BOTTOM_PX = 0f
    private val DEAL_HEIGHT get() = DEAL_BOTTOM_PX - DEAL_TOP_PX

    private val DEAL_TEXT_PADDIND_PX = DEAL_TEXT_PADDING_DP.dpToPx
    private val DEAL_TEXT_LINE_SIZE_PX = dealTextPaint.fontMetrics.let{ it.descent - it.ascent + it.leading }
    private val DEAL_TEXT_LEFT_PX = DEAL_LEFT_PX + DEAL_TEXT_PADDIND_PX
    private var DEAL_TEXT_TOP_PX = 0f

    private val EMPTY_DEAL_HEIGHT_PX = EMPTY_DEAL_HEIGHT_DP.dpToPx
    private var EMPTY_DEAL_TOP_PX = 0f
    private var EMPTY_DEAL_BOTTOM_PX = 0f
    private val EMPTY_DEAL_LEFT_PX get() = DEAL_LEFT_PX
    private val EMPTY_DEAL_RIGHT_PX get() = DEAL_RIGHT_PX

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = if (duration > 0) duration * HOUR_HEIGHT_PX else HOUR_HEIGHT_PX
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupDimensionsAndStaticLayout()
    }

    private fun setupDimensionsAndStaticLayout(){

        DEAL_RIGHT_PX = 0f
        DEAL_BOTTOM_PX = 0f
        DEAL_TEXT_TOP_PX = 0f

        if(width == 0 || height == 0)
            return

        DEAL_RIGHT_PX = width - MARGIN_PX
        DEAL_BOTTOM_PX = height - DEAL_VERTICAL_MARGIN_PX
        EMPTY_DEAL_TOP_PX = (height - EMPTY_DEAL_HEIGHT_PX) / 2
        EMPTY_DEAL_BOTTOM_PX = EMPTY_DEAL_TOP_PX + EMPTY_DEAL_HEIGHT_PX

        setupStaticLayout()
    }

    private fun setupStaticLayout(){
        staticLayout = null

        if(width == 0 || height == 0)
            return

        val localDealName = dealName
        if (localDealName == null)
            return

        val maxWidth = (width - DEAL_LEFT_PX - MARGIN_PX - 2 * DEAL_TEXT_PADDIND_PX).toInt()
        val maxLines = ((height - (DEAL_VERTICAL_MARGIN_PX + DEAL_TEXT_PADDIND_PX) * 2) / DEAL_TEXT_LINE_SIZE_PX).toInt()
        staticLayout = StaticLayout.Builder.obtain(localDealName, 0, localDealName.length, dealTextPaint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setMaxLines(maxLines)
            .build()

        staticLayout?.let{
            DEAL_TEXT_TOP_PX = DEAL_VERTICAL_MARGIN_PX + (DEAL_HEIGHT - DEAL_TEXT_LINE_SIZE_PX * it.lineCount) / 2
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHours(canvas)

        if (duration > 0) {
            drawDeal(canvas)
        }
        else {
            drawEmptyDeal(canvas)
        }
    }

    private fun drawHours(canvas: Canvas){
        val hourCount = if (duration > 0) duration else 1
        for (i in 0 until hourCount) {
            val hour = hourStart + i
            val hourText = if(hour == 23) "23:00 - 00:00" else String.format("%02d:00 - %02d:00", hour, hour + 1)

            canvas.drawText(
                hourText,
                MARGIN_PX,
                HOUR_HEIGHT_PX * (i + 0.5f) - ((hourTextPaint.descent() + hourTextPaint.ascent()) / 2f),
                hourTextPaint
            )
        }
    }

    private fun drawDeal(canvas: Canvas){
        canvas.drawRoundRect(
            DEAL_LEFT_PX,
            DEAL_TOP_PX,
            DEAL_RIGHT_PX,
            DEAL_BOTTOM_PX,
            CORNER_RADIUS_PX,
            CORNER_RADIUS_PX,
            dealBackgroundPaint
        )

        staticLayout?.let { sl ->
            canvas.withSave {
                translate(DEAL_TEXT_LEFT_PX, DEAL_TEXT_TOP_PX)
                sl.draw(canvas)
            }
        }
    }

    private fun drawEmptyDeal(canvas: Canvas){
        canvas.drawRoundRect(
            EMPTY_DEAL_LEFT_PX,
            EMPTY_DEAL_TOP_PX,
            EMPTY_DEAL_RIGHT_PX,
            EMPTY_DEAL_BOTTOM_PX,
            EMPTY_DEAL_CORNER_RADIUS_PX,
            EMPTY_DEAL_CORNER_RADIUS_PX,
            emptyDealBackgroundPaint
        )
    }

    companion object{
        const val HOUR_HEIGHT_DP = 64
        const val MARGIN_DP = 16
        const val HOUR_TO_DEAL_SPACE_DP = 20
        const val DEAL_VERTICAL_MARGIN_DP = 8
        const val CORNER_RADIUS_DP = 20
        const val EMPTY_DEAL_CORNER_RADIUS_DP = 1
        const val DEAL_TEXT_PADDING_DP = 3
        const val EMPTY_DEAL_HEIGHT_DP = 2
    }
}