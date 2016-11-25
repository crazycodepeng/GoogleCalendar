package com.liup.googlecalendar.datetimepicker.data;

/**
 * Created by liupeng on 2016/11/16.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.liup.googlecalendar.R;

/**
 * A text view which, when pressed or activated, displays a blue circle around the text.
 */
public class TextViewWithCircularIndicator extends TextView {
	private static final int SELECTED_CIRCLE_ALPHA = 60;
	Paint mCirclePaint = new Paint();
	private final int mRadius;
	private final int mCircleColor;
	private final String mItemIsSelectedText;
	private boolean mDrawCircle;
	public TextViewWithCircularIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		Resources res = context.getResources();
		mCircleColor = res.getColor(R.color.blue);
		mRadius = res.getDimensionPixelOffset(R.dimen.month_select_circle_radius);
		mItemIsSelectedText = context.getResources().getString(R.string.item_is_selected);
		init();
	}
	private void init() {
		mCirclePaint.setFakeBoldText(true);
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setColor(mCircleColor);
		mCirclePaint.setTextAlign(Paint.Align.CENTER);
		mCirclePaint.setStyle(Paint.Style.FILL);
		mCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
	}
	public void drawIndicator(boolean drawCircle) {
		mDrawCircle = drawCircle;
	}
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mDrawCircle) {
			final int width = getWidth();
			final int height = getHeight();
			int radius = Math.min(width, height) / 2;
			canvas.drawCircle(width / 2, height / 2, radius, mCirclePaint);
		}
	}
	@Override
	public CharSequence getContentDescription() {
		CharSequence itemText = getText();
		if (mDrawCircle) {
			return String.format(mItemIsSelectedText, itemText);
		} else {
			return itemText;
		}
	}
}