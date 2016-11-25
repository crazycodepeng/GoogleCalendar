package com.liup.googlecalendar.datetimepicker;

/**
 * Created by liupeng on 2016/11/16.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.TextView;

/**
 * Fake Button class, used so TextViews can announce themselves as Buttons, for accessibility.
 */
public class AccessibleTextView extends TextView {
	public AccessibleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(Button.class.getName());
	}
	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(Button.class.getName());
	}
}
