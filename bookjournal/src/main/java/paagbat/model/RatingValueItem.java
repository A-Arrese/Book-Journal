package paagbat.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Liburu edo eduki baten balorazioaren izar kopuruak gordetzen dituen klasea.
 * 
 * 1etik 5 izarrera arteko balorazioak kudeatzeko erabilia.
 */
public class RatingValueItem {
    private final IntegerProperty star1;
    private final IntegerProperty star2;
    private final IntegerProperty star3;
    private final IntegerProperty star4;
    private final IntegerProperty star5;

    /**
     * RatingValueItem objektu berri bat sortzen du izar bakoitzeko balioarekin.
     *
     * @param star1 1 izarreko balioa.
     * @param star2 2 izarreko balioa.
     * @param star3 3 izarreko balioa.
     * @param star4 4 izarreko balioa.
     * @param star5 5 izarreko balioa.
     */
    public RatingValueItem(int star1, int star2, int star3, int star4, int star5) {
        this.star1 = new SimpleIntegerProperty(star1);
        this.star2 = new SimpleIntegerProperty(star2);
        this.star3 = new SimpleIntegerProperty(star3);
        this.star4 = new SimpleIntegerProperty(star4);
        this.star5 = new SimpleIntegerProperty(star5);
    }

    /** 1 izarreko balioaren propietatea. */
    public IntegerProperty star1Property() { 
        return star1; 
    }

    /** 2 izarreko balioaren propietatea. */
    public IntegerProperty star2Property() { 
        return star2; 
    }

    /** 3 izarreko balioaren propietatea. */
    public IntegerProperty star3Property() { 
        return star3; 
    }

    /** 4 izarreko balioaren propietatea. */
    public IntegerProperty star4Property() { 
        return star4; 
    }

    /** 5 izarreko balioaren propietatea. */
    public IntegerProperty star5Property() { 
        return star5; 
    }

    /** 1 izarreko balioa eskuratzea. */
    public int getStar1() { 
        return star1.get(); 
    }

    /** 2 izarreko balioa eskuratzea. */
    public int getStar2() { 
        return star2.get(); }

    /** 3 izarreko balioa eskuratzea. */
    public int getStar3() { 
        return star3.get(); 
    }

    /** 4 izarreko balioa eskuratzea. */
    public int getStar4() { 
        return star4.get(); 
    }

    /** 5 izarreko balioa eskuratzea. */
    public int getStar5() { 
        return star5.get(); 
    }
}
