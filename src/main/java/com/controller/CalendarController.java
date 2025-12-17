package com.controller;

import com.model.calendar.CalendarCell;
import com.model.calendar.DayOfWeekCell;
import com.router.Router;
import com.service.calendar.CalendarService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import lombok.Setter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.config.CalendarUIConfig.*;

public class CalendarController {

    @FXML
    private Button prevMonthButton;
    @FXML
    private Button nextMonthButton;
    @FXML
    private Label monthLabel;
    @FXML
    private GridPane calendarGrid;

    private final CalendarService calendarService = new CalendarService();
    private YearMonth currentYearMonth;

    // 선택된 날짜 셀 (선택 표시용)
    private StackPane selectedCell;

    // Router 주입 (Router.openCalendar()에서 controller.setRouter(this)로 넣어줄 것)
    // 화면 이동/팝업 담당 Router
    @Setter
    private Router router;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        renderMonth();

        prevMonthButton.setOnAction(e -> moveMonth(-1));
        nextMonthButton.setOnAction(e -> moveMonth(1));
    }

    /* ===================== 월 이동 ===================== */

    private void moveMonth(int offset) {
        currentYearMonth = currentYearMonth.plusMonths(offset);
        renderMonth();
    }

    /* ===================== 달력 렌더링 ===================== */

    private void renderMonth() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(formatMonth(currentYearMonth));

        // 1) 요일 헤더 (row=0)
        for (DayOfWeekCell d : calendarService.generateDayOfWeek()) {
            Label lbl = new Label(d.getLabel());
            lbl.setPrefSize(CELL_WIDTH, DAY_OF_WEEK_HEIGHT);
            calendarGrid.add(lbl, d.getCol(), 0);
        }

        // 2) 날짜 데이터
        List<CalendarCell> monthCells = calendarService.generateMonth(currentYearMonth);

        // 3) 항상 6주(42칸) 채우기
        final int TOTAL = DAYS_IN_WEEK * MAX_ROWS;
        CalendarCell[] grid = new CalendarCell[TOTAL];

        for (CalendarCell c : monthCells) {
            int index = c.getRow() * DAYS_IN_WEEK + c.getCol();
            if (index >= 0 && index < TOTAL) {
                grid[index] = c;
            }
        }

        for (int index = 0; index < TOTAL; index++) {
            int col = index % DAYS_IN_WEEK;
            int row = index / DAYS_IN_WEEK;

            CalendarCell data = grid[index];

            StackPane cell = (data == null)
                    ? createEmptyCell()
                    : createDateCell(data.getDate());

            // 날짜 영역은 row + 1 (요일이 0행이니까)
            calendarGrid.add(cell, col, row + 1);
        }
    }

    private StackPane createDateCell(LocalDate date) {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);

        Label lbl = new Label(String.valueOf(date.getDayOfMonth()));
        cell.getChildren().add(lbl);

        cell.setOnMouseClicked(e -> onDateClicked(date, cell));
        return cell;
    }

    /* ===================== 날짜 클릭 ===================== */

    private void onDateClicked(LocalDate date, StackPane clickedCell) {
        if (selectedCell != null) {
            selectedCell.getStyleClass().remove("selected-cell");
        }
        clickedCell.getStyleClass().add("selected-cell");
        selectedCell = clickedCell;

        // ✅ 날짜 클릭 처리(분기)는 Router가 담당
        router.onDateClicked(calendarGrid.getScene().getWindow(), date);
    }

    /* ===================== 유틸 ===================== */

    private String formatMonth(YearMonth ym) {
        return ym.getYear() + "년 " + ym.getMonthValue() + "월";
    }

    private StackPane createEmptyCell() {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        return cell;
    }
}
