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
            if (item.getType() == TxType.INCOME) {
                income += item.getAmount();
            } else {
                expense += item.getAmount();
            }
        }

        incomeSumLabel.setText(String.valueOf(income));
        expenseSumLabel.setText(String.valueOf(expense));
    }

    /* ===== 버튼 이벤트 ===== */

    @FXML
    private void onAdd() {
        if (router == null || ledgerService == null) {
            System.out.println("Router 또는 LedgerService 미주입");
            return;
        }

        // 현재 상세창의 owner 가져오기
        Stage owner = (Stage) dateLabel.getScene().getWindow();

        boolean saved = router.openEntryDialog(owner, date);

        if (saved) {
            // 다시 데이터 불러와서 갱신
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

        Stage owner = (Stage) dateLabel.getScene().getWindow();

        boolean saved = router.openEditDialog(owner, selected);

        if (saved) {
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

        // 1) 삭제 확인창
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("선택한 항목을 삭제할까요?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return; // 취소
        }

        // 2) 삭제
        boolean ok = ledgerService.deleteById(selected.getId());
        if (!ok) {
            System.out.println("삭제 실패: 해당 id를 찾지 못했습니다.");
            return;
        }

        // 3) 화면 갱신 (현재 날짜 다시 로드)
        List<LedgerItem> items = ledgerService.findByDate(date);
        table.setItems(FXCollections.observableArrayList(items));
        updateSums(items);

        // 4) 선택 해제(옵션)
        table.getSelectionModel().clearSelection();
    }


    @FXML
    private void onClose() {
        Stage stage = (Stage) dateLabel.getScene().getWindow();
        stage.close();
    }
}
