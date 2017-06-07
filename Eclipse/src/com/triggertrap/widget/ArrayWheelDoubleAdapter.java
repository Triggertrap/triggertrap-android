package com.triggertrap.widget;

import java.util.List;

import com.triggertrap.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import antistatic.spinnerwheel.adapters.AbstractWheelTextAdapter;

/**
 * AbstractWheelTextAdapter extension that shows two TextViews instead of the
 * standard one.
 * 
 * @author scottmellors
 * @since 2.2
 * 
 */
public class ArrayWheelDoubleAdapter extends AbstractWheelTextAdapter {

	// items
	private List<String> mItems;
	private String[] mSubitems;

	public ArrayWheelDoubleAdapter(Context context, List<String> mNdValues,
			String[] subs) {
		super(context);

		// setEmptyItemResource(TEXT_VIEW_ITEM_RESOURCE);
		this.mItems = mNdValues;
		this.mSubitems = subs;
	}

	@Override
	public int getItemsCount() {
		return mItems.size();
	}

	protected CharSequence getItemText(int index, String[] items) {
		return items[index];
	}

	protected CharSequence getItemText(int index, List<String> items) {
		return items.get(index);
	}

	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		if (index >= 0 && index < getItemsCount()) {
			if (convertView == null) {
				convertView = getView(R.layout.wheel_double_text_centered,
						parent);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.text1);
			if (textView != null) {
				CharSequence text = getItemText(index, mItems);

				if (text == null) {
					text = "";
				}
				textView.setText(text);
				configureTextView(textView);
			}

			TextView subTextView = (TextView) convertView
					.findViewById(R.id.text2);
			if (subTextView != null) {
				CharSequence text = getItemText(index, mSubitems);

				if (text == null) {
					text = "";
				}
				subTextView.setText(text);
				configureTextView(subTextView);
			}
			return convertView;
		}
		return null;
	}

	/**
	 * Loads view from resources.
	 * 
	 * @param resource
	 *            the resource Id
	 * @return the loaded view or null if resource is not set
	 */
	private View getView(int resource, ViewGroup parent) {
		switch (resource) {
		case NO_RESOURCE:
			return null;
		case TEXT_VIEW_ITEM_RESOURCE:
			return new TextView(context);
		default:
			return inflater.inflate(resource, parent, false);
		}
	}

	@Override
	protected CharSequence getItemText(int index) {
		return mItems.get(index);
	}
}
