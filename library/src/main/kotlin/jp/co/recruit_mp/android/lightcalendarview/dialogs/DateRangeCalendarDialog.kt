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
    val headerFormatter = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
    val rangeFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    var date1: Date? = null
    var date2: Date? = null

    internal var callback: ((firstDate: Date, secondDate: Date) -> Unit)? = null

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
                set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 3)
                set(Calendar.MONTH, 0)
            }.time

            //monthFrom = Calendar.getInstance().apply { set(Calendar.YEAR, 2014) }.time//Calendar.getInstance().apply { set(Calendar.MONTH, 0) }.time
            monthTo = Calendar.getInstance().apply {
                set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1)
                set(Calendar.MONTH, 11)
            }.time

            monthCurrent = Calendar.getInstance().time

            // set the calendar view callbacks
            onMonthSelected = { date, view ->
                dateHeaderTextView?.text = capitalise(headerFormatter.format(date))
            }

            onDateRangeSelected = { firstDate, secondDate ->
                date1 = firstDate
                date2 = secondDate

                if (firstDate != null && secondDate != null) {
                    dateRangeTextView.text = rangeFormatter.format(firstDate) + " — " + rangeFormatter.format(secondDate)
                } else {
                    dateRangeTextView.text = ""
                }
            }
        }

        dateHeaderTextView?.text = headerFormatter.format(calendarView.monthCurrent)
        rootView.findViewById(R.id.buttonOk).setOnClickListener {
            if ((date1 != null) && (date2 != null)) {
                callback?.invoke(date1!!, date2!!)
            }
            dismiss()
        }
        rootView.findViewById(R.id.buttonCancel).setOnClickListener { dismiss() }

        return rootView
    }

    fun setCallback(callback: ((firstDate: Date, secondDate: Date) -> Unit)?): DateRangeCalendarDialog {
        this.callback = callback
        return this
    }

    fun capitalise(str : String) : String {
        return str.substring(0, 1).toUpperCase() + str.substring(1)
    }
}