package com.controller;

import com.model.ledger.LedgerItem;
import com.model.ledger.TxType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

public class DetailDialogController {

    @FXML private Label dateLabel;

    @FXML private TableView<LedgerItem> table;
    @FXML private TableColumn<LedgerItem, String> typeCol;
    @FXML private TableColumn<LedgerItem, String> categoryCol;
    @FXML private TableColumn<LedgerItem, Number> amountCol;
    @FXML private TableColumn<LedgerItem, String> memoCol;

    @FXML private Label incomeSumLabel;
    @FXML private Label expenseSumLabel;

    private LocalDate date;

    // 나중에 "추가/수정" 버튼에서 사용할 예정
    @Setter
    private com.router.Router router;

    @Setter
    private com.service.ledger.LedgerService ledgerService;

    @FXML
    private void initialize() {
        // TableColumn이 어떤 값을 표시할지 연결
        typeCol.setCellValueFactory(cell -> {
            TxType type = cell.getValue().getType();
            String text = (type == TxType.INCOME) ? "수입" : "지출";
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        categoryCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getCategory())
        );

        amountCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getAmount())
        );

        memoCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getMemo())
        );
    }

    // Router가 날짜와 리스트를 넘겨주면 화면에 표시
    public void init(LocalDate date, List<LedgerItem> items) {
        this.date = date;
        dateLabel.setText(date.toString());

        table.setItems(FXCollections.observableArrayList(items));
        updateSums(items);
    }

    private void updateSums(List<LedgerItem> items) {
        int income = 0;
        int expense = 0;

        for (LedgerItem item : items) {
            if (item.getType() == TxType.INCOME) income += item.getAmount();
            else expense += item.getAmount();
        }

        incomeSumLabel.setText(String.valueOf(income));
        expenseSumLabel.setText(String.valueOf(expense));
    }

    /* ===== 버튼 이벤트 ===== */

    @FXML
    private void onAdd() {
        // 다음 단계에서 구현 (입력창 다시 띄우기)
        System.out.println("추가 클릭: " + date);
    }

    @FXML
    private void onEdit() {
        // 다음 단계에서 구현 (선택 항목 수정)
        LedgerItem selected = table.getSelectionModel().getSelectedItem();
        System.out.println("수정 클릭: " + selected);
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) dateLabel.getScene().getWindow();
        stage.close();
    }
}
