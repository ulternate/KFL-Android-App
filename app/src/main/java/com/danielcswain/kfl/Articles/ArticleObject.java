package com.danielcswain.kfl.Articles;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Daniel Swain (ulternate) on 27/05/2016.
 *
 * A custom ArticleObject object class used by the ListView to hold an array of Articles
 * The ArticleGetHandler can return articles in a JSON
 *
 * methods:
 *  ArticleObject(Strings...): Constructor for an individual ArticleObject from the given string values
 *  ArticleObject(JSONObject): Constructor for an individual ArticleObject from the give JSONObject
 *  get...: Get the ArticleObject value
 *  set...(String value): set the ArticleObject value
 */
public class ArticleObject {

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
     * ArticleObject constructor class given the listed parameters
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
    public ArticleObject(String thumbnailURL, String imageURL, String summary, String longText,
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
     * ArticleObject constructor from a JSONObject
     *
     * @param json: A JsonObject to create model from API return
     */
    public ArticleObject(JSONObject json){
        try {
            this.thumbnailURL = json.getString("thumbnail_url");
            this.imageURL = json.getString("image_url");
            this.summary = json.getString("summary");
            this.longText = json.getString("long_text");
            this.category = json.getString("category");
            this.author = json.getString("author");
            this.title = json.getString("title");
            this.postDate = json.getString("pub_date");
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
