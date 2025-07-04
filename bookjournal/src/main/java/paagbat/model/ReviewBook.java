package paagbat.model;

public class ReviewBook {
    private static ReadingLogItem selectedItem;

    public static ReadingLogItem getSelectedItem() {
        return selectedItem;
    }

    public static void setSelectedItem(ReadingLogItem item) {
        selectedItem = item;
    }

    public static void clear() {
        selectedItem = null;
    }
}
