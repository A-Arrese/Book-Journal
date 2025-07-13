package paagbat.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import paagbat.App;
import paagbat.model.Erabiltzailea;
import paagbat.model.ReadingLogItem;
import paagbat.model.ReviewBook;
import paagbat.model.SqlConnector;
import paagbat.model.base.User;

/**
 * RedingLogController klasea erabiltzailearen irakurketa-erregistroa (Reading
 * Log) kudeatzen duen kontrolatzailea da.
 * 
 * Erabiltzaileak irakurritako liburuen zerrenda bat ikusi, berrikusi edo
 * ezabatzeko aukera ematen du.
 * Gainera, liburu berriak gehitzeko edo aplikazioko beste atal batzuetara
 * nabigatzeko aukera ematen du.
 */
public class RedingLogController {
    private User user = Erabiltzailea.getCurrentUser();

    @FXML
    private TableView<ReadingLogItem> readinLog_TableView;

    /**
     * initialize metodoa, pantaila kargatzean exekutatzen da.
     * Taula prestatzen du eta erabiltzailearen irakurketa-erregistroa kargatzen du.
     */
    @FXML
    private void initialize() {
        readinLog_TableView.requestFocus();
        setupTable();
        loadReadingLog();
    }

    /**
     * Taula konfiguratzen du ReadingLogItem objektuak erakusteko.
     * Zutabe hauek sortzen dira:
     * - Aukeratzeko laukia (CheckBox)
     * - Liburuaren titulua
     * - Egilea
     * - Balorazioa
     * - Irakurketa hasierako data
     * - Irakurketa amaierako data
     * 
     * Ondoren, zutabe guztiak taulan ezartzen dira.
     */
    private void setupTable() {
        TableColumn<ReadingLogItem, CheckBox> selectCol = new TableColumn<>("");
        selectCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSelect()));

        TableColumn<ReadingLogItem, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cell -> cell.getValue().titleProperty());

        TableColumn<ReadingLogItem, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(cell -> cell.getValue().authorProperty());

        TableColumn<ReadingLogItem, String> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(cell -> cell.getValue().ratingProperty());

        TableColumn<ReadingLogItem, LocalDate> startCol = new TableColumn<>("Start");
        startCol.setCellValueFactory(cell -> cell.getValue().startDateProperty());

        TableColumn<ReadingLogItem, LocalDate> endCol = new TableColumn<>("End");
        endCol.setCellValueFactory(cell -> cell.getValue().endDateProperty());

        // Zutabe guztiak taulan ezarri
        readinLog_TableView.getColumns().setAll(selectCol, titleCol, authorCol, startCol, endCol, ratingCol);
    }

    /**
     * Erabiltzailearen irakurketa-erregistroa datu-basetik kargatzen du.
     * Lortutako liburu guztiak taulan bistaratzen dira.
     */
    private void loadReadingLog() {
        // System.out.println(user.getId());
        List<ReadingLogItem> logItems = SqlConnector.getReadingLogForUser(user.getId());
        System.out.println("Loaded items: " + logItems.size());
        readinLog_TableView.getItems().setAll(logItems);
    }

    /**
     * Taulan aukeratutako liburuak datu-basetik ezabatzen ditu.
     * 
     * Pausoak:
     * 1. Aukeratutako liburuak (CheckBox markatuta dutenak) bilatzen dira.
     * 2. Datu-baseko dagokien erregistroak ezabatzen dira (bookID eta userID
     * erabiliz).
     * 3. Taula berriro kargatzen da aldaketak ikusteko.
     * 
     * @param event Ezabatzeko botoia sakatzean pasatzen den ekintza.
     */
    @FXML
    private void handleDeleteSelected(ActionEvent event) {
        List<ReadingLogItem> toDelete = readinLog_TableView.getItems().stream()
                .filter(item -> item.getSelect().isSelected())
                .collect(Collectors.toList());

        for (ReadingLogItem item : toDelete) {
            // Liburua datu-basetik ezabatu
            SqlConnector.deleteReviewByTitle(item.getBookID(), user.getId());
        }

        // Taula eguneratu
        loadReadingLog();
    }

    /**
     * Erabiltzaileak taulan liburu bat aukeratzen duenean, berrikusteko pantailara
     * eramaten du.
     * 
     * Baldintzak:
     * - Liburu bakarra aukeratuta egon behar da (bestela abisu bat agertuko da).
     * 
     * Prozesua:
     * 1. Aukeratutako liburua ReviewBook klasean gordetzen da (liburua
     * "pasatzeko").
     * 2. BookReview pantaila kargatzen da.
     * 
     * @throws IOException Pantaila aldatzean errore bat gertatzen bada.
     */
    @FXML
    private void handleReview() throws IOException {
        List<ReadingLogItem> selectedItems = readinLog_TableView.getItems().stream()
                .filter(item -> item.getSelect().isSelected())
                .collect(Collectors.toList());

        if (selectedItems.size() != 1) {
            // Abisua: liburu bat eta bakarra aukeratu behar da
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Wrong choice");
            alert.setHeaderText(null);
            alert.setContentText("You can choose only one book.");
            alert.showAndWait();
            return;
        }

        // Aukeratutako liburua ReviewBook klasean gordetzen da
        ReadingLogItem selectedItem = selectedItems.get(0);
        ReviewBook.setSelectedItem(selectedItem);

        // BookReview pantaila kargatu
        App.setRoot("BookReview");
    }

    /**
     * Liburu berri bat gehitzeko pantailara eramaten du erabiltzailea.
     * 
     * @throws IOException Pantaila aldatzean errore bat gertatzen bada.
     */
    @FXML
    private void handleNew() throws IOException {
        App.setRoot("NewBook");
    }

    // Navigazioko metodoak (Menuan dauden aukerak)

    /**
     * Erabiltzailea aplikaziotik irteten da eta login pantailara bueltatzen da.
     * 
     * @throws IOException Pantaila aldatzean errore bat gertatzen bada.
     */
    @FXML
    private void handleLogOut() throws IOException {
        Erabiltzailea.clear();
        App.setRoot("Login");
    }

    @FXML
    private void mainMenu() throws IOException {
        App.setRoot("MainMenu");
    }
}
