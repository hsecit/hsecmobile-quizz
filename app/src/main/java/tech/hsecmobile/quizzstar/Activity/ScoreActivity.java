package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
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
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import tech.hsecmobile.quizzstar.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScoreActivity extends AppCompatActivity {
    private TextView rightAnswers, falseAnswers, wastedPoints, earnedPoints, mPercentage, scoreTitle;
    private AdView bannerAdView;
    private LinearLayout adsLinear;
    private RequestQueue queue;
    private SharedPreferences facebookInterstitial, admobInterstitial, userSituationId, facebookBanner, admobBanner, completedOption;
    private String userId, categoryId, categoryLevel, categoryName,passedTotalPoints;
    private Button retryBtn, shareScore, goToHome;
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
        setContentView(R.layout.activity_score);
        Toolbar toolbar = findViewById(R.id.score_toolbar);
        toolbar.setTitle("Quiz Score");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        facebookBanner = getSharedPreferences("facebookBanner", Context.MODE_PRIVATE);
        admobBanner = getSharedPreferences("admobBanner", MODE_PRIVATE);
        completedOption = getSharedPreferences("completedOption", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);
        adsLinear = (LinearLayout) findViewById(R.id.score_ads_linear);
        bannerAdView = new AdView(this);
        bannerAdView.setAdUnitId(admobBanner.getString("admobBanner", null));
        bannerAdView.setAdSize(AdSize.BANNER);
        adsLinear.addView(bannerAdView);
        adsLinear.setGravity(Gravity.CENTER_HORIZONTAL);
        adsLinear.setVisibility(View.GONE);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
        bannerAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                adsLinear.setVisibility(View.VISIBLE);
            }
        });
        // Get Player Id
        userSituationId = getSharedPreferences("userId",MODE_PRIVATE);
        userId = userSituationId.getString("userId", "");
        rightAnswers = (TextView) findViewById(R.id.right_answers_number);
        falseAnswers = (TextView) findViewById(R.id.false_answers_number);
        wastedPoints = (TextView) findViewById(R.id.wasted_points);
        earnedPoints = (TextView) findViewById(R.id.earned_points);
        mPercentage = (TextView) findViewById(R.id.percentage);
        scoreTitle = (TextView) findViewById(R.id.score_title);
        retryBtn = (Button) findViewById(R.id.retry_again);
        shareScore = (Button) findViewById(R.id.share_score);
        goToHome = (Button) findViewById(R.id.go_home);
        Intent intent = getIntent();
        String passedEarnedPoints = intent.getStringExtra("earnedPoints");
        passedTotalPoints = intent.getStringExtra("totalPoints");
        categoryId = intent.getStringExtra("categoryId");
        categoryLevel = intent.getStringExtra("categoryLevel");
        categoryName = intent.getStringExtra("categoryName");
        int earnedPointsByPlayer = Integer.parseInt(passedEarnedPoints);
        int totalquizPoints = Integer.parseInt(passedTotalPoints);
        int wastedPointsByPlayer = totalquizPoints - earnedPointsByPlayer;
        earnedPoints.setText(passedEarnedPoints);
        wastedPoints.setText(String.valueOf(wastedPointsByPlayer));
        String allQuestionsNumber = intent.getStringExtra("allQuestions");
        String trueAnswers = intent.getStringExtra("trueAnswers");
        int numberOfTrueAnswers = Integer.parseInt(trueAnswers);
        int numberTotalOfQuestions = Integer.parseInt(allQuestionsNumber);
        int falseAnswersNumber = numberTotalOfQuestions - numberOfTrueAnswers;
        rightAnswers.setText(trueAnswers);
        falseAnswers.setText(String.valueOf(falseAnswersNumber));
        final int per = ((numberOfTrueAnswers*100)/numberTotalOfQuestions);
        mPercentage.setText(String.valueOf(per) + "%");

        if (per>49) {
            updatePlayerPoints();
            mPercentage.setTextColor(Color.GREEN);
            scoreTitle.setText(getString(R.string.java_question_score_success));
            shareScore.setVisibility(View.VISIBLE);
            if(completedOption.getString("completedOption", null).equals("yes")) {
                retryBtn.setVisibility(View.GONE);
            } else {
                retryBtn.setVisibility(View.VISIBLE);
            }
        } else {
            mPercentage.setTextColor(Color.RED);
            scoreTitle.setText(getString(R.string.java_question_score_failed));
            retryBtn.setVisibility(View.VISIBLE);
        }
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsIntent = new Intent(ScoreActivity.this, CategoryActivity.class);
                detailsIntent.putExtra("categoryId", categoryId);
                detailsIntent.putExtra("categoryName", categoryName);
                detailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(detailsIntent);
                finish();
            }
        });
        goToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsIntent = new Intent(ScoreActivity.this, MainActivity.class);
                detailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(detailsIntent);
                finish();
            }
        });
        shareScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, "I made "+ mPercentage.getText().toString() +" of true Answers in " + getResources().getString(R.string.app_name) + " http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()+"\nDownload it & come to challenge me!");
                startActivity(Intent.createChooser(intent, "Share Score"));
            }
        });
        facebookBanner = getSharedPreferences("facebookBanner", Context.MODE_PRIVATE);
        // Facebook Audience Network
        AudienceNetworkAds.initialize(this);

        com.facebook.ads.AdView facebookAdView = new com.facebook.ads.AdView(this, facebookBanner.getString("facebookBanner", null), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container_score_activity);
        adContainer.addView(facebookAdView);
        facebookAdView.loadAd();
        admobInterstitial = getSharedPreferences("admobInterstitial", MODE_PRIVATE);
        MobileAds.initialize(ScoreActivity.this, getString(R.string.admob_app_id));
        // Admob Interstitial Ads
        prepareInterstitialAdmobAd();
        ScheduledExecutorService schudeler = Executors.newSingleThreadScheduledExecutor();
        schudeler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        }
                    }
                });
            }
        }, 2, 1, TimeUnit.SECONDS);
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
        }, 5, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(ScoreActivity.this, MainActivity.class);
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
    private void prepareInterstitialAdmobAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(admobInterstitial.getString("admobInterstitial", ""));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    // Change Player Points
    public void updatePlayerPoints() {
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/players/"+ userId +"/update", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Make quiz Completed
                if(completedOption.getString("completedOption", "").equals("yes")) {
                    makeQuizCompleted();
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
                params.put("points", earnedPoints.getText().toString());
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

    // Make Quiz Completed
    public void makeQuizCompleted(){
        String updateUrl = getResources().getString(R.string.domain_name);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateUrl+"/api/quiz/passed/update", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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
                params.put("category_level", categoryLevel);
                params.put("total_quiz_points", passedTotalPoints);
                params.put("points", earnedPoints.getText().toString());
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

    @Override
    public boolean onSupportNavigateUp() {
        Intent main = new Intent(ScoreActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        return true;
    }
}
