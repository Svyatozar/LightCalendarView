/*
 * Copyright (C) 2016 RECRUIT MARKETING PARTNERS CO., LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.recruit_mp.android.lightcalendarview

import android.content.Context
import android.support.v4.view.ViewCompat
import android.widget.Toast
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import jp.co.recruit_mp.android.lightcalendarview.views.CapView
import java.util.*

/**
 * 月カレンダー内の日を表示する {@link ViewGroup}
 * Created by masayuki-recruit on 8/18/16.
 */
class DayLayout(context: Context, settings: CalendarSettings, var month: Date) : CellLayout(context, settings) {

    companion object {
        val DEFAULT_WEEKS = 6
        val DEFAULT_DAYS_IN_WEEK = WeekDay.values().size

        var isTodayWasSetted = false
    }

    override val rowNum: Int
        get() = DEFAULT_WEEKS
    override val colNum: Int
        get() = DEFAULT_DAYS_IN_WEEK

    internal var selectedDayView: DayView? = null
    internal var secondSelectedDayView: DayView? = null

    internal var onDateSelected: ((date: Date) -> Unit)? = null
    internal var onDateRangeSelected: ((firstDate: Date, secondDate: Date) -> Unit)? = null

    private var firstDate: Calendar = CalendarKt.getInstance(settings)
    private var dayOfWeekOffset: Int = -1
    private val thisYear: Int
    private val thisMonth: Int

    init {
        val cal: Calendar = CalendarKt.getInstance(settings).apply {
            time = month
            set(Calendar.DAY_OF_MONTH, 1)
        }
        thisYear = cal[Calendar.YEAR]
        thisMonth = cal[Calendar.MONTH]

        // update the layout
        updateLayout()

        // 今日を選択
        setSelectedDay(Date())
    }

    private val observer = Observer { observable, any ->
        updateLayout()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        settings.addObserver(observer)
        //subscribe to events
        Bus.observe<DateSelectedEvent>()
                .subscribe { fillRange() }
                .registerInBus(this)
    }

    override fun onDetachedFromWindow() {
        settings.deleteObserver(observer)
        //unsubscribe from events
        Bus.unregister(this)

        super.onDetachedFromWindow()
    }

