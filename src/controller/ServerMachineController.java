package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.NetEmLinuxCommand;
import model.PasswordDialog;
import model.VLCLinuxCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ServerMachineController implements Initializable {

    Integer delayValue = null;
    Double packetLossValue = null;
    Double packetCorruptValue = null;
    private Stage primaryStage;
    private VLCLinuxCommand vlcLinuxCommand;
    private NetEmLinuxCommand netEmLinuxCommand;
    private PasswordDialog pd;
    private String netEmCommandSudoPassword = null;
    private boolean videoTranscodingMode = false;

    @FXML
    private AnchorPane serverAnchorPane;

    @FXML
    private TextField portValueInput;

    @FXML
    private TextField packetCorruptValueText;

    @FXML
    private CheckBox packetLossParamCheckBox;

    @FXML
    private CheckBox packetCorruptParamCheckBox;

    @FXML
    private Button chooseFileBtn;

    @FXML
    private TextField fileValueInput;

    @FXML
    private TextField delayValueText;

    @FXML
    private CheckBox delayParamCheckBox;

    @FXML
    private TextField packetLossValueText;

    @FXML
    private Label portErrorLabel;

    @FXML
    private Label fileErrorLabel;

    @FXML
    private Label delayErrorLabel;

    @FXML
    private Label packetLossErrorLabel;

    @FXML
    private Label packetDupErrorLabel;

    @FXML
    private ComboBox<String> videoTranscodeCBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pd = new PasswordDialog();
        delayValueText.setDisable(true);
        packetLossValueText.setDisable(true);
        packetCorruptValueText.setDisable(true);
        videoTranscodeCBox.getItems().addAll("None", "H265");
        videoTranscodeCBox.setValue("None");
    }

    @FXML
    void handleFileDragDropped(DragEvent event) {
        File file = event.getDragboard().getFiles().get(0);
        fileValueInput.setText(file.getAbsolutePath());
    }

    @FXML
    void handleFileDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    void backToHome(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage primaryStage = (Stage) serverAnchorPane.getScene().getWindow();
            primaryStage.setScene(scene);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @FXML
    public void chooseFile(ActionEvent actionEvent) {
        primaryStage = (Stage) serverAnchorPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            System.out.println("File absolute path: " + file.getAbsolutePath());
            fileValueInput.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void handleDelayCheckbox(ActionEvent actionEvent) {
        delayValueText.setDisable(!delayParamCheckBox.isSelected());

    }

    @FXML
    void handlePacketCorruptCheckbox(ActionEvent event) {
        packetCorruptValueText.setDisable(!packetCorruptParamCheckBox.isSelected());
    }

    @FXML
    void handlePacketLossCheckbox(ActionEvent event) {
        packetLossValueText.setDisable(!packetLossParamCheckBox.isSelected());
    }

    @FXML
    public void startStreamingVideo(ActionEvent actionEvent) {
        if (validateInput()) {
            if (delayParamCheckBox.isSelected() || packetLossParamCheckBox.isSelected() || packetCorruptParamCheckBox.isSelected()) {
                if (!runningNetEmCommand()) {
                    return;
                }
            }
            //Get server private ip address
            String serverIp = null;
            try {
                serverIp = getSiteLocalIpAddress();
            } catch (Exception exception) {
                showAlert(Alert.AlertType.ERROR, exception.getMessage());
            }

            vlcLinuxCommand = new VLCLinuxCommand(serverIp, Integer.valueOf(portValueInput.getText().trim()));
            String command = vlcLinuxCommand.getVLCStreamVideoCommand(fileValueInput.getText().trim());
            if(videoTranscodingMode) {
                command = vlcLinuxCommand.getVLCStreamTranscodeVideoCommand(fileValueInput.getText().trim());
            }
            serverAnchorPane.setDisable(true);
            System.out.println(command);
            runningCommand(command);
            serverAnchorPane.setDisable(false);
            //Reset all QoS parameter
            if (netEmLinuxCommand != null &&
                    (delayParamCheckBox.isSelected() || packetLossParamCheckBox.isSelected() || packetCorruptParamCheckBox.isSelected())
            ) {
                System.out.println(netEmLinuxCommand.getClearCommand(netEmCommandSudoPassword));
                runningCommand(netEmLinuxCommand.getClearCommand(netEmCommandSudoPassword));
            }
            resetQoSParamValue();
        }
    }

    private boolean runningNetEmCommand() {
        String networkInterfaceDevice = getCurrentNetworkInterfaceDevice();

        if (networkInterfaceDevice == null) {
            showAlert(Alert.AlertType.ERROR, "Can not set param.\nReason: No ethernet or wifi device connected.");
            return false;
        }
        if (delayParamCheckBox.isSelected()) {
            delayValue = Integer.parseInt(delayValueText.getText().trim());
        }
        if (packetLossParamCheckBox.isSelected()) {
            packetLossValue = Double.parseDouble(packetLossValueText.getText().trim());
        }
        if (packetCorruptParamCheckBox.isSelected()) {
            packetCorruptValue = Double.parseDouble(packetCorruptValueText.getText().trim());
        }
        netEmLinuxCommand = new NetEmLinuxCommand(delayValue, packetLossValue, packetCorruptValue, networkInterfaceDevice);
        if (netEmCommandSudoPassword == null) {
            netEmCommandSudoPassword = getSystemPassword("[sudo] password: ");
        }
        String command = netEmLinuxCommand.getRunningCommand(netEmCommandSudoPassword);
        System.out.println("NetEm commandd: " + command);
        while (!runningCommand(command)) {
            netEmCommandSudoPassword = getSystemPassword("Wrong password, sorry. [Sudo] password again: ");
            if (netEmCommandSudoPassword.equals("")) {
                showAlert(Alert.AlertType.ERROR, "No permission for Net Simulator");
                return false;
            }
            command = netEmLinuxCommand.getRunningCommand(netEmCommandSudoPassword);
        }

        return true;
    }

    private String getCurrentNetworkInterfaceDevice() {
        String device = null;
        String command = "nmcli device status";
        try {
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(command);
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line = buf.readLine()) != null) {
                if (line.contains("ethernet") || line.contains("wifi")) {
                    if (line.contains("connected")) {
                        device = line.substring(0, line.indexOf(" "));
                        System.out.println("Network Interface Device: " + device);
                        return device;
                    }
                    device = line.substring(0, line.indexOf(" "));
                }
            }
            pr.destroy();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return device;
    }

    private boolean runningCommand(String command) {
        try {
            String line = "";
            Process pr = new ProcessBuilder(new String[]{"/bin/bash", "-c", command}).start();
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            line = input.readLine();
            while (line != null) {
                System.out.println(line);
                if (line.contains("1 incorrect password attempt")) {
                    input.close();
                    pr.destroy();
                    return false;
                }
                line = input.readLine();
            }
            input.close();
            pr.destroy();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean validateInput() {
        // check port value input
        if (!isPositiveNumber(portValueInput.getText().trim())) {
            displayErrorLabel(portErrorLabel, "Port must be positive number");
            return false;
        } else if (Integer.parseInt(portValueInput.getText().trim()) <= 0) {
            displayErrorLabel(portErrorLabel, "Port must be positive number");
            return false;
        } else {
            disableErrorLabel(portErrorLabel);
        }

        //check file path
        Path path = Paths.get(fileValueInput.getText().trim());
        File file = path.toFile();
        if (!file.exists() || file.isDirectory()) {
            displayErrorLabel(fileErrorLabel, "Wrong file path");
            return false;
        } else {
            //check file type(video)
            try {
                String mimeType = Files.probeContentType(path);
                if (!mimeType.startsWith("video")) {
                    displayErrorLabel(fileErrorLabel, "Wrong video file format");
                    return false;
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Something went wrong with inputted file");
                return false;
            }
            disableErrorLabel(fileErrorLabel);
        }

        //check delay value
        if (delayParamCheckBox.isSelected()
                && !isPositiveNumber(delayValueText.getText().trim())) {
            displayErrorLabel(delayErrorLabel, "Delay value must be positive number");
            return false;
        } else {
            disableErrorLabel(delayErrorLabel);
        }
        //check packet loss value
        if (packetLossParamCheckBox.isSelected() &&
                !(isPositiveDecimalNumber(packetLossValueText.getText().trim())
                        && Double.parseDouble(packetLossValueText.getText().trim()) <= 100
                )
        ) {
            displayErrorLabel(packetLossErrorLabel, "Packet loss value must be in range 0-100");
            return false;
        } else {
            disableErrorLabel(packetLossErrorLabel);
        }

        //check packet duplication value
        if (packetCorruptParamCheckBox.isSelected() &&
                !(isPositiveDecimalNumber(packetCorruptValueText.getText().trim())
                        && Double.parseDouble(packetCorruptValueText.getText().trim()) <= 100
                )
        ) {
            displayErrorLabel(packetDupErrorLabel, "Packet corruption value must be in range 0-100");
            return false;
        } else {
            disableErrorLabel(packetDupErrorLabel);
        }
        return true;
    }

    private boolean isPositiveNumber(String numberString) {
        if (numberString.equals("")) return false;
        Pattern pattern = Pattern.compile("\\d+");
        return pattern.matcher(numberString).matches();
    }

    private boolean isPositiveDecimalNumber(String numberString) {
        if (numberString.equals("")) return false;
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        System.out.println(numberString);
        System.out.println(pattern.matcher(numberString).matches());
        return pattern.matcher(numberString).matches();
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void displayErrorLabel(Label label, String message) {
        label.setDisable(false);
        label.setText(message);
    }

    private void disableErrorLabel(Label label) {
        label.setText("");
    }

    private String getSystemPassword(String terminalMessage) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Password Confirm");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField pwd = new PasswordField();
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        content.getChildren().addAll(new Label(terminalMessage), pwd);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return pwd.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }
        return "";

//        System.out.println("Start dialog");
//        Optional<String> result = pd.showAndWait();
//        pd.setTitle("Password Confirm");
//        pd.setContentText(terminalMessage);
//        result.ifPresent(password -> {
//            System.out.println(password);
//        });
//        System.out.println("Dialog finished");
//        return result.toString();
    }

    private String getSiteLocalIpAddress() throws UnknownHostException, SocketException {

        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaceEnumeration.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
            if (networkInterface.isUp()) {
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()
                ) {
                    if (interfaceAddress.getAddress().isSiteLocalAddress()) {
                        return interfaceAddress.getAddress().getHostAddress();
                    }
                }
            }
        }
        return Inet4Address.getLocalHost().getHostAddress();
    }

    private void resetQoSParamValue() {
        delayValue = null;
        packetLossValue = null;
        packetCorruptValue = null;
    }

    @FXML
    public void handleVideoTranscodeCBox(ActionEvent actionEvent) {
        if (videoTranscodeCBox.getValue().equals("H265")) {
            videoTranscodingMode = true;
        } else {
            videoTranscodingMode = false;
        }
    }
}
