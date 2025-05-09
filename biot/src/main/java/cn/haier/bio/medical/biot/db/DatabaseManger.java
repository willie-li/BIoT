package cn.haier.bio.medical.biot.db;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManger<T> {

    private DBHelper dbHelper;
    private static DatabaseManger instance = null;
    private SQLiteDatabase sqLiteDatabase;

    /**
     * @return
     */
    private DatabaseManger() {
    }

    /**
     * 获取本类对象的实例
     *
     * @return
     */
    public static final DatabaseManger getInstance() {
        if (instance == null) {
            instance = new DatabaseManger();
        }
        return instance;
    }

    public void init(Context context) {
        dbHelper = new DBHelper(context);
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    /**
     * 关闭数据库
     */
    public void close() {
        if (sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
            sqLiteDatabase = null;
        }
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }

        if (instance != null) {
            instance = null;
        }
    }

    /**
     * 执行一条sql语句
     */

    public void execSql(String sql) {
        if (sqLiteDatabase.isOpen()) {
            sqLiteDatabase.execSQL(sql);
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
    }

    /**
     * sql执行查询操作的sql语句
     * selection args查询条件
     * 返回查询的游标，可对数据进行操作，但是需要自己关闭游标
     */
    public Cursor queryData2Cursor(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        if (sqLiteDatabase.isOpen()) {
            cursor = sqLiteDatabase.rawQuery(sql, selectionArgs);
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
        return cursor;
    }

    /**
     * 查询表中数据总条数
     * 返回表中数据条数
     */

    public int getDataCounts(String table) {
        Cursor cursor = null;
        int counts = 0;
        if (sqLiteDatabase.isOpen()) {
            cursor = queryData2Cursor("select * from " + table, null);
            if (cursor != null && cursor.moveToFirst()) {
                counts = cursor.getCount();
            }
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
        return counts;
    }

    /**
     * 消除表中所有数据
     *
     * @param table
     */
    public void clearAllData(String table) {
        if (sqLiteDatabase.isOpen()) {
            execSql("delete from " + table);
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
    }

    /**
     * 插入数据
     *
     * @param sql      执行操作的sql语句
     * @param bindArgs sql中的参数，参数的位置对于占位符的顺序
     * @return 返回插入对应的额ID，返回0，则插入无效
     */

    public long insertDataBySql(String sql, String[] bindArgs) {
        long id = 0;
        if (sqLiteDatabase.isOpen()) {
            SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement(sql);
            if (bindArgs != null) {
                int size = bindArgs.length;
                for (int i = 0; i < size; i++) {
                    sqLiteStatement.bindString(i + 1, bindArgs[i]);
                }
                id = sqLiteStatement.executeInsert();
                sqLiteStatement.close();
            }
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
        return id;
    }

    /**
     * 插入数据
     *
     * @param table  表名
     * @param values 数据
     * @return 返回插入的ID，返回0，则插入失败
     */
    public long insetData(String table, ContentValues values) {
        long id = 0;
        if (sqLiteDatabase.isOpen()) {
            id = sqLiteDatabase.insertOrThrow(table, null, values);
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
        return id;
    }

    /**
     * 批量插入数据
     *
     * @param table 表名
     * @param list  数据源
     * @param args  数据键名 key
     * @return
     */
    public long insertBatchData(String table, List<Map<String, Object>> list, String[] args) {
        long insertNum = 0;
        sqLiteDatabase.beginTransaction();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < args.length; j++) {
                contentValues.put(args[j], list.get(i).get(args[j]).toString());
            }
            long id = insetData(table, contentValues);
            if (id > 0) {
                insertNum++;
            }
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        return insertNum;
    }

    /**
     * 更新数据
     *
     * @param table        表名
     * @param values       需要更新的数据
     * @param whereClaause 表示sql语句中条件部分的语句
     * @param whereArgs    表示占位符的值
     * @return
     */
    public int updateData(String table, ContentValues values, String whereClaause, String[] whereArgs) {
        int rowsNum = 0;
        if (sqLiteDatabase.isOpen()) {
            rowsNum = sqLiteDatabase.update(table, values, whereClaause, whereArgs);
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
        return rowsNum;
    }

    /**
     * 删除数据
     *
     * @param sql      待执行的sql语句
     * @param bindArgs sql语句中的参数，参数的顺序对应占位符的顺序
     */
    public void deleteDataBySql(String sql, String[] bindArgs) {
        if (sqLiteDatabase.isOpen()) {
            SQLiteStatement statement = sqLiteDatabase.compileStatement(sql);
            if (bindArgs != null) {
                int size = bindArgs.length;
                for (int i = 0; i < size; i++) {
                    statement.bindString(i + 1, bindArgs[i]);
                }
                statement.execute();
                statement.close();
            }
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
    }

    /**
     * 删除数据
     *
     * @param table       表名
     * @param whereClause sql中的条件语句部分
     * @param whereArgs   占位符的值
     * @return
     */
    public long deleteData(String table, String whereClause, String[] whereArgs) {
        long rowsNum = 0;
        if (sqLiteDatabase.isOpen()) {
            rowsNum = sqLiteDatabase.delete(table, whereClause, whereArgs);
        } else {
            throw new RuntimeException("The DataBase has already closed");
        }
        return rowsNum;
    }

    /**
     * @param table         表名
     * @param columns       查询需要返回的列的字段
     * @param selection     SQL语句中的条件语句
     * @param selectionArgs 占位符的值
     * @param groupBy       表示分组，可以为NULL
     * @param having        SQL语句中的having，可以为null
     * @param orderBy       表示结果排序，可以为null
     * @return
     * @throws Exception
     */
    public Cursor queryData(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) throws Exception {
        return queryData(table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }


    /**
     * @param table         表名
     * @param columns       查询需要返回的列的字段
     * @param selection     SQL语句中的条件语句
     * @param selectionArgs 占位符的值
     * @param groupBy       表示分组，可以为NULL
     * @param having        SQL语句中的having，可以为null
     * @param orderBy       表示结果排序，可以为null
     * @param limit         表示分页
     * @return
     * @throws Exception
     */
    public Cursor queryData(String table, String[] columns, String selection, String[] selectionArgs,
                            String groupBy, String having, String orderBy, String limit) throws Exception {
        return queryData(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * @param distinct      true if you want each row to be unique,false otherwise
     * @param table         表名
     * @param columns       查询需要返回的列的字段
     * @param selection     SQL语句中的条件语句
     * @param selectionArgs 占位符的值
     * @param groupBy       表示分组，可以为NULL
     * @param having        SQL语句中的having，可以为null
     * @param orderBy       表示结果排序，可以为null
     * @param limit         表示分页
     * @return
     * @throws Exception
     */
    public Cursor queryData(boolean distinct, String table, String[] columns, String selection,
                            String[] selectionArgs, String groupBy,
                            String having, String orderBy, String limit) throws Exception {
        return queryData(null, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }


    /**
     * @param cursorFactory 游标工厂
     * @param distinct      true if you want each row to be unique,false otherwise
     * @param table         表名
     * @param columns       查询需要返回的列的字段
     * @param selection     SQL语句中的条件语句
     * @param selectionArgs 占位符的值
     * @param groupBy       表示分组，可以为NULL
     * @param having        SQL语句中的having，可以为null
     * @param orderBy       表示结果排序，可以为null
     * @param limit         表示分页
     * @return
     * @throws Exception
     */
    public Cursor queryData(SQLiteDatabase.CursorFactory cursorFactory, boolean distinct, String table, String[] columns, String selection,
                            String[] selectionArgs, String groupBy,
                            String having, String orderBy, String limit) throws Exception {
        Cursor cursor = null;
        if (sqLiteDatabase.isOpen()) {
            cursor = sqLiteDatabase.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        } else {
            throw new RuntimeException("The database has already closed!");
        }
        return cursor;
    }

    /**
     * @param sql           执行查询造作的SQL语句
     * @param selectionArgs 查询条件
     * @param object        JAVABEAN对象
     * @return 查询结果
     */
    public List<Map<String, String>> query2List(String sql, String[] selectionArgs, Object object) throws Exception {
        List<Map<String, String>> list = new ArrayList<>();
        if (sqLiteDatabase.isOpen()) {
            Cursor cursor = null;
            cursor = queryData2Cursor(sql, selectionArgs);
            Field[] fields;
            HashMap<String, String> map;
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    map = new HashMap<>();
                    fields = object.getClass().getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        /**
                         * 1通过key，即列名，得到所在的列索引
                         * 2通过所在行以及所在列的索引，得到唯一确定的队友值
                         * 3将值与键封装到MAP集合中，此条数据读取完毕
                         */
                        map.put(fields[i].getName(), cursor.getString(cursor.getColumnIndex(fields[i].getName())));
                    }
                    list.add(map);
                }
                cursor.close();
            }
        } else {
            throw new RuntimeException("The database has already closed!");
        }
        return list;
    }

    public <T>  Object cursor2Model(Cursor cursor,Class<T> classz){
        Object object = null;
        Constructor<T> csr;
        try {
            csr = classz.getConstructor();
            try {
                object = csr.newInstance();
                Field[] fields = object.getClass().getFields();
                for (Field field : fields) {
                    Type type = field.getType();
                    String fieldName = field.getName();
                    field.setAccessible(true);
                    if (Integer.class == type || (type == Integer.TYPE)) {
                        field.set(object,
                                cursor.getInt(cursor.getColumnIndex(fieldName)));
                    } else if (type == String.class) {
                        field.set(object,
                                cursor.getString(cursor.getColumnIndex(fieldName)));
                    }
                }
            } catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        }

        return object;
    }


    public long insertData(String content){
        ContentValues cv = new ContentValues();
        cv.put("time",System.currentTimeMillis());
        cv.put("content",content);
        return insetData(DBHelper.TABLE_MQTT_DATA,cv);
    }

    public List<MqttModel> queryAll() {
        List<MqttModel> list = new ArrayList<>();
        String sql = "select * from "+DBHelper.TABLE_MQTT_DATA;
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        if (cursor!=null&&cursor.moveToFirst())
            do {
                MqttModel model = (MqttModel) cursor2Model(cursor,MqttModel.class);
                list.add(model);
            }while (cursor.moveToNext());

        return list;
    }


}
