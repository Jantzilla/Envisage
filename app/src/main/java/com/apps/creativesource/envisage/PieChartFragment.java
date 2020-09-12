package com.apps.creativesource.envisage;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;

// CONFIDENCE INDEX
public class PieChartFragment extends Fragment {

    private PieChart chart;
    private ArrayList<Event> eventArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_pie_chart, container, false);

        chart = v.findViewById(R.id.pieChart);
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(25f);
        chart.setTransparentCircleRadius(30f);

        Legend l = chart.getLegend();
        l.setEnabled(false);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        chart.setData(generateData(calendar));

        chart.setTouchEnabled(false);

        return v;
    }

    public PieData generateData(Calendar time) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();

        for(int i = 0; i < eventArrayList.size(); i++) {

            if(i == 6)
                break;

            calendar.setTimeInMillis(eventArrayList.get(i).startTime);
            calendar.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DATE));
            float newTime = Math.abs(100 - (Math.abs(calendar.getTimeInMillis() - time.getTimeInMillis()) / (float) DateUtils.HOUR_IN_MILLIS / 12f * 100));

            if(newTime < 35)
                if(newTime < 1)
                    continue;
                else
                    entries.add(new PieEntry(newTime));
            else
                entries.add(new PieEntry(newTime, eventArrayList.get(i).eventDescription));
        }

        PieDataSet ds1 = new PieDataSet(entries, "test");
        ds1.setColors(ColorTemplate.MATERIAL_COLORS);
        ds1.setSliceSpace(2f);
        ds1.setValueTextColor(Color.BLACK);
        ds1.setValueTextSize(12f);

        return new PieData(ds1);
    }

    public void showInteraction(Calendar time) {
        if(chart != null) {
            chart.setData(generateData(time));
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    public void setData(ArrayList<Event> eventArrayList){
        this.eventArrayList = eventArrayList;
    }
}