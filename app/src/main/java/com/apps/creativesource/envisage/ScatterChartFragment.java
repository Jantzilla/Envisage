package com.apps.creativesource.envisage;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;

// TIME / DURATION MATRIX
public class ScatterChartFragment extends Fragment {

    private ScatterChart chart;
    private ArrayList<Event> eventArrayList, historyArrayList;
//    private String[] projection;
//    private String sortOrder;
//    private Cursor cursor;
//    private EnvisageDBHelper dbHelper;
//    private SQLiteDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scatter_chart, container, false);

        chart = v.findViewById(R.id.scatterChart);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setData(generateData());
        chart.setTouchEnabled(false);
        chart.setExtraBottomOffset(16f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(24);

        LimitLine ll = new LimitLine(0f);
        ll.setLineColor(Color.BLACK);
        ll.setLineWidth(2f);
        ll.setTextSize(12f);

        leftAxis.addLimitLine(ll);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setEnabled(false);

        Legend l = chart.getLegend();
        l.setEnabled(false);

        return v;
    }

    private ScatterData generateData() {
        ArrayList<IScatterDataSet> sets = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int colorIndex = 0;
        float time;

        ArrayList<Entry> centroids = new ArrayList<>();

        ScatterChart.ScatterShape shape = ScatterChart.ScatterShape.CIRCLE;

        for(int i = 0; i < eventArrayList.size(); i++) {
            int points = 0;
            float timeTotal = 0;
            float durationTotal = 0;

            if(i == 6)
                break;

            ArrayList<Entry> entries = new ArrayList<>();

            for(int j = 0; j < historyArrayList.size(); j++) {

               if(historyArrayList.get(j).eventId == eventArrayList.get(i).eventId) {
                   calendar.setTimeInMillis(historyArrayList.get(j).startTime);
                   time = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f;
                   entries.add(new Entry((float) historyArrayList.get(j).endTime / (DateUtils.HOUR_IN_MILLIS), time));

                   points++;
                   timeTotal += time;
                   durationTotal += (float) historyArrayList.get(j).endTime / (DateUtils.HOUR_IN_MILLIS);
               }

            }

            ScatterDataSet ds = new ScatterDataSet(entries, "");
            ds.setDrawValues(false);
            ds.setScatterShapeSize(20f);
            ds.setColors(ColorTemplate.MATERIAL_COLORS[colorIndex]);
            ds.setScatterShape(shape);
            sets.add(ds);

            colorIndex++;

            if(colorIndex == 4)
                colorIndex = 0;

            centroids.add(new Entry((float) durationTotal/points, timeTotal/points));
        }

        ScatterDataSet ds = new ScatterDataSet(centroids, "");
        ds.setDrawValues(false);
        ds.setScatterShapeSize(10f);
        ds.setColors(Color.BLACK);
        ds.setScatterShape(shape);
        sets.add(ds);

        return new ScatterData(sets);
    }

    public void showInteraction(float time) {
        if(chart != null) {
            YAxis leftAxis = chart.getAxisLeft();
            LimitLine ll = new LimitLine(time);
            ll.setLineColor(Color.BLACK);
            ll.setLineWidth(2f);
            leftAxis.removeAllLimitLines();
            leftAxis.addLimitLine(ll);
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