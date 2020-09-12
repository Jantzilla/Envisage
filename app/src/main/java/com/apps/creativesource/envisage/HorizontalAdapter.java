package com.apps.creativesource.envisage;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.PollsViewHolder> {
    private ArrayList<Event> eventArrayList = new ArrayList<>();
    private ListItemClickListener clickListener;
    public Event event;

    public int getPositionByTime(Calendar time) {
        long leastTimeDifference = DateUtils.HOUR_IN_MILLIS * 12;
        int eventPosition = 0;
        Calendar calendar = Calendar.getInstance();
        for(int i = 0; i < getItemCount(); i++) {
            calendar.setTimeInMillis(eventArrayList.get(i).startTime);
            calendar.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DATE));

            if(Math.abs(calendar.getTimeInMillis() - time.getTimeInMillis()) < leastTimeDifference) {
                leastTimeDifference = Math.abs(calendar.getTimeInMillis() - time.getTimeInMillis());
                eventPosition = i;
            }
        }
        return eventPosition;
    }

    public interface ListItemClickListener{
        void onListItemClicked(int clickedItemIndex, int eventId, String eventDescription,
                               Long startTime, Long endTime, int eventFreqId);
    }


    public HorizontalAdapter(ArrayList<Event> eventArrayList, ListItemClickListener clickListener) {
        this.eventArrayList.addAll(eventArrayList);
        this.clickListener = clickListener;

    }

    public ArrayList<Event> getEventArrayList() {
        return eventArrayList;
    }

    @NonNull
    @Override
    public PollsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        View view = inflater.inflate(R.layout.trend_item, viewGroup, false);

        PollsViewHolder viewHolder = new PollsViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalAdapter.PollsViewHolder pollsViewHolder, int i) {

        if(eventArrayList.isEmpty())
            return;

        pollsViewHolder.eventFreqId = eventArrayList.get(i).eventFreqId;
        pollsViewHolder.startTime = eventArrayList.get(i).startTime;
        pollsViewHolder.endTime = eventArrayList.get(i).endTime;
        pollsViewHolder.eventId = (int) eventArrayList.get(i).eventId;
        pollsViewHolder.eventDescription = eventArrayList.get(i).eventDescription;

        pollsViewHolder.descriptionTextView.setText(pollsViewHolder.eventDescription);
        pollsViewHolder.descriptionTextView.setTag(pollsViewHolder.eventId);

    }

    @Override
    public int getItemCount() {
        return eventArrayList.size();
    }


    class PollsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView descriptionTextView;
        private String eventDescription;
        private int eventId, eventFreqId;
        private long startTime, endTime;

        public PollsViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.tv_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            clickListener.onListItemClicked(clickedPosition, eventId, eventDescription, startTime, endTime, eventFreqId);
        }
    }

    public int getPositionByString(CharSequence string) {
        for(int i = 0; i < getItemCount(); i++) {
            if(eventArrayList.get(i).eventDescription.toLowerCase().contains(string.toString().toLowerCase())) {
                event = eventArrayList.get(i);
                return i;
            }
        }
        event = null;
        return - 1;
    }

    public long getIdenticalEventId(String string) {
        for(int i = 0; i < getItemCount(); i++) {
            if(eventArrayList.get(i).eventDescription.equalsIgnoreCase(string))
                return eventArrayList.get(i).eventId;
        }
        return -1;
    }
}
