package com.model.calendar;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DayOfWeekCell {
    private String label; // "일", "월" ...
    private int col;      // 열 위치
    private int row;      // 항상 0
}