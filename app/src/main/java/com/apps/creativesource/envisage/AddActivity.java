package com.apps.creativesource.envisage;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import net.sqlcipher.database.SQLiteDatabase;

import android.database.Cursor;
import android.graphics.Typeface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBar.LayoutParams;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager;

public class AddActivity extends AppCompatActivity implements HorizontalAdapter.ListItemClickListener {
    private EditText descriptionEditText;
    private TextView dateTextView, timeTextView, timeTwoTextView;
    private int eventFreqId = 0;
    private Typeface typeface;
    private SQLiteDatabase db;
    private EnvisageDBHelper dbHelper;
    private TimePicker timePicker, timeTwoPicker;
    private DatePicker datePicker;
    private SimpleDateFormat sdf;
    private Calendar calendar, calendarTo;
    private String dateString, timeString;
    private Spinner spinner;
    private String sortOrder, eventDescription;
    private int isSet, position, newPosition, usedCount;
    private long eventId, startTime, endTime;
    private String[] projection;
    private Cursor cursor;
    private Event event;
    private RecyclerView trendsRecyclerView;
    private LinearSnapHelper snapHelper;
    private PickerLayoutManager pickerLayoutManager;
    private HorizontalAdapter horizontalAdapter;
    private boolean isTextListening = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);;
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        typeface = Typeface.createFromAsset(getAssets(), "ColorTube.otf");
        View view = LayoutInflater.from(this).inflate(R.layout.title_bar,null);
        TextView textView = view.findViewById(R.id.tv_title);
        spinner = view.findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendarTo = Calendar.getInstance();
        calendarTo.setTimeInMillis(calendar.getTimeInMillis());
        textView.setText("");
        textView.setTypeface(typeface);
        descriptionEditText = findViewById(R.id.et_description);
        trendsRecyclerView = findViewById(R.id.rv_trends);
        dateTextView = findViewById(R.id.tv_date);
        timeTextView = findViewById(R.id.tv_time);
        timeTwoTextView = findViewById(R.id.tv_time_two);
        sdf = new SimpleDateFormat("MM/dd/yyyy");
        dateString = sdf.format(calendar.getTime());
        sdf = new SimpleDateFormat("h:mm a");
        timeString = sdf.format(calendar.getTime());
        dateTextView.setText(dateString);
        timeTextView.setText(timeString);
        sdf = new SimpleDateFormat(" - h:mm a");
        calendarTo.add(Calendar.MINUTE, 30);
        timeString = sdf.format(calendarTo.getTime());
        timeTwoTextView.setText(timeString);

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(view);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        getSupportActionBar().setCustomView(view, layoutParams);
        Toolbar parent = (Toolbar) view.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        timePicker = findViewById(R.id.timePicker);
        timeTwoPicker = findViewById(R.id.timeTwoPicker);
        datePicker = findViewById(R.id.datePicker);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);

        // Horizontal picker
        pickerLayoutManager = new PickerLayoutManager(this, PickerLayoutManager.HORIZONTAL, false);
        pickerLayoutManager.setChangeAlpha(true);
        pickerLayoutManager.setScaleDownBy(0.99f);
        pickerLayoutManager.setScaleDownDistance(0.8f);
        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(trendsRecyclerView);
        trendsRecyclerView.setLayoutManager(pickerLayoutManager);

        SQLiteDatabase.loadLibs(this);
        dbHelper = new EnvisageDBHelper(this);
        db = dbHelper.getWritableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);

        getAllPosts();

        Intent intent = getIntent();

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.event_type_array, R.layout.spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        if(intent.hasExtra("eventId")){
            calendar.setTimeInMillis(Long.parseLong(intent.getStringExtra("startTime")));
            sdf = new SimpleDateFormat("MM/dd/yyyy");
            dateString = sdf.format(calendar.getTime());
            dateTextView.setText(dateString);
            sdf = new SimpleDateFormat("h:mm a");
            timeString = sdf.format(calendar.getTime());
            timeTextView.setText(timeString);
            calendarTo.setTimeInMillis(Long.parseLong(intent.getStringExtra("endTime")));
            timeString = sdf.format(calendar.getTime());
            timeTwoTextView.setText(timeString);
            spinner.setSelection(Integer.parseInt(intent.getStringExtra("eventFreqId")));
            descriptionEditText.setText(intent.getStringExtra("eventDescription"));
        }

        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(isTextListening) {
                    eventId = -1;
                    newPosition = horizontalAdapter.getPositionByString(s);

                    if (position == newPosition)
                        return;

                    position = newPosition;

                    if (position != -1) {
                        pickerLayoutManager.smoothScrollToPosition(trendsRecyclerView, null, position);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(isTextListening) {
                    setTimeData();
                }
            }
        });

        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), setChangeListener());

        timePicker.setOnTimeChangedListener(setTimeChangeListener());
        timeTwoPicker.setOnTimeChangedListener(setTimeTwoChangeListener());

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker.setVisibility(View.GONE);
                timeTwoPicker.setVisibility(View.GONE);
                datePicker.setVisibility(View.VISIBLE);
            }
        });

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setVisibility(View.GONE);
                timeTwoPicker.setVisibility(View.GONE);
                timePicker.setVisibility(View.VISIBLE);
            }
        });

        timeTwoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setVisibility(View.GONE);
                timePicker.setVisibility(View.GONE);
                timeTwoPicker.setVisibility(View.VISIBLE);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eventFreqId = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        pickerLayoutManager.setOnScrollStopListener(new PickerLayoutManager.onScrollStopListener() {
            @Override
            public void selectedView(View view) {
                setTimeData();
            }
        });

    }

    private void setTimeData() {
        if (horizontalAdapter.event != null) {
            calendar.setTimeInMillis(horizontalAdapter.event.startTime);
            calendarTo.setTimeInMillis(horizontalAdapter.event.endTime);
            spinner.setSelection(horizontalAdapter.event.eventFreqId);
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendarTo.setTimeInMillis(System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS * 15);
            spinner.setSelection(0);
        }
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), setChangeListener());
        sdf = new SimpleDateFormat("MM/dd/yyyy");
        dateString = sdf.format(calendar.getTime());
        dateTextView.setText(dateString);
        sdf = new SimpleDateFormat("h:mm a");
        timeString = sdf.format(calendar.getTime());
        timeTextView.setText(timeString);
        timePicker.setOnTimeChangedListener(null);
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setOnTimeChangedListener(setTimeChangeListener());

        sdf = new SimpleDateFormat(" - h:mm a");
        timeString = sdf.format(calendarTo.getTime());
        timeTwoTextView.setText(timeString);
        timeTwoPicker.setOnTimeChangedListener(null);
        timeTwoPicker.setCurrentMinute(calendarTo.get(Calendar.MINUTE));
        timeTwoPicker.setCurrentHour(calendarTo.get(Calendar.HOUR_OF_DAY));
        timeTwoPicker.setOnTimeChangedListener(setTimeTwoChangeListener());
    }

    public boolean isValidTextFields() {
        if(descriptionEditText.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(),getString(R.string.please_enter_statement), Toast.LENGTH_LONG).show();
            descriptionEditText.setError(getString(R.string.please_enter_statement));
            return false;
        } if(descriptionEditText.getText().toString().length() > 150) {
            Toast.makeText(getApplicationContext(),getString(R.string.cannot_exceed_150_chars), Toast.LENGTH_LONG).show();
            descriptionEditText.setError(getString(R.string.cannot_exceed_150_chars));
            return false;
        } if(calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(getApplicationContext(), "Invalid time. Please select a time in the future.", Toast.LENGTH_LONG).show();
            return false;
        } if(calendarTo.getTimeInMillis() <= calendar.getTimeInMillis() + 1000) {
            Toast.makeText(getApplicationContext(), "Invalid time. Ending time must be after start time.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void getAllPosts() {

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
            usedCount = cursor.getInt(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_USED_COUNT));
            isSet = cursor.getInt(cursor.getColumnIndex(EnvisageContract.Events.COLUMN_SET));

            if(isSet == 0 && usedCount > 0) {
                event = new Event(eventId, eventDescription, startTime, endTime, eventFreqId, isSet, usedCount);
                eventArrayList.add(event);
            }
        }
        cursor.close();

        horizontalAdapter = new HorizontalAdapter(eventArrayList, AddActivity.this);
        trendsRecyclerView.setAdapter(horizontalAdapter);

        pickerLayoutManager.smoothScrollToPosition(trendsRecyclerView, null, 0);
        horizontalAdapter.notifyDataSetChanged();

    }

    private void addNewEvent(){

        ContentValues values = new ContentValues();
        values.put(EnvisageContract.Events.COLUMN_DESCRIPTION, descriptionEditText.getText().toString());
        values.put(EnvisageContract.Events.COLUMN_START_TIME, calendar.getTimeInMillis());
        values.put(EnvisageContract.Events.COLUMN_END_TIME, calendarTo.getTimeInMillis());
        values.put(EnvisageContract.Events.COLUMN_TYPE, eventFreqId);
        values.put(EnvisageContract.Events.COLUMN_USED_COUNT, 0);
        values.put(EnvisageContract.Events.COLUMN_SET, 1);
        int id = (int) db.insert(EnvisageContract.Events.TABLE_NAME, null, values);

        int threshold = 30000; // time limit for job scheduler

        ComponentName serviceComponent = new ComponentName(this, EventService.class);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putLong("eventId", id);
        bundle.putInt("eventFreqId", eventFreqId);
        bundle.putLong("startTime", calendar.getTimeInMillis());
        bundle.putLong("endTime", calendarTo.getTimeInMillis());
        bundle.putInt("usedCount", usedCount);
        bundle.putString("eventDescription", descriptionEditText.getText().toString());

        JobInfo.Builder builder = new JobInfo.Builder((int) id, serviceComponent);
        builder.setExtras(bundle);
        builder.setPersisted(true);
        builder.setBackoffCriteria(6000, JobInfo.BACKOFF_POLICY_LINEAR);
        builder.setMinimumLatency(calendar.getTimeInMillis() - (System.currentTimeMillis())); // wait at least
        builder.setOverrideDeadline(calendar.getTimeInMillis() - (System.currentTimeMillis() - threshold)); // maximum delay

        JobScheduler jobScheduler = (JobScheduler)getApplicationContext() .getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());

    }

    private void setPastEvent() {
        ContentValues values = new ContentValues();
        String selection = EnvisageContract.Events._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(eventId)};
        values.put(EnvisageContract.Events.COLUMN_DESCRIPTION, descriptionEditText.getText().toString());
        values.put(EnvisageContract.Events.COLUMN_START_TIME, calendar.getTimeInMillis());
        values.put(EnvisageContract.Events.COLUMN_END_TIME, calendarTo.getTimeInMillis());
        values.put(EnvisageContract.Events.COLUMN_TYPE, eventFreqId);
        values.put(EnvisageContract.Events.COLUMN_SET, 1);
        db.update(
                EnvisageContract.Events.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        sdf = new SimpleDateFormat("MM/dd/yyyy");
        dateString = sdf.format(calendar.getTime());
        sdf = new SimpleDateFormat("h:mm a");
        timeString = sdf.format(calendar.getTime());

        int threshold = 30000; // time limit for job scheduler

        ComponentName serviceComponent = new ComponentName(this, EventService.class);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putLong("eventId", eventId);
        bundle.putInt("eventFreqId", eventFreqId);
        bundle.putLong("startTime", calendar.getTimeInMillis());
        bundle.putLong("endTime", calendarTo.getTimeInMillis());
        bundle.putInt("usedCount", usedCount);
        bundle.putString("eventDescription", descriptionEditText.getText().toString());

        JobInfo.Builder builder = new JobInfo.Builder((int) eventId, serviceComponent);
        builder.setExtras(bundle);
        builder.setPersisted(true);
        builder.setBackoffCriteria(6000, JobInfo.BACKOFF_POLICY_LINEAR);
        builder.setMinimumLatency(calendar.getTimeInMillis() - (System.currentTimeMillis())); // wait at least
        builder.setOverrideDeadline(calendar.getTimeInMillis() - (System.currentTimeMillis() - threshold)); // maximum delay

        JobScheduler jobScheduler = (JobScheduler)getApplicationContext() .getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());

    }

    @Override
    public boolean onSupportNavigateUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to create this event?");
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
                    eventId = horizontalAdapter.getIdenticalEventId(descriptionEditText.getText().toString());
                    if(eventId == -1)
                        addNewEvent();
                    else
                        setPastEvent();
                    Toast.makeText(getApplicationContext(), getString(R.string.poll_posted), Toast.LENGTH_LONG).show();
                    Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(returnIntent);
                    finish();
                    dialog.cancel();
                }
            }
        });
        builder.show();

        return true;
    }

    public DatePicker.OnDateChangedListener setChangeListener() {
        return (datePicker, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            if(calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.setTimeInMillis(System.currentTimeMillis() + 1000);
                calendarTo.setTimeInMillis(calendar.getTimeInMillis() + 30000);
                sdf = new SimpleDateFormat("h:mm a");
                timeString = sdf.format(calendar.getTime());
                timeTextView.setText(timeString);
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));

                sdf = new SimpleDateFormat(" - h:mm a");
                timeString = sdf.format(calendarTo.getTime());
                timeTwoTextView.setText(timeString);
                timeTwoPicker.setCurrentMinute(calendarTo.get(Calendar.MINUTE));
                timeTwoPicker.setCurrentHour(calendarTo.get(Calendar.HOUR_OF_DAY));
            }
            sdf = new SimpleDateFormat("MM/dd/yyyy");
            dateString = sdf.format(calendar.getTime());
            dateTextView.setText(dateString);
        };
    }

    public TimePicker.OnTimeChangedListener setTimeChangeListener() {
        return new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                if(calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.setTimeInMillis(System.currentTimeMillis() + 1000);
                    timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
                    timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));

                    calendarTo.setTimeInMillis(System.currentTimeMillis() + 30000);
                    timeTwoPicker.setCurrentMinute(calendarTo.get(Calendar.MINUTE));
                    timeTwoPicker.setCurrentHour(calendarTo.get(Calendar.HOUR_OF_DAY));
                }
                sdf = new SimpleDateFormat("h:mm a");
                timeString = sdf.format(calendar.getTime());
                timeTextView.setText(timeString);
                sdf = new SimpleDateFormat(" - h:mm a");
                timeString = sdf.format(calendarTo.getTime());
                timeTwoTextView.setText(timeString);
            }
        };
    }

    public TimePicker.OnTimeChangedListener setTimeTwoChangeListener() {
        return new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                calendarTo.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendarTo.set(Calendar.MINUTE, minute);
                if(calendarTo.getTimeInMillis() <= calendar.getTimeInMillis()) {
                    calendarTo.setTimeInMillis(calendar.getTimeInMillis() + 1000);
                    timeTwoPicker.setCurrentMinute(calendarTo.get(Calendar.MINUTE));
                    timeTwoPicker.setCurrentHour(calendarTo.get(Calendar.HOUR_OF_DAY));
                }
                sdf = new SimpleDateFormat(" - h:mm a");
                timeString = sdf.format(calendarTo.getTime());
                timeTwoTextView.setText(timeString);
            }
        };
    }

    @Override
    public void onListItemClicked(int clickedItemIndex, int eventId, String eventDescription,
                                  Long startTime, Long endTime, int eventFreqId) {
        isTextListening = false;
        descriptionEditText.setText(eventDescription);
        calendar.setTimeInMillis(startTime);
        calendarTo.setTimeInMillis(endTime);
        isTextListening = true;
        this.eventId = eventId;
    }

}
