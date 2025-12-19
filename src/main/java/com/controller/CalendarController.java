package com.controller;

import com.model.calendar.CalendarCell;
import com.model.calendar.DayOfWeekCell;
import com.model.ledger.LedgerItem;
import com.model.ledger.LedgerSum;
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
    @FXML private Label monthIncome;
    @FXML private Label monthExpense;
    @FXML private GridPane calendarGrid;

    private final CalendarService calendarService = new CalendarService();
    private final LedgerService ledgerService = new LedgerService();

    private YearMonth currentYearMonth;
    private StackPane selectedCell;

    // ✅ 이번 달 총합(수입/지출)
    private LedgerSum monthTotal = new LedgerSum();

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

        // ✅ 요일 헤더 (row=0)
        for (DayOfWeekCell d : calendarService.generateDayOfWeek()) {
            StackPane headerCell = new StackPane();
            headerCell.setPrefSize(CELL_WIDTH, DAY_OF_WEEK_HEIGHT);
            headerCell.getStyleClass().add("dow-cell");

            Label lbl = new Label(d.getLabel());
            lbl.getStyleClass().add("dow-label");

            headerCell.getChildren().add(lbl);

            calendarGrid.add(headerCell, d.getCol(), 0);
        }

        // ✅ 날짜 데이터
        List<CalendarCell> monthCells = calendarService.generateMonth(currentYearMonth);

        // ✅ 월 데이터 1번 로드 → 날짜별 합계 Map + 월 총합 계산
        Map<LocalDate, LedgerSum> sumMap = buildSumMapForMonth(currentYearMonth);

        // ✅ 월 총 수입/지출 라벨 갱신
        updateMonthTotalLabels();

        // ✅ 항상 6주(42칸) 채우기
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

    private void updateMonthTotalLabels() {
        int income = monthTotal.getIncome();
        int expense = monthTotal.getExpense();

        monthIncome.setText(String.format("월수입: +%,d円", income));
        monthExpense.setText(String.format("월지출: -%,d円", expense));
    }

    private StackPane createDateCell(LocalDate date, LedgerSum sum) {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);

        // ✅ 기본 셀 스타일(종이 칸 느낌)
        cell.getStyleClass().add("date-cell");

        // ✅ 이번 달/다른 달 표시
        if (!YearMonth.from(date).equals(currentYearMonth)) {
            cell.getStyleClass().add("outside-month");
        }

        // ✅ 오늘 표시
        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("today");
        }

        VBox box = new VBox();
        box.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        box.setSpacing(2);
        box.setAlignment(Pos.TOP_LEFT);

        // 날짜 숫자
        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.getStyleClass().add("date-number");

        // 금액 라벨(오른쪽 정렬)
        Label incomeLabel = new Label();
        incomeLabel.setMaxWidth(Double.MAX_VALUE);
        incomeLabel.setAlignment(Pos.CENTER_RIGHT);
        incomeLabel.getStyleClass().add("amount-income");

        Label expenseLabel = new Label();
        expenseLabel.setMaxWidth(Double.MAX_VALUE);
        expenseLabel.setAlignment(Pos.CENTER_RIGHT);
        expenseLabel.getStyleClass().add("amount-expense");

        int income = (sum == null) ? 0 : sum.getIncome();
        int expense = (sum == null) ? 0 : sum.getExpense();

        incomeLabel.setText(income > 0 ? String.format("+%,d円", income) : "");
        expenseLabel.setText(expense > 0 ? String.format("-%,d円", expense) : "");

        box.getChildren().addAll(dayLabel, incomeLabel, expenseLabel);
        cell.getChildren().add(box);

        // ✅ 클릭 시 선택 표시 + 라우터 호출
        cell.setOnMouseClicked(e -> onDateClicked(date, cell));

        return cell;
    }

    /**
     * ✅ 날짜별 합계(map) + 월 총합(monthTotal)을 한 번에 계산
     */
    private Map<LocalDate, LedgerSum> buildSumMapForMonth(YearMonth ym) {
        List<LedgerItem> items = ledgerService.findByMonth(ym);

        // ✅ renderMonth 재호출 때 누적 방지
        monthTotal = new LedgerSum();

        Map<LocalDate, LedgerSum> map = new HashMap<>();
        for (LedgerItem item : items) {
            LocalDate d = item.getDate();

            LedgerSum daySum = map.computeIfAbsent(d, k -> new LedgerSum());

            if (item.getType() == TxType.INCOME) {
                daySum.addIncome(item.getAmount());
                monthTotal.addIncome(item.getAmount());
            } else {
                daySum.addExpense(item.getAmount());
                monthTotal.addExpense(item.getAmount());
            }
        }

        return map;
    }

    private void onDateClicked(LocalDate date, StackPane clickedCell) {
        // ✅ 이전 선택 제거
        if (selectedCell != null) {
            selectedCell.getStyleClass().remove("selected");
        }

        // ✅ 새 선택 적용
        selectedCell = clickedCell;
        selectedCell.getStyleClass().add("selected");

        if (router != null) {
            router.onDateClicked(calendarGrid.getScene().getWindow(), date);
        } else {
            System.out.println("Router가 주입되지 않았습니다.");
        }
    }

    // Router가 호출해서 달력 최신화 (추가/수정/삭제 후)
    public void refreshMonth() {
        // 선택 표시가 남아있으면, 다시 렌더링되며 node가 갈아끼워지니까 null로 초기화 추천
        selectedCell = null;
        renderMonth();
    }

    private String formatMonth(YearMonth ym) {
        return ym.getYear() + "년 " + ym.getMonthValue() + "월";
    }

    private StackPane createEmptyCell() {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);

        // 빈칸도 테두리 통일하면 “탁상달력 칸” 느낌이 살아남
        cell.getStyleClass().add("date-cell");
        cell.getStyleClass().add("outside-month"); // 살짝 흐리게

        return cell;
    }
}
