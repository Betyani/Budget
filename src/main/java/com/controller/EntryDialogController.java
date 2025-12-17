package com.controller;

import com.model.ledger.LedgerItem;
import com.model.ledger.TxType;
import com.service.ledger.LedgerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.time.LocalDate;

public class EntryDialogController {

    /* ===== FXML 주입 ===== */
    @FXML private Label dateLabel;
    @FXML private RadioButton incomeRadio;
    @FXML private RadioButton expenseRadio;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField amountField;
    @FXML private TextArea memoArea;

    /* ===== 내부 상태 ===== */
    private LocalDate date;

    /* ===== 주입받을 서비스 ===== */
    @Setter
    private LedgerService ledgerService;

    @FXML
    private void initialize() {
        ToggleGroup typeGroup = new ToggleGroup();
        incomeRadio.setToggleGroup(typeGroup);
        expenseRadio.setToggleGroup(typeGroup);

        expenseRadio.setSelected(true);

        categoryCombo.getItems().addAll("식비", "교통", "생활", "쇼핑", "월급", "부수입", "기타");
    }

    public void init(LocalDate date) {
        this.date = date;
        dateLabel.setText(date.toString());
    }

    @FXML
    private void onCancel() {
        close();
    }

    @FXML
    private void onSave() {
        if (ledgerService == null) {
            alert("LedgerService가 주입되지 않았습니다.");
            return;
        }

        String category = categoryCombo.getValue();
        String amountText = amountField.getText();

        if (category == null || category.isBlank()) {
            alert("항목을 선택해 주세요.");
            return;
        }
        if (amountText == null || amountText.isBlank() || !amountText.matches("\\d+")) {
            alert("금액은 숫자만 입력해 주세요.");
            return;
        }

        TxType type = incomeRadio.isSelected() ? TxType.INCOME : TxType.EXPENSE;
        int amount = Integer.parseInt(amountText);
        String memo = memoArea.getText() == null ? "" : memoArea.getText();

        LedgerItem item = LedgerItem.builder()
                .date(date)
                .type(type)
                .category(category)
                .amount(amount)
                .memo(memo)
                .build();

        ledgerService.add(item);
        close();
    }

    private void close() {
        Stage stage = (Stage) dateLabel.getScene().getWindow();
        stage.close();
    }

    private void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
