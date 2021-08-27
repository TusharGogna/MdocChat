package mdoc.com.mdocchat;

import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.socket.client.Socket;
import mdoc.com.mdocchat.adapters.MessageAdapter;
import mdoc.com.mdocchat.database.ChatData;
import mdoc.com.mdocchat.database.ChatDatabase;
import mdoc.com.mdocchat.databinding.ActivityMainBinding;
import mdoc.com.mdocchat.models.MainModel;
import mdoc.com.mdocchat.pojo.Message;
import mdoc.com.mdocchat.presenter.MainPresenter;

public class MainActivity extends AppCompatActivity implements MainPresenter {
    private static final int TYPING_TIMER_LENGTH = 600;
    private static final String TAG = "MAIN_ACTIVITY";

    private ActivityMainBinding mBinding;
    private List<Message> mMessages = new ArrayList<Message>();
    private MessageAdapter mAdapter;
    private boolean mTyping = false;
    private final Handler mTypingHandler = new Handler();
    private String mUsername;
    private Socket mSocket;
    private Boolean isConnected = true;
    private ChatDatabase mDatabase;
    private ChatData mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mAdapter = new MessageAdapter(this, mMessages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView btnExit = (ImageView) toolbar.findViewById(R.id.imgExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leave();
            }
        });

        MainModel mModel = new MainModel(this, this);
        setUpDatabase();
        setUpUI();
        mModel.connectSocket();
    }

    private void setUpDatabase() {
        mDatabase = Room.databaseBuilder(this,
                ChatDatabase.class, "chatData").allowMainThreadQueries().build();
        mData = mDatabase.chatDao().getChatData();

        if (mData != null) {
            mMessages = new ArrayList<>(Arrays.asList(mData.getMessages()));
            mAdapter = new MessageAdapter(this, mMessages);
        } else {
            mData = new ChatData();
        }
    }

    private final Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;
            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    private void addLog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessages.add(new Message.Builder(Message.TYPE_LOG)
                        .message(message).build());
                mAdapter.notifyItemInserted(mMessages.size() - 1);
            }
        });
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        //   addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
    }

    private void addMessage(final String username, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                        .username(username).message(message).build());
                mAdapter.notifyItemInserted(mMessages.size() - 1);
            }
        });
        scrollToBottom();
    }
    private String userNameTyping="";
    private void addTyping(String username) {
        if(username.equals(userNameTyping)){
            removeTyping(username);
            userNameTyping="";
            return;
        }
        userNameTyping = username;
        mMessages.add(new Message.Builder(Message.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {
        if (mUsername == null) return;
        if (!mSocket.connected()) return;

        mTyping = false;
        String message = mBinding.messageInput.getText().toString().trim();
        JSONObject mObj = new JSONObject();
        try {
            mObj.put("id", 0); //TODO  make ID dynamic
            mObj.put("message", message);

            if (TextUtils.isEmpty(message)) {
                mBinding.messageInput.requestFocus();
                return;
            }

            mBinding.messageInput.setText("");
            addMessage(mUsername, message);

            // perform the sending message attempt.
            mSocket.emit("new message", mObj);
        } catch (JSONException ignore) {
            Snackbar.make(mBinding.sendButton, getResources().getString(R.string.unexpected_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mData.setMessages(mMessages.toArray(new Message[mMessages.size()]));
        if (mDatabase.chatDao().getChatData() == null)
            mDatabase.chatDao().insertChatData(mData);
        else
            mDatabase.chatDao().updateChatData(mData);
    }

    private void leave() {
        mUsername = null;
        mSocket.disconnect();
        finish();
    }

    private void scrollToBottom() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBinding.messages.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
    }


    private void setUpUI() {
        mBinding.messages.setLayoutManager(new LinearLayoutManager(this));
        mBinding.messages.setAdapter(mAdapter);
        mBinding.messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mBinding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    @Override
    public void getSocket(Socket socket) {
        mSocket = socket;
    }

    @Override
    public void onSocketConnect(Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isConnected) {
                    if (null != mUsername)
                        mSocket.emit("add user", mUsername);
                    Snackbar.make(mBinding.sendButton, getResources().getString(R.string.connect), Snackbar.LENGTH_LONG).show();
                    isConnected = true;
                }
            }
        });
    }

    @Override
    public void onSocketDisconnect(Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "diconnected");
                isConnected = false;
                Snackbar.make(mBinding.sendButton, getResources().getString(R.string.disconnect), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSocketConnectError(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Error connecting" + args[0].toString());
                Snackbar.make(mBinding.sendButton, getResources().getString(R.string.error_connect), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onSocketNewMessage(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                removeTyping(username);
                addMessage(username, message);
            }
        });
    }

    @Override
    public void onSocketUserJoined(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String username;
                int numUsers;
                try {
                    username = data.getString("username");
                    numUsers = data.getInt("numUsers");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }

                addLog(getResources().getString(R.string.message_user_joined, username));
                addParticipantsLog(numUsers);
            }
        });
    }

    @Override
    public void onSocketUserLeft(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String username;
                int numUsers;
                try {
                    username = data.getString("username");
                    numUsers = data.getInt("numUsers");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }

                addLog(getResources().getString(R.string.message_user_left, username));
                addParticipantsLog(numUsers);
                removeTyping(username);
            }
        });
    }

    @Override
    public void onSocketTyping(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String username;
                try {
                    username = data.getString("username");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                addTyping(username);
            }
        });
    }

    @Override
    public void onSocketStopTyping(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String username;
                try {
                    username = data.getString("username");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                removeTyping(username);
            }
        });
    }

    @Override
    public void onSocketLogin(Object... args) {
        JSONObject data = (JSONObject) args[0];

        int numUsers;
        try {
            numUsers = data.getInt("numUsers");
        } catch (JSONException e) {
            return;
        }
        addParticipantsLog(numUsers);
    }

    @Override
    public void getUserName(String userName) {
        mUsername = userName;
    }
}



