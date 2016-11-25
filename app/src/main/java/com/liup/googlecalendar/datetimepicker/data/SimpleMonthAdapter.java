package com.liup.googlecalendar.datetimepicker.data;

/**
 * Created by liupeng on 2016/11/16.
 */

import android.content.Context;

/**
 * An adapter for a list of {@link SimpleMonthView} items.
 */
public class SimpleMonthAdapter extends MonthAdapter {
	public SimpleMonthAdapter(Context context, DatePickerController controller) {
		super(context, controller);
	}
	@Override
	public MonthView createMonthView(Context context) {
		final MonthView monthView = new SimpleMonthView(context);
		monthView.setDatePickerController(mController);
		return monthView;
	}
}