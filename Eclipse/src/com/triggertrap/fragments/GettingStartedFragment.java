package com.triggertrap.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.triggertrap.R;
import com.triggertrap.adapter.GettingStartedAdapter;
import com.viewpagerindicator.LinePageIndicator;

public class GettingStartedFragment extends TriggertrapFragment {

	private static final String TAG = GettingStartedFragment.class.getSimpleName();
	private static final int PAGER_INTERVAL = 5000;

	private ViewPager mPager;
	private LinePageIndicator mIndicator; 
	private GettingStartedAdapter mAdapter;
	
	private int mCurrentPage = 0;
	private Handler mHandler = new Handler();
	
	private Runnable timerTask = new Runnable() {
		public void run() {
			
			mCurrentPage++;
			if (mCurrentPage >= mAdapter.getCount()) {
				mCurrentPage = 0;
			}
			mPager.setCurrentItem(mCurrentPage, true);
			mHandler.postDelayed(timerTask, PAGER_INTERVAL);
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new GettingStartedAdapter(getActivity());
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View rootView = inflater.inflate(R.layout.getting_started, container,
				false);

		mPager = (ViewPager) rootView.findViewById(R.id.pager);
		mIndicator = (LinePageIndicator)  rootView.findViewById(R.id.indicator);
		
		mPager.setAdapter(mAdapter);	
		mIndicator.setViewPager(mPager);
		
		mHandler.removeCallbacks(timerTask);
		mHandler.postDelayed(timerTask,PAGER_INTERVAL);
		
		mPager.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:	
					//Log.d(TAG,"Ontouch down");
					mHandler.removeCallbacks(timerTask);
					break;
				case MotionEvent.ACTION_MOVE:			
					break;
				case MotionEvent.ACTION_UP:		
					//Log.d(TAG,"Ontouch up");
					mCurrentPage = mPager.getCurrentItem();
					mHandler.removeCallbacks(timerTask);
					mHandler.postDelayed(timerTask,PAGER_INTERVAL);
					break;
				case MotionEvent.ACTION_CANCEL:
					break;
				}
				return false;
			}
		});
		
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		//Bit of a hack but helps pre-load views to
		//get a smooth scroll on the first showing.
		mPager.setCurrentItem(3);
		mPager.setCurrentItem(2);
		mPager.setCurrentItem(1);
		mPager.setCurrentItem(0);	
		mCurrentPage = 0;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(timerTask);
	}
}
