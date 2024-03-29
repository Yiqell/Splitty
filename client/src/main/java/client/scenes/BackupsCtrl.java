package client.scenes;

import client.utils.ServerUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import commons.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static client.scenes.SplittyMainCtrl.currentLocale;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import java.io.*;


public class BackupsCtrl {

    private ServerUtils server = new ServerUtils();
    private ResourceBundle bundle;
    private final SplittyMainCtrl mainCtrl;
    private Stage primaryStage;
    private Scene backupsScene;


    @FXML
    private Button downloadAllButton;
    @FXML
    private Button downloadOneButton;
    @FXML
    private ChoiceBox<Long> events;
    @FXML
    public Button backButton;

    /**
     * Constructs an instance of AdminCtrl with the specified dependencies.
     *
     * @param server   The ServerUtils instance.
     * @param mainCtrl The MainCtrl instance.
     */
    @Inject
    public BackupsCtrl(ServerUtils server, SplittyMainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Initializes the page
     *
     * @param primaryStage The primary container of this page.
     * @param backupsScene     The page with its controller.
     */
    public void initialize(Stage primaryStage, Scene backupsScene) {
        this.primaryStage = primaryStage;
        this.backupsScene = backupsScene;

        primaryStage.setScene(backupsScene);
        primaryStage.show();

        ObservableList<Long> choices = FXCollections.observableArrayList();

        bundle = ResourceBundle.getBundle("messages", currentLocale);
        updateUI();

        for (Event event : server.getEvents()){
            choices.add(event.getEventId());
        }
        events.setItems(choices);

        // You can initialize UI elements or perform other setup here
    }

    /**
     * Update UI to language setting
     */
    public void updateUI() {
        downloadAllButton.setText(bundle.getString("downloadAllButton"));
        downloadOneButton.setText(bundle.getString("downloadOneButton"));
        backButton.setText(bundle.getString("abortBackupsButton"));
    }

    /**
     * handles importing either one or a List of events
     * updates events with matching ID, and adds events without or with
     * distinct id
     * @throws IOException file reading error
     */
    public void importBackup() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        FileChooser fileChooser = new FileChooser();
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        fileChooser.setInitialDirectory(downloadsDir);
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            // Check if the file is empty
            if (file.length() == 0) {
                System.out.println("File is empty.");
                return; // Exit method
            }

            // Read the JSON content
            List<Event> eventList = new ArrayList<>();
            try {
                eventList = objectMapper.readValue(file, new TypeReference<List<Event>>() {});
            }
            catch (IOException e){
                eventList.add(objectMapper.readValue(file, Event.class)) ;
            }


            warning(eventList);


        }
    }

    private void warning(List<Event> eventList) {
        // warning
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText("Are you sure you want to add/overwrite all events " +
                "mentioned in your file?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Show the dialog and wait for a response
        Optional<ButtonType> result = alert.showAndWait();

        // Process the user's response
        if (result.isPresent() && result.get() == yesButton) {
            for(Event event: eventList) {
                if (server.getEventById(event.getEventId()) == null) {
                    server.addEvent(event);
                } else {
                    server.updateEvent(event.getEventId(), event);
                }
            }
        }
        else{
            return;
        }
    }

    /**
     * Downloads all events
     */
    @FXML
    public void downloadAll() {
        downloadAllButton.setText("...");
        server.downloadJSONFile(downloadJSONFile("all"), "all");
        downloadAllButton.setText("Download all events");

    }

    /**
     * Downloads one event that you can select in the ui
     */
    @FXML
    public void downloadOne() {
        if (events.getValue() == null) return;
        downloadOneButton.setText("...");
        server.downloadJSONFile(downloadJSONFile(String.valueOf(events.getValue())),
                String.valueOf(events.getValue()));
        downloadOneButton.setText("Download");

    }

    /**
     * method that determines a file destination and name
     * @param event event id in String format (to name the file)
     * @return File
     */
    @FXML
    private File downloadJSONFile(String event) {


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save JSON File");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
        if (event == "all") {
            fileChooser.setInitialFileName("events");
        } else {
            fileChooser.setInitialFileName("event_" + event);
        }
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        fileChooser.setInitialDirectory(downloadsDir);
        File file = fileChooser.showSaveDialog(new Stage());

        return file;
    }

    /**
     * Goes back to the start screen
     */
    public void back() {
        mainCtrl.showStartScreen();
    }
}
