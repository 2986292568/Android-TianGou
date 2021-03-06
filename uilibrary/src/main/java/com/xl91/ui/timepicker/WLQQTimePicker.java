package com.xl91.ui.timepicker;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xl91.ui.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * 时间选择器
 *
 * github：得重新查找...
 *
 */
public class WLQQTimePicker extends LinearLayout {
    public static final String PICKED_TIME_EXT = "picked_time";
    private static final int UPDATE_TITLE_MSG = 0x111;
    private static final int UPDATE_WHEEL = 0x112;
    private static final int UPDATE_UpdateDay_MSG = 0x113;
    private final int START_YEAR = 2013;
    private final int END_YEAR = 2100;
    private TextView mPickerTitle;
    private WheelView mWheelYear;
    private WheelView mWheelMonth;
    private WheelView mWheelDay;
    private WheelView mWheelHour;
    private TextView mCancelBtn;
    private TextView mConfirmBtn;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;

    // 回调接口
    private OnClickTimePickerListener onClickTimePickerListener;

    private Calendar mCalendar;
    private int mDefaultDayWhellIndex = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_TITLE_MSG: {
                    updateTitle();
                }
                break;
                case UPDATE_WHEEL: {
                    updateWheel();
                }
                break;
                case UPDATE_UpdateDay_MSG: {
                    updateDay(mMonth);
                }
                break;
            }

        }
    };
    private WheelView.OnSelectListener mYearListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int year, String text) {
            mYear = START_YEAR + year;
            mHandler.sendEmptyMessage(UPDATE_TITLE_MSG);
        }

        @Override
        public void selecting(int id, String text) {
        }
    };

    private WheelView.OnSelectListener mMonthListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int month, String text) {
            mMonth = month;
            mHandler.sendEmptyMessage(UPDATE_TITLE_MSG);
            mHandler.sendEmptyMessage(UPDATE_UpdateDay_MSG);
        }

        @Override
        public void selecting(int id, String text) {
        }
    };

    private WheelView.OnSelectListener mDayListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int day, String text) {
            mDay = day + 1;
            mHandler.sendEmptyMessage(UPDATE_TITLE_MSG);
        }

        @Override
        public void selecting(int day, String text) {
        }
    };
    private WheelView.OnSelectListener mHourListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int hour, String text) {
            mHour = hour;
            mHandler.sendEmptyMessage(UPDATE_TITLE_MSG);
        }

        @Override
        public void selecting(int day, String text) {
        }
    };
    private Activity mContext;

    public WLQQTimePicker(Context context) {
        this(context, null);
    }

    public WLQQTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContext = (Activity) getContext();
        LayoutInflater.from(mContext).inflate(R.layout.time_picker, this);
        mPickerTitle = (TextView) findViewById(R.id.picker_title);
        mWheelYear = (WheelView) findViewById(R.id.year);
        mWheelMonth = (WheelView) findViewById(R.id.month);
        mWheelDay = (WheelView) findViewById(R.id.day);
        mWheelHour = (WheelView) findViewById(R.id.hour);
        mCancelBtn = (TextView) findViewById(R.id.cancel);
        mConfirmBtn = (TextView) findViewById(R.id.confirm);
        mWheelYear.setOnSelectListener(mYearListener);
        mWheelMonth.setOnSelectListener(mMonthListener);
        mWheelDay.setOnSelectListener(mDayListener);
        mWheelHour.setOnSelectListener(mHourListener);
        mConfirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = mContext.getString(R.string.picker_title, mYear, mMonth + 1, mDay, mHour);

                // 自己添加转换时间戳
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                String dateCopy = mYear + "-" + (mMonth + 1) + "-" + mDay + " " + mHour + ":00:00";
                try {
                    long unixTime = simpleDateFormat.parse(dateCopy).getTime();
                    Log.d(WLQQTimePicker.class.getSimpleName(), String.valueOf(unixTime));

                    /**
                     * 点击确定重新设置日期
                     */
                    mCalendar.set(Calendar.YEAR, mYear);
                    mCalendar.set(Calendar.MONTH, mMonth);
                    mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
                    mCalendar.set(Calendar.HOUR_OF_DAY, mHour);

                    // 回调接口，把数据回传
                    if (onClickTimePickerListener != null) {
                        onClickTimePickerListener.onClickTimePicker(v, date, unixTime);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 回调接口，把数据回传
                if (onClickTimePickerListener != null) {
                    onClickTimePickerListener.onClickTimePicker(v, "", 0);
                }
            }
        });
    }

    private void updateDay(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        mWheelDay.resetData(getDayData(maxDay));
        if (mDay > maxDay) {
            mWheelDay.setDefault(mDefaultDayWhellIndex);
            mDay = mDefaultDayWhellIndex + 1;
        } else {
            mWheelDay.setDefault(mDay - 1);
        }
    }

    /**
     * set WLQQTimePicker date
     * @param date
     */
    public void setDate(long date) {
        mCalendar = Calendar.getInstance(Locale.CHINA);
        mCalendar.setTimeInMillis(date);
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);

        mWheelYear.setData(getYearData());
        mWheelMonth.setData(getMonthData());
        mWheelDay.setData(getDayData(mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)));
        mWheelHour.setData(getHourData());

        mHandler.sendEmptyMessage(UPDATE_TITLE_MSG);
        mHandler.sendEmptyMessage(UPDATE_WHEEL);
    }

    /**
     * 设置监听接口
     * @param onClickTimePickerListener
     */
    public void setOnClickTimePickerListener (OnClickTimePickerListener onClickTimePickerListener) {
        this.onClickTimePickerListener = onClickTimePickerListener;
    }

    private void updateTitle() {
        mPickerTitle.setText(mContext.getString(R.string.picker_title, mYear, mMonth + 1, mDay, mHour));
    }

    private void updateWheel() {
        mWheelYear.setDefault(mYear - START_YEAR);
        mWheelMonth.setDefault(mMonth);
        mWheelDay.setDefault(mDay - 1);
        mWheelHour.setDefault(mHour);
    }

    private ArrayList<String> getYearData() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = START_YEAR; i <= END_YEAR; i++) {
            list.add(i + "年");
        }
        return list;
    }

    private ArrayList<String> getMonthData() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 1; i <= 12; i++) {
            list.add(i + "月");
        }
        return list;
    }

    private ArrayList<String> getDayData(int endDay) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 1; i <= endDay; i++) {
            list.add(i + "日");
        }
        return list;
    }

    private ArrayList<String> getHourData() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            list.add(i + ":00");
        }
        return list;
    }
}