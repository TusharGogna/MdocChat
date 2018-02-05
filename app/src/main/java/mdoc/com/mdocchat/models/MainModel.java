package mdoc.com.mdocchat.models;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import mdoc.com.mdocchat.R;
import mdoc.com.mdocchat.engine.MyApplication;
import mdoc.com.mdocchat.pojo.Message;
import mdoc.com.mdocchat.presenter.MainPresenter;

/**
 * Created by tgogna on 2/1/2018.
 */

public class MainModel {

    private Activity mActivity;
    private MainPresenter mPresenter;
    private static final String TAG = "MAIN_ACTIVITY";
    private String mUsername;
    private Socket mSocket;

    private Boolean isConnected = true;


    public MainModel(Activity activity, MainPresenter mainPresenter) {
        mActivity = activity;
        mPresenter = mainPresenter;
    }

    public void connectSocket() {
        MyApplication app = (MyApplication) mActivity.getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.connect();
        mPresenter.getSocket(mSocket);
        startSignIn();
    }


    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mPresenter.onSocketConnect(args);
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mPresenter.onSocketDisconnect(args);
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mPresenter.onSocketConnectError(args);
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mPresenter.onSocketNewMessage(args);
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mPresenter.onSocketUserJoined(args);
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mPresenter.onSocketUserLeft(args);
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mPresenter.onSocketTyping(args);
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            mPresenter.onSocketStopTyping(args);
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mPresenter.onSocketLogin(args);
        }
    };

    private void startSignIn() {
        attemptLogin();
        mSocket.on("login", onLogin);
    }

    private void attemptLogin() {
        String username = "Mdoc"; //TODO make USERNAME dynamic
        mUsername = username;
        mPresenter.getUserName(mUsername);
        // perform the user login attempt.
        mSocket.emit("add user", username);
    }
}
