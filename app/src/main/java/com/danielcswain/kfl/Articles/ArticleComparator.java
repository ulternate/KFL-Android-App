package com.danielcswain.kfl.Articles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

/**
 * Created by ulternate on 28/05/2016.
 *
 * Simple custom Comparator class to sort the ArticleListAdapter by postDate
 */
public class ArticleComparator implements Comparator<ArticleObject> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public int compare(ArticleObject lhs, ArticleObject rhs) {
        try {
            return dateFormat.parse(rhs.postDate).compareTo(dateFormat.parse(lhs.postDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
