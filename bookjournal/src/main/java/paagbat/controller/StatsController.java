package paagbat.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import paagbat.App;
import paagbat.model.Erabiltzailea;
import paagbat.model.GenreTop5;
import paagbat.model.RatingValueItem;
import paagbat.model.SqlConnector;
import paagbat.model.base.User;

/**
 * StatsController klasea erabiltzailearen irakurketako estatistikak kudeatzeko arduratzen da.
 * 
 * Klase honek hainbat datu bistaratzen ditu:
 * - Liburuen balorazioaren estatistikak (izarren arabera).
 * - Gehien irakurritako generoak.
 * - Erabiltzaileak liburuetan gastatutako dirua.
 * - Irakurritako orri kopurua.
 * 
 * Interfazeko taulak eta etiketak automatikoki kargatu eta eguneratzen ditu datu-baseko informazioarekin.
 * Gainera, aplikazioaren pantaila desberdinen arteko nabigazioa kudeatzen du.
 */
public class StatsController {
    // Saioa hasi duen erabiltzailearen informazioa
    private User user = Erabiltzailea.getCurrentUser();

    // Interfazeko elementuak: balorazio taula, generoen taula eta etiketak
    @FXML
    private TableView<RatingValueItem> rating_TableView;
    @FXML
    private TableView<GenreTop5> genre_TableView;
    @FXML
    private Label dirua_Label, orriak_Label;

    /**
     * Lehenengo aldiz pantaila kargatzen denean exekutatzen da.
     * Taulak prestatzen ditu eta datuak kargatzen ditu.
     */
    @FXML
    private void initialize() {
        dirua_Label.requestFocus();
        setupRatingTable();
        setupGenreStatsTable();
        loadOrriak();
        loadDirua();
    }

    /**
     * Balorazio taula konfiguratu (1etik 5 izarrera arteko zutabeak sortuz).
     */
    private void setupRatingTable() {
        TableColumn<RatingValueItem, Number> star1Col = new TableColumn<>("⭐ 1 Star");
        star1Col.setCellValueFactory(cell -> cell.getValue().star1Property());

        TableColumn<RatingValueItem, Number> star2Col = new TableColumn<>("⭐ 2 Star");
        star2Col.setCellValueFactory(cell -> cell.getValue().star2Property());

        TableColumn<RatingValueItem, Number> star3Col = new TableColumn<>("⭐ 3 Star");
        star3Col.setCellValueFactory(cell -> cell.getValue().star3Property());

        TableColumn<RatingValueItem, Number> star4Col = new TableColumn<>("⭐ 4 Star");
        star4Col.setCellValueFactory(cell -> cell.getValue().star4Property());

        TableColumn<RatingValueItem, Number> star5Col = new TableColumn<>("⭐ 5 Star");
        star5Col.setCellValueFactory(cell -> cell.getValue().star5Property());

        rating_TableView.getColumns().setAll(star1Col, star2Col, star3Col, star4Col, star5Col);
    
        RatingValueItem stats = SqlConnector.getStarStatsForUser(user.getId());
        rating_TableView.getItems().setAll(stats);
    }

    /**
     * Generoen taula konfiguratu (Top 5 genero).
     */
    private void setupGenreStatsTable() {
        genre_TableView.getItems().clear();
        genre_TableView.getColumns().clear();

        TableColumn<GenreTop5, String> col1 = new TableColumn<>("1°");
        col1.setCellValueFactory(data -> data.getValue().genre1Property());

        TableColumn<GenreTop5, String> col2 = new TableColumn<>("2°");
        col2.setCellValueFactory(data -> data.getValue().genre2Property());

        TableColumn<GenreTop5, String> col3 = new TableColumn<>("3°");
        col3.setCellValueFactory(data -> data.getValue().genre3Property());

        TableColumn<GenreTop5, String> col4 = new TableColumn<>("4°");
        col4.setCellValueFactory(data -> data.getValue().genre4Property());

        TableColumn<GenreTop5, String> col5 = new TableColumn<>("5°");
        col5.setCellValueFactory(data -> data.getValue().genre5Property());

        genre_TableView.getColumns().addAll(col1, col2, col3, col4, col5);

        GenreTop5 stats = SqlConnector.getTopGenrePercentagesForUser(user.getId());
        genre_TableView.getItems().add(stats);
    }

    /**
     * Erabiltzaileak liburuetan guztira gastatu duen dirua kargatu eta etiketan erakutsi.
     */
    private void loadOrriak(){
        float diruTot = SqlConnector.getGastuKopTotal(user.getId());
        dirua_Label.setText(""+diruTot);
    }

    /**
     * Erabiltzaileak irakurri duen orri kopurua kargatu eta etiketan erakutsi.
     */
    private void loadDirua(){
        int orriKop = SqlConnector.getOrriKopTotal(user.getId());
        orriak_Label.setText(""+orriKop);
    }

    // Pantaila aldaketarako metodoak (menu botoiak)
    
    /** Saioa amaitu eta login pantailara joan. */
    @FXML
    private void handleLogOut() throws IOException {
        App.setRoot("Login");
    }

    @FXML
    private void mainMenu() throws IOException {
        App.setRoot("MainMenu");
    }
}
