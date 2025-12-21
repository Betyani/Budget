package com.controller;

import com.model.ledger.LedgerItem;
import com.model.ledger.TxType;
import com.service.ledger.LedgerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.function.UnaryOperator;

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

    // ✅ 수정모드 관련 상태
    private boolean editMode = false;
    private java.util.UUID editingId = null;

    private static final int AMOUNT_MAX_DIGITS = 8;          // 9자리 제한
    private static final long AMOUNT_MAX_VALUE = 100_000_000L; // 10억 미만 같은 상한
    private static final int MEMO_MAX_LEN = 12;

    // ✅ Router가 결과 확인할 수 있게
    @Getter
    private boolean saved = false;


    /* ===== 주입받을 서비스 ===== */
    @Setter
    private LedgerService ledgerService;

    @FXML
    private void initialize() {
        ToggleGroup typeGroup = new ToggleGroup();
        incomeRadio.setToggleGroup(typeGroup);
        expenseRadio.setToggleGroup(typeGroup);

        expenseRadio.setSelected(true);

        categoryCombo.getItems().addAll("食費", "交通費", "生活費", "買い物", "給料", "副収入", "その他");
        setupAmountFormatter();
        setupMemoFormatter();
    }

    public void init(LocalDate date) {
        this.date = date;
        dateLabel.setText(date.toString());
    }

    // ✅ 수정 모드 초기화 (기존 값을 화면에 채움)
    public void initForEdit(LedgerItem item) {
        this.editMode = true;
        this.editingId = item.getId();

        this.date = item.getDate();
        dateLabel.setText(date.toString());

        // 라디오 버튼
        if (item.getType() == TxType.INCOME) incomeRadio.setSelected(true);
        else expenseRadio.setSelected(true);

        // 입력값 채우기
        categoryCombo.setValue(item.getCategory());
        amountField.setText(String.valueOf(item.getAmount()));
        memoArea.setText(item.getMemo() == null ? "" : item.getMemo());
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
            alert("項目を選択してください。");
            return;
        }
        if (amountText == null || amountText.isBlank() || !amountText.matches("\\d+")) {
            alert("金額は数字のみで入力してください。");
            return;
        }

        TxType type = incomeRadio.isSelected() ? TxType.INCOME : TxType.EXPENSE;
        int amount = Integer.parseInt(amountText);
        String memo = memoArea.getText() == null ? "" : memoArea.getText();

        if (!editMode) {
            // ✅ 추가
            LedgerItem item = LedgerItem.builder()
                    .date(date)
                    .type(type)
                    .category(category)
                    .amount(amount)
                    .memo(memo)
                    .build();
            ledgerService.add(item);

        } else {
            // ✅ 수정 (id 유지!)
            LedgerItem updated = LedgerItem.builder()
                    .id(editingId) // 핵심
                    .date(date)
                    .type(type)
                    .category(category)
                    .amount(amount)
                    .memo(memo)
                    .build();

            boolean ok = ledgerService.updateById(updated);
            if (!ok) {
                alert("修正対象（ID）が見つかりませんでした。");
                return;
            }
        }

        saved = true;
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

    private void setupAmountFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            // 1) 빈 문자열은 허용 (지우기 가능해야 함)
            if (newText.isEmpty()) return change;

            // 2) 숫자만 허용
            if (!newText.matches("\\d+")) return null;

            // 3) 길이 제한
            if (newText.length() > AMOUNT_MAX_DIGITS) return null;

            // 4) 값 상한 제한 (원하면)
            try {
                long value = Long.parseLong(newText);
                if (value >= AMOUNT_MAX_VALUE) return null;
            } catch (NumberFormatException e) {
                return null;
            }

            return change;
        };

        amountField.setTextFormatter(new TextFormatter<>(filter));
    }

    private void setupMemoFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            // 1) 길이 제한
            if (newText.length() > MEMO_MAX_LEN) return null;

            // 2) 줄바꿈 금지하고 싶으면 아래 주석 해제
            // if (newText.contains("\n")) return null;

            return change;
        };

        memoArea.setTextFormatter(new TextFormatter<>(filter));
    }
}
