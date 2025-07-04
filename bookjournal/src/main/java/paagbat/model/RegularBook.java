package paagbat.model;

import paagbat.model.base.BaseBook;
import paagbat.model.enums.BookFormat;
import paagbat.model.enums.Genre;

public class RegularBook extends BaseBook {
    public RegularBook(int id, String title, String author, Genre genre, int duration, BookFormat format, String coverImage) {
        super(id, title, author, genre, duration, format, coverImage);
    }

    @Override
    public String getExtraInfo() {
        return "Este es un libro regular.";
    }
}
