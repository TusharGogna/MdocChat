package mdoc.com.mdocchat.presenter;

import io.socket.client.Socket;

/**
 * Created by tgogna on 2/1/2018.
 */

public interface MainPresenter {

    void getSocket(Socket socket);

    void onSocketConnect(Object... args);

    void onSocketDisconnect(Object... args);

    void onSocketConnectError(Object... args);

    void onSocketNewMessage(Object... args);

    void onSocketUserJoined(Object... args);

    void onSocketUserLeft(Object... args);

    void onSocketTyping(Object... args);

    void onSocketStopTyping(Object... args);

    void onSocketLogin(Object... args);

    void getUserName(String userName);
}
