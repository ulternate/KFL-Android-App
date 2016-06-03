package com.danielcswain.kfl.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.danielcswain.kfl.Articles.ArticleObject;
import com.danielcswain.kfl.Teams.PlayerObject;

import java.util.ArrayList;

/**
 * Created by ulternate on 28/05/2016.
 *
 * SQLiteOpenHelper used to create/upgrade and update our Articles database, which stores
 * our JSON data from the web api
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Public Database, Table and column variables
    public static final int DATABASE_VERSION = 2;
    // Articles strings
    public static final String DATABASE_NAME_ARTICLES = "KFL.db";
    public static final String TABLE_NAME_ARTICLES = "articles";
    public static final String COLUMN_NAME_ARTICLE_ID = "article_id";
    public static final String COLUMN_NAME_ARTICLE_TITLE = "title";
    public static final String COLUMN_NAME_ARTICLE_SUMMARY = "summary";
    public static final String COLUMN_NAME_ARTICLE_LONG_TEXT = "long_text";
    public static final String COLUMN_NAME_ARTICLE_AUTHOR = "author";
    public static final String COLUMN_NAME_ARTICLE_CATEGORY = "category";
    public static final String COLUMN_NAME_ARTICLE_THUMBNAIL_URL = "thumnail_url";
    public static final String COLUMN_NAME_ARTICLE_IMAGE_URL = "image_url";
    public static final String COLUMN_NAME_ARTICLE_PUB_DATE = "pub_date";
    // Teams strings
    public static final String TABLE_NAME_TEAM = "team";
    public static final String COLUMN_NAME_ID = "id";
    public static final String TABLE_NAME_SELECTED_TEAM = "selected_team";
    public static final String TABLE_NAME_RESERVE_TEAM = "reserve_team";
    public static final String COLUMN_NAME_PLAYER_NAME = "player_name";
    public static final String COLUMN_NAME_PLAYER_NUM = "player_num";
    public static final String COLUMN_NAME_PLAYER_AFL_TEAM = "player_afl_team";
    public static final String COLUMN_NAME_PLAYER_POSITION = "player_position";
    public static final String COLUMN_NAME_PLAYER_POSITION_NUM = "player_position_num";

    // Private SQL strings (general strings)
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String SEPARATOR = ", ";
    // Articles sql string
    private static final String SQL_CREATE_TABLE_ARTICLES =
            "CREATE TABLE " + TABLE_NAME_ARTICLES + " (" +
            COLUMN_NAME_ARTICLE_ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_ARTICLE_TITLE + TEXT_TYPE + SEPARATOR +
            COLUMN_NAME_ARTICLE_SUMMARY + TEXT_TYPE + SEPARATOR +
            COLUMN_NAME_ARTICLE_LONG_TEXT + TEXT_TYPE + SEPARATOR +
            COLUMN_NAME_ARTICLE_AUTHOR + TEXT_TYPE + SEPARATOR +
            COLUMN_NAME_ARTICLE_CATEGORY + TEXT_TYPE + SEPARATOR +
            COLUMN_NAME_ARTICLE_THUMBNAIL_URL + TEXT_TYPE + SEPARATOR +
            COLUMN_NAME_ARTICLE_IMAGE_URL + TEXT_TYPE + SEPARATOR +
            COLUMN_NAME_ARTICLE_PUB_DATE + TEXT_TYPE +
            // Any other options for the CREATE command
            " )";
    private static final String SQL_DELETE_ARTICLES =
            "DROP TABLE IF EXISTS " + TABLE_NAME_ARTICLES;
    // Teams sql strings
    private static final String SQL_CREATE_TABLE_TEAM =
            "CREATE TABLE " + TABLE_NAME_TEAM + " (" +
            COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_NAME_PLAYER_NAME + TEXT_TYPE + SEPARATOR +
            COLUMN_NAME_PLAYER_AFL_TEAM + TEXT_TYPE +
            // Any other options for the CREATE command
            " )";
    private static final String SQL_CREATE_TABLE_SELECTED_TEAM =
            "CREATE TABLE " + TABLE_NAME_SELECTED_TEAM + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_NAME_PLAYER_NAME + TEXT_TYPE + SEPARATOR +
                    COLUMN_NAME_PLAYER_AFL_TEAM + TEXT_TYPE + SEPARATOR +
                    COLUMN_NAME_PLAYER_NUM + INT_TYPE + SEPARATOR +
                    COLUMN_NAME_PLAYER_POSITION + TEXT_TYPE + SEPARATOR +
                    COLUMN_NAME_PLAYER_POSITION_NUM + INT_TYPE +
                    // Any other options for the CREATE command
                    " )";
    private static final String SQL_CREATE_TABLE_RESERVE_TEAM =
            "CREATE TABLE " + TABLE_NAME_RESERVE_TEAM + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_NAME_PLAYER_NAME + TEXT_TYPE + SEPARATOR +
                    COLUMN_NAME_PLAYER_AFL_TEAM + TEXT_TYPE + SEPARATOR +
                    COLUMN_NAME_PLAYER_NUM + INT_TYPE + SEPARATOR +
                    COLUMN_NAME_PLAYER_POSITION + TEXT_TYPE + SEPARATOR +
                    COLUMN_NAME_PLAYER_POSITION_NUM + INT_TYPE +
                    // Any other options for the CREATE command
                    " )";
    private static final String SQL_DELETE_TEAM =
            "DROP TABLE IF EXISTS " + TABLE_NAME_TEAM;
    private static final String SQL_DELETE_SELECTED_TEAM =
            "DROP TABLE IF EXISTS " + TABLE_NAME_SELECTED_TEAM;
    private static final String SQL_DELETE_RESERVE_TEAM =
            "DROP TABLE IF EXISTS " + TABLE_NAME_RESERVE_TEAM;

    // Public database helper for our application, to access the database
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME_ARTICLES, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create our database table for our articles, team, selected_team and reserve_team
        db.execSQL(SQL_CREATE_TABLE_ARTICLES);
        db.execSQL(SQL_CREATE_TABLE_TEAM);
        db.execSQL(SQL_CREATE_TABLE_SELECTED_TEAM);
        db.execSQL(SQL_CREATE_TABLE_RESERVE_TEAM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Our database is just a cache for online data, so if we update the database version
        // just drop the table and create a new one
        db.execSQL(SQL_DELETE_ARTICLES);
        db.execSQL(SQL_DELETE_TEAM);
        db.execSQL(SQL_DELETE_SELECTED_TEAM);
        db.execSQL(SQL_DELETE_RESERVE_TEAM);
        onCreate(db);
    }

    public ArrayList<ArticleObject> getArticles(){
        ArrayList<ArticleObject> articleObjects = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME_ARTICLES;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null){
            if (cursor.moveToFirst()){
                do{
                    // Get the strings/values from the database
                    String title = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_TITLE));
                    String summary = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_SUMMARY));
                    String longText = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_LONG_TEXT));
                    String author = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_AUTHOR));
                    String category = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_CATEGORY));
                    String thumnailUrl = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_THUMBNAIL_URL));
                    String imageUrl = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_IMAGE_URL));
                    String pub_date = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_PUB_DATE));
                    // Add an article object to our Array list
                    articleObjects.add(new ArticleObject(thumnailUrl, imageUrl, summary, longText, category, author, title, pub_date));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } else {
            db.close();
        }

        return articleObjects;
    }


    public void addArticle(ArticleObject articleObject){
        SQLiteDatabase db = this.getWritableDatabase();

        if (!doesArticleExist(articleObject)){
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_ARTICLE_AUTHOR, articleObject.author);
            values.put(COLUMN_NAME_ARTICLE_CATEGORY, articleObject.category);
            values.put(COLUMN_NAME_ARTICLE_IMAGE_URL, articleObject.imageURL);
            values.put(COLUMN_NAME_ARTICLE_LONG_TEXT, articleObject.longText);
            values.put(COLUMN_NAME_ARTICLE_PUB_DATE, articleObject.postDate);
            values.put(COLUMN_NAME_ARTICLE_SUMMARY, articleObject.summary);
            values.put(COLUMN_NAME_ARTICLE_THUMBNAIL_URL, articleObject.thumbnailURL);
            values.put(COLUMN_NAME_ARTICLE_TITLE, articleObject.title);

            db.insert(TABLE_NAME_ARTICLES, null, values);

            db.close();
        } else {
            db.close();
        }
    }

    public boolean doesArticleExist(ArticleObject articleObject){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_NAME_ARTICLE_AUTHOR,
                COLUMN_NAME_ARTICLE_CATEGORY,
                COLUMN_NAME_ARTICLE_PUB_DATE,
                COLUMN_NAME_ARTICLE_TITLE
        };

        String selection = COLUMN_NAME_ARTICLE_AUTHOR + "=? AND " +
                COLUMN_NAME_ARTICLE_CATEGORY + "=? AND " +
                COLUMN_NAME_ARTICLE_PUB_DATE + "=? AND " +
                COLUMN_NAME_ARTICLE_TITLE + "=?";

        String[] arguments = {
                articleObject.author,
                articleObject.category,
                articleObject.postDate,
                articleObject.title
        };

        Cursor cursor = db.query(TABLE_NAME_ARTICLES, columns, selection, arguments, null, null, null);

        if (cursor.getCount() <= 0){
            cursor.close();
            return false;
        }else {
            cursor.close();
            return true;
        }
    }

    public ArrayList<PlayerObject> getPlayers(){
        ArrayList<PlayerObject> playerObjects = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME_TEAM;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null){
            if (cursor.moveToFirst()){
                do{
                    // Get the strings/values from the database
                    String playerName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYER_NAME));
                    String aflTeam = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYER_AFL_TEAM));
                    // Add a playerObject to our ArrayList
                    playerObjects.add(new PlayerObject(playerName, aflTeam));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        } else {
            db.close();
        }

        return playerObjects;
    }

    public void addPlayer(PlayerObject playerObject){
        SQLiteDatabase db = this.getWritableDatabase();

        if(!doesPlayerExist(playerObject)){
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_PLAYER_NAME, playerObject.getName());
            values.put(COLUMN_NAME_PLAYER_AFL_TEAM, playerObject.getTeam());

            db.insert(TABLE_NAME_TEAM, null, values);

            db.close();
        } else {
            db.close();
        }
    }

    public boolean doesPlayerExist(PlayerObject playerObject){
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_NAME_PLAYER_NAME,
                COLUMN_NAME_PLAYER_AFL_TEAM
        };

        String selection = COLUMN_NAME_PLAYER_NAME + "=? AND " +
                COLUMN_NAME_PLAYER_AFL_TEAM + "=?";

        String[] arguments = {
                playerObject.getName(),
                playerObject.getTeam()
        };

        Cursor cursor = db.query(TABLE_NAME_TEAM, columns, selection, arguments, null, null, null);

        if (cursor.getCount() <= 0){
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }

    /**
     * Delete all records for the provided table
     * @param tableName the name of the table to delete records from
     */
    public void deleteAllObjects(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM " + tableName);
        db.execSQL("VACUUM");

        db.close();
    }
}
