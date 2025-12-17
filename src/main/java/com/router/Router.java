package com.router;

import com.controller.CalendarController;
import com.controller.EntryDialogController;
import com.service.ledger.LedgerService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.Objects;

public class Router {

    private final LedgerService ledgerService = new LedgerService();

    public void openCalendar(Stage stage) {
        try {
            // 1️⃣ FXMLLoader 객체 생성
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/calendar.fxml"))
            );

            // 2️⃣ FXML 로드
            Parent root = loader.load();

            // 3️⃣ 컨트롤러 꺼내서 Router 주입
            CalendarController controller = loader.getController();
            controller.setRouter(this);


            // 4️⃣ Scene / Stage 설정
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Budget 가계부");
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openEntryDialog(Window owner, LocalDate date) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
