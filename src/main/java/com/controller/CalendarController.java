package com.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.YearMonth;

public class CalendarController {

    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private Label monthLabel;
    @FXML private GridPane calendarGrid;

    private YearMonth currentYearMonth;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        drawCalendar(currentYearMonth);

        prevMonthButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            drawCalendar(currentYearMonth);
        });

        nextMonthButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            drawCalendar(currentYearMonth);
        });
    }

    private void drawCalendar(YearMonth yearMonth) {
        // 기존 그리드 내용 싹 비우기
        calendarGrid.getChildren().clear();

        // 상단에 "2025년 12월" 이런 식으로 표시
        monthLabel.setText(yearMonth.getYear() + "년 " + yearMonth.getMonthValue() + "월");

        // 요일 헤더 (첫 번째 줄)
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(days[i]);
            lbl.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            lbl.setMinSize(40, 20);
            calendarGrid.add(lbl, i, 0);
        }

        // 이번 달 1일이 무슨 요일인지
        LocalDate firstDay = yearMonth.atDay(1);
        int startCol = firstDay.getDayOfWeek().getValue() % 7; // 일요일 = 0
        int daysInMonth = yearMonth.lengthOfMonth();

        int col = startCol;
        int row = 1;

        for (int day = 1; day <= daysInMonth; day++) {
            Label lbl = new Label(String.valueOf(day));
            lbl.setMinSize(40, 40);
            lbl.setStyle("-fx-border-color: lightgray; -fx-alignment: center;");

            calendarGrid.add(lbl, col, row);

            col++;
            if (col > 6) { // 토요일 넘어가면 줄바꿈
                col = 0;
                row++;
            }
        }
    }
}
