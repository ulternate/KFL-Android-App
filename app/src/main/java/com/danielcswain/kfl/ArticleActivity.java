package com.danielcswain.kfl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielcswain.kfl.AsyncHandlers.DownloadImageTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created By Daniel Swain (ulternate)
 *
 * Activity that shows an individual article from the WebService API endpoint /api/articles
 *
 * Methods:
 *  onCreate: set the layout and fill the textViews and ImageViews
 *
 * Uses the following classes:
 *  DownloadImageTask: This asynchronous image loading class is used to perform the image download in the background
 *      for the Article header image. This is because the article image data is just a url and the image is not
 *      saved in the application data. Image loading via a url cannot be done on the Main UI thread.
 */
public class ArticleActivity extends AppCompatActivity {

    /**
     * Create the ArticleObject layout
     * @param savedInstanceState the information bundle saved by the system when the activity instance is destroyed
     *                           containing information about the activity's view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        // Get the calling intent for the intent extras
        Intent intent = getIntent();

        // Get the text view's from the layout
        TextView tvTitle = (TextView) findViewById(R.id.title);
        TextView tvAuthor = (TextView) findViewById(R.id.author);
        TextView tvDate = (TextView) findViewById(R.id.pubDate);
        WebView webView = (WebView) findViewById(R.id.webView);

        // Assert that the textViews exist and aren't null
        assert tvTitle != null;
        assert tvAuthor != null;
        assert tvDate != null;
        assert webView != null;

        // Fill the text view's with the content for the view
        tvTitle.setText(intent.getStringExtra("title"));
        tvAuthor.setText(intent.getStringExtra("author"));

        // Show the postDate in a nicer format using SimpleDateFormat to parse ArticleObject.postDate
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy");
        // Try and parse the postDate and convert it into the output format.
        try {
            Date oneWayTripDate = input.parse(intent.getStringExtra("pubDate"));
            // If successful in parsing the Date, then set the textView to the nicer format)
            tvDate.setText(output.format(oneWayTripDate));
        } catch (ParseException e) {
            e.printStackTrace();
            // The parsing of the date was unsuccessful so just use the default database value (that's not formatted)
            tvDate.setText(intent.getStringExtra("pubDate"));
        }

        // Use an embedded webView for the longText as the site content is actually formatted using html inside the longText
        // Html.fromHtml isn't good enough in this situation as it doesn't support all Html element types
        webView.loadData(intent.getStringExtra("longText"), "text/html; charset=UTF-8", null);

        // Load the image asynchronously
        new DownloadImageTask((ImageView) findViewById(R.id.image)).execute(intent.getStringExtra("imageURL"));
    }
}
