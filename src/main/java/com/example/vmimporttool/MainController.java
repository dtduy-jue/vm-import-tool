package com.example.vmimporttool;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainController implements Runnable {
    @FXML
    Pane mainPane;
    @FXML
    Label lb_status;
    @FXML
    Label lb_hostStatus;
    @FXML
    TextField tf_ESXi;
    @FXML
    TextField tf_username;
    @FXML
    TextField tf_password;
    @FXML
    TextField tf_datastore;
    @FXML
    TextField tf_VMname;
    @FXML
    TextField tf_OVF;
    @FXML
    TextField tf_numCPU;
    @FXML
    TextField tf_memory;
    @FXML
    TextField tf_storage;
    @FXML
    RadioButton radio_Win;
    @FXML
    RadioButton radio_Ubun;
    @FXML
    Button btn_OVF;
    @FXML
    Button btn_ok;
    @FXML
    Button btn_checkHost;
    @FXML
    ToggleGroup toggleGroup_OS = new ToggleGroup();

    StringBuffer sb = new StringBuffer();

    private Process cmdProcess;
    private Process powershellProcess;

    private ProcessBuilder processBuilder;

    boolean isProcessing = false;

    private BufferedReader r;
    final String UBUNTU_OVA_URL = "https://dl.dropboxusercontent.com/s/th5tdvwdfruvro3/Ubuntu1.ova?dl=1";
    final String WINDOWS_OVA_URL = "https://dl.dropboxusercontent.com/s/t6ci7pf55vgsi4l/Windows1.ova?dl=1";
    final Integer MINIMAL_MEMORY_MB = 256;
    final Double MINIMAL_STORAGE_UBUNTU_GB = 2.5D;
    final Double MINIMAL_STORAGE_WINDOWS_10_GB = 24D;

    Integer numCPU = 0;
    Double totalMemoryMB = 0D;
    Double freeStorageGB = 0D;

    @FXML
    public void initialize() {
        radio_Win.setToggleGroup(toggleGroup_OS);
        radio_Ubun.setToggleGroup(toggleGroup_OS);
        radio_Win.setOnMouseClicked(mouseEvent -> radio_Win.setSelected(true));
        radio_Ubun.setOnMouseClicked(mouseEvent -> radio_Ubun.setSelected(true));
        radio_Win.setSelected(true);

        btn_OVF.setOnAction(actionEvent -> openOVFDirectory());

        btn_ok.setOnAction(actionEvent -> {
            tf_OVF.setDisable(true);
            tf_password.setDisable(true);
            tf_VMname.setDisable(true);
            tf_username.setDisable(true);
            tf_datastore.setDisable(true);
            tf_ESXi.setDisable(true);
            btn_OVF.setDisable(true);
            btn_ok.setDisable(true);
            radio_Win.setDisable(true);
            radio_Ubun.setDisable(true);
            tf_storage.setDisable(true);
            tf_numCPU.setDisable(true);
            tf_memory.setDisable(true);
            btn_checkHost.setDisable(true);
            createVM();
        });

        btn_checkHost.setOnAction(actionEvent -> {
            if (!checkHost()) {
                setHostInfoDisabled(true);
                lb_hostStatus.setStyle("-fx-text-fill: red");
                lb_hostStatus.setText("Kết nối thất bại.");
                lb_hostStatus.setVisible(true);
                PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
                visiblePause.setOnFinished(event -> lb_hostStatus.setVisible(false));
                visiblePause.play();
                return;
            }
            lb_hostStatus.setStyle("-fx-text-fill: gold");
            lb_hostStatus.setText("Đang kiểm tra...");
            lb_hostStatus.setVisible(true);
        });
    }

    private boolean checkHost() {
        if (tf_ESXi.getCharacters().length() == 0) {
            return false;
        }
        if (tf_username.getCharacters().length() == 0) {
            return false;
        }
        if (tf_password.getCharacters().length() == 0) {
            return false;
        }
        if (tf_datastore.getCharacters().length() == 0) {
            return false;
        }
        Thread thread = new Thread(() -> {
            getHostInfo();
            Platform.runLater(() -> {
                lb_hostStatus.setStyle("-fx-text-fill: green");
                lb_hostStatus.setText("Kết nối thành công.");
                lb_hostStatus.setVisible(true);
                PauseTransition visiblePause = new PauseTransition(Duration.seconds(6));
                visiblePause.setOnFinished(event -> lb_hostStatus.setVisible(false));
                visiblePause.play();
                setHostInfoDisabled(false);
                sb.setLength(0);
                tf_numCPU.setPromptText(sb.append("Tối đa ").append(numCPU).toString());
                sb.setLength(0);
                tf_memory.setPromptText(sb.append("Tối đa ").append(totalMemoryMB.intValue()).append("MB").toString());
                sb.setLength(0);
                tf_storage.setPromptText(sb.append("Tối đa ").append(freeStorageGB.intValue()).append("GB").toString());
            });
        });
        thread.start();
        return true;
    }

    private void setHostInfoDisabled(boolean isDisabled) {
        if (isDisabled) {
            tf_numCPU.setDisable(true);
            tf_memory.setDisable(true);
            tf_storage.setDisable(true);
        } else {
            tf_numCPU.setDisable(false);
            tf_memory.setDisable(false);
            tf_storage.setDisable(false);

            tf_numCPU.setPromptText("");
            tf_memory.setPromptText("");
            tf_storage.setPromptText("");
        }
    }

    private void getHostInfo() {
        sb.setLength(0);
        processBuilder = new ProcessBuilder("powershell.exe", sb.append("connect-viserver -server ").append(tf_ESXi.getText()).append(" -user ").append(tf_username.getText()).append(" -password ").append(tf_password.getText()).append("; $esxiHost = Get-VMHost;$esxiHost.NumCpu;$esxiHost.MemoryTotalMB;$dts = Get-Datastore datastore1;$dts.freespacegb").toString());
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            for (int i = 1; i <= 7; i++) {
                line = r.readLine();
                if (line == null) {
                    break;
                } else {
                    System.out.println(line);
                    if (i == 5) {
                        numCPU = Integer.parseInt(line);
                    }
                    if (i == 6) {
                        totalMemoryMB = Double.parseDouble(line);
                    }
                    if (i == 7) {
                        freeStorageGB = Double.parseDouble(line);
                    }
                }
            }
        }
        catch (IOException | NumberFormatException e) {
            throw new RuntimeException();
        }
    }

    public void createVM() {
        if (tf_ESXi.getText().trim().isEmpty()) {
            setLb_status_pleaseFill();
            return;
        }
        if (tf_username.getText().trim().isEmpty()) {
            setLb_status_pleaseFill();
            return;
        }
        if (tf_password.getText().trim().isEmpty()) {
            setLb_status_pleaseFill();
            return;
        }
        if (tf_VMname.getText().trim().isEmpty()) {
            setLb_status_pleaseFill();
            return;
        }

        if (tf_OVF.getText().trim().isEmpty()) {
            setLb_status_pleaseFill();
            return;
        }

        sb.setLength(0);
        File ovftoolExe = new File(sb.append(tf_OVF.getText()).append("\\ovftool.exe").toString());
        if (!ovftoolExe.canExecute()) {
            setLb_status_invalidOVF();
        }

        String differentDiskLetter = null;

        System.out.println("Creating...");

        processBuilder = new ProcessBuilder("cmd.exe", "/c", "cd");
        processBuilder.redirectErrorStream(true);
        try {
            cmdProcess = processBuilder.start();
            r = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) { break; }
                else {
                    char OVFDiskLetter = tf_OVF.getText().charAt(0);
                    if (line.charAt(0) != OVFDiskLetter) {
                        sb.setLength(0);
                        differentDiskLetter = sb.append(OVFDiskLetter).append(":").toString();
                        System.out.println("Different disk letter found!");
                    }
                }
                System.out.println(line);
            }
            sb.setLength(0);
            if (differentDiskLetter != null) {
                sb.append(differentDiskLetter).append(" && ");
            }
            sb.append("cd ").append(tf_OVF.getText()).append(" && ovftool.exe -ds=").append(tf_datastore.getText()).append(" -n=").append(tf_VMname.getText());
            RadioButton selectedOSRadioButton = (RadioButton) toggleGroup_OS.getSelectedToggle();
            if (selectedOSRadioButton.getText().equals("Windows")) {
                sb.append(" ").append(WINDOWS_OVA_URL);
            }
            if (selectedOSRadioButton.getText().equals("Ubuntu")) {
                sb.append(" ").append(UBUNTU_OVA_URL);
            }
            sb.append(" vi://").append(tf_username.getText()).append(":").append(tf_password.getText()).append("@").append(tf_ESXi.getText());

            processBuilder = new ProcessBuilder("cmd.exe", "/c", sb.toString());
            Thread thread = new Thread(this);
            thread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setLb_status_invalidOVF() {
        lb_status.setStyle("-fx-text-fill: red");
        lb_status.setText("Đường dẫn OVFTool không hợp lệ.");
        lb_status.setVisible(true);
        PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
        visiblePause.setOnFinished(event -> lb_status.setVisible(false));
        visiblePause.play();
    }

    private void setLb_status_pleaseFill() {
        lb_status.setStyle("-fx-text-fill: red");
        lb_status.setText("Vui lòng nhập đầy đủ.");
        lb_status.setVisible(true);
        PauseTransition visiblePause = new PauseTransition(Duration.seconds(3));
        visiblePause.setOnFinished(event -> lb_status.setVisible(false));
        visiblePause.play();
    }

    private void openOVFDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File file = directoryChooser.showDialog(stage);
        if (file != null) {
            tf_OVF.setText(file.getAbsolutePath());
        }
    }

    @Override
    public void run() {
        try {
            isProcessing = true;
            cmdProcess = processBuilder.start();
            r = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            String line;
            Platform.runLater(() -> {
                lb_status.setStyle("-fx-text-fill: green");
                lb_status.setText("Đang khởi tạo tiến trình...");
                lb_status.setVisible(true);
            });
            while (isProcessing) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                else {
                    String finalLine = line;
                    Platform.runLater(() -> lb_status.setText(finalLine.length() == 0 ? "Processing..." : finalLine));
                }
                System.out.println(line);
            }


            sb.setLength(0);
            processBuilder = new ProcessBuilder("powershell.exe", sb.append("connect-viserver -server ").append(tf_ESXi.getText()).append(" -user ").append(tf_username.getText()).append(" -password ").append(tf_password.getText()).append(";$vm = Get-VM ").append(tf_VMname.getText()).append(";Set-VM $vm -NumCPU ").append(tf_numCPU.getText()).append(" -MemoryMB ").append(tf_memory.getText()).append(" -confirm:$false").append(";$disk = Get-HardDisk -vm $vm; Set-HardDisk $disk -CapacityGB ").append(tf_storage.getText()).append(" -confirm:$false").toString());
            Thread adjustVMThread = new Thread(() -> {
                try {
                    powershellProcess = processBuilder.start();
                    BufferedReader r = new BufferedReader(new InputStreamReader(powershellProcess.getInputStream()));
                    while (true) {
                        String l = r.readLine();
                        if (l == null) {
                            break;
                        } else {
                            System.out.println(l);
                            Platform.runLater(() -> lb_status.setText(l.length() == 0 ? "Processing..." : l));
                        }
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            Platform.runLater(() -> lb_status.setText("Setting up VM..."));
            adjustVMThread.start();

            while (true) {
                if (isProcessTerminated(powershellProcess)) {
                    isProcessing = false;
                    Platform.runLater(() -> {
                        tf_OVF.setDisable(false);
                        tf_password.setDisable(false);
                        tf_VMname.setDisable(false);
                        tf_username.setDisable(false);
                        tf_datastore.setDisable(false);
                        tf_ESXi.setDisable(false);
                        btn_OVF.setDisable(false);
                        btn_ok.setDisable(false);
                        btn_checkHost.setDisable(false);
                        radio_Win.setDisable(false);
                        radio_Ubun.setDisable(false);
                        lb_status.setDisable(true);
                    });
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isProcessTerminated(Process process) {
        return process == null;
    }

    public void manualTerminate() {
        try {
            isProcessing = false;
            r.close();
            Runtime.getRuntime().exec("kill -9 " + cmdProcess.pid());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}