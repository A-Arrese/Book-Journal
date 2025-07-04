package paagbat.model.base;

import paagbat.model.enums.BookFormat;
import paagbat.model.enums.Genre;

public abstract class BaseBook {
    private int id;
    private String title;
    private String author;
    private Genre genre;
    private int duration;
    private BookFormat format;
    private String coverImage;

    public BaseBook(int id, String title, String author, Genre genre, int duration, BookFormat format, String coverImage) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.duration = duration;
        this.format = format;
        this.coverImage = coverImage;
    }

    // Getter y Setter
    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    // Método abstracto para información adicional
    public String getExtraInfo(){
        return "Libro Basico";
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public BookFormat getFormat() {
        return format;
    }

    public void setFormat(BookFormat format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return String.format(
            "Libro{id=%d, título='%s', autor='%s', género=%s, duración=%d, formato=%s}",
            id, title, author, genre.name(), duration, format.toString()
        );
    }
}
