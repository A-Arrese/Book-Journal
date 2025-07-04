package paagbat.model.enums;

public enum ReadingStatus {
    RED("0-10"),   // No leyó ese día
    ORANGE("11-30"),       // Leyó ese día
    YELLOW("31-50"),       // Leyó ese día
    GREEN("51-70"),       // Leyó ese día
    BLUE("71-90"),       // Leyó ese día
    PURPLE("91-150"),
    PINK("150+");    // Leyó un poco (si quieres distinguir)
    // Puedes añadir más estados según lo que necesites

    private final String label;

    ReadingStatus(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}
