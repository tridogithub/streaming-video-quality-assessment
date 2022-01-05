package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainController {
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private Button startVideoBtn;

    @FXML
    private Button viewVideoBtn;

    @FXML
    void startStreamingVideo(ActionEvent event) {
//        if(!runningCommand("apt list --installed vlc")) {
//            showAlert(Alert.AlertType.ERROR, "Not found VLC. Please install it for this machine!");
//        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ServerMachineView.fxml"));
            try {
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage primaryStage = (Stage) mainAnchorPane.getScene().getWindow();
                primaryStage.setScene(scene);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
//        }
    }

    @FXML
    void viewStreamingVideo(ActionEvent event) {
        if(!runningCommand("apt list --installed vlc")) {
            showAlert(Alert.AlertType.ERROR, "Not found VLC. Please install it for this machine!");
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClientMachineView.fxml"));
            try {
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage primaryStage = (Stage) mainAnchorPane.getScene().getWindow();
                primaryStage.setScene(scene);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private boolean runningCommand(String command) {
        try {
            String line = "";
            Process pr = new ProcessBuilder(new String[]{"/bin/bash", "-c", command}).start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            line = input.readLine();
            while (line != null) {
                System.out.println(line);
                if(line.contains("vlc") && line.contains("installed")) {
                    return true;
                }
                line = input.readLine();
            }
            input.close();
            pr.destroy();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return false;
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
