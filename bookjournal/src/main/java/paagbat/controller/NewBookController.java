/**
 * NewBookController klaseak erabiltzaileari liburu berri bat sartu eta
 * irakurketen erregistroan gordetzeko aukera eskaintzen dio.
 *
 * FXML bidez eraikitako interfaze grafikoarekin konektatuta dago eta hainbat osagai
 * kudeatzen ditu: testu-eremuak (izenburua, idazlea, orrialdeak), generoaren hautapen
 * menua eta liburuaren formatua hautatzeko kontrolak (eBook, audioa, fisikoa).
 *
 * Funtzionalitate nagusiak:
 * - Liburuaren informazioa erabiltzailearen sarreratik jasotzea.
 * - Liburuaren formatua aukeratzea (eBook, AudioBook edo PhysicalBook).
 * - Genero aukera aurrez ezarritako balioetatik hautatzea.
 * - Liburu objektua sortu eta SqlConnector bidez datu-basean gordetzea.
 * - Erroreak erakustea edo arrakasta-mezua bistaratzea.
 *
 * Erabiltzaile aktiboaren informazioa Erabiltzailea.getCurrentUser() bidez lortzen da.
 */
package paagbat.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import paagbat.App;
import paagbat.model.AudioBook;
import paagbat.model.Ebook;
import paagbat.model.Erabiltzailea;
import paagbat.model.PhysicalBook;
import paagbat.model.SqlConnector;
import paagbat.model.base.BaseBook;
import paagbat.model.base.User;
import paagbat.model.enums.Genre;

public class NewBookController {
    @FXML
    private TextField izenburuaText;
    @FXML
    private TextField idazleaText;
    @FXML
    private TextField orriakText;

    @FXML
    private ComboBox<Genre> genreCombo;

    @FXML
    private CheckBox ebookCheck;
    @FXML
    private CheckBox audioCheck;
    @FXML
    private CheckBox fisikoaCheck;

    @FXML
    private Button portadaButton;
    @FXML
    private Button gordeButton;
    @FXML
    private Button itxuliButton;

    @FXML
    private ImageView coverImageView;

    private String coverImagePath = null;

    private User user = Erabiltzailea.getCurrentUser();;

    /**
     * Hasierako metodoa. Interfaze grafikoa kargatzen denean exekutatzen da.
     * Generoaren hautapen-koadroa (ComboBox) {@link Genre} balio guztiekin betetzen
     * du
     * eta balio lehenetsia "OTHER" ezartzen du.
     */
    @FXML
    public void initialize() {
        izenburuaText.requestFocus();
        genreCombo.getItems().setAll(Genre.values()); // Llena el combo con los géneros

        genreCombo.setValue(Genre.OTHER); // Valor default
        if (coverImagePath != null) {
            String imagePath = "/paagbat/img/covers/" + coverImagePath;
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            coverImageView.setImage(image);
        }
    }

    @FXML
    private void handlePortada() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Aukeratu portada");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Irudiak", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(portadaButton.getScene().getWindow());
        if (selectedFile != null) {
            // Solo el nombre del archivo
            String fileName = selectedFile.getName();
            // Ruta destino en resources/img/covers
            java.nio.file.Path destDir = java.nio.file.Paths.get("bookjournal/src/main/resources/paagbat/img/covers/");
            java.nio.file.Path destPath = destDir.resolve(fileName);

            try {
                // Crea la carpeta si no existe
                java.nio.file.Files.createDirectories(destDir);
                // Copia la imagen (sobrescribe si ya existe)
                java.nio.file.Files.copy(selectedFile.toPath(), destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                // Guarda solo el nombre para la base de datos
                coverImagePath = fileName;
                // Muestra la imagen en el ImageView
                javafx.scene.image.Image image = new javafx.scene.image.Image(destPath.toUri().toString());
                coverImageView.setImage(image);
            } catch (Exception e) {
                showAlert("Errorea irudia kopiatzean: " + e.getMessage());
            }
        }
    }

    /**
     * Gorde botoia sakatzean exekutatzen da.
     * Erabiltzailearen sarreratik liburuaren informazioa jasotzen du, egokia dela
     * egiaztatzen du,
     * eta aukeratutako formatuaren arabera dagokion liburu mota sortzen du.
     * Datuak ondo badaude, datu-basean gordetzen dira.
     *
     * Akats bat gertatuz gero (adibidez, formatu bat aukeratu gabe edo
     * orrialde-kopurua okerra),
     * informazio-mezu egokia bistaratzen da.
     *
     * @throws IOException pantaila aldatzean arazoak sortzen badira
     */
    @FXML
    private void handleSave() throws IOException {
        String title = izenburuaText.getText();
        String author = idazleaText.getText();
        Genre genre = genreCombo.getValue();
        int pages;

        try {
            pages = Integer.parseInt(orriakText.getText());
        } catch (NumberFormatException e) {
            showAlert("Orrialde-kopurua ez da zuzena.");
            return;
        }

        BaseBook newBook = null;

        // ...existing code...
        if (ebookCheck.isSelected()) {
            newBook = new Ebook(0, title, author, genre, pages, coverImagePath);
        } else if (audioCheck.isSelected()) {
            newBook = new AudioBook(0, title, author, genre, pages, coverImagePath);
        } else if (fisikoaCheck.isSelected()) {
            newBook = new PhysicalBook(0, title, author, genre, pages, coverImagePath);
        }else {
            showAlert("Formatu bat aukeratu beharra duzu.");
            return;
        }

        if (SqlConnector.saveBookToDatabase(newBook, user.getId())) {
            showAlert("Liburua zuzen gorde da.");
            clearForm();
            App.setRoot("RedingLog");

        } else {
            showAlert("Errorea liburua gordetzean.");
        }
    }

    /**
     * Erabiltzaileari informazio-mezu bat bistaratzen dio.
     *
     * @param message Pantailan erakutsi nahi den mezua
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Formularioaren eremu guztiak garbitzen ditu:
     * testu-eremuak hustu, generoa null ezarri eta formatuko aukera guztiak
     * desaktibatu.
     */
    private void clearForm() {
        izenburuaText.clear();
        idazleaText.clear();
        orriakText.clear();
        genreCombo.setValue(null);
        ebookCheck.setSelected(false);
        audioCheck.setSelected(false);
        fisikoaCheck.setSelected(false);
    }

    /**
     * Atzera botoia sakatzean deitzen da.
     * Aurreko pantailara bueltatzen da (RedingLog izenekoa).
     *
     * @throws IOException pantaila aldatzean errorea gertatzen bada
     */
    @FXML
    private void handleBack() throws IOException {
        App.setRoot("RedingLog");
    }
}
