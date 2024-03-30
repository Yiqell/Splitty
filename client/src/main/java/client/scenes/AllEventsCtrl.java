package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import static client.scenes.SplittyMainCtrl.currentLocale;

public class AllEventsCtrl {

    private ServerUtils server;
    private SplittyMainCtrl mainCtrl;
    private Stage primaryStage;
    private Scene scene;
    @FXML
    private ListView<Event> listView;
    private ObservableList<Event> events;
    private ResourceBundle bundle;
    @FXML
    private Button backButton;
    @FXML
    private Button lastActivityButton;
    @FXML
    private Button creationDateButton;
    @FXML
    private Button titleButton;
    @FXML
    private Button backupsButton;

    /**
     * Constructs an instance of EventsOverviewCtrl with the specified dependencies.
     *
     * @param server   The ServerUtils instance.
     * @param mainCtrl The MainCtrl instance.
     */
    @Inject
    public AllEventsCtrl(ServerUtils server, SplittyMainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    /**
     * Initializes the page
     *
     * @param primaryStage The primary container of this page
     * @param overview     The page with its controller
     */
    public void initialize(Stage primaryStage, Scene overview) {
        this.primaryStage = primaryStage;
        this.scene = overview;

        primaryStage.setScene(scene);
        primaryStage.show();

        bundle = ResourceBundle.getBundle("messages", currentLocale);
        updateUI();

        listView.setCellFactory(listView -> new CustomListCell());
        displayAllEvents();

    }

    private class CustomListCell extends ListCell<Event> {
        private Button deleteButton;

        public CustomListCell() {
            deleteButton = new Button("Delete");
            deleteButton.setOnAction(event -> {
                Event item = getItem();
                if (item != null) {
                    listView.getItems().remove(item);
                    server.deleteEvent(item.getEventId());
                }
            });
        }

        @Override
        protected void updateItem(Event item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.toString());
                setGraphic(deleteButton);
            }
        }
    }

    /**
     * Update UI to language setting
     */
    private void updateUI() {
        backButton.setText(bundle.getString("backButton"));
        lastActivityButton.setText(bundle.getString("lastActivityButton"));
        creationDateButton.setText(bundle.getString("creationDateButton"));
        titleButton.setText(bundle.getString("titleButton"));
        backupsButton.setText(bundle.getString("backupsButton"));
    }


    /**
     * displays all events from the server in the list view
     */
    public void displayAllEvents() {
        events = FXCollections.observableArrayList();
        List<Event> serverEvents = server.getEvents();
        listView.setItems(events);
        events.addAll(serverEvents);
    }

    /**
     * Goes back to the start screen
     */
    public void back(){
        mainCtrl.showStartScreen();
    }

    /**
     * Goes to the backups page
     */
    public void backups(){
        mainCtrl.showBackups();
    }

    /**
     * getter for the list of events
     * @return the list of events
     */
    public ListView<Event> getListView() {
        return listView;
    }

    /**
     * setter for the list of events
     * @param listView the new list
     */
    public void setListView(ListView<Event> listView) {
        this.listView = listView;
    }

    /**
     * sorts the events by title
     */
    public void sortByTitle() {
        events.sort(Comparator.comparing(Event::getTitle));
        listView.setItems(events);
    }

    /**
     *sorts the events by creation date
     */
    public void sortByCreationDate() {
        events.sort(Comparator.comparing(Event::getCreationDate));
        listView.setItems(events);
    }

    /**
     * sort the events by last activity
     */
    public void sortByLastActivity() {
        events.sort(Comparator.comparing(Event::getLastActivity));
        listView.setItems(events);
    }

    /**
     * goes to the clicked event's overview
     */
    @FXML
    public void handleEventClick() {
        Event selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            mainCtrl.showOverview(selectedItem);
        }
    }
}