package sample;

import javax.activation.MimetypesFileTypeMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestCommand {

    public static void main(String[] args) {
//        try {
//            String line = "";
//            Runtime run = Runtime.getRuntime();
////            String command = "vlc /home/trido2/Downloads/anni007.mpg --sout \'#standard{access=http,mux=ogg,dst=192.168.1.15:8080}\'";
////            Process process = run.exec(command);
////            Process process = new ProcessBuilder("vlc", "-vvv", "/home/trido2/Downloads/anni007.mpg", "--sout", "\'#standard{access=http,mux=ogg,dst=192.168.1.15:8080}\'").start();
//            ProcessBuilder processBuilder = new ProcessBuilder("vlc", "-vvv", "/home/trido2/Downloads/anni007.mpg", "--sout", "\'#standard{access=http,mux=ogg,dst=192.168.1.15:8080}\'");
//            Process process = processBuilder.start();
//
//            System.out.println("Starting...");
//            System.out.println("vlc /home/trido2/Downloads/anni007.mpg --sout '#standard{access=http,mux=ogg,dst=192.168.1.15:8080}'");
////            process.waitFor();
//            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            while ((line = input.readLine()) != null) {
//                System.out.println(line);
//            }
//            while ((line = error.readLine()) != null) {
//                System.out.println(line);
//            }
//            System.out.println("Exit: " + process.exitValue());
////            process.destroy();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }

//        try {
//            String line = "";
//            Runtime run = Runtime.getRuntime();
//            String[] cmd = {"/bin/bash", "-c", "sudo -S tc qdisc add dev enp0s3 root netem delay 100ms"};
//            Process pr = run.exec(cmd);
//            Process pb = new ProcessBuilder(new String[]{"/bin/bash", "-c", "/usr/bin/sudo tc qdisc add dev enp0s3 root netem delay 100ms"}).start();
//            OutputStreamWriter output = new OutputStreamWriter(pb.getOutputStream());
//            InputStreamReader input = new InputStreamReader(pb.getInputStream());
//
//            int bytes;
//            char buffer[] = new char[1024];
//            while ((bytes = input.read(buffer, 0, 1024)) != -1) {
//                System.out.println("Inside while loop...");
//                if(bytes != 0) {
//                    String data = String.valueOf(buffer,0,1024);
//                    System.out.println(data);
//                    if(data.contains("[sudo] password")) {
//                        output.write("3833415");
//                        output.write("\n");
//                        output.flush();
//                        output.close();
//                    }
//                }
//            }
//            System.out.println("Finish while loop");
//            input.close();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }


//        Dialog<String> dialog = new Dialog<>();
//        dialog.setTitle("Password Confirm");
//        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
//
//        PasswordField pwd = new PasswordField();
//        HBox content = new HBox();
//        content.setAlignment(Pos.CENTER_LEFT);
//        content.setSpacing(10);
//        content.getChildren().addAll(new Label("Password"), pwd);
//        dialog.getDialogPane().setContent(content);
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == ButtonType.OK) {
//                return pwd.getText();
//            }
//            return null;
//        });
//
//        Optional<String> result = dialog.showAndWait();
//        if (result.isPresent()) {
//            System.out.println(result.get());
//        }
//        String path = "E:\\STUDYing\\ITSS Linux system and Network Management\\project\\video1.mpeg";
//        Path actualPath = Paths.get(path);
//        System.out.println(actualPath);
//        String mimeType = null;
//        try {
//            mimeType = Files.probeContentType(actualPath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(mimeType);

        try {
            String line = "";
            Process pr = Runtime.getRuntime().exec("apt list vlc");
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            line = input.readLine();
            while (line != null) {
                System.out.println(line);
                line = input.readLine();
            }
            input.close();
            pr.destroy();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String formatHttpStream(String serverAddress, int serverPort) {
        StringBuilder sb = new StringBuilder(60);
        sb.append(":sout=#standard{access=http,mux=ogg,");
        sb.append("dst=");
        sb.append(serverAddress);
        sb.append(':');
        sb.append(serverPort);
        sb.append("}");
        return sb.toString();
    }

}
