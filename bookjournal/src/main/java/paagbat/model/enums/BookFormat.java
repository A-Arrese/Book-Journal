package paagbat.model.enums;

public enum BookFormat {
    FISICO("FISICO"),
    EBOOK("EBOOK"),
    AUDIOBOOK("AUDIOBOOK");

    private final String label;

    BookFormat(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}
