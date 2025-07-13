package paagbat.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import paagbat.App;
import paagbat.model.ReadingTrackerItem;
import paagbat.model.SqlConnector;
import paagbat.model.enums.ReadingStatus;

public class ReadingTrackerController {
    @FXML
    private ChoiceBox<String> readingStatusChoiceBox;

    @FXML
    private DatePicker readingDatePicker;

    @FXML
    private GridPane trackerGridPane;

    private int userID = paagbat.model.Erabiltzailea.getCurrentUser().getId();

    @FXML
    private void initialize() {
        readingDatePicker.requestFocus();

        // Cargar los labels del enum ReadingStatus en el ChoiceBox
        readingStatusChoiceBox.getItems().setAll(
                Arrays.stream(ReadingStatus.values())
                        .map(ReadingStatus::toString)
                        .collect(Collectors.toList()));
        for (int month = 0; month < 12; month++) {
            int daysInMonth = Month.of(month + 1).length(Year.now().isLeap());
            for (int day = 1; day <= daysInMonth; day++) {
                Label dayLabel = new Label();
                dayLabel.setMinSize(14, 12.5);
                GridPane.setColumnIndex(dayLabel, month);
                GridPane.setRowIndex(dayLabel, day);
                trackerGridPane.getChildren().add(dayLabel);
            }
        }
        loadReadingTrackerGrid();

    }

    @FXML
    private void handleUpdate() {
        String selectedLabel = readingStatusChoiceBox.getValue();
        LocalDate date = readingDatePicker.getValue();
        
        ReadingStatus status = Arrays.stream(ReadingStatus.values())
                .filter(rs -> rs.toString().equals(selectedLabel))
                .findFirst()
                .orElse(null);

        if (status == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error (Incorrect status)");
            alert.setHeaderText(null);
            alert.setContentText("Invalid status.");
            alert.showAndWait();
            return;
        }

        ReadingTrackerItem item = new ReadingTrackerItem(
                userID,
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                status.name());

        SqlConnector.updateReadingTrackerItem(item);
        loadReadingTrackerGrid();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edited");
        alert.setHeaderText(null);
        alert.setContentText("Record edited successfully.");
        alert.showAndWait();
    }


    @FXML
    private void handleSave() {
        String selectedLabel = readingStatusChoiceBox.getValue();
        LocalDate date = readingDatePicker.getValue();

        // Validar campos vacíos
        if (selectedLabel == null || date == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error (Invalid data)");
            alert.setHeaderText(null);
            alert.setContentText("Empty entries. Choose a status and date.");
            alert.showAndWait();
            return;
        }

        // No permitir fechas futuras
        if (date.isAfter(LocalDate.now())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error (Future date)");
            alert.setHeaderText(null);
            alert.setContentText("It is not possible to select a future date. Please select a valid date.");
            alert.showAndWait();
            return;
        }

        // No permitir editar valores ya metidos
        List<ReadingTrackerItem> items = SqlConnector.getReadingTrackerItems(userID);
        boolean exists = items.stream().anyMatch(item -> item.getYear() == date.getYear() &&
                item.getMonth() == date.getMonthValue() &&
                item.getDay() == date.getDayOfMonth());
        if (exists) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error (Date already exists)");
            alert.setHeaderText(null);
            alert.setContentText("This date cannot be used, it is already registered.");
            alert.showAndWait();
            return;
        }

        // Convertir el label seleccionado en el enum ReadingStatus
        ReadingStatus status = Arrays.stream(ReadingStatus.values())
                .filter(rs -> rs.toString().equals(selectedLabel))
                .findFirst()
                .orElse(null);

        if (status == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error (Incorrect status)");
            alert.setHeaderText(null);
            alert.setContentText("Invalid status selected. Please select a valid status.");
            alert.showAndWait();
            return;
        }

        ReadingTrackerItem item = new ReadingTrackerItem(
                userID,
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                status.name());

        SqlConnector.insertReadingTrackerItem(item);
        loadReadingTrackerGrid();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Saved");
        alert.setHeaderText(null);
        alert.setContentText("Record saved successfully.");
        alert.showAndWait();
    }

    private void highlightDay(LocalDate date, ReadingStatus status) {
        int month = date.getMonthValue() - 1; // Columnas empiezan en 0
        int day = date.getDayOfMonth();

        String color1, color2;
        switch (status) {
            case RED:
                color1 = "#f6a7c7";
                color2 = "#d06d95";
                break;
            case ORANGE:
                color1 = "#f6c7a7";
                color2 = "#c98b60";
                break;
            case YELLOW:
                color1 = "#f6e7a7";
                color2 = "#ffd058";
                break;
            case GREEN:
                color1 = "#b7e3d8";
                color2 = "#6ac5ae";
                break;
            case BLUE:
                color1 = "#b2d8f5";
                color2 = "#3b8af5";
                break;
            case PURPLE:
                color1 = "#c7a7f6";
                color2 = "#9065d0";
                break;
            case PINK:
                color1 = "#f2a7f6";
                color2 = "#d35fd9";
                break;
            default:
                color1 = "#e7f4fd";
                color2 = "#668094";
        }

        for (javafx.scene.Node node : trackerGridPane.getChildren()) {
            if (node instanceof Label) {
                Integer col = GridPane.getColumnIndex(node);
                Integer row = GridPane.getRowIndex(node);
                // Si col o row son null, ponlos a 0 (GridPane lo hace por defecto)
                if (col == null)
                    col = 0;
                if (row == null)
                    row = 0;
                if (col == month && row == day) {
                    node.setStyle("-fx-background-color: " + color1 + "; -fx-border-color: " + color2 + ";");
                }
            }
        }
    }

    private void loadReadingTrackerGrid() {
        int userId = paagbat.model.Erabiltzailea.getCurrentUser().getId();
        // Supón que tienes este método en SqlConnector:
        // List<ReadingTrackerItem> items = SqlConnector.getReadingTrackerItems(userId);
        List<ReadingTrackerItem> items = SqlConnector.getReadingTrackerItems(userId);

        // Limpia los estilos previos
        for (javafx.scene.Node node : trackerGridPane.getChildren()) {
            if (node instanceof Label) {
                node.setStyle(""); // o pon el estilo por defecto
            }
        }

        // Pinta los días guardados
        for (ReadingTrackerItem item : items) {
            LocalDate date = LocalDate.of(item.getYear(), item.getMonth(), item.getDay());
            ReadingStatus status = ReadingStatus.valueOf(item.getStatus());
            highlightDay(date, status);
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
