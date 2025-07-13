package paagbat.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import paagbat.App;
import paagbat.model.Erabiltzailea;
import paagbat.model.SqlConnector;
import paagbat.model.WishListItem;
import paagbat.model.base.User;

/**
 * Erosketa-zerrendaren (WishList) kontrolatzailea.
 * 
 * Erabiltzailearen erosketarako zerrenda kargatzen eta kudeatzen du.
 * Gainera, liburu berriak gehitzeko, erosketak markatzeko edo liburuak
 * ezabatzeko funtzionalitateak eskaintzen ditu.
 */
public class WishListController {

    private User user = Erabiltzailea.getCurrentUser();

    @FXML
    private TableView<WishListItem> wishList_TableView;

    /**
     * initialize metodoa, pantaila lehen aldiz kargatzean exekutatzen da. 
     * Taula konfiguratu eta datuak kargatzen ditu.
     */
    @FXML
    private void initialize() {
        wishList_TableView.requestFocus();
        setupTable();
        loadWishList();
    }

    /**
     * Taula (TableView) konfiguratzeko metodoa.
     * Zutabe guztiak (hautaketa, izenburua, egilea, prezioa eta erosketaren egoera)
     * definitzen ditu.
     */
    private void setupTable() {
        TableColumn<WishListItem, CheckBox> selectCol = new TableColumn<>("");
        selectCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSelect()));

        TableColumn<WishListItem, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cell -> cell.getValue().titleProperty());

        TableColumn<WishListItem, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(cell -> cell.getValue().authorProperty());

        TableColumn<WishListItem, Number> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cell -> cell.getValue().priceProperty());

        TableColumn<WishListItem, String> buytCol = new TableColumn<>("Purchased");
        buytCol.setCellValueFactory(cell -> cell.getValue().buyProperty());

        wishList_TableView.getColumns().setAll(selectCol, titleCol, authorCol, priceCol, buytCol);
    }

    /**
     * Erabiltzailearen erosketarako liburu-zerrenda kargatzen du datu-baseko
     * informaziotik.
     */
    private void loadWishList() {
        List<WishListItem> logItems = SqlConnector.getWishListForUser(user.getId());
        System.out.println("Loaded items: " + logItems.size());
        wishList_TableView.getItems().setAll(logItems);
    }

    /**
     * Liburu berri bat gehitzeko pantaila irekitzen du.
     *
     * @throws IOException FXML fitxategia kargatzean errorea gertatzen bada.
     */
    @FXML
    private void handleNew() throws IOException {
        App.setRoot("NewWishBook");
    }

    /**
     * Hautatutako liburuak "erosita" bezala markatzen ditu.
     * 
     * Erabiltzaileak aukeratutako (checkbox aktibo duten) liburuak bilatzen ditu.
     * Horiek datu-basean eguneratzen ditu, erosita bezala markatuz.
     * Azkenik, zerrenda berriro kargatzen du aldaketak ikusteko.
     *
     * @param event Botoiaren klik ekintza.
     */
    @FXML
    private void handleBuy(ActionEvent event) {
        // Zerrendako hautatutako (checkbox aktibatuta duten) elementuak lortu
        List<WishListItem> toUpdate = wishList_TableView.getItems().stream()
                .filter(item -> item.getSelect().isSelected())
                .collect(Collectors.toList());

        // Hautatutako liburu bakoitza datu-basean eguneratu ("erosita" gisa markatu)
        for (WishListItem item : toUpdate) {
            SqlConnector.updateWishByTitle(item.getBookID(), user.getId());
        }

        // Taula berriro kargatu, aldaketak erakusteko
        loadWishList();
    }

    /**
     * Hautatutako liburuak ezabatzen ditu erosketen zerrendatik.
     * 
     * Erabiltzaileak aukeratutako (checkbox aktibo duten) liburuak bilatzen ditu.
     * Horiek datu-basetik ezabatzen ditu.
     * Azkenik, zerrenda berriro kargatzen du aldaketak ikusteko.
     *
     * @param event Botoiaren klik ekintza.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        // Zerrendako hautatutako (checkbox aktibatuta duten) elementuak lortu
        List<WishListItem> toDelete = wishList_TableView.getItems().stream()
                .filter(item -> item.getSelect().isSelected())
                .collect(Collectors.toList());

        // Hautatutako liburu bakoitza datu-basetik ezabatu
        for (WishListItem item : toDelete) {
            SqlConnector.deleteWishByTitle(item.getBookID(), user.getId());
        }

        // Taula berriro kargatu, aldaketak erakusteko
        loadWishList();
    }

    // Pantaila aldaketarako metodoak (menu botoiak)

    /**
     * Erabiltzailea saio-hasiera pantailara itzultzen du (logout).
     *
     * @throws IOException FXML fitxategia kargatzean errorea gertatzen bada.
     */
    @FXML
    private void handleLogOut() throws IOException {
        App.setRoot("Login");
    }


    /**
     * Erosketa-zerrenda pantaila berriro kargatzen du.
     *
     * @throws IOException FXML fitxategia kargatzean errorea gertatzen bada.
     */
    @FXML
    private void mainMenu() throws IOException {
        App.setRoot("MainMenu");
    }

}
