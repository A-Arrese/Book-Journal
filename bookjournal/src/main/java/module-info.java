module paagbat {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;

    opens paagbat.controller to javafx.fxml;
    opens paagbat to javafx.fxml;
    opens paagbat.model.base to javafx.base;

    exports paagbat;
    exports paagbat.model;
    exports paagbat.model.base;
    exports paagbat.model.enums;

}
