package com.apps.creativesource.envisage;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.PollsViewHolder> {
    private boolean first;
    private ArrayList<Event> eventArrayList = new ArrayList<>();
    private boolean twoPane;
    private ListItemClickListener clickListener;

    public interface ListItemClickListener{
        void onListItemClicked(int clickedItemIndex, int eventId, String eventDescription,
                               Long startTime, Long endTime, int usedCount, int eventFreqId);
    }


    public EventsAdapter(ArrayList<Event> eventArrayList, boolean first, boolean twoPane, ListItemClickListener clickListener) {
        this.eventArrayList.addAll(eventArrayList);
        this.clickListener = clickListener;
        this.twoPane = twoPane;
        this.first = first;
    }

    @NonNull
    @Override
    public PollsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        View view = inflater.inflate(R.layout.event_item, viewGroup, false);

        PollsViewHolder viewHolder = new PollsViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventsAdapter.PollsViewHolder pollsViewHolder, int i) {

            if(eventArrayList.isEmpty())
                return;

        pollsViewHolder.startTime = eventArrayList.get(i).startTime;
        pollsViewHolder.endTime = eventArrayList.get(i).endTime;
        pollsViewHolder.usedCount = eventArrayList.get(i).usedCount;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(pollsViewHolder.startTime);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String dateString = sdf.format(calendar.getTime());
        sdf = new SimpleDateFormat("h:mm a");
        String timeString = sdf.format(calendar.getTime());

        pollsViewHolder.eventFreqId = eventArrayList.get(i).eventFreqId;
        String eventFrequency = "ONCE";

        switch(pollsViewHolder.eventFreqId) {
            case 0:
                eventFrequency = "ONCE";
                break;
            case 1:
                eventFrequency = "DAILY";
                break;
            case 2:
                eventFrequency = "WEEKLY";
                break;
            case 3:
                eventFrequency = "BI-WEEKLY";
                break;
            case 4:
                eventFrequency = "MONTHLY";
                break;
            case 5:
                eventFrequency = "YEARLY";
                break;
        }

        pollsViewHolder.eventId = (int) eventArrayList.get(i).eventId;
        pollsViewHolder.eventDescription = eventArrayList.get(i).eventDescription;

        if(first && twoPane && i == 0 && !(eventArrayList.get(i).eventDescription.equals("Test"))) {
            pollsViewHolder.itemView.performClick();
        }

        pollsViewHolder.descriptionTextView.setText(pollsViewHolder.eventDescription);
        pollsViewHolder.frequencyTextView.setText(eventFrequency);
        pollsViewHolder.dateTextView.setText(dateString);
        pollsViewHolder.timeTextView.setText(timeString);

        sdf = new SimpleDateFormat(" - h:mm a");
        calendar.setTimeInMillis(pollsViewHolder.endTime);
        timeString = sdf.format(calendar.getTime());
        pollsViewHolder.timeTwoTextView.setText(timeString);

    }

    @Override
    public int getItemCount() {
        return eventArrayList.size();
    }


    class PollsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView descriptionTextView;
        private TextView dateTextView;
        private TextView timeTextView, timeTwoTextView;
        private TextView frequencyTextView;
        private Long startTime, endTime;
        private String eventDescription;
        private int eventId, eventFreqId, usedCount;

        public PollsViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.tv_description);
            dateTextView = itemView.findViewById(R.id.tv_date);
            timeTextView = itemView.findViewById(R.id.tv_time);
            timeTwoTextView = itemView.findViewById(R.id.tv_time_two);
            frequencyTextView = itemView.findViewById(R.id.tv_event_frequency);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            clickListener.onListItemClicked(clickedPosition, eventId, eventDescription, startTime, endTime, usedCount, eventFreqId);
        }
    }
}
