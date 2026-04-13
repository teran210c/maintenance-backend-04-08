package com.pegatron.maintenance.dto;

public class ConformanceHistoryDTO {

    private String date;
    private int score;

    public ConformanceHistoryDTO(String date, int score) {
        this.date = date;
        this.score = score;
    }

    public String getDate() { return date; }
    public int getScore() { return score; }

}