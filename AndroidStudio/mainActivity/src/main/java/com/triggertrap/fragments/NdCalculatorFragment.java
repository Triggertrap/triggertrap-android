package com.triggertrap.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.triggertrap.R;
import com.triggertrap.TTApp;
import com.triggertrap.view.ZeroTopPaddingTextView;
import com.triggertrap.widget.ArrayWheelDoubleAdapter;
import com.triggertrap.widget.TimerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelScrollListener;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;

/**
 * Fragment that handles the display ND Filter Calculator and associated
 * calculations.
 *
 * @author scottmellors
 * @since 2.2
 */
public class NdCalculatorFragment extends TriggertrapFragment {

    private List<String> mNdValues;
    private String[] mBaseShutterSpeedVals;
    private double[] mNdValueArray;
    private String[] mNdNameVals;

    private int mCurrentBaseShutterSpeed;
    private int mCurrentStops;

    private TextView mNdFilterTextView;
    private TextView mResultTextView;
    private ZeroTopPaddingTextView mResultTextLabel;

    private TimerView mTimedView;

    private static final double TWELFTH_OF_A_MS = 0.12;
    private static final double SIXTEENTH_OF_A_MS = 0.16;
    private static final double TWENTIETH_OF_A_MS = 0.2;
    private static final double QUARTER_OF_A_MS = 0.25;
    private static final double THIRTYONE_OF_A_MS = 0.31;
    private static final double FOURTY_OF_A_MS = 0.4;
    private static final double HALF_OF_A_MS = 0.5;
    private static final double SIXTYTHREE_OF_A_MS = 0.63;
    private static final double EIGHTY_OF_A_MS = 0.8;
    private static final double ONE_MS = 1;
    private static final double ONE_AND_QUART_OF_MS = 1.25;
    private static final double ONE_FIFTY_SIX_MS = 1.56;
    private static final double TWO_MS = 2;
    private static final double TWO_AND_HALF_MS = 2.5;
    private static final double THREE_THIRTEEN_MS = 3.13;
    private static final double FOUR_MS = 4;
    private static final double FIVE_MS = 5;
    private static final double SIX_AND_QUART_MS = 6.25;
    private static final double EIGHT_MS = 8;
    private static final double TEN_MS = 10;
    private static final double THIRTEEN_MS = 13;
    private static final double SEVENTEEN_MS = 17;
    private static final double TWENTY_MS = 20;
    private static final double TWENTY_FIVE_MS = 25;
    private static final double THIRTYTHREE_MS = 33;
    private static final double FOURTY_MS = 40;
    private static final double FIFTY_MS = 50;
    private static final double SIXTYSEVEN_MS = 67;
    private static final double SEVENTYSEVEN_MS = 77;
    private static final double HUNDRED_MS = 100;
    private static final double EIGTH_OF_A_SEC = 125;
    private static final double HUNDRED_SIXYSEVEN_MS = 167;
    private static final double TWO_HUNDRED_MS = 200;
    private static final double QUART_OF_A_SEC = 250;
    private static final double THREE_HUNDRED_MS = 300;
    private static final double FOUR_HUNDRED_MS = 400;
    private static final double HALF_OF_A_SEC = 500;
    private static final double SIX_HUNDRED_MS = 600;
    private static final double EIGHT_HUNDRED_MS = 800;
    private static final double ONE_SEC = 1000;
    private static final double THIRTEEN_HUNDRED_MS = 1300;
    private static final double SIXTEEN_HUNDRED_MS = 1600;
    private static final double TWO_SEC = 2000;
    private static final double TWENTYFIVE_HUNDRED_MS = 2500;
    private static final double THIRTYTWO_HUNDRED_MS = 3200;
    private static final double FOUR_SEC = 4000;
    private static final double FIVE_SEC = 5000;
    private static final double SIX_SEC = 6000;
    private static final double EIGHT_SEC = 8000;
    private static final double TEN_SEC = 10000;
    private static final double THIRTEEN_SEC = 13000;
    private static final double FIFTEEN_SEC = 15000;
    private static final double TWENTY_SEC = 20000;
    private static final double TWENTYFIVE_SEC = 25000;
    private static final double THIRTY_SEC = 30000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.nd_calc_mode, container,
                false);

        mNdValueArray = new double[]{TWELFTH_OF_A_MS, SIXTEENTH_OF_A_MS,
                TWENTIETH_OF_A_MS, QUARTER_OF_A_MS, THIRTYONE_OF_A_MS,
                FOURTY_OF_A_MS, HALF_OF_A_MS, SIXTYTHREE_OF_A_MS,
                EIGHTY_OF_A_MS, ONE_MS, ONE_AND_QUART_OF_MS, ONE_FIFTY_SIX_MS,
                TWO_MS, TWO_AND_HALF_MS, THREE_THIRTEEN_MS, FOUR_MS, FIVE_MS,
                SIX_AND_QUART_MS, EIGHT_MS, TEN_MS, THIRTEEN_MS, SEVENTEEN_MS,
                TWENTY_MS, TWENTY_FIVE_MS, THIRTYTHREE_MS, FOURTY_MS, FIFTY_MS,
                SIXTYSEVEN_MS, SEVENTYSEVEN_MS, HUNDRED_MS, EIGTH_OF_A_SEC,
                HUNDRED_SIXYSEVEN_MS, TWO_HUNDRED_MS, QUART_OF_A_SEC,
                THREE_HUNDRED_MS, FOUR_HUNDRED_MS, HALF_OF_A_SEC,
                SIX_HUNDRED_MS, EIGHT_HUNDRED_MS, ONE_SEC, THIRTEEN_HUNDRED_MS,
                SIXTEEN_HUNDRED_MS, TWO_SEC, TWENTYFIVE_HUNDRED_MS,
                THIRTYTWO_HUNDRED_MS, FOUR_SEC, FIVE_SEC, SIX_SEC, EIGHT_SEC,
                TEN_SEC, THIRTEEN_SEC, FIFTEEN_SEC, TWENTY_SEC, TWENTYFIVE_SEC,
                THIRTY_SEC};

        mNdFilterTextView = (TextView) rootView
                .findViewById(R.id.ndTFilterTypeText);

        mTimedView = (TimerView) rootView.findViewById(R.id.timerTimeText);
        mTimedView.drawAllBold();

        mResultTextView = (TextView) rootView.findViewById(R.id.resultTextView);
        mResultTextLabel = (ZeroTopPaddingTextView) rootView.findViewById(R.id.result_label);

        mNdValues = new ArrayList<String>();

        for (int i = 1; i < 21; i++) {
            mNdValues.add(getResources().getQuantityString(
                    R.plurals.numberOfStops, i, i));
        }

        mBaseShutterSpeedVals = getResources().getStringArray(
                R.array.base_shutter_speeds);
        mNdNameVals = getResources().getStringArray(R.array.nd_names);

        mNdFilterTextView.setText(getResources().getQuantityString(
                R.plurals.numberOfStops, 1, 1)
                + " ("
                + mNdNameVals[0]
                + ") "
                + getResources().getString(R.string.nd_filter_end));

        ArrayWheelDoubleAdapter ndValueAdapter = new ArrayWheelDoubleAdapter(
                getActivity(), mNdValues, mNdNameVals);

        View ndValueView = rootView.findViewById(R.id.nd_filter_picker);
        AbstractWheel ndValueWheel = (AbstractWheel) ndValueView
                .findViewById(R.id.wheelHorizontalView);
        ndValueAdapter.setItemResource(R.layout.wheel_double_text_centered);
        ndValueAdapter.setItemTextResource(R.id.text);
        ndValueWheel.setViewAdapter(ndValueAdapter);

        ndValueWheel.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            public void onScrollingFinished(AbstractWheel wheel) {
                mCurrentStops = wheel.getCurrentItem();

                mNdFilterTextView.setText(getResources().getQuantityString(
                        R.plurals.numberOfStops, wheel.getCurrentItem() + 1,
                        wheel.getCurrentItem() + 1)
                        + " ("
                        + mNdNameVals[wheel.getCurrentItem()]
                        + ") "
                        + getResources().getString(R.string.nd_filter_end));

                mResultTextView.setText(getNdTime(mCurrentStops,
                        mCurrentBaseShutterSpeed));
            }
        });

        ndValueWheel.setCurrentItem(0);

        ArrayWheelAdapter<String> shutterSpeedAdapter = new ArrayWheelAdapter<String>(
                getActivity(), mBaseShutterSpeedVals);

        View shutterSpeedView = rootView
                .findViewById(R.id.base_shutter_speed_picker);

        AbstractWheel shutterSpeedWheel = (AbstractWheel) shutterSpeedView
                .findViewById(R.id.wheelHorizontalView);
        shutterSpeedAdapter.setItemResource(R.layout.wheel_text_centered);
        shutterSpeedAdapter.setItemTextResource(R.id.text);
        TTApp app = TTApp.getInstance(getActivity());

        shutterSpeedWheel.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            public void onScrollingFinished(AbstractWheel wheel) {
                mCurrentBaseShutterSpeed = wheel.getCurrentItem();

                TTApp.getInstance(getActivity()).setDefaultShutterSpeedVal(
                        wheel.getCurrentItem());

                mResultTextView.setText(getNdTime(mCurrentStops,
                        mCurrentBaseShutterSpeed));
            }
        });

        shutterSpeedWheel.setViewAdapter(shutterSpeedAdapter);

        shutterSpeedWheel.setCurrentItem(app.getDefaultShutterSpeedVal());

        mCurrentBaseShutterSpeed = app.getDefaultShutterSpeedVal();

        mResultTextView.setText(getNdTime(0, mCurrentBaseShutterSpeed));

        return rootView;
    }

    /**
     * Function which takes two values based on the position of the wheel
     * pickers and calculates the ND filter modified time.
     *
     * @param stops        - currently selected stops value
     * @param shutterSpeed - currently selected shutterSpeed value
     * @return String output of calculated time value.
     */
    public String getNdTime(int stops, int shutterSpeed) {

        String output = "";

        long result = (long) (mNdValueArray[shutterSpeed] * (Math.pow(2,
                stops + 1)));

        if (result > 360000000) {

            mTimedView.setVisibility(View.GONE);
            mResultTextView.setVisibility(View.VISIBLE);
            mResultTextLabel.setVisibility(View.VISIBLE);
            mResultTextLabel.setText(getText(R.string.hours_label));

            output = String.valueOf(result / 3600000);


        } else if (result >= mNdValueArray[mNdValueArray.length - 1]) {

            mTimedView.setVisibility(View.VISIBLE);
            mResultTextView.setVisibility(View.GONE);
            mResultTextLabel.setVisibility(View.GONE);

            output = String.format(
                    Locale.getDefault(),
                    "%dh %02dm %02ds",
                    TimeUnit.MILLISECONDS.toHours(result),
                    TimeUnit.MILLISECONDS.toMinutes(result)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(result)),
                    TimeUnit.MILLISECONDS.toSeconds(result)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(result)));

            mTimedView.setTextInputTime(result);

        } else {

            mTimedView.setVisibility(View.GONE);
            mResultTextView.setVisibility(View.VISIBLE);

            double val = (mNdValueArray[shutterSpeed] * (Math.pow(2, stops + 1)));

            for (int i = shutterSpeed; val >= mNdValueArray[i]; i++) {

                if (val > mNdValueArray[i]) {
                    double lowerDif = (val - mNdValueArray[i]);
                    double higherDif = (mNdValueArray[i + 1] - val);

                    double min = Math.min(lowerDif, higherDif);

                    if (min == lowerDif) {
                        output = mBaseShutterSpeedVals[i];
                    } else if (min == higherDif) {
                        output = mBaseShutterSpeedVals[i + 1];
                    }

                    if (output.contains("\"") || output.contains("/")) {
                        mResultTextLabel.setVisibility(View.GONE);
                    } else {
                        mResultTextLabel.setText(getText(R.string.seconds_label));
                        mResultTextLabel.setVisibility(View.VISIBLE);
                    }

                }
            }
        }

        return output;
    }

}
