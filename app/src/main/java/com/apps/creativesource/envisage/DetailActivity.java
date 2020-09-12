package com.apps.creativesource.envisage;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import net.sqlcipher.database.SQLiteDatabase;
import android.graphics.Typeface;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {
    @BindView(R.id.ll_transparent) LinearLayout linearLayout;
    private boolean twoPane;
    private String eventDescription;
    String newEventDescription, newEventFreqId;
    int usedCount;
    private String eventId, eventFreqId;
    private Typeface typeface;
    private SQLiteDatabase db;
    private EnvisageDBHelper dbHelper;
    Spinner spinner;
    Calendar calendar, calendarTo, newCalendar, newCalendarTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        typeface = Typeface.createFromAsset(getAssets(), "ColorTube.otf");
        View view = LayoutInflater.from(this).inflate(R.layout.title_bar,null);
        spinner = view.findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);
        TextView textView = view.findViewById(R.id.tv_title);
        textView.setText("");
        textView.setTypeface(typeface);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(view);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        getSupportActionBar().setCustomView(view, layoutParams);
        Toolbar parent = (Toolbar) view.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        Intent initialIntent = getIntent();

        fragment = new EventDetailFragment();

        fragmentManager.beginTransaction()
                .add(R.id.fl_detail, fragment)
                .commit();

        eventId = initialIntent.getStringExtra("eventId");
        twoPane = initialIntent.getBooleanExtra("twoPane", false);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(initialIntent.getStringExtra("startTime")));
        newCalendar = calendar;

        calendarTo = Calendar.getInstance();
        calendarTo.setTimeInMillis(Long.parseLong(initialIntent.getStringExtra("endTime")));
        newCalendarTo = calendarTo;

        eventDescription = initialIntent.getStringExtra("eventDescription");
        newEventDescription = eventDescription;

        eventFreqId = initialIntent.getStringExtra("eventFreqId");
        newEventFreqId = eventFreqId;

        usedCount = Integer.parseInt(initialIntent.getStringExtra("usedCount"));

        SQLiteDatabase.loadLibs(this);

        dbHelper = new EnvisageDBHelper(this);
        db = dbHelper.getWritableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);


        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_type_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public boolean isValidTextFields() {
        if(newEventDescription.equals("")){
            Toast.makeText(getApplicationContext(),getString(R.string.please_enter_statement), Toast.LENGTH_LONG).show();
            return false;
        } if(newEventDescription.length() > 150) {
            Toast.makeText(getApplicationContext(),getString(R.string.cannot_exceed_150_chars), Toast.LENGTH_LONG).show();
            return false;
        } if(newCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(getApplicationContext(), "Please select a time in the future.", Toast.LENGTH_LONG).show();
            return false;
        } if(newCalendarTo.getTimeInMillis() <= newCalendar.getTimeInMillis() + 1000) {
            Toast.makeText(getApplicationContext(), "Invalid time. Ending time must be after start time.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(calendar.getTimeInMillis() != newCalendar.getTimeInMillis() ||
                calendarTo.getTimeInMillis() != newCalendarTo.getTimeInMillis() ||
                !eventDescription.equals(newEventDescription) ||
                !eventFreqId.equals(newEventFreqId)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Do you want to save changes?");
            builder.setCancelable(true);
            builder.setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(homeIntent);
                    finish();
                    dialog.cancel();
                }
            });
            builder.setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(isValidTextFields()) {
                        linearLayout.setVisibility(View.VISIBLE);
                        updateEvent();
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
                        finish();
                        dialog.cancel();
                    }
                }
            });
            builder.show();
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View view = getLayoutInflater().inflate(R.layout.activity_main, null);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && view.findViewById(R.id.detail_container) != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("eventDescription", eventDescription);
            intent.putExtra("eventFreqId", eventFreqId);
            intent.putExtra("orientation", "twoPane");
            startActivity(intent);
            finish();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra("eventDescription", eventDescription);
            detailIntent.putExtra("eventFreqId", eventFreqId);
            detailIntent.putExtra("twoPane", twoPane);
            startActivity(detailIntent);
            finish();
        }
    }

    private void updateEvent() {
        ContentValues values = new ContentValues();
        String selection = EnvisageContract.Events._ID + " = ?";
        String[] selectionArgs = new String[] {eventId};
        values.put(EnvisageContract.Events.COLUMN_DESCRIPTION, newEventDescription);
        values.put(EnvisageContract.Events.COLUMN_START_TIME, newCalendar.getTimeInMillis());
        values.put(EnvisageContract.Events.COLUMN_END_TIME, newCalendarTo.getTimeInMillis());
        values.put(EnvisageContract.Events.COLUMN_TYPE, newEventFreqId);
        db.update(
                EnvisageContract.Events.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        JobScheduler jobScheduler = (JobScheduler)getApplicationContext() .getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(Integer.parseInt(eventId));
        Log.d("JOb ", Integer.parseInt(eventId) + " is Stopped! hopefully...");

        int threshold = 30000; // time limit for job scheduler

        ComponentName serviceComponent = new ComponentName(this, EventService.class);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putLong("eventId", Long.parseLong(eventId));
        bundle.putInt("eventFreqId", Integer.parseInt(newEventFreqId));
        bundle.putLong("startTime", newCalendar.getTimeInMillis());
        bundle.putLong("endTime", newCalendarTo.getTimeInMillis());
        bundle.putLong("usedCount", usedCount);
        bundle.putString("eventDescription", newEventDescription);

        JobInfo.Builder builder = new JobInfo.Builder(Integer.parseInt(eventId), serviceComponent);
        builder.setExtras(bundle);
        builder.setPersisted(true);
        builder.setMinimumLatency(newCalendar.getTimeInMillis() - (System.currentTimeMillis())); // wait at least
        builder.setOverrideDeadline(newCalendar.getTimeInMillis() - (System.currentTimeMillis() - threshold)); // maximum delay

        jobScheduler.schedule(builder.build());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String dateString = sdf.format(newCalendar.getTime());
        sdf = new SimpleDateFormat("h:mm a");
        String timeString = sdf.format(newCalendar.getTime());

        Log.d("Alarm set to ", dateString + " " + timeString);

    }

    public void refresh() {
        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(homeIntent);
        finish();
    }

}
