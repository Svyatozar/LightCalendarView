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
import android.view.View
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
        val FIRST_ROW = 0
        val PENULT_ROW = DEFAULT_WEEKS - 2
        val LAST_ROW = DEFAULT_WEEKS - 1
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

    /**
     * I'm not touching that one with a twenty-foot pole
     */
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
        }

        if (secondSelectedDayView != null) {
            if (secondSelectedDayView?.date != LightCalendarView.secondDate) {
                secondSelectedDayView?.apply {
                    isSelected = false
                    updateState()
                }

                secondSelectedDayView = null
            }
        }

        /**
         * Очищаем заполнение цветом
         */
        for (i in 0 until childCount)
        {
            (getChildAt(i) as CapView).state = CapView.INVISIBLE
        }

        if (null != dateFrom && null != dateTo)
        {
            /**
             * Проходимся по датам и заглушкам в промежутке, учитывая отрезки, выходящие за пределы месяца
             */
            var cal = firstDate.clone() as Calendar

            for (i in 0 until rowNum) {
                for (j in 0 until colNum) {
                    when (cal.time) {
                        dateFrom -> {
                            if (cal.time > dateTo) {
                                getCapView(i,j)?.state = CapView.ROUND_RIGHT
                            } else {
                                getCapView(i,j)?.state = CapView.ROUND_LEFT
                            }

                            if (null == selectedDayView) {
                                val markView = getCapView(i,j)

                                if (markView is DayView) {
                                    selectedDayView = markView.apply {
                                        isSelected = true
                                        updateState()
                                    }
                                }
                            }
                        }

                        dateTo -> {
                            if (cal.time > dateFrom) {
                                getCapView(i,j)?.state = CapView.ROUND_RIGHT
                            } else {
                                getCapView(i,j)?.state = CapView.ROUND_LEFT
                            }

                            if (null == secondSelectedDayView) {
                                val markView = getCapView(i,j)

                                if (markView is DayView) {
                                    secondSelectedDayView = markView.apply {
                                        isSelected = true
                                        updateState()
                                    }
                                }
                            }
                        }

                        else -> {
                            val capView = getCapView(i,j)

                            if (cal.time.between(dateFrom, dateTo)) {
                                capView?.state = CapView.VISIBLE
                            }
                        }
                    }

                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            /**
             * Заполняем пустые места, или наоборот очищаем, в зависимости от того, находится ли дата за
             * пределами данного месяца
             */
            cal = firstDate.clone() as Calendar
            val isDaysExistInPenultRow = isDayViewExistInRow(PENULT_ROW)
            val isDaysExistInLastRow = isDayViewExistInRow(LAST_ROW)
            for (i in 0 until rowNum) {
                (0 until colNum)
                .map { getCapView(i, it) }
                .forEach {
                    if (it !is DayView) {
                        when (i) {
                            FIRST_ROW -> {
                                if ((dateFrom.month() < thisMonth) || (dateTo.month() < thisMonth) || (dateFrom.year() < thisYear) || (dateTo.year() < thisYear)) {
                                    if (((dateFrom.month() == thisMonth) || (dateTo.month() == thisMonth)) and (cal.time.month() < thisMonth)) {
                                        if (isDateInRange()) {
                                            it?.state = CapView.VISIBLE
                                        }
                                    } else {
                                        it?.state = CapView.INVISIBLE
                                    }
                                }
                            }

                            PENULT_ROW -> {
                                if ((dateFrom.month() > thisMonth) || (dateTo.month() > thisMonth) || (dateFrom.year() > thisYear) || (dateTo.year() > thisYear)) {
                                    if (isDaysExistInPenultRow) {
                                        if (cal.time.month() > thisMonth) {
                                            if ((dateFrom.month() == thisMonth) || (dateTo.month() == thisMonth)) {
                                                if (isDateInRange()) {
                                                    it?.state = CapView.VISIBLE
                                                }
                                            } else {
                                                it?.state = CapView.INVISIBLE
                                            }
                                        }
                                    } else {
                                        it?.state = CapView.INVISIBLE
                                    }
                                }
                            }

                            LAST_ROW -> {
                                if ((dateFrom.month() > thisMonth) || (dateTo.month() > thisMonth) || (dateFrom.year() > thisYear) || (dateTo.year() > thisYear)) {
                                    if (isDaysExistInLastRow) {
                                        if (cal.time.month() > thisMonth) {
                                            if ((dateFrom.month() == thisMonth) || (dateTo.month() == thisMonth)) {
                                                if (isDateInRange()) {
                                                    it?.state = CapView.VISIBLE
                                                }
                                            } else {
                                                it?.state = CapView.INVISIBLE
                                            }
                                        }
                                    } else {
                                        it?.state = CapView.INVISIBLE
                                    }
                                }
                            }
                        }
                    }

                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            /**
             * Рассматриваем случай, если месяц оказался посередине между двумя датами - закрашиваем все, если это так
             */
            cal = firstDate.clone() as Calendar

            if ((dateFrom.month() != thisMonth) && (dateTo.month() != thisMonth)) {
                if (month.between(dateFrom, dateTo)) {
                    for (i in 0 until rowNum) {
                        (0 until colNum)
                                .map { getCapView(i, it) }
                                .forEach {if (isDayViewExistInRow(i)) { it?.state = CapView.VISIBLE } }
                    }
                }
            }
        }
    }

    fun isDateInRange() : Boolean {
        val dateFrom = LightCalendarView.firstDate
        val dateTo = LightCalendarView.secondDate

        if (null != dateFrom) {
            if (null != dateTo) {
                val minYear = listOf(dateFrom.year(), dateTo.year()).min() ?: Int.MAX_VALUE
                val maxYear = listOf(dateFrom.year(), dateTo.year()).max() ?: Int.MIN_VALUE

                return (minYear <= thisYear) and (maxYear >= thisYear)
            }
        }

        return false
    }

    /**
     * 日付に対応する {@link DayView} を返す
     */
    fun getDayView(date: Date): DayView? = childList.getOrNull(date.daysAfter(firstDate.time).toInt()) as? DayView
    fun getCapView(row: Int, col: Int): CapView? {
        val position = row*colNum + col
        return childList.getOrNull(position) as? CapView
    }

    fun isDayViewExistInRow(row: Int) : Boolean {
        return (0 until colNum).any { getCapView(row, it) is DayView }
    }

    class DateSelectedEvent(date: Date)
}
