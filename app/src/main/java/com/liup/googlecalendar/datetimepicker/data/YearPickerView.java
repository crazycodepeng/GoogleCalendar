package com.liup.googlecalendar.datetimepicker.data;

/**
 * Created by liupeng on 2016/11/16.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.liup.googlecalendar.R;
import com.liup.googlecalendar.datetimepicker.data.DatePickerDialog.OnDateChangedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a selectable list of years.
 */
public class YearPickerView extends ListView implements AdapterView.OnItemClickListener, OnDateChangedListener {
	private static final String TAG = "YearPickerView";
	private final DatePickerController mController;
	private YearAdapter mAdapter;
	private int mViewSize;
	private int mChildSize;
	private TextViewWithCircularIndicator mSelectedView;
	/**
	 * @param context
	 */
	public YearPickerView(Context context, DatePickerController controller) {
		super(context);
		mController = controller;
		mController.registerOnDateChangedListener(this);
		ViewGroup.LayoutParams frame = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		setLayoutParams(frame);
		Resources res = context.getResources();
		mViewSize = res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
		mChildSize = res.getDimensionPixelOffset(R.dimen.year_label_height);
		setVerticalFadingEdgeEnabled(true);
		setFadingEdgeLength(mChildSize / 3);
		init(context);
		setOnItemClickListener(this);
		setSelector(new StateListDrawable());
		setDividerHeight(0);
		onDateChanged();
	}
	private void init(Context context) {
		ArrayList<String> years = new ArrayList<String>();
		for (int year = mController.getMinYear(); year <= mController.getMaxYear(); year++) {
			years.add(String.format("%d", year));
		}
		mAdapter = new YearAdapter(context, R.layout.year_label_text_view, years);
		setAdapter(mAdapter);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mController.tryVibrate();
		TextViewWithCircularIndicator clickedView = (TextViewWithCircularIndicator) view;
		if (clickedView != null) {
			if (clickedView != mSelectedView) {
				if (mSelectedView != null) {
					mSelectedView.drawIndicator(false);
					mSelectedView.requestLayout();
				}
				clickedView.drawIndicator(true);
				clickedView.requestLayout();
				mSelectedView = clickedView;
			}
			mController.onYearSelected(getYearFromTextView(clickedView));
			mAdapter.notifyDataSetChanged();
		}
	}
	private static int getYearFromTextView(TextView view) {
		return Integer.parseInt(view.getText().toString());
	}
	private class YearAdapter extends ArrayAdapter<String> {
		public YearAdapter(Context context, int resource, List<String> objects) {
			super(context, resource, objects);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextViewWithCircularIndicator v = (TextViewWithCircularIndicator)
					super.getView(position, convertView, parent);
			v.requestLayout();
			int year = getYearFromTextView(v);
			boolean selected = mController.getSelectedDay().year == year;
			v.drawIndicator(selected);
			if (selected) {
				mSelectedView = v;
			}
			return v;
		}
	}
	public void postSetSelectionCentered(final int position) {
		postSetSelectionFromTop(position, mViewSize / 2 - mChildSize / 2);
	}
	public void postSetSelectionFromTop(final int position, final int offset) {
		post(new Runnable() {
			@Override
			public void run() {
				setSelectionFromTop(position, offset);
				requestLayout();
			}
		});
	}
	public int getFirstPositionOffset() {
		final View firstChild = getChildAt(0);
		if (firstChild == null) {
			return 0;
		}
		return firstChild.getTop();
	}
	@Override
	public void onDateChanged() {
		mAdapter.notifyDataSetChanged();
		postSetSelectionCentered(mController.getSelectedDay().year - mController.getMinYear());
	}
	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
			event.setFromIndex(0);
			event.setToIndex(0);
		}
	}
}