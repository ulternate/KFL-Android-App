package com.danielcswain.kfl.Articles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.danielcswain.kfl.R;

import java.util.ArrayList;

/**
 * Created by ulternate on 27/05/2016.
 *
 * Out ListAdapter for our Articles class
 */
public class ArticleListAdapter extends ArrayAdapter<ArticleObject>{

    /**
     * Our Constructor for our custom list adaptor.
     * @param context: The view context/application context
     * @param articleObjects: the array of ArticleObject objects we want to show in our list view
     */
    public ArticleListAdapter(Context context, ArrayList<ArticleObject> articleObjects) {
        // Call the super ArrayAdapter class with our array of articleObjects
        // We only override the getView method
        super(context, 0, articleObjects);
    }

    /**
     * Get the view for a single element in our Array so we can populate the list view
     * @param position: the position of the element in the array and list
     * @param convertView: The view we need to inflate and/or populate with info
     * @param parent: the list view parent
     * return: return the list item view to the list view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ArticleObject articleObject = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.article_item, parent, false);
        }

        // Lookup view for data population
        TextView tvTitle = (TextView) convertView.findViewById(R.id.articleTitle);
        TextView tvDate = (TextView) convertView.findViewById(R.id.articlePubDate);
        TextView tvSummary = (TextView) convertView.findViewById(R.id.articleSummary);
        TextView tvAuthor = (TextView) convertView.findViewById(R.id.articleAuthor);

        // Populate the data into the template view using the data object
        tvTitle.setText(articleObject.title);
        tvAuthor.setText(articleObject.author);
        tvDate.setText(articleObject.postDate);
        tvSummary.setText(articleObject.summary);

        // Return the completed view to render on screen
        return convertView;
    }
}
