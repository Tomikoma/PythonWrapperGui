package hu.szt;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Controller {
    @FXML
    BorderPane borderPane;

    @FXML
    Button videoButton;

    @FXML
    Button startButton;

    @FXML
    Label vNameLabel;

    @FXML
    ProgressBar progressBar;

    @FXML
    Label progressLabel;

    @FXML
    Button rotateButton;

    private String pathToVideo;
    private int rotatingAngle = 0;


    public void chooseVideo(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Videó megnyitása");
        Stage stage = (Stage)borderPane.getScene().getWindow();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP4,AVI","*.mp4", "*.MP4", "*.avi", "*.AVI","*.mpeg"));
        File file = fileChooser.showOpenDialog(stage);
        if (file!= null) {
            startButton.setDisable(false);
            rotateButton.setDisable(false);
            String[] splitted = file.toString().split("/");
            vNameLabel.setText(splitted[splitted.length-1]);
            pathToVideo = file.toString();
        }
    }

    public void startProcessing(){
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        startButton.setDisable(true);
        rotateButton.setDisable(true);
        Task task = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    Process p = Runtime.getRuntime().exec("executable/"
                            + Main.osType +"/Main " + pathToVideo + " " + rotatingAngle);
                    String s;
                    double progress;
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
                    while ((s = br.readLine()) != null){
                        if(s.equals("DONE")){
                            updateProgress(1,1);
                            updateMessage("Kész");
                        } else {
                            progress = Double.parseDouble(s);
                            updateProgress(progress, 1);
                            updateMessage(String.format("%.3g",(progress*100)) + "%");
                            //System.out.println(Double.parseDouble(s));
                        }
                    }

                    p.waitFor();
                    p.destroy();
                } catch (IOException|InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }

    public void setRotatingAngle(){
        VBox vBox = new VBox();
        Stage stage = new Stage();
        stage.setTitle("Forgatás");

        final ToggleGroup group = new ToggleGroup();

        RadioButton rb1 = new RadioButton("0");
        rb1.setToggleGroup(group);

        RadioButton rb2 = new RadioButton("90");
        rb2.setToggleGroup(group);

        RadioButton rb3 = new RadioButton("180");
        rb3.setToggleGroup(group);

        RadioButton rb4 = new RadioButton("270");
        rb4.setToggleGroup(group);

        switch (rotatingAngle){
            case 0:
                rb1.setSelected(true);
                break;
            case 90:
                rb2.setSelected(true);
                break;
            case 180:
                rb3.setSelected(true);
                break;
            case 270:
                rb4.setSelected(true);
                break;
        }

        Button okButton = new Button("Ok");
        okButton.setOnAction(e -> {
            RadioButton rb = (RadioButton) group.getSelectedToggle();
            rotatingAngle = Integer.parseInt(rb.getText());
            rotateButton.setText("Forgatás: " + rotatingAngle);
            stage.close();
        });
        Button cancalButton = new Button("Mégse");
        cancalButton.setOnAction( e -> {
            stage.close();
        });

        HBox hBox = new HBox(okButton, cancalButton);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(15);

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.getChildren().addAll(rb1,rb2,rb3,rb4, hBox);
        stage.setScene(new Scene(vBox,200,150));
        stage.show();
    }
}
