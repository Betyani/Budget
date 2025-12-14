package com.controller;

import com.model.calendar.CalendarCell;
import com.model.calendar.DayOfWeekCell;
import com.service.calendar.CalendarService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class CalendarController {

    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private Label monthLabel;
    @FXML private GridPane dayOfWeekGrid;
    @FXML private GridPane calendarGrid;

    private final CalendarService calendarService = new CalendarService();
    private YearMonth currentYearMonth;

    // 선택된 날짜 셀 (선택 표시용)
    private StackPane selectedCell;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();

        renderDayOfWeek();   // 요일 헤더
        renderMonth();       // 달력 날짜

        prevMonthButton.setOnAction(e -> moveMonth(-1));
        nextMonthButton.setOnAction(e -> moveMonth(1));
    }

    /* ===================== 월 이동 ===================== */

    private void moveMonth(int offset) {
        currentYearMonth = currentYearMonth.plusMonths(offset);
        renderMonth();
    }

    /* ===================== 요일 헤더 ===================== */

    private void renderDayOfWeek() {
        dayOfWeekGrid.getChildren().clear();

        List<DayOfWeekCell> days = calendarService.generateDayOfWeek();
        for (DayOfWeekCell d : days) {
            Label lbl = new Label(d.getLabel());
            lbl.setMinSize(40, 20);

            dayOfWeekGrid.add(lbl, d.getCol(), d.getRow());
        }
    }

    /* ===================== 달력 렌더링 ===================== */

    private void renderMonth() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(formatMonth(currentYearMonth));

        List<CalendarCell> cells =
                calendarService.generateMonth(currentYearMonth);

        for (CalendarCell data : cells) {
            StackPane cell = createDateCell(data.getDate());
            calendarGrid.add(cell, data.getCol(), data.getRow());
        }
    }

    private StackPane createDateCell(LocalDate date) {
        StackPane cell = new StackPane();

        Label lbl = new Label(String.valueOf(date.getDayOfMonth()));
        lbl.setMinSize(40, 40);

        cell.getChildren().add(lbl);

        cell.setOnMouseClicked(e -> onDateClicked(date, cell));

        return cell;
    }

    /* ===================== 날짜 클릭 ===================== */

    private void onDateClicked(LocalDate date, StackPane clickedCell) {
        // 선택 상태 관리 (스타일은 CSS에서 처리)
        if (selectedCell != null) {
            selectedCell.getStyleClass().remove("selected-cell");
        }
        clickedCell.getStyleClass().add("selected-cell");
        selectedCell = clickedCell;

        System.out.println("클릭한 날짜: " + date);
        // TODO: 수입/지출 입력 팝업 연결
    }

    /* ===================== 유틸 ===================== */

    private String formatMonth(YearMonth ym) {
        return ym.getYear() + "년 " + ym.getMonthValue() + "월";
    }
}
