module com.example.vmimporttool {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    opens com.example.vmimporttool to javafx.fxml;
    exports com.example.vmimporttool;
}