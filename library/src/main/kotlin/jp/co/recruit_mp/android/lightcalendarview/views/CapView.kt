package jp.co.recruit_mp.android.lightcalendarview.views

import android.content.Context
import android.graphics.Canvas
import jp.co.recruit_mp.android.lightcalendarview.CalendarSettings
import jp.co.recruit_mp.android.lightcalendarview.CellView
import jp.co.recruit_mp.android.lightcalendarview.R

/**
 * Created by svyatozar on 17.02.17.
 */

open class CapView(context: Context, settings: CalendarSettings) : CellView(context, settings) {
    companion object {
        const val INVISIBLE = 0
        const val VISIBLE = 1
        const val ROUND_RIGHT = 2
        const val ROUND_LEFT = 3
    }

    var state: Int = INVISIBLE
    set(value) {
        field = value
        invalidate()
    }

    val capPaint = settings.dayView.defaultCapPaint
    val capMargin = resources.getDimensionPixelSize(R.dimen.light_calendar_view_cap_margin).toFloat()

    override fun onDraw(canvas: Canvas?) {
        val canvasHeight = canvas?.height ?: 0

        when (state) {
            VISIBLE -> {
                canvas?.drawRect(0f, capMargin, canvasHeight.toFloat(), canvasHeight - capMargin, capPaint)
            }

            ROUND_RIGHT -> {
                canvas?.drawRect(0f, capMargin, canvasHeight.toFloat() / 2, canvasHeight - capMargin, capPaint)
            }

            ROUND_LEFT -> {
                canvas?.drawRect(canvasHeight.toFloat() / 2, capMargin, canvasHeight.toFloat(), canvasHeight - capMargin, capPaint)
            }
        }
    }
}