    private fun updateLayout() {
        if (dayOfWeekOffset != settings.dayOfWeekOffset) {
            dayOfWeekOffset = settings.dayOfWeekOffset

            // calculate the date of top-left cell
            val cal: Calendar = CalendarKt.getInstance(settings).apply {
                time = month
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.DAY_OF_YEAR, (-this[Calendar.DAY_OF_WEEK] + dayOfWeekOffset + 1).let { offset ->
                    if (offset > 0) (offset - WeekDay.values().size) else offset
                })
            }
            firstDate = cal

            // remove all children
            removeAllViews()

            // populate children
            populateViews()
        }
    }

    private fun populateViews() {
        val cal = firstDate.clone() as Calendar

        // 7 x 6 マスの DayView を追加する
        (0 until rowNum).forEach {
            (0 until colNum).forEach {
                when (cal[Calendar.MONTH]) {
                    thisMonth -> {
                        addView(instantiateDayView(cal.clone() as Calendar))
                    }
                    else -> {
                        addView(EmptyView(context, settings))
                    }
                }
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun instantiateDayView(cal: Calendar): DayView = DayView(context, settings, cal).apply {
        setOnClickListener { setSelectedDay(this) }
    }

    internal fun invalidateDayViews() {
        childList.map { it as? DayView }.filterNotNull().forEach {
            it.updateState()
            ViewCompat.postInvalidateOnAnimation(it)
        }
    }

    /**
     * 日付を選択する
     * @param date 選択する日
     */
    fun setSelectedDay(date: Date) {
        var dayView = getDayView(date)

        if ((null != dayView) and (!isTodayWasSetted))
        {
            isTodayWasSetted = true
        }
        else {
            dayView = null
        }

        setSelectedDay(dayView)
    }

    private fun setSelectedDay(view: DayView?) {
        //TODO

        // TODO view?.let { Bus.send(DateSelectedEvent(view.date)) }

        if (view == null) {
            fillRange()
            return
        }

        /**
         * Рассматриваем случай, если первая дата уже выбрана, и она не в этом месяце
         */
        val dateFrom = LightCalendarView.firstDate

        dateFrom?.let {
            if ((dateFrom.month() != thisMonth) || (dateFrom.year() != thisYear)) {
                secondSelectedDayView?.apply {
                    isSelected = false
                    updateState()
                }

                secondSelectedDayView = view?.apply {
                    isSelected = true
                    updateState()
                }

                view?.let {
                    LightCalendarView.secondDate = view.date
                    onDateRangeSelected?.invoke(dateFrom, view.date)
                    Bus.send(DateSelectedEvent(view.date))
                }

                return
            }
        }

        when (view)
        {
            selectedDayView -> {
                selectedDayView?.apply {
                    isSelected = false
                    updateState()
                }

                selectedDayView = null
                LightCalendarView.firstDate = null
            }

            secondSelectedDayView -> {
                secondSelectedDayView?.apply {
                    isSelected = false
                    updateState()
                }

                secondSelectedDayView = null
                LightCalendarView.secondDate = null
            }

            else -> {
                if (selectedDayView != null) {
                    secondSelectedDayView?.apply {
                        isSelected = false
                        updateState()
                    }

                    secondSelectedDayView = view?.apply {
                        isSelected = true
                        updateState()
                    }

                    val firstDate = selectedDayView?.date
                    val secondDate = secondSelectedDayView?.date

                    if (null != firstDate && null != secondDate)
                    {
                        onDateRangeSelected?.invoke(firstDate, secondDate)

                        LightCalendarView.firstDate = firstDate
                        LightCalendarView.secondDate = secondDate
                    }
                }
                else {
                    selectedDayView = view?.apply {
                        isSelected = true
                        updateState()
                    }

                    LightCalendarView.firstDate = view?.date
                    view?.let { onDateSelected?.invoke(it.date) }
                }
            }
        }

        view?.let {
            Bus.send(DateSelectedEvent(view.date))
        }
    }

    fun fillRange()
    {
        val dateFrom = LightCalendarView.firstDate
        val dateTo = LightCalendarView.secondDate

        /**
         * Снимаем метки, если начало или окончание промежутка в другом месяце
         */
        if (selectedDayView != null) {
            if (selectedDayView?.date != LightCalendarView.firstDate) {
                selectedDayView?.apply {
                    isSelected = false
                    updateState()
                }

                selectedDayView = null
            }
        } else {
            dateFrom?.let {
                if (it.month() == thisMonth) {
                    selectedDayView = getDayView(it)
                    selectedDayView?.apply {
                        isSelected = true
                        updateState()
                    }
                }
            }
        }

        /**
         * Восстанавливаем метки, в случае если view был создан заново
         */
        if (secondSelectedDayView != null) {
            if (secondSelectedDayView?.date != LightCalendarView.secondDate) {
                secondSelectedDayView?.apply {
                    isSelected = false
                    updateState()
                }

                secondSelectedDayView = null
            }
        }
        else {
            dateTo?.let {
                if (it.month() == thisMonth) {
                    secondSelectedDayView = getDayView(it)
                    secondSelectedDayView?.apply {
                        isSelected = true
                        updateState()
                    }
                }
            }
        }

        /**
         * Очищаем заполнение цветом
         */
        for (i in 0 until childCount)
        {
            (getChildAt(0) as CapView).isNeedCapDraw = false
        }

        if (null != dateFrom && null != dateTo)
        {
            /**
             * Проходимся по датам и заглушкам в промежутке, учитывая отрезки, выходящие за пределы месяца
             */
            val cal = firstDate.clone() as Calendar

            for (i in 0 until rowNum) {
                var isDayViewExistInRow = true

                for (j in 0 until colNum) {
                    if ((i != 0 && j == 0) && (getCapView(i,j) is EmptyView)) {
                        isDayViewExistInRow = false
                    }

                    if (cal.time.between(dateFrom, dateTo)) {
                        getCapView(i,j)?.isNeedCapDraw = true
                    }
                    else if ((cal[Calendar.MONTH] < thisMonth) || (cal[Calendar.YEAR] < thisYear)) {
                        getCapView(i,j)?.isNeedCapDraw = true
                    }
                    else if (isDayViewExistInRow and ((cal[Calendar.MONTH] > thisMonth) || (cal[Calendar.YEAR] > thisYear))) {
                        getCapView(i,j)?.isNeedCapDraw = true
                    }

                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
    }
    /**
     * 日付に対応する {@link DayView} を返す
     */
    fun getDayView(date: Date): DayView? = childList.getOrNull(date.daysAfter(firstDate.time).toInt()) as? DayView
    fun getCapView(row: Int, col: Int): CapView? {
        val position = row*rowNum + col
        return childList.getOrNull(position) as? CapView
    }

    class DateSelectedEvent(date: Date)
}
