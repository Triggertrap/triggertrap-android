package com.triggertrap.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.activities.MainActivity;

public class GettingStartedAdapter extends PagerAdapter {

	private static final String TAG = GettingStartedAdapter.class.getSimpleName();
	
	private static final int PAGE_COUNT = 4;
	private LayoutInflater mInflator;
	private Typeface SAN_SERIF_LIGHT = null;
	private String[] titles;
	private String[] headers;
	private String[] bodies;
	private String[] footers;
	private Context mContext;
	
	public GettingStartedAdapter(Context ctx) {
		mContext = ctx;
		mInflator= LayoutInflater.from(ctx);
		SAN_SERIF_LIGHT = Typeface.createFromAsset(ctx.getAssets(),
	             "fonts/Roboto-Light.ttf");
		
		titles = ctx.getResources().getStringArray(R.array.getting_started_titles);
		headers = ctx.getResources().getStringArray(R.array.getting_started_headers);
		bodies = ctx.getResources().getStringArray(R.array.getting_started_body);
		footers = ctx.getResources().getStringArray(R.array.getting_started_footer);
		
		
	}

	@Override
	public Object instantiateItem(View collection, int position) {
	   

	    View layout = mInflator.inflate(R.layout.getting_started_content, null);

	    TextView title = (TextView) layout.findViewById(R.id.gettingStartedTitle);
	    TextView header = (TextView) layout.findViewById(R.id.gettingStartedHeader);
	    TextView body = (TextView) layout.findViewById(R.id.gettingStartedBody);
	    TextView footer = (TextView) layout.findViewById(R.id.gettingStartedFooter);
	    Button button =  (Button) layout.findViewById(R.id.gettingStartedButton);
	    if(position != 3) {
	    	button.setVisibility(View.GONE);
	    } else {
	    	button.setVisibility(View.VISIBLE);
	    	button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MainActivity mainActivity = (MainActivity) mContext;
					mainActivity.openDrawer();
				}
			});
	    }
	    
	    title.setText(titles[position]);
	    header.setText(headers[position]);
	    body.setText(bodies[position]);
	    footer.setText(footers[position]);
	    
	    
	    header.setTypeface(SAN_SERIF_LIGHT);
	    body.setTypeface(SAN_SERIF_LIGHT);
	    footer.setTypeface(SAN_SERIF_LIGHT);
	    
	    ((ViewPager) collection).addView(layout);

	    return layout;
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
	     ((ViewPager) collection).removeView((View) view);
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
	    return view == object;
	}
	


	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

}
