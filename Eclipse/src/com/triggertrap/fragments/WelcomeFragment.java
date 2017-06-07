package com.triggertrap.fragments;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.triggertrap.R;

public class WelcomeFragment extends TriggertrapFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.welcome, container, false);
		
		TextView header = (TextView) rootView.findViewById(R.id.welcomeHeader);
//		TextView body = (TextView) rootView.findViewById(R.id.buyBody);
//		TextView footer = (TextView) rootView.findViewById(R.id.buyFooter);
		Button welcome = (Button) rootView.findViewById(R.id.welcomeButton);
	    
		header.setTypeface(SAN_SERIF_LIGHT);
//	    body.setTypeface(SAN_SERIF_LIGHT);
//	    footer.setTypeface(SAN_SERIF_LIGHT);
	    
	    welcome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 onClickWelcome(v);
			}
		});

		return rootView;
	}
	
	private void onClickWelcome(View v) {
		 FragmentManager fm = getActivity().getFragmentManager();
         fm.popBackStack();
	}
}
