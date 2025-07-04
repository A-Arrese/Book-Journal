package paagbat.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import paagbat.App;
import paagbat.model.SqlConnector;

import java.io.IOException;

/**
 * SingupController klasea erabiltzaile berriak erregistratzeko pantaila kudeatzen du.
 * 
 * FXML bidez lotutako interfazearen elementuak erabiltzen ditu (erabiltzaile izena eta bi pasahitz eremu).
 * 
 * Erabiltzaileak izena eta pasahitza sartu ondoren, honako prozesuak egiten dira:
 * - Eremu guztiak beteta daudela egiaztatu.
 * - Bi pasahitzak berdinak direla egiaztatu.
 * - Datu-basean erabiltzaile berria erregistratu.
 * 
 * Arrakastaz erregistratuz gero, erabiltzailea login pantailara bideratzen da.
 * Akatsen bat gertatuz gero (adibidez, erabiltzailea existitzen bada edo pasahitzak desberdinak badira),
 * errore mezu bat bistaratzen da.
 */

public class SingupController {
    // Interfazeko eremuak
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField passwordField1;

    /**
     * Erabiltzailea saiatzen denean kontu bat sortzen, metodo hau exekutatzen da.
     * Eremuak balidatu, eta erabiltzailea datu-basean erregistratzen saiatzen da.
     * 
     * @throws IOException Pantaila aldaketak egitean errore bat gertatuz gero.
     */
    @FXML
    private void handleSingUp() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = passwordField1.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            showAlert("Eremu guztiak bete behar dira.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Pasahitzak ez dira berdinak.");
            return;
        }

        boolean success = SqlConnector.registerUser(username, password);

        if (success) {
            showInfo("Kontua sortu da! Saioa hasi dezakezu.");
            App.setRoot("Login");
        } else {
            showAlert("Erabiltzaile hori jadanik existitzen da.");
        }
    }

    
    /**
     * Emandako mezu batekin errore alerta bat erakusten du.
     * 
     * @param message Erakutsiko den mezuaren testua.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errorea");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Emandako mezu batekin informazio alerta bat erakusten du.
     * 
     * @param message Erakutsiko den mezuaren testua.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazioa");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Erabiltzailea login pantailara eramaten du.
     * 
     * @throws IOException Pantaila aldaketak egitean errore bat gertatuz gero.
     */
    @FXML
    private void handleLogin() throws IOException {
        App.setRoot("Login");
    }

}
