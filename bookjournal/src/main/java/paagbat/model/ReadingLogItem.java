package paagbat.model;

import javafx.beans.property.*;
import javafx.scene.control.CheckBox;
import paagbat.model.base.BaseBook;

import java.time.LocalDate;

/**
 * Liburu baten irakurketaren erregistro-elementua.
 * 
 * Liburuaren informazioa eta irakurketaren datak gordetzen ditu, baita hautatzeko aukera bat ere.
 */
public class ReadingLogItem {
    private final BaseBook baseBook;
    private final StringProperty title;
    private final StringProperty author;
    private final StringProperty genre;
    private final StringProperty rating;
    private final StringProperty coverImage;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> endDate;
    private final CheckBox select;

    /**
     * ReadingLogItem berri bat sortzen du oinarrizko liburuarekin, balorazioarekin eta irakurketaren hasiera/amaiera datarekin.
     *
     * @param baseBook Oinarrizko liburua.
     * @param rating Balorazioa (izarren bidez).
     * @param startDate Irakurketaren hasiera data.
     * @param endDate Irakurketaren amaiera data.
     */
    public ReadingLogItem(BaseBook baseBook, String rating, String startDate, String endDate) {
        this.baseBook = baseBook;
        this.title = new SimpleStringProperty(baseBook.getTitle());
        this.author = new SimpleStringProperty(baseBook.getAuthor());
        this.genre = new SimpleStringProperty(baseBook.getGenre().name());
        this.coverImage = new SimpleStringProperty(baseBook.getCoverImage());

        if (rating != null) {
            this.rating = new SimpleStringProperty(rating);
        } else {
            this.rating = new SimpleStringProperty("Baloratu gabe");
        }

        if (startDate != null) {
            this.startDate = new SimpleObjectProperty<>(LocalDate.parse(startDate));
        } else {
            this.startDate = new SimpleObjectProperty<>(null);
        }

        if (endDate != null) {
            this.endDate = new SimpleObjectProperty<>(LocalDate.parse(endDate));
        } else {
            this.endDate = new SimpleObjectProperty<>(null);
        }

        this.select = new CheckBox();
    }

    /** Izenburua eskuratzeko propietatea. */
    public StringProperty titleProperty() { 
        return title; 
    }

    /** Egilea eskuratzeko propietatea. */
    public StringProperty authorProperty() { 
        return author; 
    }

    /** Generoa eskuratzeko propietatea. */
    public StringProperty genreProperty() { 
        return genre; 
    }

    /** Balorazioa eskuratzeko propietatea. */
    public StringProperty ratingProperty() { 
        return rating; 
    }

    /** Hasiera data eskuratzeko propietatea. */
    public ObjectProperty<LocalDate> startDateProperty() { 
        return startDate; 
    }

    /** Amaiera data eskuratzeko propietatea. */
    public ObjectProperty<LocalDate> endDateProperty() { 
        return endDate; 
    }

    /** Liburuaren ID-a itzultzen du. */
    public int getBookID() { 
        return baseBook.getId(); 
    }

    /** Izenburua itzultzen du. */
    public String getTitle() { 
        return title.get(); 
    }

    /** Egilea itzultzen du. */
    public String getAuthor() { 
        return author.get(); 
    }

    /** Generoa itzultzen du. */
    public String getGenre() { 
        return genre.get(); 
    }

    /** Balorazioa itzultzen du. */
    public String getRating() { 
        return rating.get(); 
    }

    /** Irakurketaren hasiera data itzultzen du. */
    public LocalDate getStartDate() { 
        return startDate.get(); 
    }

    /** Irakurketaren amaiera data itzultzen du. */
    public LocalDate getEndDate() { 
        return endDate.get(); 
    }
    public String getCoverImage() { 
        return coverImage.get(); 
    }
    /** Hautatzeko CheckBox objektua itzultzen du. */
    public CheckBox getSelect() { 
        return select; 
    }

    /** Oinarrizko liburua itzultzen du. */
    public BaseBook getBaseBook() { 
        return baseBook; 
    }
}