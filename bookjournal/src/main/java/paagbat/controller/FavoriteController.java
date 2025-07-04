package paagbat.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import paagbat.App;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import paagbat.model.SqlConnector;

import java.util.List;

public class FavoriteController {

    @FXML
    private ImageView imgView1;
    @FXML
    private ImageView imgView2;
    @FXML
    private ImageView imgView3;
    @FXML
    private ImageView imgView4;
    @FXML
    private ImageView imgView5;
    @FXML
    private ImageView imgView6;

    @FXML
    private Label totalFav;
    @FXML
    private Button nextBtn;
    @FXML
    private Button prevBtn;

    private List<String> favoriteBooks;
    private int pageIndex = 0; // 0 = primeros 6, 1 = siguientes 6, etc.

    @FXML
    public void initialize() {
        totalFav.requestFocus();
        favoriteBooks = SqlConnector.getFavoriteBooks();
        totalFav.setText(""+favoriteBooks.size()+"");
        if(favoriteBooks.size() <= 6){
            nextBtn.setDisable(true);
            prevBtn.setDisable(true);
        } else {
            nextBtn.setDisable(false);
            prevBtn.setDisable(true);
        }
        showPage();
    }

    private void showPage() {
        ImageView[] views = { imgView1, imgView2, imgView3, imgView4, imgView5, imgView6 };
        int start = pageIndex * 6;
        for (int i = 0; i < 6; i++) {
            if (start + i < favoriteBooks.size()) {
                String imgPath = "/paagbat/img/covers/" + favoriteBooks.get(start + i);
                views[i].setImage(new Image(getClass().getResourceAsStream(imgPath)));
            } else {
                views[i].setImage(null);
            }
        }
    }

    @FXML
    private void nextPage() {
        if ((pageIndex + 1) * 6 < favoriteBooks.size()) {
            pageIndex++;
            prevBtn.setDisable(false);
            showPage();
        }
    }

    @FXML
    private void prevPage() {
        if (pageIndex > 0) {
            pageIndex--;
            showPage();
        }
    }

    @FXML
    private void handleLogOut() throws IOException {
        App.setRoot("Login");
    }

    @FXML
    private void mainMenu() throws IOException {
        App.setRoot("MainMenu");
    }
}
