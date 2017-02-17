package jp.co.recruit_mp.android.lightcalendarview.views

import android.content.Context
import android.graphics.Canvas
import jp.co.recruit_mp.android.lightcalendarview.CalendarSettings
import jp.co.recruit_mp.android.lightcalendarview.CellView

/**
 * Created by svyatozar on 17.02.17.
 */

open class CapView(context: Context, settings: CalendarSettings) : CellView(context, settings){
    var isNeedCapDraw : Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    val capPaint = settings.dayView.defaultCapPaint

    override fun onDraw(canvas: Canvas?) {
        if (isNeedCapDraw) {
            /**
             * Описываем высоту грани квадрата, которого можно вписать в окружность
             */

            val canvasHeight = canvas?.height ?: 0
            val resultHeight = canvasHeight / settings.dayView.squareRootOf2
            val margin = (canvasHeight - resultHeight) / 2

            canvas?.drawRect(0f, margin.toFloat(), canvasHeight.toFloat(), (canvasHeight.toFloat() - margin).toFloat(), capPaint)
        }
    }
}