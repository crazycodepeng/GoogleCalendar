package com.liup.googlecalendar.common.contacts;

/**
 * Created by liupeng on 2016/11/16.
 */

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.liup.googlecalendar.common.widget.CompositeCursorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for email address autocomplete adapters. It uses
 * {@link Email#CONTENT_FILTER_URI} to search for data rows by email address
 * and/or contact name. It also searches registered {@link Directory}'s.
 */
public abstract class BaseEmailAddressAdapter extends CompositeCursorAdapter implements Filterable {
	private static final String TAG = "BaseEmailAddressAdapter";
	/**
	 * Model object for a {@link Directory} row. There is a partition in the
	 * {@link CompositeCursorAdapter} for every directory (except
	 * {@link Directory#LOCAL_INVISIBLE}.
	 */
	public final static class DirectoryPartition extends CompositeCursorAdapter.Partition {
		public long directoryId;
		public String directoryType;
		public String displayName;
		public String accountName;
		public String accountType;
		public boolean loading;
		public CharSequence constraint;
		public DirectoryPartitionFilter filter;
		public DirectoryPartition() {
			super(false, false);
		}
	}
	private static class EmailQuery {
		public static final String[] PROJECTION = {
				Contacts.DISPLAY_NAME,  // 0
				Email.ADDRESS           // 1
		};
		public static final int NAME = 0;
		public static final int ADDRESS = 1;
	}
	private static class DirectoryListQuery {
		public static final String[] PROJECTION = {
				Directory._ID,              // 0
				Directory.ACCOUNT_NAME,     // 1
				Directory.ACCOUNT_TYPE,     // 2
				Directory.DISPLAY_NAME,     // 3
				Directory.PACKAGE_NAME,     // 4
				Directory.TYPE_RESOURCE_ID, // 5
		};
		public static final int ID = 0;
		public static final int ACCOUNT_NAME = 1;
		public static final int ACCOUNT_TYPE = 2;
		public static final int DISPLAY_NAME = 3;
		public static final int PACKAGE_NAME = 4;
		public static final int TYPE_RESOURCE_ID = 5;
	}
	/**
	 * A fake column name that indicates a "Searching..." item in the list.
	 */
	private static final String SEARCHING_CURSOR_MARKER = "searching";
	/**
	 * An asynchronous filter used for loading two data sets: email rows from the local
	 * contact provider and the list of {@link Directory}'s.
	 */
	private final class DefaultPartitionFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			Cursor directoryCursor = null;
			if (!mDirectoriesLoaded) {
				directoryCursor = mContentResolver.query(
						Directory.CONTENT_URI, DirectoryListQuery.PROJECTION, null, null, null);
				mDirectoriesLoaded = true;
			}
			FilterResults results = new FilterResults();
			Cursor cursor = null;
			if (!TextUtils.isEmpty(constraint)) {
				Uri uri = Uri.withAppendedPath(
						Email.CONTENT_FILTER_URI, Uri.encode(constraint.toString()));
				cursor = mContentResolver.query(uri, EmailQuery.PROJECTION, null, null, null);
				results.count = cursor.getCount();
			}
			results.values = new Cursor[] { directoryCursor, cursor };
			return results;
		}
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results.values != null) {
				Cursor[] cursors = (Cursor[]) results.values;
				onDirectoryLoadFinished(constraint, cursors[0], cursors[1]);
			}
			results.count = getCount();
		}
		@Override
		public CharSequence convertResultToString(Object resultValue) {
			return makeDisplayString((Cursor) resultValue);
		}
	}
	/**
	 * An asynchronous filter that performs search in a particular directory.
	 */
	private final class DirectoryPartitionFilter extends Filter {
		private final int mPartitionIndex;
		private final long mDirectoryId;
		public DirectoryPartitionFilter(int partitionIndex, long directoryId) {
			this.mPartitionIndex = partitionIndex;
			this.mDirectoryId = directoryId;
		}
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			if (!TextUtils.isEmpty(constraint)) {
				Uri uri = Email.CONTENT_FILTER_URI.buildUpon()
						.appendPath(constraint.toString())
						.appendQueryParameter(
								ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(mDirectoryId))
						.build();
				Cursor cursor = mContentResolver.query(
						uri, EmailQuery.PROJECTION, null, null, null);
				results.values = cursor;
			}
			return results;
		}
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			Cursor cursor = (Cursor) results.values;
			onPartitionLoadFinished(constraint, mPartitionIndex, cursor);
			results.count = getCount();
		}
	}
	protected final ContentResolver mContentResolver;
	private boolean mDirectoriesLoaded;
	private Account mAccount;
	public BaseEmailAddressAdapter(Context context) {
		super(context);
		mContentResolver = context.getContentResolver();
	}
	/**
	 * Set the account when known. Causes the search to prioritize contacts from
	 * that account.
	 */
	public void setAccount(Account account) {
		mAccount = account;
	}
	/**
	 * Override to create a view for line item in the autocomplete suggestion list UI.
	 */
	protected abstract View inflateItemView(ViewGroup parent);
	/**
	 * Override to populate the autocomplete suggestion line item UI with data.
	 */
	protected abstract void bindView(View view, String directoryType, String directoryName,
									 String displayName, String emailAddress);
	/**
	 * Override to create a view for a "Searching directory" line item, which is
	 * displayed temporarily while the corresponding filter is running.
	 */
	protected abstract View inflateItemViewLoading(ViewGroup parent);
	/**
	 * Override to populate the "Searching directory" line item UI with data.
	 */
	protected abstract void bindViewLoading(View view, String directoryType, String directoryName);
	@Override
	protected int getItemViewType(int partitionIndex, int position) {
		DirectoryPartition partition = (DirectoryPartition)getPartition(partitionIndex);
		return partition.loading ? 1 : 0;
	}
	@Override
	protected View newView(Context context, int partitionIndex, Cursor cursor,
						   int position, ViewGroup parent) {
		DirectoryPartition partition = (DirectoryPartition)getPartition(partitionIndex);
		if (partition.loading) {
			return inflateItemViewLoading(parent);
		} else {
			return inflateItemView(parent);
		}
	}
	@Override
	protected void bindView(View v, int partition, Cursor cursor, int position) {
		DirectoryPartition directoryPartition = (DirectoryPartition)getPartition(partition);
		String directoryType = directoryPartition.directoryType;
		String directoryName = directoryPartition.displayName;
		if (directoryPartition.loading) {
			bindViewLoading(v, directoryType, directoryName);
		} else {
			String displayName = cursor.getString(EmailQuery.NAME);
			String emailAddress = cursor.getString(EmailQuery.ADDRESS);
			bindView(v, directoryType, directoryName, displayName, emailAddress);
		}
	}
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	@Override
	protected boolean isEnabled(int partitionIndex, int position) {
		// The "Searching..." item should not be selectable
		return !((DirectoryPartition)getPartition(partitionIndex)).loading;
	}
	@Override
	public Filter getFilter() {
		return new DefaultPartitionFilter();
	}
	/**
	 * Handles the result of the initial call, which brings back the list of
	 * directories as well as the search results for the local directories.
	 */
	protected void onDirectoryLoadFinished(
			CharSequence constraint, Cursor directoryCursor, Cursor defaultPartitionCursor) {
		if (directoryCursor != null) {
			PackageManager packageManager = getContext().getPackageManager();
			DirectoryPartition preferredDirectory = null;
			List<DirectoryPartition> directories = new ArrayList<DirectoryPartition>();
			while (directoryCursor.moveToNext()) {
				long id = directoryCursor.getLong(DirectoryListQuery.ID);
				// Skip the local invisible directory, because the default directory
				// already includes all local results.
				if (id == Directory.LOCAL_INVISIBLE) {
					continue;
				}
				DirectoryPartition partition = new DirectoryPartition();
				partition.directoryId = id;
				partition.displayName = directoryCursor.getString(DirectoryListQuery.DISPLAY_NAME);
				partition.accountName = directoryCursor.getString(DirectoryListQuery.ACCOUNT_NAME);
				partition.accountType = directoryCursor.getString(DirectoryListQuery.ACCOUNT_TYPE);
				String packageName = directoryCursor.getString(DirectoryListQuery.PACKAGE_NAME);
				int resourceId = directoryCursor.getInt(DirectoryListQuery.TYPE_RESOURCE_ID);
				if (packageName != null && resourceId != 0) {
					try {
						Resources resources =
								packageManager.getResourcesForApplication(packageName);
						partition.directoryType = resources.getString(resourceId);
						if (partition.directoryType == null) {
							Log.e(TAG, "Cannot resolve directory name: "
									+ resourceId + "@" + packageName);
						}
					} catch (NameNotFoundException e) {
						Log.e(TAG, "Cannot resolve directory name: "
								+ resourceId + "@" + packageName, e);
					}
				}
				// If an account has been provided and we found a directory that
				// corresponds to that account, place that directory second, directly
				// underneath the local contacts.
				if (mAccount != null && mAccount.name.equals(partition.accountName) &&
						mAccount.type.equals(partition.accountType)) {
					preferredDirectory = partition;
				} else {
					directories.add(partition);
				}
			}
			if (preferredDirectory != null) {
				directories.add(1, preferredDirectory);
			}
			for (DirectoryPartition partition : directories) {
				addPartition(partition);
			}
		}
		// The filter has loaded results for the default partition too.
		if (defaultPartitionCursor != null && getPartitionCount() > 0) {
			changeCursor(0, defaultPartitionCursor);
		}
		// Start search in other directories
		int count = getPartitionCount();
		// Note: skipping the default partition (index 0), which has already been loaded
		for (int i = 1; i < count; i++) {
			DirectoryPartition partition = (DirectoryPartition) getPartition(i);
			partition.constraint = constraint;
			if (!partition.loading) {
				partition.loading = true;
				changeCursor(i, createLoadingCursor());
			}
			if (partition.filter == null) {
				partition.filter = new DirectoryPartitionFilter(i, partition.directoryId);
			}
			partition.filter.filter(constraint);
		}
	}
	/**
	 * Creates a dummy cursor to represent the "Searching directory..." item.
	 */
	private Cursor createLoadingCursor() {
		MatrixCursor cursor = new MatrixCursor(new String[]{SEARCHING_CURSOR_MARKER});
		cursor.addRow(new Object[]{""});
		return cursor;
	}
	public void onPartitionLoadFinished(
			CharSequence constraint, int partitionIndex, Cursor cursor) {
		if (partitionIndex < getPartitionCount()) {
			DirectoryPartition partition = (DirectoryPartition) getPartition(partitionIndex);
			// Check if the received result matches the current constraint
			// If not - the user must have continued typing after the request
			// was issued
			if (TextUtils.equals(constraint, partition.constraint)) {
				partition.loading = false;
				changeCursor(partitionIndex, cursor);
			} else {
				// We got the result for an unexpected query (the user is still typing)
				// Just ignore this result
				if (cursor != null) {
					cursor.close();
				}
			}
		} else if (cursor != null) {
			cursor.close();
		}
	}
	private final String makeDisplayString(Cursor cursor) {
		if (cursor.getColumnName(0).equals(SEARCHING_CURSOR_MARKER)) {
			return "";
		}
		String name = cursor.getString(EmailQuery.NAME);
		String address = cursor.getString(EmailQuery.ADDRESS);
		return new Rfc822Token(name, address, null).toString();
	}
}
