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

    public void openCalendar(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/calendar.fxml"))
            );
            Parent root = loader.load();

            CalendarController controller = loader.getController();
            controller.setRouter(this); // ✅ Router 주입

            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Budget 가계부");
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** ✅ 날짜 클릭 시: 데이터 있으면 상세, 없으면 입력 */
    public void onDateClicked(Window owner, LocalDate date) {
        List<LedgerItem> items = ledgerService.findByDate(date);

        if (items == null || items.isEmpty()) {
            openEntryDialog(owner, date);
        } else {
            openDetailDialog(owner, date, items);
        }
    }

    private void openEntryDialog(Window owner, LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/entryDialog.fxml"))
            );
            Parent root = loader.load();

            EntryDialogController controller = loader.getController();
            controller.init(date);
            controller.setLedgerService(ledgerService); // ✅ 저장 서비스 주입

            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("입력");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDetailDialog(Window owner, LocalDate date, List<LedgerItem> items) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/detailDialog.fxml"))
            );
            Parent root = loader.load();

            DetailDialogController controller = loader.getController();
            controller.init(date, items);

            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("상세");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
