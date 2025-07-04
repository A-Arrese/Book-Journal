package paagbat.model;

import javafx.beans.property.*;

/**
 * Irakurleen gustuko 5 genero onenak gordetzeko klasea.
 * 
 * Genero bakoitza StringProperty gisa gordetzen da, JavaFXekin bateragarritasuna bermatzeko.
 */
public class GenreTop5 {
    private final SimpleStringProperty genre1 = new SimpleStringProperty();
    private final SimpleStringProperty genre2 = new SimpleStringProperty();
    private final SimpleStringProperty genre3 = new SimpleStringProperty();
    private final SimpleStringProperty genre4 = new SimpleStringProperty();
    private final SimpleStringProperty genre5 = new SimpleStringProperty();

    /**
     * Lehenengo generoaren propietatea itzultzen du.
     */
    public StringProperty genre1Property() { 
        return genre1; 
    }

    /**
     * Bigarren generoaren propietatea itzultzen du.
     */
    public StringProperty genre2Property() { 
        return genre2; 
    }

    /**
     * Hirugarren generoaren propietatea itzultzen du.
     */
    public StringProperty genre3Property() { 
        return genre3; 
    }

    /**
     * Laugarren generoaren propietatea itzultzen du.
     */
    public StringProperty genre4Property() { 
        return genre4; 
    }

    /**
     * Bosgarren generoaren propietatea itzultzen du.
     */
    public StringProperty genre5Property() { 
        return genre5; 
    }

    /**
     * Aukeratutako indizeko generoa ezartzen du.
     *
     * @param index Generoaren posizioa (0-4 artekoa).
     * @param value Ezartzeko generoaren izena.
     */
    public void setGenre(int index, String value) {
        switch (index) {
            case 0: genre1.set(value);
            case 1: genre2.set(value);
            case 2: genre3.set(value);
            case 3: genre4.set(value);
            case 4: genre5.set(value);
        }
    }
}
