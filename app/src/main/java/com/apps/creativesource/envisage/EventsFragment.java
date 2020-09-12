package com.apps.creativesource.envisage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsFragment extends Fragment implements EventsAdapter.ListItemClickListener {
    @BindView(R.id.fab_add) FloatingActionButton fab;
    @BindView(R.id.rv_events) RecyclerView eventsList;
    @BindView(R.id.pb_events) ProgressBar progressBar;
    @BindView(R.id.tv_empty_list_notification) TextView emptyListTextView;

    private boolean twoPane = false;

    private FragmentManager fragmentManager;
    private EnvisageDBHelper dbHelper;
    private SQLiteDatabase db;
    private String sortOrder, eventDescription;
    private int eventFreqId, isSet, usedCount;
    private long eventId, startTime, endTime;
    private String[] projection;
    private Cursor cursor;
    private Event event;

    private EventsAdapter adapter;
    private boolean first = true;
    private BroadcastReceiver updateUIReciver;

    @Override
    public void onResume() {
        super.onResume();
        getAllPosts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this, view);

        SQLiteDatabase.loadLibs(getContext());
        dbHelper = new EnvisageDBHelper(getContext());
        db = dbHelper.getReadableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);
        fragmentManager = getActivity().getSupportFragmentManager();

        if(getActivity().findViewById(R.id.detail_container) != null) {
            twoPane = true;
        }

        first = getArguments().getBoolean("first");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        eventsList.setLayoutManager(layoutManager);
        eventsList.setHasFixedSize(true);

        IntentFilter filter = new IntentFilter();

        filter.addAction("events.update.action");

        updateUIReciver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                getAllPosts();
            }
        };

        getActivity().registerReceiver(updateUIReciver,filter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAdd = new Intent(getContext(), AddActivity.class);
                startActivity(intentAdd);
            }
        });

        return view;
    }

    public void getAllPosts() {

        ArrayList<Event> eventArrayList = new ArrayList<>();

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
            eventId = cursor.getLong(cursor.getColumnIndex(EnvisageContract.Events._ID));
            eventDescription = cursor.getString(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_DESCRIPTION));
            startTime = cursor.getLong(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_START_TIME));
            endTime = cursor.getLong(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_END_TIME));
            eventFreqId = cursor.getInt(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_TYPE));
            isSet = cursor.getInt(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_SET));
            usedCount = cursor.getInt(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_USED_COUNT));

            event = new Event(eventId, eventDescription, startTime, endTime, eventFreqId, isSet, usedCount);

            if(isSet == 1) {
                eventArrayList.add(event);
            }
        }
        cursor.close();
        loadAdapter(eventArrayList);

    }

    public void loadAdapter(ArrayList<Event> eventArrayList) {

        adapter = new EventsAdapter(eventArrayList, first, twoPane, EventsFragment.this);
        eventsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(!eventArrayList.isEmpty()) {
            eventsList.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE );
            emptyListTextView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE );
            emptyListTextView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onListItemClicked(int clickedItemIndex, int eventId, String eventDescription,
                                  Long startTime, Long endTime, int usedCount, int eventFreqId) {
        if(twoPane) {

            Fragment fragment;

            fragment = new EventDetailFragment();

            Bundle args = new Bundle();

            args.putString("eventId", String.valueOf(eventId));
            args.putString("startTime", String.valueOf(startTime));
            args.putString("endTime", String.valueOf(endTime));
            args.putString("usedCount", String.valueOf(usedCount));
            args.putString("eventDescription",eventDescription);
            args.putString("eventFreqId", String.valueOf(eventFreqId));

            fragment.setArguments(args);

            ProgressBar progressBar = getActivity().findViewById(R.id.pb_detail);
            progressBar.setVisibility(View.GONE);

            fragmentManager.beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        } else {

            Intent detailIntent = new Intent(getContext(), DetailActivity.class);
            detailIntent.putExtra("eventId", String.valueOf(eventId));
            detailIntent.putExtra("startTime", String.valueOf(startTime));
            detailIntent.putExtra("endTime", String.valueOf(endTime));
            detailIntent.putExtra("usedCount", String.valueOf(usedCount));
            detailIntent.putExtra("eventDescription", eventDescription);
            detailIntent.putExtra("eventFreqId", String.valueOf(eventFreqId));
            detailIntent.putExtra("twoPane", twoPane);
            detailIntent.putExtra("portrait", true);

            startActivity(detailIntent);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(updateUIReciver);
    }
}
