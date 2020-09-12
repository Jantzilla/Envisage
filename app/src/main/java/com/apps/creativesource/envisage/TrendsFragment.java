package com.apps.creativesource.envisage;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.PersistableBundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class TrendsFragment extends Fragment implements HorizontalAdapter.ListItemClickListener {
    @BindView(R.id.rv_trends) RecyclerView trendsList;
    @BindView(R.id.tv_empty_list_notification) TextView emptyListTextView;
    @BindView(R.id.vp_visuals) ViewPager viewPager;

    private ViewPagerAdapter viewPagerAdapter;
    private EnvisageDBHelper dbHelper;
    private SQLiteDatabase db;
    private String sortOrder, eventDescription, timeString;
    private int eventFreqId, isSet, usedCount;
    private long eventId, startTime, endTime, duration;
    private String[] projection;
    private Cursor cursor;
    private Event event;

    private HorizontalAdapter adapter;
    private SeekBar seekBar;
    private TextView timeTextView;
    private SimpleDateFormat sdf;
    private LinearSnapHelper snapHelper;
    private PickerLayoutManager pickerLayoutManager;
    private int pagerPosition;
    private PieChartFragment pieChartFragment;
    private BarChartFragment barChartFragment;
    private ScatterChartFragment scatterChartFragment;
    private Calendar seekTime;
    private boolean scrolled = false;
    private ArrayList<Event> historyArrayList, eventArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trends, container, false);
        ButterKnife.bind(this, view);

        SQLiteDatabase.loadLibs(getContext());
        dbHelper = new EnvisageDBHelper(getContext());
        db = dbHelper.getReadableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        seekBar = view.findViewById(R.id.seekbar);
        timeTextView = view.findViewById(R.id.tv_time);

        pickerLayoutManager = new PickerLayoutManager(getContext(), PickerLayoutManager.HORIZONTAL, false);
        pickerLayoutManager.setChangeAlpha(true);
        pickerLayoutManager.setScaleDownBy(0.99f);
        pickerLayoutManager.setScaleDownDistance(0.8f);
        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(trendsList);
        trendsList.setLayoutManager(pickerLayoutManager);

        getAllPosts();

        barChartFragment = new BarChartFragment();

        scatterChartFragment = new ScatterChartFragment();
        viewPagerAdapter.addFragment(scatterChartFragment);
        scatterChartFragment.setData(historyArrayList, adapter.getEventArrayList());

        pieChartFragment = new PieChartFragment();
        viewPagerAdapter.addFragment(pieChartFragment);
        pieChartFragment.setData(adapter.getEventArrayList());

        viewPagerAdapter.addFragment(barChartFragment);
        barChartFragment.setData(historyArrayList, adapter.getEventArrayList());

        viewPager.setAdapter(viewPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pagerPosition = position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        seekBar.setMax(24 * 4); //24 hours and 4 step in one hour.
        seekTime = Calendar.getInstance();

        seekBar.setOnSeekBarChangeListener(setSeekBarListener());

        pickerLayoutManager.setOnScrollStopListener(setScrollStopListener());

        return view;
    }

    private PickerLayoutManager.onScrollStopListener setScrollStopListener() {
        return new PickerLayoutManager.onScrollStopListener() {
            @Override
            public void selectedView(View view) {
                // Work around for double stop call due to use of "SnapHelper".
                if(scrolled) {
                    scrolled = false;
                } else {
                    scrolled = true;
                    adapter.getPositionByString(((TextView)view).getText().toString());
                    setTimeData();
                }
            }
        };
    }

    private SeekBar.OnSeekBarChangeListener setSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int hours = progress / 4; // it will return hours.
                int minutes = (progress % 4) * 15; // here will be minutes.
                seekTime.set(Calendar.HOUR_OF_DAY, hours);
                seekTime.set(Calendar.MINUTE, minutes);
                sdf = new SimpleDateFormat("h:mm a");
                timeString = sdf.format(seekTime.getTime());
                timeTextView.setText(timeString);
                pickerLayoutManager.setOnScrollStopListener(null);
                pickerLayoutManager.smoothScrollToPosition(trendsList, null, adapter.getPositionByTime(seekTime));
                graphInteraction(pagerPosition);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pickerLayoutManager.setOnScrollStopListener(setScrollStopListener());
            }
        };
    }

    private void setTimeData() {
        if (adapter.event != null) {
            seekTime.setTimeInMillis(adapter.event.startTime);
        }

        sdf = new SimpleDateFormat("h:mm a");
        timeString = sdf.format(seekTime.getTime());
        timeTextView.setText(timeString);
        graphInteraction(pagerPosition);
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setProgress(seekTime.get(Calendar.HOUR_OF_DAY) * 4);
        seekBar.setOnSeekBarChangeListener(setSeekBarListener());
    }

    private void getAllPosts() {

        eventArrayList = new ArrayList<>();

        projection = new String[]{
                EnvisageContract.Events._ID,
                EnvisageContract.Events.COLUMN_DESCRIPTION,
                EnvisageContract.Events.COLUMN_START_TIME,
                EnvisageContract.Events.COLUMN_END_TIME,
                EnvisageContract.Events.COLUMN_TYPE,
                EnvisageContract.Events.COLUMN_USED_COUNT,
                EnvisageContract.Events.COLUMN_SET
        };

        sortOrder = EnvisageContract.Events.COLUMN_START_TIME;

        cursor = db.query(
                EnvisageContract.Events.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while(cursor.moveToNext()) {

            if(eventArrayList.size() == 6)
                break;

            Calendar oldTime, newTime;
            eventId = cursor.getLong(cursor.getColumnIndex(EnvisageContract.Events._ID));
            eventDescription = cursor.getString(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_DESCRIPTION));
            startTime = cursor.getLong(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_START_TIME));
            endTime = cursor.getLong(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_END_TIME));
            eventFreqId = cursor.getInt(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_TYPE));
            usedCount = cursor.getInt(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_USED_COUNT));
            isSet = cursor.getInt(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_SET));

            if(isSet == 0 && usedCount > 0) {
                if(startTime <= System.currentTimeMillis()) {
                    oldTime = Calendar.getInstance();
                    oldTime.setTimeInMillis(startTime);

                    newTime = Calendar.getInstance();

                    switch (eventFreqId) {
                        case 0:
                        case 5:
                            newTime.setTimeInMillis(System.currentTimeMillis());
                            newTime.set(Calendar.MONTH, oldTime.get(Calendar.MONTH));
                            newTime.set(Calendar.DATE, oldTime.get(Calendar.DATE));
                            newTime.set(Calendar.HOUR_OF_DAY, oldTime.get(Calendar.HOUR_OF_DAY));
                            newTime.set(Calendar.MINUTE, oldTime.get(Calendar.MINUTE));
                            if(newTime.getTimeInMillis() <= System.currentTimeMillis())
                                newTime.add(Calendar.YEAR, 1);
                            startTime = newTime.getTimeInMillis();
                            break;
                        case 1:
                            newTime.setTimeInMillis(System.currentTimeMillis());
                            newTime.set(Calendar.HOUR_OF_DAY, oldTime.get(Calendar.HOUR_OF_DAY));
                            newTime.set(Calendar.MINUTE, oldTime.get(Calendar.MINUTE));
                            if(newTime.getTimeInMillis() <= System.currentTimeMillis())
                                newTime.add(Calendar.DATE, 1);
                            startTime = newTime.getTimeInMillis();
                            break;
                        case 2:
                        case 3:
                            newTime.setTimeInMillis(System.currentTimeMillis());
                            newTime.set(Calendar.DAY_OF_WEEK, oldTime.get(Calendar.DAY_OF_WEEK));
                            newTime.set(Calendar.HOUR_OF_DAY, oldTime.get(Calendar.HOUR_OF_DAY));
                            newTime.set(Calendar.MINUTE, oldTime.get(Calendar.MINUTE));
                            if(newTime.getTimeInMillis() <= System.currentTimeMillis())
                                newTime.add(Calendar.DATE, 7);
                            startTime = newTime.getTimeInMillis();
                            break;
                        case 4:
                            newTime.setTimeInMillis(System.currentTimeMillis());
                            newTime.set(Calendar.DATE, oldTime.get(Calendar.DATE));
                            newTime.set(Calendar.HOUR_OF_DAY, oldTime.get(Calendar.HOUR_OF_DAY));
                            newTime.set(Calendar.MINUTE, oldTime.get(Calendar.MINUTE));
                            if(newTime.getTimeInMillis() <= System.currentTimeMillis())
                                newTime.add(Calendar.MONTH, 1);
                            startTime = newTime.getTimeInMillis();
                            break;
                    }
                }

                event = new Event(eventId, eventDescription, startTime, endTime, eventFreqId, isSet, usedCount);
                eventArrayList.add(event);
            }
        }

        historyArrayList = new ArrayList<>();

        db = dbHelper.getReadableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);

        projection = new String[]{
                EnvisageContract.Events.COLUMN_START_TIME,
                EnvisageContract.History.COLUMN_DURATION,
                EnvisageContract.History.COLUMN_EVENT
        };

        sortOrder = EnvisageContract.History.COLUMN_DURATION;

        cursor = db.query(
                EnvisageContract.History.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while(cursor.moveToNext()) {
            startTime = cursor.getLong(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_START_TIME));
            duration = cursor.getLong(cursor.getColumnIndex(EnvisageContract.History.COLUMN_DURATION));
            eventId = cursor.getInt(cursor.getColumnIndex(EnvisageContract.History.COLUMN_EVENT));

            event = new Event(eventId, "past event", startTime, duration, 0, 0, 0);
            historyArrayList.add(event);
        }

        cursor.close();

        eventArrayList = ClusteringUtils.getCentroids(historyArrayList, eventArrayList);

        loadAdapter(eventArrayList);
    }

