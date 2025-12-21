package com.service.calendar;

import com.model.calendar.CalendarCell;
import com.model.calendar.DayOfWeekCell;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static com.config.CalendarUIConfig.*;

public class CalendarService {

    public List<CalendarCell> generateMonth(YearMonth yearMonth) {
        List<CalendarCell> cells = new ArrayList<>();

        LocalDate firstDay = yearMonth.atDay(1); // 해당하는 달의 1일을 가져옴
        int startCol = firstDay.getDayOfWeek().getValue() % DAYS_IN_WEEK; // DayOfWeek: 월=1~토=6, 일=7 → %7 해서 일요일만 0으로 변환
        int daysInMonth = yearMonth.lengthOfMonth(); // 해당하는 달의 마지막 날이 몇 일인지 확인

        int col = startCol;
        int row = 0;

        for(int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            cells.add(new CalendarCell(date, col, row));

            col++;
            if(col > 6) {
                col = 0;
                row ++;
            }
        }

        return cells;
    }

    public List<DayOfWeekCell> generateDayOfWeek(){
        List<DayOfWeekCell> list = new ArrayList<>();

        String[] days = {"日", "月", "火", "水", "木", "金", "土"};
        for (int i = 0; i < 7; i++) {
            list.add(new DayOfWeekCell(days[i], i, HEADER_ROW));
        }

        return list;
    }

}
