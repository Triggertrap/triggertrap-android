package com.triggertrap.fragments.handler;

import java.util.ArrayList;

import com.triggertrap.R;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;

public class DrawerFragmentHandler {

   
    private final FragmentManager mManager;
    private final int mContainerId;
    private final ArrayList<DrawContentInfo> mDrawerPanes = new ArrayList<DrawContentInfo>();
    private DrawContentInfo mLastDrawerPane;
    private boolean mInitialized;
    private String mCurrentDrawerTag;
    
    static final class DrawContentInfo {
        private final String tag;
        private final Class<?> clss;
        private final Bundle args;
        private Fragment fragment;

        DrawContentInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }
    }    
    
    public DrawerFragmentHandler(FragmentManager manager, int containerId) {      
        mManager = manager;
        mContainerId = containerId;
        mInitialized = true;
    }
    
    public String getCurrentFragmentTag() {
    	return mCurrentDrawerTag;
    }
    
    public void addDrawerPane(String drawerTag, Class<?> clss, Bundle args) {
        String tag = drawerTag;
        DrawContentInfo info = new DrawContentInfo(tag, clss, args);
        mDrawerPanes.add(info);
    }
    
    public void onDrawerSelected(Context context,String tag, boolean playAnimation) {
        if (!mInitialized) {
            return;
        }
        FragmentTransaction ft = doDrawerChanged(context,tag, null, playAnimation);
//        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        ft.setCustomAnimations(R.animator.fragment_fade_in, R.animator.fragment_slide_out);
     
        if (ft != null) {
            ft.commit();
            mManager.executePendingTransactions();
        }
    }
    
    private FragmentTransaction doDrawerChanged(Context context, String tag, FragmentTransaction ft, boolean playAnimation) {
    	DrawContentInfo newDrawerPane = null;
        for (int i=0; i< mDrawerPanes.size(); i++) {
        	DrawContentInfo drawerPaner = mDrawerPanes.get(i);
            if (drawerPaner.tag.equals(tag)) {
            	newDrawerPane = drawerPaner;
            	break;
            }
        }
        if (newDrawerPane == null) {
            throw new IllegalStateException("No Drawer Fragment known for tag " + tag);
        }
        if (mLastDrawerPane != newDrawerPane) {
            if (ft == null) {
                ft = mManager.beginTransaction();
            }
            if (playAnimation) {
            	ft.setCustomAnimations(R.animator.fragment_fade_in, R.animator.fragment_slide_out);
            }
            if (mLastDrawerPane != null) {
                if (mLastDrawerPane.fragment != null) {
                    ft.detach(mLastDrawerPane.fragment);
                }
            }
            if (newDrawerPane != null) {
                if (newDrawerPane.fragment == null) {
                	newDrawerPane.fragment = Fragment.instantiate(context,
                			newDrawerPane.clss.getName(), newDrawerPane.args);
                    ft.add(mContainerId, newDrawerPane.fragment, newDrawerPane.tag);
                } else {
                    ft.attach(newDrawerPane.fragment);
                }
            }

            mLastDrawerPane = newDrawerPane;
            mCurrentDrawerTag = tag;
        }
        return ft;
    }
    
    public void addBackstackFragment(Context context, Class<?> clss) {
    	Fragment fragment = Fragment.instantiate(context,
    			clss.getName(), null);
    	FragmentTransaction ft = mManager.beginTransaction();
    	ft.replace(mContainerId, fragment);
    	ft.addToBackStack(null);
    	ft.commit();
    }
    
    public boolean setFragmentArgs(String tag, Bundle args) {
    	boolean setArgs = false;
    	
    	return setArgs;
    }
}
