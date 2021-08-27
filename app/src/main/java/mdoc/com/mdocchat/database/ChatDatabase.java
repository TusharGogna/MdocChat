package mdoc.com.mdocchat.database;


import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Created by tgogna on 2/2/2018.
 */

@Database(entities = {ChatData.class}, version = 1)
public abstract class ChatDatabase extends RoomDatabase {
    public abstract ChatDao chatDao();
}
