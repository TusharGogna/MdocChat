package mdoc.com.mdocchat.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Created by tgogna on 2/2/2018.
 */

@Dao
public interface ChatDao {
    @Query("SELECT * FROM chatData")
    ChatData getChatData();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChatData(ChatData ChatData);

    @Update
    void updateChatData(ChatData ChatData);

    @Delete
    void deleteChatData(ChatData ChatData);

    @Query("delete from chatData")
    void deleteAllChattData();
}
