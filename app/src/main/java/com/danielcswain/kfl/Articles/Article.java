package com.danielcswain.kfl.Articles;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ulternate on 27/05/2016.
 *
 * A custom Article object class used by the ListView to hold an array of Articles
 * The APIGetHandler can return articles in a JSON
 */
public class Article {

    /* Object constants */
    public String thumbnailURL;
    public String imageURL;
    public String category;
    public String summary;
    public String longText;
    public String title;
    public String author;
    public String postDate;

    /**
     * Article constructor class given the listed parameters
     * There are no setters as the Articles are read only
     *
     * @param thumbnailURL: the url for the thumbnail image
     * @param imageURL: the url for the main image
     * @param summary: the short article text
     * @param longText: the long article text
     * @param category: the article category
     * @param title: the article title
     * @param author: the article author
     * @param postDate: the post date of the article
     */
    public Article(String thumbnailURL, String imageURL, String summary, String longText,
                   String category, String author, String title, String postDate){
        this.thumbnailURL = thumbnailURL;
        this.imageURL = imageURL;
        this.summary = summary;
        this.longText = longText;
        this.category = category;
        this.author = author;
        this.title = title;
        this.postDate = postDate;
    }

    /**
     * Article constructor from a JSONObject
     *
     * @param json: A JsonObject to create model from API return
     */
    public Article(JSONObject json){
        try {
            this.thumbnailURL = json.getString("thumbnail_url");
            this.imageURL = json.getString("image_url");
            this.summary = json.getString("summary");
            this.longText = json.getString("long_text");
            this.category = json.getString("category");
            this.author = json.getString("author");
            this.title = json.getString("title");
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy");
            try {
                Date oneWayTripDate = input.parse(json.getString("pub_date"));
                this.postDate = output.format(oneWayTripDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // The following are the getter methods to get an Articles properties
    public String getPostDate() {
        return this.postDate;
    }

    public String getLongText() {
        return this.longText;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getCategory() {
        return this.category;
    }

    public String getImageURL() {
        return this.imageURL;
    }

    public String getThumbnailURL() {
        return this.thumbnailURL;
    }

    public String getTitle() {
        return this.title;
    }
}
