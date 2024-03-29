package mdoc.com.mdocchat.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;

import mdoc.com.mdocchat.pojo.Message;

/**
 * Created by tgogna on 2/2/2018.
 */
@Entity(tableName = "chatData")
@TypeConverters({ChatData.Converter.class})
public class ChatData {

    @PrimaryKey
    int id;

    @ColumnInfo(name = "messages")
    private Message[] messages;

    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(Message[] messages) {
        this.messages = messages;
    }

    public static class Converter {
        @TypeConverter
        public Message[] getArray(String value) {
            Gson gson = new Gson();
            Message[] obj = gson.fromJson(value, Message[].class);
            return value == null ? null : obj;
        }

        @TypeConverter
        public String getString(Message[] itienaries) {
            if (itienaries == null) {
                return null;
            } else {
                Gson gson = new Gson();
                return gson.toJson(itienaries);
            }
        }
    }
}
