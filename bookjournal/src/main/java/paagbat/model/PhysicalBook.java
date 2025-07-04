package paagbat.model;

import paagbat.model.base.BaseBook;
import paagbat.model.enums.BookFormat;
import paagbat.model.enums.Genre;

public class PhysicalBook extends BaseBook {
    public PhysicalBook(int id, String title, String author, Genre genre, int duration ,String coverImage) {
        super(id, title, author, genre, duration, BookFormat.FISICO, coverImage);
    }

    @Override
    public String getExtraInfo() {
        return "Formato físico";
    }
}
