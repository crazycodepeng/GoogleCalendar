package com.liup.googlecalendar.ex.chips;

/**
 * Created by liupeng on 2016/11/16.
 */

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

/**
 * RecipientChip defines an ImageSpan that contains information relevant to a
 * particular recipient.
 */
/* package */class RecipientChip extends ImageSpan {
	private final CharSequence mDisplay;
	private final CharSequence mValue;
	private final long mContactId;
	private final long mDataId;
	private RecipientEntry mEntry;
	private boolean mSelected = false;
	private CharSequence mOriginalText;
	public RecipientChip(Drawable drawable, RecipientEntry entry, int offset) {
		super(drawable, DynamicDrawableSpan.ALIGN_BOTTOM);
		mDisplay = entry.getDisplayName();
		mValue = entry.getDestination().trim();
		mContactId = entry.getContactId();
		mDataId = entry.getDataId();
		mEntry = entry;
	}
	/**
	 * Set the selected state of the chip.
	 * @param selected
	 */
	public void setSelected(boolean selected) {
		mSelected = selected;
	}
	/**
	 * Return true if the chip is selected.
	 */
	public boolean isSelected() {
		return mSelected;
	}
	/**
	 * Get the text displayed in the chip.
	 */
	public CharSequence getDisplay() {
		return mDisplay;
	}
	/**
	 * Get the text value this chip represents.
	 */
	public CharSequence getValue() {
		return mValue;
	}
	/**
	 * Get the id of the contact associated with this chip.
	 */
	public long getContactId() {
		return mContactId;
	}
	/**
	 * Get the id of the data associated with this chip.
	 */
	public long getDataId() {
		return mDataId;
	}
	/**
	 * Get associated RecipientEntry.
	 */
	public RecipientEntry getEntry() {
		return mEntry;
	}
	public void setOriginalText(String text) {
		if (!TextUtils.isEmpty(text)) {
			text = text.trim();
		}
		mOriginalText = text;
	}
	public CharSequence getOriginalText() {
		return !TextUtils.isEmpty(mOriginalText) ? mOriginalText : mEntry.getDestination();
	}
}