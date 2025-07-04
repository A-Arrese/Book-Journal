/**
 * NewWishBookController klaseak erabiltzaileari nahi duen liburua
 * desio-zerrendan gehitzeko interfaze grafikoarekin elkarreragiteko aukera ematen dio.
 * 
 * FXML fitxategi batekin lotuta dago eta hiru testu-eremu (izenburua, idazlea eta prezioa)
 * eta bi botoi (gordetzeko eta atzera egiteko) kudeatzen ditu.
 * 
 * Funtzio nagusiak:
 * - Liburu baten datuak irakurri erabiltzailearen sarreratik.
 * - Prezioaren formatu egokia bermatu (komak edo apostrofoak puntu bihurtuz).
 * - WishBook objektua sortu eta SqlConnector bidez datu-basean gorde.
 * - Eragiketa arrakastatsua izan den jakinarazi eta formularioa garbitu.
 * - Atzera botoia sakatzean aurreko pantailara bueltatu.
 * 
 * Klase honek Erabiltzailea.getCurrentUser() bidez erabiltzaile aktiboaren ID-a lortzen du.
 */
package paagbat.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import paagbat.App;
import paagbat.model.Erabiltzailea;
import paagbat.model.SqlConnector;
import paagbat.model.WishBook;
import paagbat.model.base.User;

public class NewWishBookController {
    @FXML
    private TextField izenburuaText;
    @FXML
    private TextField idazleaText;
    @FXML
    private TextField prezioaText;

    @FXML
    private Button gordeButton;
    @FXML
    private Button itxuliButton;

    private User user = Erabiltzailea.getCurrentUser();;

    @FXML
    public void initialize() {
        izenburuaText.requestFocus();
    }

    /**
     * Gorde botoia sakatzean exekutatzen da.
     * Erabiltzaileak sartutako datuekin liburu objektu bat sortzen du eta
     * datu-basean gordetzen saiatzen da.
     * 
     * Arrakastaz gordetzen bada, mezua bistaratzen da eta pantaila aldatzen da.
     * Arazoren bat badago, errore-mezua bistaratzen da.
     * 
     * @throws IOException pantaila aldatzean errorea gertatzen bada
     */
    @FXML
    private void handleSave() throws IOException {
        String title = izenburuaText.getText();
        String author = idazleaText.getText();
        String prezioaStr = prezioaText.getText();
        prezioaStr = prezioaStr.replace(",", "."); // Koma puntuz ordezkatzen du
        prezioaStr = prezioaStr.replace("'", "."); // ' puntuz ordezkatzen du
        float prezioa = Float.parseFloat(prezioaStr);

        WishBook newBook = new WishBook(0, title, author, prezioa);

        if (SqlConnector.saveWishBookToDatabase(newBook, user.getId())) {
            showAlert("Liburua zuzen gorde da.");
            clearForm();
            App.setRoot("WishList");

        } else {
            showAlert("Errorea liburua gordetzean.");
        }
    }

    /**
     * Mezu bat bistaratzen du leiho informatibo batean.
     * 
     * @param message Erabiltzaileari erakutsi nahi zaion mezua
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Formularioaren testu-eremu guztiak garbitzen ditu.
     */
    private void clearForm() {
        izenburuaText.clear();
        idazleaText.clear();
        prezioaText.clear();
    }

    /**
     * Atzera botoia sakatzean erabiltzen da.
     * Aurreko pantailara bueltatzen da (WishList).
     * 
     * @throws IOException pantaila aldatzean errorea gertatzen bada
     */
    @FXML
    private void handleBack() throws IOException {
        App.setRoot("WishList");
    }
}
