package client.scenes;

import client.utils.ConfigUtils;
import client.utils.ServerUtils;
import commons.*;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.util.*;

import static javafx.scene.input.KeyCode.ESCAPE;

public class OpenDebtsCtrl {
    private final SplittyMainCtrl mainCtrl;


    private List<Debt> debtList;
    private Event event;
    private final ServerUtils server;
    private ResourceBundle bundle;

    @FXML
    private Label noDebtMessage;

    @FXML
    private Accordion accordionDebts;
    @FXML
    public Button abortDebtsButton;
    @FXML
    public Text titleText;

    /**
     * Constructs an instance of OpenDebtsCtrl with the specified dependencies.
     *
     * @param mainCtrl The MainCtrl instance.
     * @param server   The ServerUtils instance
     */
    @Inject
    public OpenDebtsCtrl(ServerUtils server, SplittyMainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        debtList = new ArrayList<>();
    }

    /**
     * Initializes the page.
     *
     * @param event        The event.
     */
    public void initialize(Event event) {
        this.event = event;
        debtList = event.generateDebts();
        debtList.removeIf(Debt::isSettled);

        bundle = ResourceBundle.getBundle("messages", mainCtrl.getCurrentLocale());
        updateUI();
    }

    /**
     * updates the bundle
     */
    public void updateLocale() {
        bundle = ResourceBundle.getBundle("messages", mainCtrl.getCurrentLocale());
        updateUI();
        }

    /**
     * Updates to the language setting
     */
    public void updateUI() {
        abortDebtsButton.setText(bundle.getString("abortDebtsButton"));
        noDebtMessage.setText(bundle.getString("noDebtMessage"));
        titleText.setText(bundle.getString("titleText"));
        accordionDebts.getPanes().clear();
        noDebtMessage.setVisible(false);
        if (debtList.isEmpty()) {
            noDebtMessage.setVisible(true);
        } else {
            initTitledPanes();
        }
    }

    /**
     * Initializes the TitledPanes in the accordion container. Each TitledPane shows an open debt.
     */
    public void initTitledPanes() {
        debtList = event.generateDebts();
        // Dynamically create TitledPanes and their content based on debtList
        for (Debt debt : debtList) {
            TitledPane titledPane = new TitledPane();
            titledPane.setText(debt.getUser1().getName() + " " + bundle.getString("owes")
                    + debt.getAmount() + " " + ConfigUtils.getCurrency() + " "
                    + bundle.getString("to") + debt.getUser2().getName());
            AnchorPane contentPane = new AnchorPane();
            ToggleButton mailButton = new ToggleButton();
            mailButton.setGraphic(generateIcons("mail"));
            mailButton.setOpacity(0.5);
            Button markReceivedButton = new Button(bundle.getString("markReceived"));
            ToggleButton bankButton = new ToggleButton();
            bankButton.setGraphic(generateIcons("bank"));
            bankButton.setOpacity(0.5);

            // Add Text for bank details (initially invisible)
            Text bankDetailsText;
            if (!debt.getUser2().getBankAccount().equals("")) {
                bankDetailsText = new Text(bundle.getString("bankDetails") + "\n"
                        + bundle.getString("accHolder") + debt.getUser2().getName() + "\n"
                        + "IBAN: " + debt.getUser2().getBankAccount() + "\nBIC: "
                        + debt.getUser2().getBic());
            } else {
                bankDetailsText = new Text("No bank details available.");
            }
            bankDetailsText.setVisible(false);
            contentPane.getChildren().add(bankDetailsText);

            // Set actions for the buttons
            mailButton.setOnAction(event -> handleMailButton(contentPane, debt, mailButton));
            markReceivedButton.setOnAction(event -> markReceived(debt, titledPane));
            bankButton.setOnAction(event -> handleBankButton(bankDetailsText, bankButton));

            // Set the positioning of the entities
            AnchorPane.setTopAnchor(bankDetailsText, 10.0);
            AnchorPane.setLeftAnchor(bankDetailsText, 10.0);
            AnchorPane.setTopAnchor(mailButton, 10.0);
            AnchorPane.setRightAnchor(mailButton, 50.0);
            AnchorPane.setTopAnchor(bankButton, 10.0);
            AnchorPane.setRightAnchor(bankButton, 10.0);
            AnchorPane.setBottomAnchor(markReceivedButton, 10.0);
            AnchorPane.setRightAnchor(markReceivedButton, 10.0);

            // Connect the created entities
            contentPane.getChildren().addAll(mailButton, markReceivedButton, bankButton);
            titledPane.setContent(contentPane);
            accordionDebts.getPanes().add(titledPane);
        }
    }

    /**
     * Handles the action when the "Bank" button is clicked.
     *
     * @param bankDetailsText The bank details text.
     * @param bankButton      The "Bank" button.
     */
    public void handleBankButton(Text bankDetailsText, ToggleButton bankButton) {
        if (bankButton.isSelected()) {
            bankButton.setOpacity(1.0);
            bankDetailsText.setVisible(true);
        } else {
            bankButton.setOpacity(0.5);
            bankDetailsText.setVisible(false);
        }
    }

    /**
     * Handles the action when the "Mark Received" button is clicked.
     *
     * @param debt       The open debt.
     * @param titledPane
     */
    public void markReceived(Debt debt, TitledPane titledPane) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Mark Debt as Received");
        alert.setContentText("Are you sure you want to mark this debt as received?");

        // Add buttons to the alert
        Optional<ButtonType> result = alert.showAndWait();


