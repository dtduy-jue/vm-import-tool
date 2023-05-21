package com.example.vmimporttool;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main-view.fxml"));
        MainController mainController = new MainController();
        fxmlLoader.setController(mainController);
        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        stage.setTitle("ESXi VM ImportTool");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(evt -> {
            if (mainController.isProcessing) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Xác nhận thoát");
                alert.setHeaderText("Tiến trình tạo máy ảo chưa hoàn thành.\nBạn có chắc chắn muốn thoát?");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        mainController.manualTerminate();
                    } else {
                        evt.consume();
                    }
                });
            }
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}