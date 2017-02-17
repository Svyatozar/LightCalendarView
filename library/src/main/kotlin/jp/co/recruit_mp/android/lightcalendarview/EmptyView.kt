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
import android.graphics.Canvas
import jp.co.recruit_mp.android.lightcalendarview.views.CapView

/**
 * Created by masayuki-recruit on 8/23/16.
 */
class EmptyView(context: Context, settings: CalendarSettings) : CapView(context, settings) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun toString(): String = "EmptyView"
}
