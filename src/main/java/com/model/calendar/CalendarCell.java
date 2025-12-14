package com.model.calendar;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CalendarCell {
    private LocalDate date; // 실제 날짜
    private int col; // x축
    private int row; // y축
}
