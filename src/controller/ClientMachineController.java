package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.VLCLinuxCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ClientMachineController implements Initializable {
    private VLCLinuxCommand vlcLinuxCommand;

    @FXML
    private AnchorPane clientAnchorPane;

    @FXML
    private Button connectBtn;

    @FXML
    private TextField portValueInput;

    @FXML
    private TextField addressValueInput;

    @FXML
    private Label addressErrorLabel;

    @FXML
    private Label portErrorLabel;

    @FXML
    void startConnect(ActionEvent event) {
        String addressValue = addressValueInput.getText();
        String portText = portValueInput.getText();
        Pattern pattern = Pattern.compile("\\d+");
        if (!pattern.matcher(portText).matches() || Integer.parseInt(portValueInput.getText().trim()) == 0) {
            portErrorLabel.setText("Wrong port number");
        } else {
            portErrorLabel.setText("");
            Integer portValue = Integer.valueOf(portText);
            vlcLinuxCommand = new VLCLinuxCommand(addressValue, portValue);
            String command = vlcLinuxCommand.getVLCConnectCommand();
            System.out.println(command);
            runningCommand(command);
        }
    }

    @FXML
    void backToHome(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage primaryStage = (Stage) clientAnchorPane.getScene().getWindow();
            primaryStage.setScene(scene);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void runningCommand(String command) {
        try {
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(command);
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line = buf.readLine()) != null) {
                System.out.println(line);
            }
            pr.destroy();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
