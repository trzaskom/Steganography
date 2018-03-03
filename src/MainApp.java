import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.NoSuchElementException;

/**
 * Created by miki on 2018-01-25.
 */
public class MainApp extends Application {

    Scene scene1, scene2;
    BufferedImage carrierImage;
    byte[] carrierAudio;
    boolean inImage;
    TextArea area;
    String messageString;


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage window) throws Exception {

        Image image = new Image();
        Audio audio = new Audio();


        FileChooser.ExtensionFilter extensionFilterPng = new FileChooser.ExtensionFilter("image files", "*.png");
        FileChooser.ExtensionFilter extensionFilterWav = new FileChooser.ExtensionFilter("audio files", "*.wav");

        // Filechooser for carrier file to open
        FileChooser fileChooserOpen = new FileChooser();
        fileChooserOpen.getExtensionFilters().add(extensionFilterPng);
        fileChooserOpen.getExtensionFilters().add(extensionFilterWav);


        //Filechooser for stego file to save
        FileChooser fileChooserSaveImage = new FileChooser();
        fileChooserSaveImage.getExtensionFilters().add(extensionFilterPng);

        FileChooser fileChooserSaveAudio = new FileChooser();
        fileChooserSaveAudio.getExtensionFilters().add(extensionFilterWav);



        //SCENE1------------------------------------------------------------
        Label label = new Label("Welcome to application which allows hiding " +
                                "text data in image(.png) as well as audio(.wav) files. " +
                                "Please choose, what you want to perform:");
        label.setWrapText(true);
        label.setId("welcome-text");

        //button for opening carrier file
        Button openCarrierButton = new Button();
        openCarrierButton.setText("Encode message in...");
        openCarrierButton.setOnAction(event -> {
            File file = fileChooserOpen.showOpenDialog(window);
            if (file != null) {
                String fileExt = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                if (fileExt.equals("png")) {
                    carrierImage = image.read(file);
                    window.setScene(scene2);
                    inImage = true;
                } else if (fileExt.equals("wav")) {
                    carrierAudio = audio.toBytes(file);
                    window.setScene(scene2);
                    inImage = false;
                }
            }
        });

        //button for opening and decoding message
        Button decodeButton = new Button();
        decodeButton.setText("Decode message from...");
        decodeButton.setOnAction(event -> {
            String decodedMessage;
            File file = fileChooserOpen.showOpenDialog(window);
            if (file != null) {
                String fileExt = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                if (fileExt.equals("png")) {
                    carrierImage = image.read(file);
                    decodedMessage = image.decodeMessage(carrierImage);
                    displayAlert("Decoded message", decodedMessage, false);
                } else if (fileExt.equals("wav")) {
                    carrierAudio = audio.toBytes(file);
                    try {
                        decodedMessage = audio.decodeMessage(carrierAudio);
                    } catch (NoSuchElementException e) {
                        displayAlert("Error", "File's data is damaged! Cant't embed message!", true);
                        window.setScene(scene1);
                        return;
                    }
                    displayAlert("Decoded message", decodedMessage, false);
                }
            }
        });

        //button for exiting application
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(event2 -> {
            window.close();
        });

        //scene1 layout
        HBox topMenu1 = new HBox();
        topMenu1.getChildren().add(label);
        topMenu1.setPadding(new Insets(10,10,10,10));
        topMenu1.setAlignment(Pos.CENTER);

        VBox centerMenu1 = new VBox(40);
        centerMenu1.setPrefWidth(150);
        centerMenu1.setPrefHeight(50);
        openCarrierButton.setMinSize(centerMenu1.getPrefWidth(), centerMenu1.getPrefHeight());
        decodeButton.setMinSize(centerMenu1.getPrefWidth(), centerMenu1.getPrefHeight());
        centerMenu1.getChildren().addAll(openCarrierButton, decodeButton);
        centerMenu1.setAlignment(Pos.CENTER);

        HBox bottomMenu1 = new HBox(40);
        bottomMenu1.getChildren().add(exitButton);
        bottomMenu1.setPadding(new Insets(20,20,20,20));
        bottomMenu1.setAlignment(Pos.CENTER);

        BorderPane borderPane1 = new BorderPane();
        borderPane1.setTop(topMenu1);
        borderPane1.setCenter(centerMenu1);
        borderPane1.setBottom(bottomMenu1);
        borderPane1.setId("pane");

        scene1 = new Scene(borderPane1,500,440);
        scene1.getStylesheets().add("stylesheet.css");


        //SCENE2------------------------------------------------------------------------
        // buttons for configurating text area
        Button submitButton = new Button("Submit message");
        Button clearButton = new Button("Clear input");
        area = new TextArea();
        area.setMinSize(500,250);
        area.setMaxHeight(300);
        area.setWrapText(true);
        area.setPromptText("Input message for encoding");
        area.setOnMouseClicked(event1 -> {
            submitButton.setOnAction(event -> {
                messageString = area.getText();
                submitButton.setVisible(false);
                area.setText("Message has been loaded successfully!");
                area.setEditable(false);
            });
            clearButton.setOnAction(event -> {
                area.clear();
                messageString = null;
                submitButton.setVisible(true);
                area.setEditable(true);
            });
        });

        // button for encoding message and saving stego file
        Button encodeButton = new Button();
        encodeButton.setText("Encode");
        encodeButton.setOnAction(event -> {
            if (messageString != null) {
                if (inImage) {
                    try {
                        image.encodeMessage(carrierImage, messageString);
                    } catch (IllegalArgumentException e) {
                        displayAlert("Error", "Size of image file is not big enough to contain message!", true);
                        return;
                    }
                    File saveFile = fileChooserSaveImage.showSaveDialog(window);
                    if (saveFile != null) {
                        image.write(carrierImage, saveFile);
                        displayAlert("Alert", "Message has been successfully encoded and saved!", true);
                        window.setScene(scene1);
                        area.clear();
                        submitButton.setVisible(true);
                        area.setEditable(true);
                    }
                } else {
                    try {
                        audio.encodeMessage(carrierAudio, messageString);
                    } catch (IllegalArgumentException e) {
                        displayAlert("Error", "Size of audio file is not big enough to contain message!", true);
                        return;
                    } catch (NoSuchElementException e) {
                        displayAlert("Error", "File's data is damaged! Cant't embed message!", true);
                        window.setScene(scene1);
                        return;
                    }
                    File saveFile = fileChooserSaveAudio.showSaveDialog(window);
                    if (saveFile != null) {
                        audio.write(carrierAudio, saveFile);
                        displayAlert("Alert", "Message has been successfully encoded and saved!", true);
                        window.setScene(scene1);
                        area.clear();
                        submitButton.setVisible(true);
                        area.setEditable(true);
                    }
                }
            } else
                displayAlert("Alert", "Please input message first!", true);
        });

        //button for returning to scene1
        Button returnButton = new Button();
        returnButton.setText("Return");
        returnButton.setOnAction(event -> {
            window.setScene(scene1);
            area.clear();
            messageString = null;
            submitButton.setVisible(true);
            area.setEditable(true);
        });

        //scene2 layout
        HBox topMenu2 = new HBox();
        topMenu2.getChildren().add(returnButton);
        topMenu2.setPadding(new Insets(10,10,10,10));

        HBox bottomMenu2 = new HBox(40);
        bottomMenu2.getChildren().addAll(submitButton, encodeButton, clearButton);
        bottomMenu2.setPadding(new Insets(15,30,15,30));
        bottomMenu2.setAlignment(Pos.CENTER);

        BorderPane borderPane2 = new BorderPane();
        borderPane2.setTop(topMenu2);
        borderPane2.setCenter(area);
        borderPane2.setBottom(bottomMenu2);
        borderPane2.setId("pane");

        scene2 = new Scene(borderPane2, 500, 400);
        scene2.getStylesheets().add("stylesheet.css");


        //window settings
        window.setTitle("Steganography");
        window.setScene(scene1);
        window.show();
    }


    public void displayAlert(String title, String message, boolean alert){
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(250);

        Button closeButton = new Button("Ok");
        Label label = null;
        TextArea area = null;

        // checks whether regular alert or decoded message
        if (alert) {
            label = new Label();
            label.setText(message);
        } else {
            window.setMinWidth(500);
            window.setMinHeight(500);

            area = new TextArea();
            area.setMinSize(500, 400);
            area.setText(message);
            area.setWrapText(true);
            area.setEditable(false);
        }
        closeButton.setOnAction(event -> {
            window.close();
        });

        VBox layout = new VBox(10);
        if (alert)
            layout.getChildren().addAll(label, closeButton);
        else
            layout.getChildren().addAll(area, closeButton);
        layout.setAlignment(Pos.CENTER);
        layout.setId("pane");

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("stylesheet.css");
        window.setScene(scene);
        window.showAndWait();

    }
}