//    private void getCentroids() {
//        Calendar calendar = Calendar.getInstance();
//        float time;
//
//        for(int i = 0; i < eventArrayList.size(); i++) {
//            int points = 0;
//            float timeTotal = 0;
//            float durationTotal = 0;
//
//            if(i == 6)
//                break;
//
//            for(int j = 0; j < historyArrayList.size(); j++) {
//
//                if(historyArrayList.get(j).eventId == eventArrayList.get(i).eventId) {
//                    calendar.setTimeInMillis(historyArrayList.get(j).startTime);
//                    time = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f;
//
//                    points++;
//                    timeTotal += time;
//                    durationTotal += (float) historyArrayList.get(j).endTime / (DateUtils.HOUR_IN_MILLIS);
//                }
//
//            }
//            calendar.setTimeInMillis(eventArrayList.get(i).startTime);
//            calendar.set(Calendar.HOUR_OF_DAY, (int) (timeTotal/points));
//            eventArrayList.get(i).startTime = calendar.getTimeInMillis();
//
//            eventArrayList.get(i).endTime = (long) (eventArrayList.get(i).startTime + DateUtils.HOUR_IN_MILLIS * (durationTotal/points));
//        }
//    }

    public void loadAdapter(ArrayList<Event> eventArrayList) {

        adapter = new HorizontalAdapter(eventArrayList, TrendsFragment.this);
        trendsList.setAdapter(adapter);
        pickerLayoutManager.smoothScrollToPosition(trendsList, null, 0);
        adapter.notifyDataSetChanged();

        if(!eventArrayList.isEmpty()) {
            trendsList.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        } else {
            trendsList.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onListItemClicked(int clickedItemIndex, int eventId, String eventDescription,
                                  Long startTime, Long endTime, int eventFreqId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create this event?");
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateEvent(eventId, eventDescription, startTime, endTime, eventFreqId);
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void graphInteraction(int position) {
        switch (position) {
            case 0:
                scatterChartFragment.showInteraction(seekTime.get(Calendar.HOUR_OF_DAY) + (seekTime.get(Calendar.MINUTE) / 60f));
                break;
            case 1:
                pieChartFragment.showInteraction(seekTime);
                break;
            case 2:
                barChartFragment.showInteraction(seekTime);
                break;
        }
    }

    private void updateEvent(int eventId, String eventDescription,
                             Long startTime, Long endTime, int eventFreqId) {
        ContentValues values = new ContentValues();
        String selection = EnvisageContract.Events._ID + " = ?";
        String[] selectionArgs = new String[] {String.valueOf(eventId)};
        values.put(EnvisageContract.Events.COLUMN_SET, 1);
        values.put(EnvisageContract.Events.COLUMN_START_TIME, startTime);
        values.put(EnvisageContract.Events.COLUMN_END_TIME, endTime);
        db.update(
                EnvisageContract.Events.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        int threshold = 30000; // time limit for job scheduler

        ComponentName serviceComponent = new ComponentName(getContext(), EventService.class);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putLong("eventId", eventId);
        bundle.putInt("eventFreqId", eventFreqId);
        bundle.putLong("startTime", startTime);
        bundle.putLong("endTime", endTime);
        bundle.putString("eventDescription", eventDescription);

        JobInfo.Builder builder = new JobInfo.Builder((int) eventId, serviceComponent);
        builder.setExtras(bundle);
        builder.setPersisted(true);
        builder.setMinimumLatency(startTime - (System.currentTimeMillis())); // wait at least
        builder.setOverrideDeadline(startTime - (System.currentTimeMillis() - threshold)); // maximum delay

        JobScheduler jobScheduler = (JobScheduler)getActivity().getApplicationContext() .getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());

        getAllPosts();

        Intent local = new Intent();

        local.setAction("main.update.action");

        getActivity().sendBroadcast(local);
    }

}