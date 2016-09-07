package com.hnnews.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hnnews.news.adapter.FeedAdapter;
import com.hnnews.news.dbhandler.DatabaseHandler;
import com.hnnews.news.pojo.News;
import com.hnnews.news.utils.InternetConnection;
import com.hnnews.news.utils.RecyclerItemClickListener;
import com.hnnews.news.utils.UrlUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private FeedAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    FetchTopStories fetchStoriesTask;
    ArrayList<News> newsArrayLst = new ArrayList<>();
    ArrayList<News> topStoriesDetails = new ArrayList<>();
    DatabaseHandler db ;
    JSONArray jArray;

    LinearLayout mProgressLayout;
    SwipeRefreshLayout mSwipeRefreshLayout;

    String My_PREF_NAME ="topstories_id";
    String topStoriesArray = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mProgressLayout = (LinearLayout) findViewById(R.id.progess_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        fetchStoriesTask = new FetchTopStories();
        db = new DatabaseHandler(this);

        // retrive data from local DB
        topStoriesDetails =  db.getAllNews();

             if(topStoriesDetails  != null && topStoriesDetails.size() != 0){
                 newsArrayLst.clear();
                 newsArrayLst =topStoriesDetails;
                 mAdapter = new FeedAdapter(newsArrayLst);
                 // set data adapter
                 mRecyclerView.setAdapter(mAdapter);
                 mProgressLayout.setVisibility(View.GONE);
                 mRecyclerView.setVisibility(View.VISIBLE);



                }else{
                 // Fecth the news
                 if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                     fetchStoriesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                 else
                     fetchStoriesTask.execute();
             }




        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // On click get news details
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(MainActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent in = new Intent(MainActivity.this, DetailActivity.class);
                        in.putExtra("url",newsArrayLst.get(position).getUrl());
                        startActivity(in);

                    }
                }) {
                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                    }
                }
        );

// Refresh the news feed
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                SharedPreferences prefs = getSharedPreferences(My_PREF_NAME, MODE_PRIVATE);
                topStoriesArray = prefs.getString("topStoriesArray", null);
                try {
                    jArray = new JSONArray(topStoriesArray);
                }catch (Exception ex){

                }


                if (topStoriesArray != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        new FetchTopStoriesDetails().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        new FetchTopStoriesDetails().execute();
                }else{
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

    }


    // Fetching Haker news Top Stories
    private class FetchTopStories extends AsyncTask<String, String, String> {

        private String resp;
        @Override
        protected void onPreExecute() {
            mProgressLayout.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... params) {

            try {

                if(InternetConnection.checkConnection(getApplicationContext())) {
                    URL url = new URL(UrlUtils.BASE_URL+UrlUtils.TOP_STORIES);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();
                    String inputString;
                    while ((inputString = bufferedReader.readLine()) != null) {
                        builder.append(inputString);
                    }

                    JSONArray topStoriesArray = new JSONArray(builder.toString());
                    jArray =topStoriesArray;
                    parseJonsArray();

                    //   parseJsonData(feedObject);
                    urlConnection.disconnect();
                    resp ="success";
                }else{
                    resp ="failed";
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation

            if(jArray != null ) {
                SharedPreferences prefs = getSharedPreferences(My_PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("topStoriesArray", jArray.toString());
                edit.commit();
            }
            if(resp.equalsIgnoreCase("success")) {
                // specify an adapter (see also next example)
                if(newsArrayLst != null && newsArrayLst.size() !=0) {
                    mAdapter = new FeedAdapter(newsArrayLst);
                    mRecyclerView.setAdapter(mAdapter);
                    mProgressLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);

                }
            }else{
                mProgressLayout.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this,getResources().getString(R.string.string_internet_connection_not_available), Toast.LENGTH_SHORT).show();
            }
        }

    }

    // Parse json Array and store in DB
    public void parseJonsArray(){
        for(int i=topStoriesDetails.size(); i<topStoriesDetails.size()+10; i++){



            try {
               // Log.e("feed","topStoriesArray   :: "+jArray.getString(i));
                URL url = new URL(UrlUtils.BASE_URL+"/item/"+jArray.getString(i)+".json?print=pretty");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();
                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }
                JSONObject topStoriesArray = new JSONObject(builder.toString());

                News newsObjet = new News();
                newsObjet.setId(jArray.getString(i));
                newsObjet.setTitle(topStoriesArray.getString("title"));
                newsObjet.setUrl(topStoriesArray.getString("url"));
                db.addNews(newsObjet);

               //   parseJsonData(feedObject);
                urlConnection.disconnect();
                newsArrayLst.add(newsObjet);
            }
            catch (Exception ex){
                ex.printStackTrace();

            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    // Fetching  Haker news Top Stories Details
    private class FetchTopStoriesDetails extends AsyncTask<String, String, String> {

        private String resp;
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected String doInBackground(String... params) {
            db = new DatabaseHandler(MainActivity.this);
            try {

                if(InternetConnection.checkConnection(getApplicationContext())) {

                    parseJonsArray();

                    resp ="success";
                }else{
                    resp ="failed";
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
           // if(resp.equalsIgnoreCase("success")) {
            topStoriesDetails =  db.getAllNews();
            // Updating data in recycleView
                mAdapter.updateAdapter(topStoriesDetails);
                mAdapter. notifyDataSetChanged();
                 mSwipeRefreshLayout.setRefreshing(false);
          //  }else{

           // }
        }

    }
}
