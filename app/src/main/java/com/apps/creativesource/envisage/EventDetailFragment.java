package com.apps.creativesource.envisage;

import android.app.job.JobScheduler;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import net.sqlcipher.database.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class EventDetailFragment extends Fragment {

    private String dateString, timeString, selection;

    private TextView timeTextView, timeTwoTextView, dateTextView, descriptionTextView;
    private DatePicker datePicker;
    private TimePicker timePicker, timeTwoPicker;
    private SimpleDateFormat sdf;
    private int eventFreqId, eventId;
    private JobScheduler jobScheduler;
    private EnvisageDBHelper dbHelper;
    private SQLiteDatabase db;
    private String[] selectionArgs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_detail, container, false);
        descriptionTextView = view.findViewById(R.id.tv_description);
        dateTextView = view.findViewById(R.id.tv_date);
        timeTextView = view.findViewById(R.id.tv_time);
        timeTwoTextView = view.findViewById(R.id.tv_time_two);
        FloatingActionButton fab = view.findViewById(R.id.fab_delete);
        timePicker = view.findViewById(R.id.timePicker);
        timeTwoPicker = view.findViewById(R.id.timeTwoPicker);
        datePicker = view.findViewById(R.id.datePicker);

        datePicker.setMinDate(System.currentTimeMillis() - 1000);

        jobScheduler = (JobScheduler)getActivity().getApplicationContext() .getSystemService(JOB_SCHEDULER_SERVICE);

        Calendar calendar = Calendar.getInstance();
        Calendar calendarTo = Calendar.getInstance();

        Intent initialIntent = getActivity().getIntent();

        if(initialIntent.hasExtra("broadcastType")) {
            eventId = Integer.parseInt(initialIntent.getStringExtra("eventId"));
            eventFreqId = Integer.parseInt(initialIntent.getStringExtra("eventFreqId"));
            calendar.setTimeInMillis(Long.parseLong(initialIntent.getStringExtra("startTime")));
            calendarTo.setTimeInMillis(Long.parseLong(initialIntent.getStringExtra("endTime")));
            sdf = new SimpleDateFormat("MM/dd/yyyy");
            dateString = sdf.format(calendar.getTime());
            sdf = new SimpleDateFormat("h:mm a");
            timeString = sdf.format(calendar.getTime());

            descriptionTextView.setText(initialIntent.getStringExtra("eventDescription"));
            dateTextView.setText(dateString);
            timeTextView.setText(timeString);
            timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

            sdf = new SimpleDateFormat(" - h:mm a");
            timeString = sdf.format(calendarTo.getTime());
            timeTwoTextView.setText(timeString);
            timeTwoPicker.setCurrentHour(calendarTo.get(Calendar.HOUR_OF_DAY));
            timeTwoPicker.setCurrentMinute(calendarTo.get(Calendar.MINUTE));

        } else if(initialIntent.hasExtra("portrait")) {
            eventId = Integer.parseInt(initialIntent.getStringExtra("eventId"));
            eventFreqId = Integer.parseInt(initialIntent.getStringExtra("eventFreqId"));
            calendar.setTimeInMillis(Long.parseLong(initialIntent.getStringExtra("startTime")));
            calendarTo.setTimeInMillis(Long.parseLong(initialIntent.getStringExtra("endTime")));
            sdf = new SimpleDateFormat("MM/dd/yyyy");
            dateString = sdf.format(calendar.getTime());
            sdf = new SimpleDateFormat("h:mm a");
            timeString = sdf.format(calendar.getTime());

            descriptionTextView.setText(initialIntent.getStringExtra("eventDescription"));
            dateTextView.setText(dateString);
            timeTextView.setText(timeString);
            timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

            sdf = new SimpleDateFormat(" - h:mm a");
            timeString = sdf.format(calendarTo.getTime());
            timeTwoTextView.setText(timeString);
            timeTwoPicker.setCurrentHour(calendarTo.get(Calendar.HOUR_OF_DAY));
            timeTwoPicker.setCurrentMinute(calendarTo.get(Calendar.MINUTE));

        } else {
            eventId = Integer.parseInt(getArguments().getString("eventId"));
            eventFreqId = Integer.parseInt(getArguments().getString("eventFreqId"));
            calendar.setTimeInMillis(Integer.parseInt(getArguments().getString("startTime")));
            calendarTo.setTimeInMillis(Integer.parseInt(getArguments().getString("endTime")));
            sdf = new SimpleDateFormat("MM/dd/yyyy");
            dateString = sdf.format(calendar.getTime());
            sdf = new SimpleDateFormat("h:mm a");
            timeString = sdf.format(calendar.getTime());

            descriptionTextView.setText(getArguments().getString("eventDescription"));
            dateTextView.setText(dateString);
            timeTextView.setText(timeString);
            sdf = new SimpleDateFormat(" - h:mm a");
            timeString = sdf.format(calendarTo.getTime());
            timeTwoTextView.setText(timeString);
        }

        ((DetailActivity) getActivity()).spinner.setSelection(eventFreqId);

        ((DetailActivity) getActivity()).spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eventFreqId = position;
                ((DetailActivity) getActivity()).newEventFreqId = String.valueOf(eventFreqId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
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
                ((DetailActivity) getActivity()).newCalendar = calendar;
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
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
                ((DetailActivity) getActivity()).newCalendar = calendar;
            }
        });

        timeTwoPicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
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
                ((DetailActivity) getActivity()).newCalendarTo = calendarTo;
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.delete_post_confirm));
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
                        cancelEvent();
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        descriptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = new EditText(getContext());
                editText.setText(descriptionTextView.getText().toString());
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Edit Title")
                        .setView(editText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                descriptionTextView.setText(editText.getText().toString());
                                ((DetailActivity) getActivity()).newEventDescription = editText.getText().toString();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });

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

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void cancelEvent() {
        SQLiteDatabase.loadLibs(getContext());

        dbHelper = new EnvisageDBHelper(getContext());
        db = dbHelper.getWritableDatabase(EnvisageDBHelper.DATABASE_PASSWORD);

        selection = EnvisageContract.Events._ID + " = ?";
        selectionArgs = new String[] {String.valueOf(eventId)};

        ContentValues values = new ContentValues();
        values.put(EnvisageContract.Events.COLUMN_SET, 0);
        db.update(
                EnvisageContract.Events.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        jobScheduler.cancel(eventId);

        ((DetailActivity) getActivity()).refresh();
    }
}
