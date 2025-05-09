package cn.haier.bio.medical.biot.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    //***数据库名称
    private static  final String DATABASE_NAME = "mqtt.db";
    //数据库版本号
    private static final int DATABASE_VERSION=5;
    //创建表,数据表
    public static final  String TABLE_MQTT_DATA ="mqtt_data";


    private static final String CREATE_MQTT_DATA_SQL ="CREATE TABLE "
            + TABLE_MQTT_DATA
            + " (_id Integer primary key autoincrement,"
            + " time text,"
            + " content text,"
            + " value1 text,"
            + " value2 text,"
            + " value3 text);";

    public DBHelper (Context context)
    {
        this(context,DATABASE_NAME,null,DATABASE_VERSION);

    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MQTT_DATA_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_MQTT_DATA);
            onCreate(db);
        }
    }
}

