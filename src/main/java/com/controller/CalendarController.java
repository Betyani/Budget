package com.controller;

import com.model.calendar.CalendarCell;
import com.model.calendar.DayOfWeekCell;
import com.model.ledger.DailySum;
import com.model.ledger.LedgerItem;
import com.model.ledger.TxType;
import com.router.Router;
import com.service.calendar.CalendarService;
import com.service.ledger.LedgerService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.config.CalendarUIConfig.*;

public class CalendarController {

    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private Label monthLabel;
    @FXML private GridPane calendarGrid;

    private final CalendarService calendarService = new CalendarService();
    private final LedgerService ledgerService = new LedgerService();

    private YearMonth currentYearMonth;
    private StackPane selectedCell;

    @Setter
    private Router router;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        renderMonth();

        prevMonthButton.setOnAction(e -> moveMonth(-1));
        nextMonthButton.setOnAction(e -> moveMonth(1));
    }

    private void moveMonth(int offset) {
        currentYearMonth = currentYearMonth.plusMonths(offset);
        renderMonth();
    }

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

        // ✅ 월 데이터 1번 로드 → 날짜별 합계 Map
        Map<LocalDate, DailySum> sumMap = buildSumMapForMonth(currentYearMonth);

        // 3) 항상 6주(42칸) 채우기
        final int TOTAL = DAYS_IN_WEEK * MAX_ROWS;
        CalendarCell[] grid = new CalendarCell[TOTAL];

        for (CalendarCell c : monthCells) {
            int index = c.getRow() * DAYS_IN_WEEK + c.getCol();
            if (index >= 0 && index < TOTAL) grid[index] = c;
        }

        for (int index = 0; index < TOTAL; index++) {
            int col = index % DAYS_IN_WEEK;
            int row = index / DAYS_IN_WEEK;

            CalendarCell data = grid[index];

            StackPane cell = (data == null)
                    ? createEmptyCell()
                    : createDateCell(data.getDate(), sumMap.get(data.getDate()));

            calendarGrid.add(cell, col, row + 1); // 요일이 0행이니까 +1
        }
    }

    private StackPane createDateCell(LocalDate date, DailySum sum) {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);

        VBox box = new VBox();
        box.setPrefSize(CELL_WIDTH, CELL_HEIGHT);

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));

        Label incomeLabel = new Label();
        incomeLabel.setMaxWidth(Double.MAX_VALUE);
        incomeLabel.setAlignment(Pos.CENTER_RIGHT);

        Label expenseLabel = new Label();
        expenseLabel.setMaxWidth(Double.MAX_VALUE);
        expenseLabel.setAlignment(Pos.CENTER_RIGHT);

        int income = (sum == null) ? 0 : sum.getIncome();
        int expense = (sum == null) ? 0 : sum.getExpense();

        incomeLabel.setText(income > 0 ? String.format("+%,d円", income) : "");
        expenseLabel.setText(expense > 0 ? String.format("-%,d円", expense) : "");

        box.getChildren().addAll(dayLabel, incomeLabel, expenseLabel);
        cell.getChildren().add(box);

        cell.setOnMouseClicked(e -> onDateClicked(date, cell));
        return cell;
    }

    private Map<LocalDate, DailySum> buildSumMapForMonth(YearMonth ym) {
        List<LedgerItem> items = ledgerService.findByMonth(ym);

        Map<LocalDate, DailySum> map = new HashMap<>();
        for (LedgerItem item : items) {
            LocalDate d = item.getDate();

            DailySum sum = map.computeIfAbsent(d, k -> new DailySum());
            if (item.getType() == TxType.INCOME) sum.addIncome(item.getAmount());
            else sum.addExpense(item.getAmount());
        }
        return map;
    }

    private void onDateClicked(LocalDate date, StackPane clickedCell) {
        if (selectedCell != null) {
            selectedCell.getStyleClass().remove("selected-cell");
        }
        clickedCell.getStyleClass().add("selected-cell");
        selectedCell = clickedCell;

        if (router != null) {
            router.onDateClicked(calendarGrid.getScene().getWindow(), date);
        } else {
            System.out.println("Router가 주입되지 않았습니다.");
        }
    }

    // ✅ Router가 호출해서 달력 최신화
    public void refreshMonth() {
        renderMonth();
    }

    private String formatMonth(YearMonth ym) {
        return ym.getYear() + "년 " + ym.getMonthValue() + "월";
    }

    private StackPane createEmptyCell() {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        return cell;
    }
}
