package com.danielcswain.kfl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
 *  onCreateOptionsMenu: build the options menu
 *  onOptionsItemSelected: handle the optionsMenuItem click
 *  buildArticleLink(String category, string title): Build the article link for this article
 */
public class ArticleActivity extends AppCompatActivity {

    private final String BASE_URL = "http://www.kfl.com.au/";
    private static String linkUrl = "";
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

        // Get the views from the layout resource
        TextView tvTitle = (TextView) findViewById(R.id.title);
        TextView tvAuthor = (TextView) findViewById(R.id.author);
        TextView tvDate = (TextView) findViewById(R.id.pubDate);
        WebView webView = (WebView) findViewById(R.id.webView);

        // Assert that the views exist and aren't null
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

        // Build the linkUrl for the article
        linkUrl = buildArticleLink(intent.getStringExtra("category"), intent.getStringExtra("title"));
    }

    /**
     * Inflate the article_menu.xml
     * @param menu the menu inside the activity
     * @return true to ensure the menu popup is opened
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu for the Activity
        getMenuInflater().inflate(R.menu.article_menu, menu);
        return true;
    }

    /**
     * Handle optionsMenuItem clicks, in this case to view the article in the user's browser
     * @param item the menuItem that was clicked
     * @return true if the action is actionArticleViewInBrowser, else it returns super.onOptionsItemSelected(item)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // Handle the options menu item click actions, in this case view the article in the phone's browser
        if (id == R.id.actionArticleViewInBrowser){
            // Open a browser action with the calculated url
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(linkUrl));
            startActivity(browserIntent);
            return true;
        }
        // Return super.onOptionsItemSelected for any item we haven't explicitly covered above.
        return super.onOptionsItemSelected(item);
    }

    /**
     * Build the url link for the current article
     * @param articleCategory the article category (used to determine which section of the kfl site to link to)
     * @param articleTitle the title, used to point to the specific article on kfl.com.au
     * @return the url string for this article
     */
    private String buildArticleLink(String articleCategory, String articleTitle){
        String urlEncodedTitle = "";
        String returnString = "";
        // Try and encode special characters (note spaces are '_' characters in kfl.com.au links, not %20 so
        // these are changed before encoding)
        try{
            urlEncodedTitle = URLEncoder.encode(articleTitle.replace(" ", "_"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // Build the full link depending on the Article category
        switch (articleCategory){
            case "A":
                // Category: Article
                returnString = BASE_URL + "articles/" + urlEncodedTitle;
                break;
            case "N":
                // Category: News
                returnString = BASE_URL + "news/";
                break;
            case "TW":
                // Category: Team Of The Week
                returnString = BASE_URL + "Team_of_the_Week/" + urlEncodedTitle;
                break;
            case "TU":
                // Category: Tipping Update
                returnString = BASE_URL + "Tipping_Update/" + urlEncodedTitle;
                break;
            case "DR":
                // Category: Donut Review
                returnString = BASE_URL + "Donut_Review/" + urlEncodedTitle;
                break;
            case "TP":
                // Category: Team Profile
                returnString = BASE_URL + "Team_Profile/" + urlEncodedTitle;
                break;
            case "EA":
                // Category: Email Archive
                returnString = BASE_URL + "Email_Archives/" + urlEncodedTitle;
                break;
        }
        // Return the url
        return returnString;
    }
}
