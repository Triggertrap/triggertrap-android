package com.triggertrap.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class WifiPagerAdapter extends FragmentPagerAdapter {

	private String[] mTitles;
	
	public WifiPagerAdapter(FragmentManager fm, String [] titles) {
		super(fm);
		mTitles = titles;
		
	}

	@Override
	public CharSequence getPageTitle(int position) {		
		return mTitles[position];
	}
	
	@Override
	public Fragment getItem(int position) {	
		return null;
	}

	@Override
	public int getCount() {
		return mTitles.length;
	}

}
