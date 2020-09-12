package com.apps.creativesource.envisage;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;

// MOST LIKELY DURATION
public class BarChartFragment extends Fragment implements OnChartGestureListener {

    private HorizontalBarChart chart;
    private ArrayList<Event> eventArrayList, historyArrayList;
    private ArrayList<TextView> viewArrayList;
//    private String[] projection;
//    private String sortOrder;
//    private Cursor cursor;
//    private EnvisageDBHelper dbHelper;
//    private SQLiteDatabase db;
    private TextView textView1, textView2, textView3, textView4, textView5, textView6;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_bar_chart, container, false);

        viewArrayList = new ArrayList<>();

        textView1 = v.findViewById(R.id.tv_1);
        textView2 = v.findViewById(R.id.tv_2);
        textView3 = v.findViewById(R.id.tv_3);
        textView4 = v.findViewById(R.id.tv_4);
        textView5 = v.findViewById(R.id.tv_5);
        textView6 = v.findViewById(R.id.tv_6);

        viewArrayList.add(textView1);
        viewArrayList.add(textView2);
        viewArrayList.add(textView3);
        viewArrayList.add(textView4);
        viewArrayList.add(textView5);
        viewArrayList.add(textView6);

        chart = new HorizontalBarChart(getActivity());
        chart.getDescription().setEnabled(false);
        chart.setOnChartGestureListener(this);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        chart.setData(generateData(calendar));

        Legend l = chart.getLegend();
        l.setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setEnabled(false);

        chart.getAxisRight().setEnabled(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(false);

        FrameLayout parent = v.findViewById(R.id.frameLayout);
        parent.addView(chart);

        return v;
    }

    private BarData generateData(Calendar time) {
        ArrayList<IBarDataSet> sets = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int colorIndex = 0;

        for(int i = 0; i < eventArrayList.size(); i++) {

            if(i == 6)
                break;

            ArrayList<BarEntry> entries = new ArrayList<>();
            long closestTime = DateUtils.DAY_IN_MILLIS;
            int historyIndex = 0;

            for(int j = 0; j < historyArrayList.size(); j++) {

                if(historyArrayList.get(j).eventId == eventArrayList.get(i).eventId) {

                    calendar.setTimeInMillis(historyArrayList.get(j).startTime);
                    calendar.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DATE));

                    long testTime = Math.abs(calendar.getTimeInMillis() - time.getTimeInMillis());

                    if(testTime < closestTime) {
                        closestTime = testTime;
                        historyIndex = j;
                    }

                }

            }

            entries.add(new BarEntry(i, (float) historyArrayList.get(historyIndex).endTime / (DateUtils.HOUR_IN_MILLIS)));
            viewArrayList.get(i).setVisibility(View.VISIBLE);
            viewArrayList.get(i).setText(eventArrayList.get(eventArrayList.size() - (i + 1)).eventDescription);
            BarDataSet ds = new BarDataSet(entries, "");
            ds.setValueTextSize(12f);
            ds.setColors(ColorTemplate.MATERIAL_COLORS[colorIndex]);
            ds.setDrawValues(false);
            sets.add(ds);

            colorIndex++;

            if(colorIndex == 4)
                colorIndex = 0;
        }

//        }

        return new BarData(sets);
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START");
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END");
        chart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart long pressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart fling. VelocityX: " + velocityX + ", VelocityY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    public void showInteraction(Calendar time) {
        if(chart != null) {
            chart.setData(generateData(time));
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    public void setData(ArrayList<Event> historyArrayList, ArrayList<Event> eventArrayList){
        this.eventArrayList = eventArrayList;
        this.historyArrayList = historyArrayList;
//        getHistory(context);
    }

//    private void getHistory(Context context) {
//        Event event;
//        long startTime, duration;
//        int eventId;
//        historyArrayList = new ArrayList<>();
//
//        SQLiteDatabase.loadLibs(context);
//        dbHelper = new EnvisageDBHelper(context);
//        db = dbHelper.getReadableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);
//
//        projection = new String[]{
//                EnvisageContract.Events.COLUMN_START_TIME,
//                EnvisageContract.History.COLUMN_DURATION,
//                EnvisageContract.History.COLUMN_EVENT
//        };
//
//        sortOrder = EnvisageContract.History.COLUMN_DURATION;
//
//        cursor = db.query(
//                EnvisageContract.History.TABLE_NAME,
//                projection,
//                null,
//                null,
//                null,
//                null,
//                sortOrder
//        );
//
//        while(cursor.moveToNext()) {
//            startTime = cursor.getLong(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_START_TIME));
//            duration = cursor.getLong(cursor.getColumnIndex(EnvisageContract.History.COLUMN_DURATION));
//            eventId = cursor.getInt(cursor.getColumnIndex(EnvisageContract.History.COLUMN_EVENT));
//
//            event = new Event(eventId, "past event", startTime, duration, 0, 0, 0);
//            historyArrayList.add(event);
//        }
//        cursor.close();
//
//    }
}