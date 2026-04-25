/**
 * This interface lets a Swing frame receive server messages.
 */
public interface ServerMessageHandler {

    void handleServerMessage(String message);

}
