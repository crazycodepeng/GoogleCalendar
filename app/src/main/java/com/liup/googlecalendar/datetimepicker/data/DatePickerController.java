package com.liup.googlecalendar.datetimepicker.data;

import com.liup.googlecalendar.datetimepicker.data.DatePickerDialog.OnDateChangedListener;
import com.liup.googlecalendar.datetimepicker.data.MonthAdapter.CalendarDay;

import java.util.Calendar;


/**
 * Created by liupeng on 2016/11/16.
 */

/**
 * Controller class to communicate among the various components of the date picker dialog.
 */
public interface DatePickerController {
	void onYearSelected(int year);
	void onDayOfMonthSelected(int year, int month, int day);
	void registerOnDateChangedListener(OnDateChangedListener listener);
	void unregisterOnDateChangedListener(OnDateChangedListener listener);
	CalendarDay getSelectedDay();
	int getFirstDayOfWeek();
	int getMinYear();
	int getMaxYear();
	Calendar getMinDate();
	Calendar getMaxDate();
	void tryVibrate();
}