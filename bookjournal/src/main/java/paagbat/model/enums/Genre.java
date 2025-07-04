package paagbat.model.enums;

public enum Genre {
    FICTION("Fiction"),
    NON_FICTION("Non-fiction"),
    SCIENCE_FICTION("Science-fiction"),
    FANTASY("Fantasy"),
    ROMANTASY("Romantasy"),
    MYSTERY("Mystery"),
    BIOGRAPHY("Biography"),
    HISTORY("History"),
    ROMANCE("Romance"),
    DARK_ROMANCE("Dark romance"),
    THRILLER("Thriller"),
    SELF_HELP("Self help"),
    PHILOSOPHY("Philosophy"),
    NEW_ADULT("New adult"),
    OTHER("Other");

    private final String label;

    Genre(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}

