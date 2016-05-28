package com.danielcswain.kfl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielcswain.kfl.AsyncHandlers.DownloadImageTask;

public class Article extends AppCompatActivity {

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

        // Fill the text view's with the content for the view
        assert tvTitle != null;
        assert tvAuthor != null;
        assert tvDate != null;
        assert webView != null;
        tvTitle.setText(intent.getStringExtra("title"));
        tvAuthor.setText(intent.getStringExtra("author"));
        tvDate.setText(intent.getStringExtra("pubDate"));
        webView.loadData(intent.getStringExtra("longText"), "text/html; charset=UTF-8", null);

        // Load the image asynchronously
        new DownloadImageTask((ImageView) findViewById(R.id.image)).execute(intent.getStringExtra("imageURL"));
    }
}
