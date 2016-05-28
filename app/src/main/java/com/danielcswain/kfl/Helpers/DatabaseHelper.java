package com.danielcswain.kfl.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.danielcswain.kfl.Articles.ArticleObject;

import java.util.ArrayList;

/**
 * Created by ulternate on 28/05/2016.
 *
 * SQLiteOpenHelper used to create/upgrade and update our Articles database, which stores
 * our JSON data from the web api
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Public Database, Table and column variables
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Articles.db";
    public static final String TABLE_NAME = "articles";
    public static final String COLUMN_NAME_ARTICLE_ID = "article_id";
    public static final String COLUMN_NAME_ARTICLE_TITLE = "title";
    public static final String COLUMN_NAME_ARTICLE_SUMMARY = "summary";
    public static final String COLUMN_NAME_ARTICLE_LONG_TEXT = "long_text";
    public static final String COLUMN_NAME_ARTICLE_AUTHOR = "author";
    public static final String COLUMN_NAME_ARTICLE_CATEGORY = "category";
    public static final String COLUMN_NAME_ARTICLE_THUMBNAIL_URL = "thumnail_url";
    public static final String COLUMN_NAME_ARTICLE_IMAGE_URL = "image_url";
    public static final String COLUMN_NAME_ARTICLE_PUB_DATE = "pub_date";


    // Private SQL strings
    private static final String TEXT_TYPE = " TEXT";
    private static final String SEPARATOR = ",";
    private static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
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
    private static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    // Public database helper for our application, to access the database
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create our database table for our articles
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Our database is just a cache for online data, so if we update the database version
        // just drop the table and create a new one
        db.execSQL(SQL_DELETE);
        onCreate(db);
    }

    public ArrayList<ArticleObject> getArticles(){
        ArrayList<ArticleObject> articleObjects = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME;

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

            db.insert(TABLE_NAME, null, values);

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

        Cursor cursor = db.query(TABLE_NAME, columns, selection, arguments, null, null, null);

        if (cursor.getCount() <= 0){
            cursor.close();
            return false;
        }else {
            cursor.close();
            return true;
        }
    }
}
