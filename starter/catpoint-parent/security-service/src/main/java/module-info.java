module security.service {
    requires java.desktop;
    requires java.prefs;

    requires image.service;

    requires com.google.gson;
    requires com.google.common;
    requires com.miglayout.swing;

    exports com.udacity.catpoint.application;
    exports com.udacity.catpoint.data;
    exports com.udacity.catpoint.service;

    opens com.udacity.catpoint.data to com.google.gson;
}