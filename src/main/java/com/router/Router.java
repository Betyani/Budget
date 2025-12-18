package com.router;

import com.controller.CalendarController;
import com.controller.DetailDialogController;
import com.controller.EntryDialogController;
import com.model.ledger.LedgerItem;
import com.service.ledger.LedgerService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Router {

    private final LedgerService ledgerService = new LedgerService();

    // ✅ 달력 컨트롤러 참조 보관(갱신용)
    private CalendarController calendarController;

    public void openCalendar(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/calendar.fxml"))
            );
            Parent root = loader.load();

            CalendarController controller = loader.getController();
            controller.setRouter(this);

            // ✅ 저장(중요): 나중에 refreshMonth() 호출하려고
            this.calendarController = controller;

            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Budget 가계부");
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 날짜 클릭 시: 데이터 있으면 상세, 없으면 입력 */
    public void onDateClicked(Window owner, LocalDate date) {
        List<LedgerItem> items = ledgerService.findByDate(date);

        if (items == null || items.isEmpty()) {
            boolean saved = openEntryDialog(owner, date);
            if (saved) refreshCalendar(); // ✅ 입력 저장되면 달력 갱신
        } else {
            openDetailDialog(owner, date, items);
        }
    }

    // ✅ 상세창 닫힐 때 DetailDialogController가 호출
    public void onDetailClosed(boolean changed) {
        if (changed) refreshCalendar();
    }

    private void refreshCalendar() {
        if (calendarController != null) {
            calendarController.refreshMonth();
        }
    }

    // 입력창 열기
    public boolean openEntryDialog(Window owner, LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/entryDialog.fxml"))
            );
            Parent root = loader.load();

            EntryDialogController controller = loader.getController();
            controller.init(date);
            controller.setLedgerService(ledgerService);

            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("입력");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            // ✅ 저장 여부를 반환해야 함 (이게 달력 갱신 판별 기준)
            return controller.isSaved();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 상세창 열기
    private void openDetailDialog(Window owner, LocalDate date, List<LedgerItem> items) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/detailDialog.fxml"))
            );
            Parent root = loader.load();

            DetailDialogController controller = loader.getController();
            controller.init(date, items);

            controller.setRouter(this);
            controller.setLedgerService(ledgerService);

            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("상세");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            // ✅ X 버튼으로 닫힐 때도 onClose() 로직 타게 하기
            dialog.setOnCloseRequest(event -> {
                event.consume();          // 기본 close 막기
                controller.onClose();     // 우리가 만든 닫기 로직으로 위임
            });

            dialog.showAndWait();

            // ✅ 여기서 refresh를 해도 되지만,
            // 우리는 onClose()에서 changed 여부 기반으로 처리하도록 분리해둠.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 수정창
    public boolean openEditDialog(Window owner, LedgerItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/entryDialog.fxml"))
            );
            Parent root = loader.load();

            EntryDialogController controller = loader.getController();
            controller.setLedgerService(ledgerService);
            controller.initForEdit(item);

            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("수정");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            return controller.isSaved();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
