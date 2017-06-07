package com.triggertrap.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.triggertrap.R;

public class BuyDongleFragment extends TriggertrapFragment {

	
	private static final String BUY_DONGLE_URL = "http://shop.triggertrap.com/?utm_source=TT-App&utm_medium=android-app&utm_campaign=app";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.buy_dongle, container, false);
		
		TextView header = (TextView) rootView.findViewById(R.id.buyHeader);
		TextView body = (TextView) rootView.findViewById(R.id.buyBody);
		TextView footer = (TextView) rootView.findViewById(R.id.buyFooter);
		Button buyDongle = (Button) rootView.findViewById(R.id.buyDongleButton);
	    
		header.setTypeface(SAN_SERIF_LIGHT);
	    body.setTypeface(SAN_SERIF_LIGHT);
	    footer.setTypeface(SAN_SERIF_LIGHT);
	    
	    buyDongle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 onClickBuyDongle(v);
			}
		});

		return rootView;
	}
	
	private void onClickBuyDongle(View v) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(BUY_DONGLE_URL));
		startActivity(i);
		
	}
}
