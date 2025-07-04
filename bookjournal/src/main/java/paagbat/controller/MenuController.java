package paagbat.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import paagbat.App;

public class MenuController {
    @FXML
    private void ratingSystem() throws IOException {
        App.setRoot("RatingSystem");
    }

    @FXML
    private void readingLog() throws IOException {
        App.setRoot("RedingLog");
    }

    @FXML
    private void wishList() throws IOException {
        App.setRoot("WishList");
    }

    @FXML
    private void readingStats() throws IOException {
        App.setRoot("Stats");
    }

    @FXML
    private void readingPixel() throws IOException {
        App.setRoot("ReadingTracker");
        //System.out.println("Reading Pixel feature is not implemented yet.");
    }
    
    @FXML
    private void favoriteReading() throws IOException {
        App.setRoot("FavoriteReading");
    }

    @FXML
    private void handleLogOut() throws IOException {
        App.setRoot("Login");
    }
}
