package Server.networking.http;

import java.io.IOException;

//Functional interface for method ref
public interface Handler {
    void handle(RequestWrapper req, ResponseWrapper res) throws IOException;
}