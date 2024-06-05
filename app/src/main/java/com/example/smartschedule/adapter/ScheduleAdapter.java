package com.example.smartschedule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartschedule.R;
import com.example.smartschedule.dto.ScheduleDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_SCHEDULE = 1;

    private final List<Object> consolidatedList;

    public ScheduleAdapter(List<ScheduleDTO> scheduleList) {
        this.consolidatedList = new ArrayList<>();
        organizeData(scheduleList);
    }

    @Override
    public int getItemViewType(int position) {
        if (consolidatedList.get(position) instanceof String) {
            return VIEW_TYPE_DATE;
        } else {
            return VIEW_TYPE_SCHEDULE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_header_item,
                    parent, false);
            return new DateViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item,
                    parent, false);
            return new ScheduleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).bind((String) consolidatedList.get(position));
        } else {
            ((ScheduleViewHolder) holder).bind((ScheduleDTO) consolidatedList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return consolidatedList.size();
    }

    private void organizeData(List<ScheduleDTO> scheduleList) {
        Map<String, List<ScheduleDTO>> groupedByDate = new LinkedHashMap<>();

        for (ScheduleDTO schedule : scheduleList) {
            String date = schedule.getDate();
            if (!groupedByDate.containsKey(date)) {
                groupedByDate.put(date, new ArrayList<>());
            }
            groupedByDate.get(date).add(schedule);
        }

        List<String> dates = new ArrayList<>(groupedByDate.keySet());
        Collections.sort(dates, new Comparator<String>() {
            @Override
            public int compare(String date1, String date2) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
                            Locale.getDefault());
                    Date d1 = dateFormat.parse(date1);
                    Date d2 = dateFormat.parse(date2);
                    return d1.compareTo(d2);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("uk"));

        for (String date : dates) {
            List<ScheduleDTO> schedules = groupedByDate.get(date);
            try {
                Date parsedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
                String dayOfWeek = dayFormat.format(parsedDate);
                consolidatedList.add(capitalizeFirstLetter(dayOfWeek) + ", " + date);
            } catch (Exception e) {
                e.printStackTrace();
                consolidatedList.add(date);
            }
            consolidatedList.addAll(schedules);
        }
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;

        DateViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        void bind(String date) {
            dateTextView.setText(date);
        }
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView bgNumberTextView;
        TextView timeTextView;
        TextView classTitleTextView;
        TextView lecturerTextView;
        TextView auditoriumTextView;

        ScheduleViewHolder(View view) {
            super(view);
            bgNumberTextView = view.findViewById(R.id.bgNumber);
            timeTextView = view.findViewById(R.id.timeTextView);
            classTitleTextView = view.findViewById(R.id.classTitle);
            lecturerTextView = view.findViewById(R.id.lecturer);
            auditoriumTextView = view.findViewById(R.id.auditorium);
        }

        void bind(ScheduleDTO schedule) {
            String startTime = schedule.getStartTime() != null ? schedule.getStartTime().toString() : "";
            String endTime = schedule.getEndTime() != null ? schedule.getEndTime().toString() : "";
            String classTitle = schedule.getSubject() != null ? schedule.getSubject() : "";
            String lecturer = schedule.getTeacher() != null ? schedule.getTeacher() : "";
            String auditorium = schedule.getRoom() != null ? schedule.getRoom() : "";

            timeTextView.setText(startTime + " - " + endTime);
            classTitleTextView.setText("предмет : " + classTitle);
            lecturerTextView.setText("викладач : " + lecturer);
            auditoriumTextView.setText("аудиторія : " + auditorium);
            bgNumberTextView.setText(getPairNumber(startTime));
        }

        private String getPairNumber(String startTime) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date start = timeFormat.parse(startTime);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(start);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                if (hour == 8 && minute == 0) {
                    return "1 пара";
                } else if (hour == 9 && minute == 45) {
                    return "2 пара";
                } else if (hour == 11 && minute == 45) {
                    return "3 пара";
                } else if (hour == 13 && minute == 30) {
                    return "4 пара";
                } else if (hour == 15 && minute == 15) {
                    return "5 пара";
                } else {
                    return "";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}
