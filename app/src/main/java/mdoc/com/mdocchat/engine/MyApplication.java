package mdoc.com.mdocchat.engine;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by tgogna on 2/1/2018.
 */

public class MyApplication extends Application {
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://54.210.217.166:30004");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
