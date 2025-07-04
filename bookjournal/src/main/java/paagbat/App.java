package paagbat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Login"), 424, 600);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("BookJournal");

        Image image = new Image(App.class.getResourceAsStream("/paagbat/img/logo.png"));
        stage.getIcons().add(image);


        //Estilo de la aplicacion
        scene.getStylesheets().add(getClass().getResource("/paagbat/css/modena.css").toExternalForm());
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/paagbat/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}