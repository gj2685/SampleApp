package com.example.micro.sample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micro.sample.adapter.ArticleAdapter;
import com.example.micro.sample.model.Article;
import com.example.micro.sample.model.JsonKeys;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    //https://www.mediawiki.org/wiki/API:Page_info_in_search_results
    private static final String JSON_URL = "https://en.wikipedia.org/w/api.php?action=query" +
            "&prop=pageimages&format=json&gpslimit=50&piprop=thumbnail&pithumbsize=50" +
            "&pilimit=50&generator=prefixsearch&gpssearch=";
    private ArrayList<Article> mList = new ArrayList<>();
    private ArticleAdapter mAdapter = null;
    private ProgressBar mProgress;
    private TextView mTvToast;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTvToast = (TextView) findViewById(R.id.tv_toast);
        mProgress = (ProgressBar) findViewById(R.id.pg_bar);

        final EditText etSearchQuery = (EditText) findViewById(R.id.et_search);
        if (etSearchQuery != null) {
            //etSearchQuery.setOnEditorActionListener(getEditorListener(etSearchQuery));
            etSearchQuery.addTextChangedListener(getTextChangedListener(etSearchQuery));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_articles);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAdapter = new ArticleAdapter(this, mList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(layoutManager);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                loadArticles();
            }
        };
    }

    private void showMessage(int strId) {
        mTvToast.setText(strId);
        mProgress.setVisibility(View.GONE);
        mTvToast.setVisibility(View.VISIBLE);
        mAdapter.updateArticles(null);
    }

    /*public TextView.OnEditorActionListener getEditorListener(final EditText etSearchQuery) {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    loadArticles(JSON_URL + v.getText());

                    //Hiding keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                    return true;
                }
                return false;
            }
        };
    }*/

    public TextWatcher getTextChangedListener(final EditText etSearchQuery) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                mHandler.removeCallbacks(mRunnable);
                mAdapter.setQueryText(s.toString());
                if (s.length() == 0) {
                    showMessage(R.string.no_result);
                    if (!Util.isConnected(MainActivity.this))
                        showMessage(R.string.no_internet);
                    return;
                }
                mHandler.postDelayed(mRunnable, 400);
            }
        };
    }

    public void loadArticles() {
        if (Util.isConnected(this)) {
            mProgress.setVisibility(View.VISIBLE);
            mTvToast.setVisibility(View.GONE);
            requestJSON(JSON_URL + mAdapter.getQueryText());
        } else {
            showMessage(R.string.no_internet);
            //requestDB();
        }
    }

    private void requestJSON(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection con = null;
                BufferedReader br = null;
                try {
                    URL url = new URL(query);
                    con = (HttpsURLConnection) url.openConnection();
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    mHandler.sendMessage(Message.obtain(mHandler, Util.JSON_FETCH_COMPLETED, sb.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.sendMessage(Message.obtain(mHandler, Util.JSON_FETCH_ERROR, e.getMessage()));
                } finally {
                    if (con != null)
                        con.disconnect();
                    if (br != null)
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }).start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Util.JSON_FETCH_COMPLETED:
                    parseJSON((String) msg.obj);
                    break;
                case Util.JSON_FETCH_ERROR:
                    handleError((String) msg.obj);
                    break;
            }
        }
    };

    private void handleError(String error) {
        showMessage(R.string.error_msg);
    }

    private void parseJSON(String json) {
        mList.clear();
        if (json != null) {
            try {
                JSONObject jsonObj = new JSONObject(json);

                if (!jsonObj.has(JsonKeys.QUERY)) {
                    showMessage(R.string.no_result);
                    return;
                }

                JSONObject queryObj = jsonObj.getJSONObject(JsonKeys.QUERY);
                JSONObject pagesObj = queryObj.getJSONObject(JsonKeys.PAGES);
                Iterator<String> pageIds = pagesObj.keys();
                while (pageIds.hasNext()) {
                    JSONObject pageId = pagesObj.getJSONObject(pageIds.next());
                    String pageTitle = pageId.getString(JsonKeys.TITLE);
                    String thumbSource = null;
                    int thumbWidth = 0;
                    int thumbHeight = 0;

                    if (pageId.has(JsonKeys.THUMBNAIL)) {
                        JSONObject thumbObj = pageId.getJSONObject(JsonKeys.THUMBNAIL);
                        thumbSource = thumbObj.getString(JsonKeys.SOURCE);
                        thumbWidth = thumbObj.getInt(JsonKeys.WIDTH);
                        thumbHeight = thumbObj.getInt(JsonKeys.HEIGHT);
                    }
                    Article art = new Article(pageTitle, thumbSource, thumbWidth, thumbHeight);
                    mList.add(art);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                handleError("JSONException:: " + e.getMessage());
            }
            if (mList.size() > 0 && !mAdapter.getQueryText().isEmpty()) {
                mProgress.setVisibility(View.GONE);
                mAdapter.updateArticles(mList);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}