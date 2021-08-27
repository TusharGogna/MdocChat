package mdoc.com.mdocchat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mdoc.com.mdocchat.R;
import mdoc.com.mdocchat.pojo.Message;

/**
 * Created by tgogna on 2/1/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private int[] mUsernameColors;

    public MessageAdapter(Context context, List<Message> messages) {
        mMessages = messages;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Message.TYPE_MESSAGE:
                layout = R.layout.item_message;
                break;
            case Message.TYPE_LOG:
                layout = R.layout.item_log;
                break;
            case Message.TYPE_ACTION:
                layout = R.layout.item_action;
                break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Message message = mMessages.get(position);
        if (message != null && message.getUsername() != null) {
            if (message.getUsername().equalsIgnoreCase("Mdoc")) {
                viewHolder.setMessage(message.getMessage(), 0);
                viewHolder.setUsername(message.getUsername(), 0);
            } else {
                viewHolder.setMessage(message.getMessage(), 1);
                viewHolder.setUsername(message.getUsername(), 1);
            }
        } else {
            viewHolder.setMessage(message.getMessage(), 2);
            viewHolder.setUsername(message.getUsername(), 2);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsernameView;
        private TextView mMessageView;


        private ViewHolder(View itemView) {
            super(itemView);
            mUsernameView = (TextView) itemView.findViewById(R.id.username);
            mMessageView = (TextView) itemView.findViewById(R.id.message);
        }

        private void setUsername(String username, int id) {
            if (null == mUsernameView) return;

            if (id == 0) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                params.gravity = Gravity.END;
                mUsernameView.setLayoutParams(params);
            } else if (id == 1) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                params.gravity = Gravity.START;
                mUsernameView.setLayoutParams(params);
            }

            mUsernameView.setText(username);
            mUsernameView.setTextColor(getUsernameColor(username));
        }

        private void setMessage(String message, int id) {
            if (null == mMessageView) return;
            if (id == 0) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                params.gravity = Gravity.END;
                mMessageView.setLayoutParams(params);
            } else if (id == 1) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                params.gravity = Gravity.START;
                mMessageView.setLayoutParams(params);
            }
            setChatBubbleColor(id);
            mMessageView.setText(message);
        }


        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }

        private void setChatBubbleColor(int id) {
            GradientDrawable gd = (GradientDrawable) mMessageView.getBackground().getCurrent();
            if (id == 0) {
                gd.setColor(Color.parseColor("#96C0CE"));
              /*  gd.setCornerRadii(new float[]{30, 30, 30, 30, 0, 0, 30, 30});
                gd.setStroke(2, Color.parseColor("#00FFFF"), 5, 6);*/
            } else if (id == 1) {
                gd.setColor(Color.parseColor("#a8f07a"));
            } else {
                gd.setColor(Color.parseColor("#FFFFFF"));
            }

        }
    }


}
