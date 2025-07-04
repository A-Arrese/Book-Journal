package paagbat.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import paagbat.App;
import paagbat.model.Erabiltzailea;
import paagbat.model.RatingValue;
import paagbat.model.SqlConnector;
import paagbat.model.base.User;

public class RatingController {
    @FXML private TextField star1;
    @FXML private TextField star2;
    @FXML private TextField star3;
    @FXML private TextField star4;
    @FXML private TextField star5;

    /**
    * Metodo honek ikuspegia "inicializatuko" du, erabiltzailearen balorazioak
    * kargatuz eta balorazio horiek erakutsiko ditu.
    * 
    * Balorazioak datu-basean badaude, horiek testu-eremuen bidez erakutsiko dira,
    * eta testu-eremuak desaktibatuko dira erabiltzaileak ez ditzan aldatu. 
    * Baloraziorik ez badago, testu-eremuak aktibo utziko dira, erabiltzaileak 
    * balorazioak sartu ahal izan ditzan.
    */

    @FXML
    private void initialize(){
        star1.requestFocus();
        
        User user = Erabiltzailea.getCurrentUser();
        if (user == null) return; // Segurtasun neurriak erabiltzaile bat ez badago

        RatingValue rating = SqlConnector.getRatingForUser(user.getId());

        if (rating != null) {
            // Datuak daude: balorazioak erakutsi eta testu-eremuak desgaitu
            star1.setText(rating.getMeaningForStar(1));
            star2.setText(rating.getMeaningForStar(2));
            star3.setText(rating.getMeaningForStar(3));
            star4.setText(rating.getMeaningForStar(4));
            star5.setText(rating.getMeaningForStar(5));

            setFieldsDisabled(true);
        } else {
            // Ez dago baloraziorik: testu-eremuak aktibo uzten dira
            setFieldsDisabled(false);
        }
    }

    /**
     * Gorde botoia sakatzen denean exekutatzen den metodoa.
     * Erabiltzaileak sartutako balorazioak datu-basera gordetzen ditu.
     * 
     * @param event Gorde botoia sakatzen den ekintza.
     */
    @FXML
    private void handleGorde(ActionEvent event){
        User currentUser = Erabiltzailea.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Ez dago erabiltzaile aktiborik.");
            return;
        }
    
        int userID = currentUser.getId();
    
        String s1 = star1.getText();
        String s2 = star2.getText();
        String s3 = star3.getText();
        String s4 = star4.getText();
        String s5 = star5.getText();
    
        SqlConnector.saveRatingValues(userID, s1, s2, s3, s4, s5);
        setFieldsDisabled(true);

        System.out.println("Balorazioa ondo gorde da.");
    }

    /**
     * Editatu botoia sakatzen denean, testu-eremuak berriro aktibatzen ditu
     * erabiltzaileak balorazioak alda ditzan.
     * 
     * @param event Editatu botoia sakatzean jasotako ekintza.
     */
    @FXML
    private void handleEdit(ActionEvent event) {
        setFieldsDisabled(false);
    }

    /**
     * Testu-eremuak aktibo edo desgaitzeko metodoa.
     * 
     * Balorazioak editatzeko edo irakurri besterik ez egiteko aukera ematen du.
     * 
     * @param value True baldin bada, testu-eremuak desgaitzen dira; False baldin bada,
     *              testu-eremuak aktibatzen dira.
     */
    private void setFieldsDisabled(boolean value) {
        star1.setDisable(value);
        star2.setDisable(value);
        star3.setDisable(value);
        star4.setDisable(value);
        star5.setDisable(value);
    }

    // Pantaila aldaketarako metodoak (menu botoiak)

    /**
     * Saioa amaitu eta login pantailara bideratzen du.
     * 
     * @throws IOException Pantaila aldaketak errore bat gertatuz gero.
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
