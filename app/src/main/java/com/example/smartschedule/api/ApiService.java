package com.example.smartschedule.api;

import com.example.smartschedule.dto.ScheduleDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("getFaculties")
    Call<List<String>> getFaculties();

    @GET("getGroups")
    Call<List<String>> getGroups(@Query("facultyId") Long facultyId);

    @GET("getScheduleForDay")
    Call<List<ScheduleDTO>> getScheduleForDay(@Query("groupId") Long groupId, @Query("date") String date);

    @GET("getScheduleForWeek")
    Call<List<ScheduleDTO>> getScheduleForWeek(@Query("groupId") Long groupId, @Query("startDate") String startDate);

    @GET("getScheduleForMonth")
    Call<List<ScheduleDTO>> getScheduleForMonth(@Query("groupId") Long groupId, @Query("startDate") String startDate);
}
