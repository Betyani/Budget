package com.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class EntryDialogController {

    /* ===== FXML 주입 ===== */

    @FXML
    private Label dateLabel;
    @FXML
    private RadioButton incomeRadio;
    @FXML
    private RadioButton expenseRadio;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private TextField amountField;
    @FXML
    private TextArea memoArea;

    /* ===== 내부 상태 ===== */

    private LocalDate date;

    private ToggleGroup typeGroup;

    /* ===== 초기화 ===== */

    @FXML
    private void initialize() {
        // 수입 / 지출 라디오 묶기
        typeGroup = new ToggleGroup();
        incomeRadio.setToggleGroup(typeGroup);
        expenseRadio.setToggleGroup(typeGroup);

        // 기본값 (지출로 가정)
        expenseRadio.setSelected(true);

        // 카테고리 예시 (나중에 enum이나 서비스로 뺄 수 있음)
        categoryCombo.getItems().addAll(
                "식비", "교통", "생활", "쇼핑", "월급", "부수입", "기타"
        );
    }

    /* ===== 외부에서 호출하는 초기화 ===== */

    public void init(LocalDate date) {
        this.date = date;
        dateLabel.setText(date.toString());
    }

    /* ===== 버튼 이벤트 ===== */

    @FXML
    private void onCancel() {
        close();
    }

    @FXML
    private void onSave() {
        // 지금은 저장 X → 값 확인만
        System.out.println("날짜: " + date);
        System.out.println("구분: " + getSelectedType());
        System.out.println("항목: " + categoryCombo.getValue());
        System.out.println("금액: " + amountField.getText());
        System.out.println("메모: " + memoArea.getText());

        // 일단 저장 성공했다고 가정하고 닫기
        close();
    }

    /* ===== 유틸 ===== */

    private String getSelectedType() {
        return incomeRadio.isSelected() ? "수입" : "지출";
    }

    private void close() {
        Stage stage = (Stage) dateLabel.getScene().getWindow();
        stage.close();
    }
}