        if (result.isPresent() && result.get() == ButtonType.OK) {
            debt.setSettled(true);
            accordionDebts.getPanes().remove(titledPane);
            boolean allDebtsSettled = true;
            for (Debt d : debtList) {
                if (!d.isSettled()) {
                    allDebtsSettled = false;
                    break;
                }
            }
            if (allDebtsSettled) {
                noDebtMessage.setVisible(true);
            }
            ArrayList<Participant> beneficiaries = new ArrayList<>();
            beneficiaries.add(debt.getUser2());
            Date date = java.sql.Date.valueOf(LocalDate.now());
            List<Tag> debtTags = server.getTags(event);
            Tag debtTag = debtTags.get(0);
            for (Tag tag : debtTags) {
                if ("debt settlement".equals(tag.getName())) {
                    debtTag = tag;
                }
            }
            Expense debtSettlement = new Expense(debt.getUser1(),
                    debt.getAmount(), ConfigUtils.getCurrency(),
                    beneficiaries, "Debt Settlement", date, debtTag);
            event.addExpense(server.addExpense(debtSettlement));
            event = server.updateEvent(event.getEventId(), event);
        }
        Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION);
        alert2.setTitle("Debt marked received");
        alert2.setHeaderText(null);
        alert2.setContentText("The debt: " + debt + " is successfully marked as received");
        alert2.showAndWait();
    }


    /**
     * Handles the action when the "Mail" button is clicked and the email is configured.
     *
     * @param contentPane The content of the debt.
     * @param debt        The open debt.
     * @param mailButton  The mail button.
     */
    public void handleMailButton(AnchorPane contentPane, Debt debt, ToggleButton mailButton) {
        // Handles different actions based on if the button was toggled on or off at first
        // (by the presence of the "send reminder" button)
        if (debt.getUser1().getEmail() != null) {
            if (mailButton.isSelected()) {
                // If no button is present (it was toggled off), add a new button (now toggle on)
                mailButton.setOpacity(1.0);
                Text emailConfiguredText = new Text(bundle.getString("emailConfigured"));
                emailConfiguredText.setId("emailConfiguredText");
                Button sendReminder = new Button(bundle.getString("sendReminder"));
                sendReminder.setId("sendReminderButton");
                sendReminder.setOnAction(event -> sendReminder(debt));
                contentPane.getChildren().add(emailConfiguredText);
                contentPane.getChildren().add(sendReminder);

                // Set the positioning of the entities
                AnchorPane.setBottomAnchor(emailConfiguredText, 40.0);
                AnchorPane.setLeftAnchor(emailConfiguredText, 10.0);
                AnchorPane.setBottomAnchor(sendReminder, 10.0);
                AnchorPane.setLeftAnchor(sendReminder, 10.0);
            } else {
                // If a button is present (it was toggled on), remove it (now toggle off)
                mailButton.setOpacity(0.5);
                contentPane.getChildren().remove(contentPane.lookup("#emailConfiguredText"));
                contentPane.getChildren().remove(contentPane.lookup("#sendReminderButton"));
            }
        } else handleNoEmail(contentPane, mailButton);
    }

    /**
     * Handles the action when the "Mail" button is clicked and the email is not configured.
     *
     * @param contentPane The content of the debt.
     * @param mailButton  The mail button.
     */
    public void handleNoEmail(AnchorPane contentPane, ToggleButton mailButton) {
        if (mailButton.isSelected()) {
            mailButton.setOpacity(1.0);
            Text emailConfiguredText = new Text("The Email is not configured.");
            emailConfiguredText.setId("emailConfiguredText");
            contentPane.getChildren().add(emailConfiguredText);
            AnchorPane.setBottomAnchor(emailConfiguredText, 10.0);
            AnchorPane.setLeftAnchor(emailConfiguredText, 10.0);
        } else {
            contentPane.getChildren().remove(contentPane.lookup("#emailConfiguredText"));
        }
    }

    /**
     * Send a default email, to test whether the email credentials are correct
     * and the email is delivered.
     */
    public void checkDefaultEmail() {
        Email defaultEmail = new Email();
        defaultEmail.setToRecipient(defaultEmail.getEmailUsername());
        defaultEmail.setEmailSubject("Default Email");
        defaultEmail.setEmailBody("Default Body - Checking Credentials/Delivery");
        server.sendEmail(defaultEmail);
    }

    /**
     * Handles the action when the "send reminder" button is clicked.
     *
     * @param debt The open debt.
     */
    public void sendReminder(Debt debt) {
        checkDefaultEmail();
        Email email = new Email(debt.getUser1().getEmail(), "Debt Reminder",
                bundle.getString("dear") + debt.getUser1().getName() + ",<br><br>" +
                        bundle.getString("reminderStart")
                        + "<br>" + debt.toStringHtml() + "<br><br>" +
                        bundle.getString("reminderEnd") + "<br><br>" + debt.getUser2().getName());
        server.sendEmail(email);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reminder sent");
        alert.setHeaderText(null);
        alert.setContentText("The reminder is sent successfully to " + debt.getUser1().getName() +
                " (" + debt.getUser1().getEmail() + ")");
        alert.showAndWait();
    }

    private ImageView generateIcons(String path) {
        String iconPath = "file:src/main/resources/" + path + ".png";
        Image image = new Image(iconPath);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        return imageView;
    }

    /**
     * go back to overview page
     */
    public void abortDebts() {
        accordionDebts.getPanes().clear();
        mainCtrl.showOverview(event, "-1");
    }

    /**
     * Getter for the event
     *
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Handles the action when common keys are pressed.
     *
     * @param e The key instance.
     */
    public void keyPressed(KeyEvent e) {
        if (e.getCode() == ESCAPE) {
            abortDebts();
        }
    }
}
