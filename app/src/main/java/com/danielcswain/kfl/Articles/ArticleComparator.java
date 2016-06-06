package com.danielcswain.kfl.Articles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

/**
 * Created by Daniel Swain (ulternate) on 28/05/2016.
 *
 * Simple custom Comparator class to sort the ArticleListAdapter by postDate
 */
public class ArticleComparator implements Comparator<ArticleObject> {

    // The dateFormat used to get a date object from the ArticleObject.postDate string
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Compare two ArticleObjects to sort via their postDates
     * @param lhs the left hand side or top article
     * @param rhs the right hand side or bottom article
     * @return -1, 0 or 1 depending on if on is smaller, they're both equal or one is bigger
     */
    @Override
    public int compare(ArticleObject lhs, ArticleObject rhs) {
        try {
            // Sort the ArticleObjects in descending order (i.e. most recent or largest date first)
            return dateFormat.parse(rhs.getPostDate()).compareTo(dateFormat.parse(lhs.getPostDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
