package jp.co.recruit_mp.android.lightcalendarview.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.co.recruit_mp.android.lightcalendarview.R

/**
 * Created by svyatozar on 22.02.17.
 */

class DateRangeCalendarDialog : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.calendar_dialog_layout, null)
    }
}