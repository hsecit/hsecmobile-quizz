package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CategoryActivity extends AppCompatActivity {

    SharedPreferences bottomBannerType, admobInterstitial, facebookInterstitial, facebookBanner, admobBanner, questionTime, userSituationId, completedOption;
    String categoryId, categoryName, userId, bannerBottomType;
    TextView catName;
    private AdView bannerAdmobAdView;
    private LinearLayout adsLinear;
    private RequestQueue queue;
    private Button easyBtn, mediumBtn, hardBtn, expertBtn;
    private com.facebook.ads.InterstitialAd facebookInterstitialAd;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.category_toolbar);
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryName = intent.getStringExtra("categoryName");
        userSituationId = getSharedPreferences("userId", Context.MODE_PRIVATE);
        completedOption = getSharedPreferences("completedOption", Context.MODE_PRIVATE);
        userId = userSituationId.getString("userId", null);
        toolbar.setTitle(categoryName);
        setSupportActionBar(toolbar);
        queue = Volley.newRequestQueue(this);
        questionTime = getSharedPreferences("seconds", MODE_PRIVATE);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        facebookBanner = getSharedPreferences("facebookBanner", Context.MODE_PRIVATE);
        admobBanner = getSharedPreferences("admobBanner", MODE_PRIVATE);
        // Admob Banner
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        // Facebook Interstitial
        facebookInterstitial = getSharedPreferences("facebookInterstitial", MODE_PRIVATE);
        prepareInterstitialAd();
        ScheduledExecutorService schudeler2 = Executors.newSingleThreadScheduledExecutor();
        schudeler2.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (facebookInterstitialAd.isAdLoaded()) {
                            facebookInterstitialAd.show();
                        }
                    }
                });
            }
        }, 2, 1, TimeUnit.SECONDS);
        adsLinear = (LinearLayout) findViewById(R.id.single_category_ads_linear);
        bannerAdmobAdView = new AdView(this);
        bannerAdmobAdView.setAdUnitId(admobBanner.getString("admobBanner", ""));
        bannerAdmobAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        adsLinear.addView(bannerAdmobAdView);
        adsLinear.setGravity(Gravity.CENTER_HORIZONTAL);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdmobAdView.loadAd(adRequest);
        // Set Data
        catName = (TextView) findViewById(R.id.category_name);
        catName.setText(categoryName);
        easyBtn = (Button) findViewById(R.id.easy_btn);
        mediumBtn = (Button) findViewById(R.id.medium_btn);
        hardBtn = (Button) findViewById(R.id.hard_btn);
        expertBtn = (Button) findViewById(R.id.expert_btn);
        if(completedOption.getString("completedOption", "").equals("yes")) {
            // Completed Option Activated
            checkifEasyLevelContainsQuestions();
            checkifMediumLevelContainsQuestions();
            checkifHardLevelContainsQuestions();
            checkifExpertLevelContainsQuestions();
        } else {
            // Completed Option Deactivated
            checkifEasyContainsQuestions();
            checkifMediumContainsQuestions();
            checkifHardContainsQuestions();
            checkifExpertContainsQuestions();
        }
        // Admob Banner Bottom
        bottomBannerType = getSharedPreferences("bottomBannerType",MODE_PRIVATE);
        bannerBottomType = bottomBannerType.getString("bottomBannerType", "");
        if (bannerBottomType.equals("admob")) {
            MobileAds.initialize(CategoryActivity.this, getString(R.string.admob_app_id));
            admobBanner = getSharedPreferences("admobBanner",MODE_PRIVATE);
            adsLinear = (LinearLayout) findViewById(R.id.banner_container_category_activity);
            bannerAdmobAdView = new AdView(this);
            bannerAdmobAdView.setAdUnitId(admobBanner.getString("admobBanner", ""));
            bannerAdmobAdView.setAdSize(com.google.android.gms.ads.AdSize.FULL_BANNER);
            adsLinear.addView(bannerAdmobAdView);
            adsLinear.setGravity(Gravity.CENTER_HORIZONTAL);
            AdRequest adRequest2 = new AdRequest.Builder().build();
            bannerAdmobAdView.loadAd(adRequest2);
            bannerAdmobAdView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    adsLinear.setVisibility(View.VISIBLE);
                }
            });
        } else if(bannerBottomType.equals("facebook")) {
            facebookBanner = getSharedPreferences("facebookBanner",MODE_PRIVATE);
            AudienceNetworkAds.initialize(this);

            com.facebook.ads.AdView facebookAdView = new com.facebook.ads.AdView(this, facebookBanner.getString("facebookBanner", null), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container_category_activity);
            adContainer.addView(facebookAdView);
            facebookAdView.loadAd();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Intent main = new Intent(CategoryActivity.this, CategoriesActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(CategoryActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        super.onBackPressed();
    }

    private void prepareInterstitialAd() {
        AudienceNetworkAds.initialize(this);

        facebookInterstitialAd = new com.facebook.ads.InterstitialAd(this, facebookInterstitial.getString("facebookInterstitial", ""));
        facebookInterstitialAd.loadAd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lang_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_rate:
                smartRating();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void smartRating() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .icon(getDrawable(R.drawable.smart_rating))
                .threshold(3)
                .title(getString(R.string.rate_dialog_title))
                .titleTextColor(R.color.black)
                .positiveButtonText(getString(R.string.rate_dialog_cancel))
                .negativeButtonText(getString(R.string.rate_dialog_no))
                .positiveButtonTextColor(R.color.colorPrimaryDark)
                .negativeButtonTextColor(R.color.grey_500)
                .formTitle(getString(R.string.rate_dialog_suggest))
                .formHint(getString(R.string.rate_dialog_suggestion))
                .formSubmitText(getString(R.string.rate_dialog_submit))
                .formCancelText(getString(R.string.rate_form_cancel))
                .playstoreUrl("http://play.google.com/store/apps/details?id=" + this.getPackageName())
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        // Save Suggestion
                    }
                }).build();

        ratingDialog.show();
    }

    public void checkIfEasyLevelIsCompleted() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/completed/check", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                easyBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("quizCompleted")){
                        easyBtn.setEnabled(false);
                        easyBtn.setText(getString(R.string.java_easy_btn));
                        easyBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.completed, 0, 0, 0);
                        easyBtn.setTextSize(18);
                    } else {
                        easyBtn.setEnabled(true);
                        easyBtn.setClickable(true);
                        easyBtn.setFocusable(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("player_id", userId);
                params.put("category_id", categoryId);
                params.put("category_level", "easy");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    public void checkIfMediumLevelIsCompleted() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/completed/check", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mediumBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("quizCompleted")){
                        mediumBtn.setEnabled(false);
                        mediumBtn.setText(getString(R.string.java_medium_btn));
                        mediumBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.completed, 0, 0, 0);
                        mediumBtn.setTextSize(18);
                    } else {
                        mediumBtn.setEnabled(true);
                        mediumBtn.setClickable(true);
                        mediumBtn.setFocusable(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("player_id", userId);
                params.put("category_id", categoryId);
                params.put("category_level", "medium");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    public void checkIfHardLevelIsCompleted() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/completed/check", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hardBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("quizCompleted")){
                        hardBtn.setEnabled(false);
                        hardBtn.setText(getString(R.string.java_hard_btn));
                        hardBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.completed, 0, 0, 0);
                        hardBtn.setTextSize(18);
                    } else {
                        hardBtn.setEnabled(true);
                        hardBtn.setClickable(true);
                        hardBtn.setFocusable(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("player_id", userId);
                params.put("category_id", categoryId);
                params.put("category_level", "hard");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    public void checkIfExpertLevelIsCompleted() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/completed/check", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                expertBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("quizCompleted")){
                        expertBtn.setEnabled(false);
                        expertBtn.setText(getString(R.string.java_expert_btn));
                        expertBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.completed, 0, 0, 0);
                        expertBtn.setTextSize(18);
                    } else {
                        expertBtn.setEnabled(true);
                        expertBtn.setClickable(true);
                        expertBtn.setFocusable(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("player_id", userId);
                params.put("category_id", categoryId);
                params.put("category_level", "expert");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void checkifEasyLevelContainsQuestions() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/level/check/questions", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                easyBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("questionsExists")){
                        checkIfEasyLevelIsCompleted();
                        // On Click Listeners (Levels)
                        easyBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.domain_name)+"/api/categories/" + categoryId + "/questions/easy";
                                Intent questionIntent = new Intent(CategoryActivity.this, QuestionActivity.class);
                                questionIntent.putExtra("url", url);
                                questionIntent.putExtra("categoryId", String.valueOf(categoryId));
                                questionIntent.putExtra("categoryLevel", "easy");
                                questionIntent.putExtra("categoryName", catName.getText().toString());
                                questionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(questionIntent);
                                finish();
                            }
                        });
                    }
                    if (success.equals("noQuestions")) {
                        easyBtn.setEnabled(false);
                        easyBtn.setText(getString(R.string.java_easy_no_question));
                        easyBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.empty_stats, 0, 0, 0);
                        easyBtn.setTextSize(18);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId);
                params.put("level", "easy");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void checkifMediumLevelContainsQuestions() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/level/check/questions", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mediumBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("questionsExists")){
                        checkIfMediumLevelIsCompleted();
                        mediumBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.domain_name)+"/api/categories/" + categoryId + "/questions/medium";
                                Intent questionIntent = new Intent(CategoryActivity.this, QuestionActivity.class);
                                questionIntent.putExtra("url", url);
                                questionIntent.putExtra("categoryId", String.valueOf(categoryId));
                                questionIntent.putExtra("categoryName", catName.getText().toString());
                                questionIntent.putExtra("categoryLevel", "medium");
                                questionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(questionIntent);
                                finish();
                            }
                        });
                    }
                    if (success.equals("noQuestions")){
                        mediumBtn.setEnabled(false);
                        mediumBtn.setText(getString(R.string.java_medium_no_question));
                        mediumBtn.setTextSize(18);
                        mediumBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.empty_stats, 0, 0, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId);
                params.put("level", "medium");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void checkifHardLevelContainsQuestions() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/level/check/questions", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hardBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("questionsExists")){
                        checkIfHardLevelIsCompleted();
                        hardBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.domain_name)+"/api/categories/" + categoryId + "/questions/hard";
                                Intent questionIntent = new Intent(CategoryActivity.this, QuestionActivity.class);
                                questionIntent.putExtra("url", url);
                                questionIntent.putExtra("categoryId", String.valueOf(categoryId));
                                questionIntent.putExtra("categoryLevel", "hard");
                                questionIntent.putExtra("categoryName", catName.getText().toString());
                                questionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(questionIntent);
                                finish();
                            }
                        });
                    }
                    if (success.equals("noQuestions")) {
                        hardBtn.setEnabled(false);
                        hardBtn.setText(getString(R.string.java_hard_no_question));
                        hardBtn.setTextSize(18);
                        hardBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.empty_stats, 0, 0, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId);
                params.put("level", "hard");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void checkifExpertLevelContainsQuestions() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/level/check/questions", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                expertBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("questionsExists")){
                        checkIfExpertLevelIsCompleted();
                        expertBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.domain_name)+"/api/categories/" + categoryId + "/questions/expert";
                                Intent questionIntent = new Intent(CategoryActivity.this, QuestionActivity.class);
                                questionIntent.putExtra("url", url);
                                questionIntent.putExtra("categoryId", String.valueOf(categoryId));
                                questionIntent.putExtra("categoryLevel", "expert");
                                questionIntent.putExtra("categoryName", catName.getText().toString());
                                questionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(questionIntent);
                                finish();
                            }
                        });
                    } if (success.equals("noQuestions")) {
                        expertBtn.setEnabled(false);
                        expertBtn.setText(getString(R.string.java_expert_no_question));
                        expertBtn.setTextSize(18);
                        expertBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.empty_stats, 0, 0, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId);
                params.put("level", "expert");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void checkifEasyContainsQuestions() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/level/check/questions", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                easyBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("questionsExists")){
                        // On Click Listeners (Levels)
                        easyBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.domain_name)+"/api/categories/" + categoryId + "/questions/easy";
                                Intent questionIntent = new Intent(CategoryActivity.this, QuestionActivity.class);
                                questionIntent.putExtra("url", url);
                                questionIntent.putExtra("categoryId", String.valueOf(categoryId));
                                questionIntent.putExtra("categoryLevel", "easy");
                                questionIntent.putExtra("categoryName", catName.getText().toString());
                                questionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(questionIntent);
                                finish();
                            }
                        });
                    }
                    if (success.equals("noQuestions")) {
                        easyBtn.setEnabled(false);
                        easyBtn.setText(getString(R.string.java_easy_no_question));
                        easyBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.empty_stats, 0, 0, 0);
                        easyBtn.setTextSize(18);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId);
                params.put("level", "easy");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void checkifMediumContainsQuestions() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/level/check/questions", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mediumBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("questionsExists")){
                        mediumBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.domain_name)+"/api/categories/" + categoryId + "/questions/medium";
                                Intent questionIntent = new Intent(CategoryActivity.this, QuestionActivity.class);
                                questionIntent.putExtra("url", url);
                                questionIntent.putExtra("categoryId", String.valueOf(categoryId));
                                questionIntent.putExtra("categoryName", catName.getText().toString());
                                questionIntent.putExtra("categoryLevel", "medium");
                                questionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(questionIntent);
                                finish();
                            }
                        });
                    }
                    if (success.equals("noQuestions")){
                        mediumBtn.setEnabled(false);
                        mediumBtn.setText(getString(R.string.java_medium_no_question));
                        mediumBtn.setTextSize(18);
                        mediumBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.empty_stats, 0, 0, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId);
                params.put("level", "medium");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void checkifHardContainsQuestions() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/level/check/questions", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hardBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("questionsExists")){
                        hardBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.domain_name)+"/api/categories/" + categoryId + "/questions/hard";
                                Intent questionIntent = new Intent(CategoryActivity.this, QuestionActivity.class);
                                questionIntent.putExtra("url", url);
                                questionIntent.putExtra("categoryId", String.valueOf(categoryId));
                                questionIntent.putExtra("categoryLevel", "hard");
                                questionIntent.putExtra("categoryName", catName.getText().toString());
                                questionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(questionIntent);
                                finish();
                            }
                        });
                    }
                    if (success.equals("noQuestions")) {
                        hardBtn.setEnabled(false);
                        hardBtn.setText(getString(R.string.java_hard_no_question));
                        hardBtn.setTextSize(18);
                        hardBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.empty_stats, 0, 0, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId);
                params.put("level", "hard");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void checkifExpertContainsQuestions() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/level/check/questions", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                expertBtn.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("questionsExists")){
                        expertBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.domain_name)+"/api/categories/" + categoryId + "/questions/expert";
                                Intent questionIntent = new Intent(CategoryActivity.this, QuestionActivity.class);
                                questionIntent.putExtra("url", url);
                                questionIntent.putExtra("categoryId", String.valueOf(categoryId));
                                questionIntent.putExtra("categoryLevel", "expert");
                                questionIntent.putExtra("categoryName", catName.getText().toString());
                                questionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(questionIntent);
                                finish();
                            }
                        });
                    } if (success.equals("noQuestions")) {
                        expertBtn.setEnabled(false);
                        expertBtn.setText(getString(R.string.java_expert_no_question));
                        expertBtn.setTextSize(18);
                        expertBtn.setCompoundDrawablesWithIntrinsicBounds( R.drawable.empty_stats, 0, 0, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("category_id", categoryId);
                params.put("level", "expert");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }
}
