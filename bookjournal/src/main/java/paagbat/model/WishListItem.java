package paagbat.model;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.FloatProperty;
import javafx.scene.control.CheckBox;

/**
 * Erosgai dauden liburuen zerrendarako elementua.
 * 
 * Liburu baten izena, egilea, prezioa eta erosketaren egoera gordetzen ditu.
 */
public class WishListItem {
    private final WishBook book;
    private final StringProperty title;
    private final StringProperty author;
    private final FloatProperty price;
    private final StringProperty buy;
    private final CheckBox select;

    /**
     * WishListItem objektu berri bat sortzen du.
     *
     * @param book Erosgai dagoen liburua (WishBook objektua).
     * @param buy Erosketa egoera (adibidez, "Erosita" edo "Erosi gabe").
     */
    public WishListItem(WishBook book, String buy) {
        this.book = book;
        this.title = new SimpleStringProperty(book.getTitle());
        this.author = new SimpleStringProperty(book.getAuthor());
        this.price = new SimpleFloatProperty(book.getPrice());
        this.buy = new SimpleStringProperty(buy);
        this.select = new CheckBox();
    }

    /**
     * Izenburuaren propietatea itzultzen du (TableView-erako erabilgarria).
     *
     * @return Izenburuaren StringProperty-a.
     */
    public StringProperty titleProperty() {
        return title;
    }

    /**
     * Egilearen propietatea itzultzen du (TableView-erako erabilgarria).
     *
     * @return Egilearen StringProperty-a.
     */
    public StringProperty authorProperty() {
        return author;
    }

    /**
     * Prezioaren propietatea itzultzen du (TableView-erako erabilgarria).
     *
     * @return Prezioaren FloatProperty-a.
     */
    public FloatProperty priceProperty() {
        return price;
    }

    /**
     * Erosketa egoeraren propietatea itzultzen du (TableView-erako erabilgarria).
     *
     * @return Erosketa egoeraren StringProperty-a.
     */
    public StringProperty buyProperty() {
        return buy;
    }

    /**
     * Liburuaren identifikadorea (ID) itzultzen du.
     *
     * @return Liburuaren ID-a.
     */
    public int getBookID() {
        return book.getId();
    }

    /**
     * Liburua hautatzeko CheckBox kontrola itzultzen du.
     *
     * @return CheckBox objektua.
     */
    public CheckBox getSelect() {
        return select;
    }

    /**
     * Lotutako oinarrizko liburua (WishBook) itzultzen du.
     *
     * @return WishBook objektua.
     */
    public WishBook getBaseBook() {
        return book;
    }
}
