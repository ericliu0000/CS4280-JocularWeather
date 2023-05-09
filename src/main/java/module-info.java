module org.nothing.jocularweather {
    requires javafx.controls;
    requires com.fasterxml.jackson.databind;

    opens org.nothing.jocularweather to com.fasterxml.jackson.databind;
    exports org.nothing.jocularweather;
}