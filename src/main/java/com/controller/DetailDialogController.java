package com.controller;

import com.model.ledger.LedgerItem;
import com.model.ledger.TxType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

    @Setter
    private com.router.Router router;

    @Setter
    private com.service.ledger.LedgerService ledgerService;

    private final NumberFormat yenFormat = NumberFormat.getCurrencyInstance(Locale.JAPAN);

    // ✅ 상세창에서 변경이 있었는지(추가/수정/삭제)
    private boolean changed = false;

    @FXML
    private void initialize() {
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

        amountCol.setCellFactory(col -> new TableCell<LedgerItem, Number>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(yenFormat.format(value.intValue()));
                }
            }
        });

        memoCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getMemo())
        );
    }

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

        incomeSumLabel.setText(yenFormat.format(income));
        expenseSumLabel.setText(yenFormat.format(expense));
    }

    /* ===== 버튼 이벤트 ===== */

    @FXML
    private void onAdd() {
        if (router == null || ledgerService == null) {
            System.out.println("Router 또는 LedgerService 미주입");
            return;
        }

        Stage owner = (Stage) dateLabel.getScene().getWindow();
        boolean saved = router.openEntryDialog(owner, date);

        if (saved) {
            changed = true; // ✅ 달력 갱신 필요
            List<LedgerItem> items = ledgerService.findByDate(date);
            table.setItems(FXCollections.observableArrayList(items));
            updateSums(items);
        }
    }

    @FXML
    private void onEdit() {
        LedgerItem selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("수정할 항목을 선택해 주세요.");
            return;
        }
        if (router == null || ledgerService == null) {
            System.out.println("Router 또는 LedgerService 미주입");
            return;
        }

        Stage owner = (Stage) dateLabel.getScene().getWindow();
        boolean saved = router.openEditDialog(owner, selected);

        if (saved) {
            changed = true; // ✅ 달력 갱신 필요
            List<LedgerItem> items = ledgerService.findByDate(date);
            table.setItems(FXCollections.observableArrayList(items));
            updateSums(items);
        }
    }

    @FXML
    private void onDelete() {
        LedgerItem selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("삭제할 항목을 선택해 주세요");
            return;
        }
        if (ledgerService == null) {
            System.out.println("LedgerService가 주입되지 않았습니다.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("선택한 항목을 삭제할까요?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        boolean ok = ledgerService.deleteById(selected.getId());
        if (!ok) {
            System.out.println("삭제 실패: 해당 id를 찾지 못했습니다.");
            return;
        }

        changed = true; // ✅ 달력 갱신 필요

        List<LedgerItem> items = ledgerService.findByDate(date);
        table.setItems(FXCollections.observableArrayList(items));
        updateSums(items);
        table.getSelectionModel().clearSelection();
    }

    @FXML
    public void onClose() {
        // ✅ 닫힐 때 Router에 “변경 여부” 통지 → Router가 달력 refresh
        if (router != null) {
            router.onDetailClosed(changed);
        }

        Stage stage = (Stage) dateLabel.getScene().getWindow();
        stage.close();
    }
}
