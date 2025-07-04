package paagbat.model;

public class RatingValue {
    private int userID;
    private String star_1;
    private String star_2;
    private String star_3;
    private String star_4;
    private String star_5;

    public RatingValue(int userID, String star_1, String star_2, String star_3, String star_4, String star_5) {
        this.userID = userID;
        this.star_1 = star_1;
        this.star_2 = star_2;
        this.star_3 = star_3;
        this.star_4 = star_4;
        this.star_5 = star_5;
    }

    // Getters
    public int getUserID() {
        return userID;
    }

    public String getStar1() {
        return star_1;
    }

    public String getStar2() {
        return star_2;
    }

    public String getStar3() {
        return star_3;
    }

    public String getStar4() {
        return star_4;
    }

    public String getStar5() {
        return star_5;
    }

    // Setters
    public void setStar1(String star_1) {
        this.star_1 = star_1;
    }

    public void setStar2(String star_2) {
        this.star_2 = star_2;
    }

    public void setStar3(String star_3) {
        this.star_3 = star_3;
    }

    public void setStar4(String star_4) {
        this.star_4 = star_4;
    }

    public void setStar5(String star_5) {
        this.star_5 = star_5;
    }

    public String getMeaningForStar(int star) {
        switch (star) {
            case 1: return star_1;
            case 2: return star_2;
            case 3: return star_3;
            case 4: return star_4;
            case 5: return star_5;
            default: return "";
        }
    }
}
