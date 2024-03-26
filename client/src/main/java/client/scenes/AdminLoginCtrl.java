package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import java.util.ResourceBundle;
import static client.scenes.SplittyMainCtrl.currentLocale;

public class AdminLoginCtrl {
    private final ServerUtils server;
    private final SplittyMainCtrl mainCtrl;
    private Stage primaryStage;
    private Scene adminLoginScene;
    private ResourceBundle bundle;


    @FXML
    public Button backButton;
    @FXML
    public Button loginButton;
    @FXML
    private PasswordField passwordField;

    /**
     * Constructs an instance of AdminCtrl with the specified dependencies.
     *
     * @param server   The ServerUtils instance.
     * @param mainCtrl The MainCtrl instance.
     */
    @Inject
    public AdminLoginCtrl(ServerUtils server, SplittyMainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Initializes the page
     *
     * @param primaryStage The primary container of this page.
     * @param adminLoginScene     The page with its controller.
     */
    public void initialize(Stage primaryStage, Scene adminLoginScene) {
        this.primaryStage = primaryStage;
        this.adminLoginScene = adminLoginScene;

        primaryStage.setScene(adminLoginScene);
        primaryStage.show();

        bundle = ResourceBundle.getBundle("messages", currentLocale);
        updateUI();
    }

    /**
     * Update UI to language setting
     */
    private void updateUI() {
        passwordField.setPromptText(bundle.getString("passwordField"));
        loginButton.setText(bundle.getString("loginButton"));
        backButton.setText(bundle.getString("abortLoginButton"));

    }

    /**
     * validates the admin information when the button is pressed
     */
    public void validate() {
        String password = null;
        if(passwordField != null )
             password = passwordField.getText();

        if (isValid(password)) {
           // switchScene("EventsOverview.fxml");
            // TODO after the management overview is implemented
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Wrong password. Please try again.");
            alert.showAndWait();
        }
        clearField();
    }

    /**
     * Validate the admin information
     * @param password the password associated with the username
     * @return true if the information is correct and false otherwise
     */
    public boolean isValid(String password) {
        if(password == null) return false;
        // TODO check against the randomly generated password
        //something like  return password.equals(generatedPassword);
        // after the password generator is implemented
        return true;
    }

    /**
     * Clears all input fields.
     */
    public void clearField() {
        if(passwordField != null)
            passwordField.clear();
    }

    /**
     * Goes back to the start screen
     */
    public void back(){
        mainCtrl.showStartScreen();
    }
}