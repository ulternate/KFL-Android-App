package com.danielcswain.kfl.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.danielcswain.kfl.Articles.ArticleObject;
import com.danielcswain.kfl.Teams.PlayerObject;
import com.danielcswain.kfl.Teams.SelectionObject;

import java.util.ArrayList;

/**
 * Created by Daniel Swain ulternate on 28/05/2016.
 *
 * SQLiteOpenHelper used to create/upgrade the tables used to store our WebServices JSON response data
 *
 * Methods:
 *  DatabaseHelper: The public constructor that serves as the access point for all these methods.
 *  onCreate: Create the database tables if the version number has increased.
 *  onUpdate: Drop all the tables as they only serve as a local cache for our WebService data,
 *      so if the DATABASE_VERSION increases it'll mean the data will be fetched again from the WebService.
 *  getArticles(): Get all the articles from the database as an ArrayList<ArticleObject>().
 *  addArticle(ArticleObject obj): Add a single ArticleObject to the database.
 *      This calls doesArticleExist(ArticleObject obj) internally to only ensure a unique Article is added.
 *  doesArticleExist(ArticleObject obj): Check to see if the provided ArticleObject exists already in the db.
 *      Returns boolean (true or false).
 *  getPlayers(): Get all the players from the database as an ArrayList<PlayerObject>().
 *  addPlayer(PlayerObject obj): Add a single PlayerObject to the database.
 *      This calls doesPlayerExist(PlayerObject obj) internally to only ensure a unique Player is added.
 *  doesPlayerExist(PlayerObject obj): Check to see if the provided PlayerObject exists in the db already.
 *      Returns boolean (true or false).
 *  getSelections(): Get all the selectedPlayers from the database as an ArrayList<SelectionObject>().
 *  getSelectionAtPosition(int position): Get the SelectionObject in the provided position (1 - 14).
 *  addSelection(SelectionObject obj): Add a single SelectionObject to the database.
 *      This calls doesSelectionExist(SelectionObject obj) internally to only ensure a unique SelectionObject is added.
 *  doesSelectionExist(SelectionObject obj): Check to see if the provided SelectionObject exists in the db already.
 *      Returns boolean (true or false).
 *  deleteAllObjects(String tableName): Delete all records from the provided table. Used when logging the user out.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Public Database, Table and column variables
    public static final int DATABASE_VERSION = 5;
    // Articles strings
    public static final String DATABASE_NAME = "KFL.db";
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

    /**
     * Public database helper for our application, to access the database
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the database tables
     * @param db the SQLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create our database table for our articles, team, selected_team and reserve_team
        db.execSQL(SQL_CREATE_TABLE_ARTICLES);
        db.execSQL(SQL_CREATE_TABLE_TEAM);
        db.execSQL(SQL_CREATE_TABLE_SELECTED_TEAM);
        db.execSQL(SQL_CREATE_TABLE_RESERVE_TEAM);
    }

    /**
     * When the Database version has increase, or we're updating the database this will be called
     * @param db the SQLiteDatabase
     * @param oldVersion the old Database version (int)
     * @param newVersion the new Database version (int)
     */
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

    /**
     * Get and return an ArrayList of ArticleObjects from the Database
     * @return An ArrayList of ArticleObjects is returned
     */
    public ArrayList<ArticleObject> getArticles(){
        // The ArrayList that will be returned
        ArrayList<ArticleObject> articleObjects = new ArrayList<>();
        // Get a read only instance of the database for the query
        SQLiteDatabase db = this.getReadableDatabase();
        // Build the SQL query
        String query = "SELECT * FROM " + TABLE_NAME_ARTICLES;
        // Get a database cursor containing the results from the query
        Cursor cursor = db.rawQuery(query, null);
        // If the query returned results (cursor) then loop through each database row and add an ArticleObject
        // to the ArrayList
        if (cursor != null){
            if (cursor.moveToFirst()){
                do{
                    // Get the strings/values from the database row
                    String title = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_TITLE));
                    String summary = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_SUMMARY));
                    String longText = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_LONG_TEXT));
                    String author = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_AUTHOR));
                    String category = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_CATEGORY));
                    String thumbnailURL = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_THUMBNAIL_URL));
                    String imageUrl = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_IMAGE_URL));
                    String pub_date = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ARTICLE_PUB_DATE));
                    // Add an article object to the Arraylist
                    articleObjects.add(new ArticleObject(thumbnailURL, imageUrl, summary, longText, category, author, title, pub_date));
                    // Move to the next row
                } while (cursor.moveToNext());
            }
            // Close the cursor and the database to avoid memory leaks
            cursor.close();
            db.close();
        } else {
            // No cursor so no results, but close the database connection to avoid memory leaks
            db.close();
        }
        // Return the ArrayList containing the ArticleObjects (or it's a blank ArrayList)
        return articleObjects;
    }

    /**
     * Add a single ArticleObject to the Database
     * @param articleObject The ArticleObject to be added to the database
     */
    public void addArticle(ArticleObject articleObject){
        // Get a writable instance of the Database
        SQLiteDatabase db = this.getWritableDatabase();
        // Check if the ArticleObject exists, if it doesn't, then add it to the database
        if (!doesArticleExist(articleObject)){
            // Create the ContentValues object which will hold the new database values for the ArticleObject
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_ARTICLE_AUTHOR, articleObject.author);
            values.put(COLUMN_NAME_ARTICLE_CATEGORY, articleObject.category);
            values.put(COLUMN_NAME_ARTICLE_IMAGE_URL, articleObject.imageURL);
            values.put(COLUMN_NAME_ARTICLE_LONG_TEXT, articleObject.longText);
            values.put(COLUMN_NAME_ARTICLE_PUB_DATE, articleObject.postDate);
            values.put(COLUMN_NAME_ARTICLE_SUMMARY, articleObject.summary);
            values.put(COLUMN_NAME_ARTICLE_THUMBNAIL_URL, articleObject.thumbnailURL);
            values.put(COLUMN_NAME_ARTICLE_TITLE, articleObject.title);
            // Insert the ArticleObject into the database
            db.insert(TABLE_NAME_ARTICLES, null, values);
            // Close the connection to the database to avoid memory leaks
            db.close();
        } else {
            // The ArticleObject already exists, lets close the connection to the Database to avoid memory leaks
            db.close();
        }
    }

    /**
     * Check if an ArticleObject already exists in the database
     * @param articleObject the article we are checking to see exists already
     * @return boolean answer to the question doesArticleExist (either true or false)
     */
    public boolean doesArticleExist(ArticleObject articleObject){
        // Get a read only connection to the database
        SQLiteDatabase db = this.getReadableDatabase();
        // The database columns to compare against
        String[] columns = {
                COLUMN_NAME_ARTICLE_AUTHOR,
                COLUMN_NAME_ARTICLE_CATEGORY,
                COLUMN_NAME_ARTICLE_PUB_DATE,
                COLUMN_NAME_ARTICLE_TITLE
        };
        // The selection statement, i.e. what columns we match values against from the ArticleObject
        String selection = COLUMN_NAME_ARTICLE_AUTHOR + "=? AND " +
                COLUMN_NAME_ARTICLE_CATEGORY + "=? AND " +
                COLUMN_NAME_ARTICLE_PUB_DATE + "=? AND " +
                COLUMN_NAME_ARTICLE_TITLE + "=?";
        // The values for the selection statement, i.e. the ArticleObject properties we are using for the search.
        String[] arguments = {
                articleObject.author,
                articleObject.category,
                articleObject.postDate,
                articleObject.title
        };
        // Perform the query and return any database rows that match.
        Cursor cursor = db.query(TABLE_NAME_ARTICLES, columns, selection, arguments, null, null, null);
        // If there were matches then cursor.getCount() would be > 0
        if (cursor.getCount() <= 0){
            // No matches so close the cursor for memory leak prevention and return false
            cursor.close();
            return false;
        }else {
            // There was a match so close the cursor for memory leak prevention and return true.
            cursor.close();
            return true;
        }
    }

    /**
     * Get an ArrayList of PlayerObjects from the Database
     * @return an ArrayList of PlayerObjects
     */
    public ArrayList<PlayerObject> getPlayers(){
        // The ArrayList that will be returned
        ArrayList<PlayerObject> playerObjects = new ArrayList<>();
        // Get a read only connection to the database.
        SQLiteDatabase db = this.getReadableDatabase();
        // The database query
        String query = "SELECT * FROM " + TABLE_NAME_TEAM;
        // Get the rows from the database that match the SQL query
        Cursor cursor = db.rawQuery(query, null);
        // If we have a result(s) then populate the ArrayList
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
            // Close the cursor and database connection to avoid memory leaks
            cursor.close();
            db.close();
        } else {
            // The database didn't have any matching data, close the connection to avoid memory leaks
            db.close();
        }
        // Return the ArrayList of playerObjects (or an empty list).
        return playerObjects;
    }

    /**
     * Add an individual unique playerObject to the database
     * @param playerObject the Player being added
     */
    public void addPlayer(PlayerObject playerObject){
        // Get a writable connection to the database
        SQLiteDatabase db = this.getWritableDatabase();
        // If the PlayerObject doesn't exist then add it to the database
        if(!doesPlayerExist(playerObject)){
            // Create the ContentValues object which will hold the new database values for the PlayerObject
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_PLAYER_NAME, playerObject.getName());
            values.put(COLUMN_NAME_PLAYER_AFL_TEAM, playerObject.getTeam());
            // Insert the PlayerObject into the database
            db.insert(TABLE_NAME_TEAM, null, values);
            // Close the connection to the database to minimise memory leaks
            db.close();
        } else {
            // The player exists so we don't add it. Close the connection to the database to minimise memory leaks.
            db.close();
        }
    }

    /**
     * Check to see if the PlayerObject provided already exists in the database
     * @param playerObject the PlayerObject query
     * @return true or false depending on whether the PlayerObject exists in the database or not
     */
    public boolean doesPlayerExist(PlayerObject playerObject){
        // Get a read only connection to the database
        SQLiteDatabase db = this.getReadableDatabase();
        // The columns the query is performed over
        String[] columns = {
                COLUMN_NAME_PLAYER_NAME,
                COLUMN_NAME_PLAYER_AFL_TEAM
        };
        // The SQL query columns to check the values in
        String selection = COLUMN_NAME_PLAYER_NAME + "=? AND " +
                COLUMN_NAME_PLAYER_AFL_TEAM + "=?";
        // The values being used for the query
        String[] arguments = {
                playerObject.getName(),
                playerObject.getTeam()
        };
        // Perform the query and return any matching rows from the database
        Cursor cursor = db.query(TABLE_NAME_TEAM, columns, selection, arguments, null, null, null);
        // If the cursor.getCount > 0 then the PlayerObject exists, otherwise it doesn't
        if (cursor.getCount() <= 0){
            // Close the connection to the cursor to avoid memory leaks and return false (doesNotExist)
            cursor.close();
            return false;
        } else {
            // Close the connection to the cursor to avoid memory leaks and return true (doesExist)
            cursor.close();
            return true;
        }
    }

    /**
     * Get an ArrayList of SelectionObjects from the database
     * @return an ArrayList of SelectionObjects
     */
    public ArrayList<SelectionObject> getSelections(){
        // The ArrayList that will be returned
        ArrayList<SelectionObject> selectionObjects = new ArrayList<>();
        // Get a read only connection to the database.
        SQLiteDatabase db = this.getReadableDatabase();
        // The columns to return data from
        String[] columns = {
                COLUMN_NAME_PLAYER_NAME,
                COLUMN_NAME_PLAYER_AFL_TEAM,
                COLUMN_NAME_PLAYER_POSITION,
                COLUMN_NAME_PLAYER_NUM
        };
        // The selection criteria and arguments aren't required as all rows are required to be returned
        String orderString = COLUMN_NAME_PLAYER_NUM + " ASC";
        // Get all rows from the database ordered in ascending order via COLUMN_NAME_PLAYER_NUM
        Cursor cursor = db.query(TABLE_NAME_SELECTED_TEAM, columns, null, null, null, null, orderString);
        // If we have a result(s) then populate the ArrayList
        if (cursor != null){
            if (cursor.moveToFirst()){
                do{
                    // Get the strings/values from the database
                    String playerName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYER_NAME));
                    String aflTeam = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYER_AFL_TEAM));
                    String position = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYER_POSITION));
                    int number = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_PLAYER_NUM));
                    // Add a SelectionObject to our ArrayList
                    selectionObjects.add(new SelectionObject(new PlayerObject(playerName, aflTeam), position, number));
                } while (cursor.moveToNext());
            }
            // Close the cursor and database connection to avoid memory leaks
            cursor.close();
            db.close();
        } else {
            // The database didn't have any matching data, close the connection to avoid memory leaks
            db.close();
        }
        // Return the ArrayList of SelectionObjects (or an empty list).
        return selectionObjects;
    }

    /**
     * Get the selectionObject at the specified position (i.e. Ruck = position 1, Tackler = 2 or 3...)
     * @param position the number referring to the COLUMN_NAME_PLAYER_NUM value for the desired SelectionObject
     * @return a selectionObject, or null if none exist
     */
    public SelectionObject getSelectionAtPosition(int position){
        // SelectionObject to be returned
        SelectionObject selectionObject = null;
        // Get a read only connection to the database
        SQLiteDatabase db = this.getReadableDatabase();
        // The column the query will return data from
        String[] columns = {
                COLUMN_NAME_PLAYER_NAME,
                COLUMN_NAME_PLAYER_AFL_TEAM,
                COLUMN_NAME_PLAYER_POSITION,
                COLUMN_NAME_PLAYER_NUM
        };
        // The selection criteria for the database query
        String selection = COLUMN_NAME_PLAYER_NUM + "=?";
        // The argument(s) for the database query
        String[] arguments = {
                String.valueOf(position)
        };
        // Perform the query and return any matching rows from the database
        Cursor cursor = db.query(TABLE_NAME_SELECTED_TEAM, columns, selection, arguments, null, null, null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                String playerName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYER_NAME));
                String aflTeam = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYER_AFL_TEAM));
                String playerPosition = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLAYER_POSITION));
                int number = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_PLAYER_NUM));
                selectionObject = new SelectionObject(new PlayerObject(playerName, aflTeam), playerPosition, number);
            }
            // Close the cursor and connection to the database
            cursor.close();
            db.close();
        } else {
            // Close the connection to the database
            db.close();
        }
        // Return the selectionObject (or null)
        return selectionObject;
    }

    /**
     * Add an individual unique selectionObject to the database
     * This also will update an existing selectionObject if one exists already (TODO)
     * @param selectionObject the SelectionObject to be added
     */
    public void addSelection(SelectionObject selectionObject){
        // Get a writable connection to the database
        SQLiteDatabase db = this.getWritableDatabase();
        // If the SelectionObject doesn't exist then add it to the database
        if(!doesSelectionExist(selectionObject)){
            // Create the ContentValues object which will hold the new database values for the PlayerObject
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_PLAYER_NAME, selectionObject.getPlayerObject().getName());
            values.put(COLUMN_NAME_PLAYER_AFL_TEAM, selectionObject.getPlayerObject().getTeam());
            values.put(COLUMN_NAME_PLAYER_NUM, selectionObject.getNumber());
            values.put(COLUMN_NAME_PLAYER_POSITION, selectionObject.getPosition());
            values.put(COLUMN_NAME_PLAYER_POSITION_NUM, selectionObject.getNumber());
            // Insert the SelectionObject into the database
            db.insert(TABLE_NAME_SELECTED_TEAM, null, values);
            // Close the connection to the database to minimise memory leaks
            db.close();
        } else {
            // The selection already exists, so lets update it. with the latest from the WebService (or edit action)
            ContentValues updatedValues = new ContentValues();
            // New values
            updatedValues.put(COLUMN_NAME_PLAYER_NAME, selectionObject.getPlayerObject().getName());
            updatedValues.put(COLUMN_NAME_PLAYER_AFL_TEAM, selectionObject.getPlayerObject().getTeam());
            updatedValues.put(COLUMN_NAME_PLAYER_NUM, selectionObject.getNumber());
            updatedValues.put(COLUMN_NAME_PLAYER_POSITION, selectionObject.getPosition());
            updatedValues.put(COLUMN_NAME_PLAYER_POSITION_NUM, selectionObject.getNumber());
            // Columns to search for the old row (so the correct row is updated)
            String whereClause = COLUMN_NAME_PLAYER_NUM + "=? AND " +
                    COLUMN_NAME_PLAYER_POSITION_NUM + "=?";
            String[] whereArgs = {
                    String.valueOf(selectionObject.getNumber()),
                    String.valueOf(selectionObject.getNumber())
            };
            // Update the SelectionObject in the database row
            db.update(TABLE_NAME_SELECTED_TEAM, updatedValues, whereClause, whereArgs);
            // Close the connection to the database to minimise memory leaks
            db.close();
        }
    }

    /**
     * Check to see if the SelectionObject provided already exists in the database
     * @param selectionObject the SelectionObject query
     * @return true or false depending on whether the SelectionObject exists in the database or not
     */
    public boolean doesSelectionExist(SelectionObject selectionObject){
        // Get a read only connection to the database
        SQLiteDatabase db = this.getReadableDatabase();
        // The columns the query is performed over (We're only concerned about the player num and position num
        // As the selections can be updated, so it will update an existing record
        String[] columns = {
                COLUMN_NAME_PLAYER_NUM,
                COLUMN_NAME_PLAYER_POSITION_NUM
        };
        // The SQL query columns to check the values in
        String selection = COLUMN_NAME_PLAYER_NUM + "=? AND " +
                COLUMN_NAME_PLAYER_POSITION_NUM + "=?";
        // The values being used for the query
        String[] arguments = {
                String.valueOf(selectionObject.getNumber()),
                String.valueOf(selectionObject.getNumber())
        };
        // Perform the query and return any matching rows from the database
        Cursor cursor = db.query(TABLE_NAME_SELECTED_TEAM, columns, selection, arguments, null, null, null);
        // If the cursor.getCount > 0 then the PlayerObject exists, otherwise it doesn't
        if (cursor.getCount() <= 0){
            // Close the connection to the cursor to avoid memory leaks and return false (doesNotExist)
            cursor.close();
            return false;
        } else {
            // Close the connection to the cursor to avoid memory leaks and return true (doesExist)
            cursor.close();
            return true;
        }
    }

    /**
     * Delete all records for the provided table
     * @param tableName the name of the table to delete records from
     */
    public void deleteAllObjects(String tableName){
        // Get a writable connection to the database
        SQLiteDatabase db = this.getWritableDatabase();
        // Execute the delete from tableName sql statement.
        db.execSQL("DELETE FROM " + tableName);
        // execute the VACUUM sql statement to clear the space allocation from the old table data (since deleted)
        db.execSQL("VACUUM");
        // Close the connection to the database to minimise memory leaks
        db.close();
    }
}
