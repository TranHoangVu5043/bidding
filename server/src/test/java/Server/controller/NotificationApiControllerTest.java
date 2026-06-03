package Server.controller;

import Server.model.Notification;
import Server.model.users.Bidder;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.NotificationService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationApiControllerTest {

    private NotificationApiController notificationApiController;

    @Mock private NotificationService notifService;
    @Mock private RequestWrapper requestWrapper;
    @Mock private ResponseWrapper responseWrapper;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        notificationApiController = new NotificationApiController(notifService);
    }
    @Test
    void getNotifications_Unauthorized_Returns401() {
        when(requestWrapper.getUser()).thenReturn(null);
        notificationApiController.getNotifications(requestWrapper, responseWrapper);

        verify(responseWrapper).error(401, "Unauthorized");
        verifyNoInteractions(notifService);
    }
    @Test
    void getNotifications_Success_Returns200WithList() {
        User mockUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(mockUser);
        Notification mockNotif = new Notification();

        when(notifService.getForUser(mockUser.getId())).thenReturn(List.of(mockNotif));
        notificationApiController.getNotifications(requestWrapper, responseWrapper);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(responseWrapper).sendJson(eq(200), jsonCaptor.capture());

        JsonObject responseJson = gson.fromJson(jsonCaptor.getValue(), JsonObject.class);
        assertEquals(200, responseJson.get("status").getAsInt());
        assertEquals("OK", responseJson.get("message").getAsString());
        assertTrue(responseJson.get("data").isJsonArray());
    }

    @Test
    void getNotifications_Exception_Returns500() {
        User mockUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(mockUser);

        when(notifService.getForUser(mockUser.getId())).thenThrow(new RuntimeException("Connection timed out"));
        notificationApiController.getNotifications(requestWrapper, responseWrapper);
        verify(responseWrapper).error(500, "Server error: Connection timed out");
    }
    @Test
    void markAllRead_Unauthorized_Returns401() {
        when(requestWrapper.getUser()).thenReturn(null);
        notificationApiController.markAllRead(requestWrapper, responseWrapper);
        verify(responseWrapper).error(401, "Unauthorized");
    }

    @Test
    void markAllRead_Success_Returns200() {
        User mockUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(mockUser);
        notificationApiController.markAllRead(requestWrapper, responseWrapper);
        verify(notifService).markAllRead(mockUser.getId());
        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    @Test
    void markAllRead_Exception_Returns500() {
        User mockUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(mockUser);
        doThrow(new RuntimeException("Database error")).when(notifService).markAllRead(mockUser.getId());
        notificationApiController.markAllRead(requestWrapper, responseWrapper);
        verify(responseWrapper).error(500, "Server error: Database error");
    }
}