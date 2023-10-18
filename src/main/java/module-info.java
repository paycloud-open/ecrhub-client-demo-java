module com.example.ecrhub {
    requires javafx.controls;
    requires javafx.fxml;
    requires cn.hutool;
    requires com.alibaba.fastjson2;
    requires com.google.gson;
    requires com.google.common;
    requires com.google.errorprone.annotations;
    requires com.fazecast.jSerialComm;
    requires org.java_websocket;
    requires org.slf4j;
    requires ecrhub.client.sdk.java;
    requires javax.jmdns;


    opens com.example.ecrhub to javafx.fxml;
    exports com.example.ecrhub;
    exports com.example.ecrhub.manager;
    opens com.example.ecrhub.manager to javafx.fxml;
    exports com.example.ecrhub.controller;
    opens com.example.ecrhub.controller to javafx.fxml;
}