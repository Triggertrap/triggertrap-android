package com.triggertrap.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.triggertrap.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelScrollListener;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;

/**
 * Fragment that handles the display of the cable selector and navigates users to the store.
 *
 * @author scottmellors
 * @since 2.3
 */
public class CableSelectorFragment extends TriggertrapFragment {

    public static final String CABLE_CB1 = "CB1";
    public static final String CABLE_DC0 = "DC0";
    public static final String CABLE_DC1 = "DC1";
    public static final String CABLE_DC2 = "DC2";
    public static final String CABLE_E3 = "E3";
    public static final String CABLE_L1 = "L1";
    public static final String CABLE_N3 = "N3";
    public static final String CABLE_NX = "NX";
    public static final String CABLE_R9 = "R9";
    public static final String CABLE_S1 = "S1";
    public static final String CABLE_S2 = "S2";
    public static final String CABLE_UC1 = "UC1";

    private HashMap<String, LinkedHashMap<String, String>> mCableChooserData = new HashMap<String, LinkedHashMap<String, String>>();
    private Button mBuyCableBtn;
    private String[] mCurrentCameraArray;
    private ImageView mCableImageView;

    private HashMap<String, String> mStoreLinks = new HashMap<String, String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cable_chooser, container, false);

        final String[] manufacturers = getResources().getStringArray(R.array.tt_camera_manufacturers);

        mCableImageView = (ImageView) rootView.findViewById(R.id.cableImage);

        JSONObject obj;
        try {
            obj = new JSONObject(loadJSONFromAsset("StoreLinks.json"));

            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                while (keys.hasNext()) {
                    String cable = keys.next();
                    mStoreLinks.put(cable, obj.getString(cable));
                    keys.remove();
                }
            }

            obj = new JSONObject(loadJSONFromAsset("CameraChooser.json"));

            for (String manufacturer : manufacturers) {

                JSONObject tempObj = obj.getJSONObject(manufacturer);
                LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();

                SortedMap map = listFromJsonSorted(tempObj);

                map.entrySet();

                for (Object entry : map.entrySet()) {
                    Map.Entry<String, String> tempEntry = (Map.Entry<String, String>) entry;
                    String key = tempEntry.getKey();
                    String value = tempEntry.getValue();
                    temp.put(key, value);
                }

                mCableChooserData.put(manufacturer, temp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mBuyCableBtn = (Button) rootView.findViewById(R.id.buy_cable_btn);

        final AbstractWheel cameraManufacturers = (AbstractWheel) rootView.findViewById(R.id.manufacturerWheel);
        final AbstractWheel cameraModelWheel = (AbstractWheel) rootView.findViewById(R.id.cameraWheel);

        cameraManufacturers.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {
                ArrayWheelAdapter<String> cameraAdapter =
                        new ArrayWheelAdapter<String>(getActivity(), new String[]{"- - -"});
                cameraAdapter.setItemResource(R.layout.wheel_text_stretch);
                cameraAdapter.setItemTextResource(R.id.text);
                cameraModelWheel.setViewAdapter(cameraAdapter);
                cameraModelWheel.setCyclic(true);
            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {

                mCurrentCameraArray = getCamerasFor(cameraManufacturers.getCurrentItem());

                ArrayWheelAdapter<String> cameraAdapter =
                        new ArrayWheelAdapter<String>(getActivity(), mCurrentCameraArray);
                cameraAdapter.setItemResource(R.layout.wheel_text_stretch);
                cameraAdapter.setItemTextResource(R.id.text);
                cameraModelWheel.setViewAdapter(cameraAdapter);
                cameraModelWheel.setCyclic(true);
                cameraModelWheel.setCurrentItem(0);

                updateUIForCamera(cameraManufacturers.getCurrentItem(), 0);
            }
        });

        cameraModelWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {
                //hide camera
            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                //set image
                updateUIForCamera(cameraManufacturers.getCurrentItem(), cameraModelWheel.getCurrentItem());
            }
        });

        ArrayWheelAdapter<String> manufacturerAdapter =
                new ArrayWheelAdapter<String>(getActivity(), manufacturers);
        manufacturerAdapter.setItemResource(R.layout.wheel_text_stretch);
        manufacturerAdapter.setItemTextResource(R.id.text);
        cameraManufacturers.setViewAdapter(manufacturerAdapter);
        cameraManufacturers.setCyclic(true);


        ArrayWheelAdapter<String> cameraAdapter =
                new ArrayWheelAdapter<String>(getActivity(), getCamerasFor(cameraManufacturers.getCurrentItem()));
        cameraAdapter.setItemResource(R.layout.wheel_text_stretch);
        cameraAdapter.setItemTextResource(R.id.text);
        cameraModelWheel.setViewAdapter(cameraAdapter);
        cameraModelWheel.setCyclic(true);
        //Set initial state
        mCurrentCameraArray = getCamerasFor(0);
        updateUIForCamera(0, 0);

        return rootView;
    }


    public String[] getCamerasFor(int manufacturer) {

        final String[] manufacturers = getResources().getStringArray(R.array.tt_camera_manufacturers);

        HashMap<String, String> tempMap = mCableChooserData.get(manufacturers[manufacturer]);

        return tempMap.keySet().toArray(new String[tempMap.keySet().size()]);
    }

    public void updateUIForCamera(int manufacturer, int cameraModel) {
        final String[] manufacturers = getResources().getStringArray(R.array.tt_camera_manufacturers);
        HashMap<String, String> tempMap = mCableChooserData.get(manufacturers[manufacturer]);
        String cableType = tempMap.get(mCurrentCameraArray[cameraModel]);

        if (cableType != null) {
            if (cableType.equals(CABLE_CB1)) {
                setupButton(R.string.buy_cable_cb1, CABLE_CB1, R.drawable.cable_cb1);
            } else if (cableType.equals(CABLE_DC0)) {
                setupButton(R.string.buy_cable_dc0, CABLE_DC0, R.drawable.cable_dc0);
            } else if (cableType.equals(CABLE_DC1)) {
                setupButton(R.string.buy_cable_dc1, CABLE_DC1, R.drawable.cable_dc1);
            } else if (cableType.equals(CABLE_DC2)) {
                setupButton(R.string.buy_cable_dc2, CABLE_DC2, R.drawable.cable_dc2);
            } else if (cableType.equals(CABLE_E3)) {
                setupButton(R.string.buy_cable_e3, CABLE_E3, R.drawable.cable_e3);
            } else if (cableType.equals(CABLE_L1)) {
                setupButton(R.string.buy_cable_l1, CABLE_L1, R.drawable.cable_l1);
            } else if (cableType.equals(CABLE_N3)) {
                setupButton(R.string.buy_cable_n3, CABLE_N3, R.drawable.cable_n3);
            } else if (cableType.equals(CABLE_NX)) {
                setupButton(R.string.buy_cable_nx, CABLE_NX, R.drawable.cable_nx);
            } else if (cableType.equals(CABLE_R9)) {
                setupButton(R.string.buy_cable_r9, CABLE_R9, R.drawable.cable_r9);
            } else if (cableType.equals(CABLE_S1)) {
                setupButton(R.string.buy_cable_s1, CABLE_S1, R.drawable.cable_s1);
            } else if (cableType.equals(CABLE_S2)) {
                setupButton(R.string.buy_cable_s2, CABLE_S2, R.drawable.cable_s2);
            } else if (cableType.equals(CABLE_UC1)) {
                setupButton(R.string.buy_cable_uc1, CABLE_UC1, R.drawable.cable_uc1);
            }
        }
    }

    private void setupButton(int stringResource, final String storeLink, int drawable) {
        mBuyCableBtn.setText(getResources().getString(stringResource));
        mCableImageView.setImageDrawable(getResources().getDrawable(drawable));
        mBuyCableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(mStoreLinks.get(storeLink)));
                startActivity(i);
            }
        });
    }

    public String loadJSONFromAsset(String fileName) {
        String json;
        try {
            InputStream is = getActivity().getAssets().open(fileName);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    public static SortedMap listFromJsonSorted(JSONObject json) {
        if (json == null) return null;
        SortedMap map = new TreeMap();
        Iterator i = json.keys();
        while (i.hasNext()) {
            try {
                String key = i.next().toString();
                String j = json.getString(key);
                map.put(key, j);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return map;
    }
}
