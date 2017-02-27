package jp.co.recruit_mp.android.lightcalendarview.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.co.recruit_mp.android.lightcalendarview.LightCalendarView
import jp.co.recruit_mp.android.lightcalendarview.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by svyatozar on 22.02.17.
 */

class DateRangeCalendarDialog : DialogFragment() {
    lateinit var arrowNextImageView : ImageView
    lateinit var arrowPrevImageView : ImageView
    lateinit var dateHeaderTextView : TextView
    lateinit var dateRangeTextView : TextView

    lateinit var calendarView : LightCalendarView
    private val headerFormatter = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
    private val rangeFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.calendar_dialog_layout, null)

        arrowNextImageView = rootView.findViewById(R.id.arrowNextImageView) as ImageView
        arrowPrevImageView = rootView.findViewById(R.id.arrowPrevImageView) as ImageView
        dateHeaderTextView = rootView.findViewById(R.id.dateHeaderTextView) as TextView
        dateRangeTextView = rootView.findViewById(R.id.dateRangeTextView) as TextView
        calendarView = rootView.findViewById(R.id.calendarView) as LightCalendarView

        arrowNextImageView.setOnClickListener { calendarView.setCurrentItem(calendarView.currentItem + 1, true) }
        arrowPrevImageView.setOnClickListener { calendarView.setCurrentItem(calendarView.currentItem - 1, true) }

        calendarView.apply {
            monthFrom = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2014)
                set(Calendar.MONTH, 0)
            }.time

            //monthFrom = Calendar.getInstance().apply { set(Calendar.YEAR, 2014) }.time//Calendar.getInstance().apply { set(Calendar.MONTH, 0) }.time
            monthTo = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2018)
                set(Calendar.MONTH, 11)
            }.time

            monthCurrent = Calendar.getInstance().time

            // set the calendar view callbacks
            onMonthSelected = { date, view ->
                dateHeaderTextView?.text = capitalise(headerFormatter.format(date))
            }

            onDateRangeSelected = { firstDate, secondDate ->
                dateRangeTextView.text = rangeFormatter.format(firstDate) + " — " + rangeFormatter.format(secondDate)
            }
        }

        dateHeaderTextView?.text = headerFormatter.format(calendarView.monthCurrent)

        return rootView
    }

    fun capitalise(str : String) : String {
        return str.substring(0, 1).toUpperCase() + str.substring(1)
    }
}