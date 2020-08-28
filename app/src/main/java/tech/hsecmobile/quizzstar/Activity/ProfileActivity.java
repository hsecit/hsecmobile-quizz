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
import com.facebook.ads.AdSize;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.squareup.picasso.Picasso;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView name, email, memberSince, points;
    private CircleImageView profileImage;
    private Button earnings, withdrawals, leaderboard, statistics, logout;
    private RequestQueue queue;
    private LinearLayout adsLinear;
    private String avatarLink;
    GoogleSignInClient mGoogleSignInClient;
    SharedPreferences userSituation, admobBanner, facebookBanner, facebookInterstitial, admobInterstitial;
    public String spUserEmail, url;
    private AdView bannerAdmobAdView;
    private Button editProfile;
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd facebookInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        toolbar.setTitle(getString(R.string.drawer_menu_profile));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        admobBanner = getSharedPreferences("admobBanner", MODE_PRIVATE);
        userSituation = getSharedPreferences("userEmail",MODE_PRIVATE);
        spUserEmail = userSituation.getString("userEmail", "");
        name = (TextView) findViewById(R.id.profile_name);
        email = (TextView) findViewById(R.id.profile_email);
        memberSince = (TextView) findViewById(R.id.member_since);
        points = (TextView) findViewById(R.id.profile_points);
        profileImage = (CircleImageView) findViewById(R.id.profile_image);
        earnings = (Button) findViewById(R.id.earnings_btn);
        withdrawals = (Button) findViewById(R.id.withdrawals_btn);
        leaderboard = (Button) findViewById(R.id.leaderboard_btn);
        statistics = (Button) findViewById(R.id.statistics_btn);
        logout = (Button) findViewById(R.id.profile_logout);
        earnings = (Button) findViewById(R.id.earnings_btn);
        withdrawals = (Button) findViewById(R.id.withdrawals_btn);
        editProfile = (Button) findViewById(R.id.edit_profile);
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        adsLinear = (LinearLayout) findViewById(R.id.ads_linear_profile);
        bannerAdmobAdView = new AdView(this);
        bannerAdmobAdView.setAdUnitId(admobBanner.getString("admobBanner", ""));
        bannerAdmobAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        adsLinear.addView(bannerAdmobAdView);
        adsLinear.setGravity(Gravity.CENTER_HORIZONTAL);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdmobAdView.loadAd(adRequest);
        bannerAdmobAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                adsLinear.setVisibility(View.VISIBLE);
            }
        });
        url = getString(R.string.domain_name);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change Shared Preferences
                userSituation.edit().putString("userEmail", "").apply();
                // Logout Google
                if (mGoogleSignInClient != null) {
                    mGoogleSignInClient.signOut();
                }
                // Logout Facebook
                if (LoginManager.getInstance() != null) {
                    LoginManager.getInstance().logOut();
                }
                // Go To Login Page
                Intent loginPage = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(loginPage);
                finish();
            }
        });
        // Go To Earnings Activity
        earnings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoEarnings = new Intent(ProfileActivity.this, EarningsActivity.class);
                startActivity(gotoEarnings);
                finish();
            }
        });
        // Go To Statistics
        statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotostat = new Intent(ProfileActivity.this, StatisticsActivity.class);
                startActivity(gotostat);
                finish();
            }
        });
        withdrawals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent history = new Intent(ProfileActivity.this, WithdrawalsHistoryActivity.class);
                history.putExtra("userEmail", spUserEmail);
                startActivity(history);
                finish();
            }
        });
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lead = new Intent(ProfileActivity.this, LeaderboardsActivity.class);
                startActivity(lead);
                finish();
            }
        });
        getConnectedUserData();
        // Facebook Ads
        facebookBanner = getSharedPreferences("facebookBanner", Context.MODE_PRIVATE);
        AudienceNetworkAds.initialize(this);

        com.facebook.ads.AdView facebookAdView = new com.facebook.ads.AdView(this, facebookBanner.getString("facebookBanner", ""), AdSize.BANNER_HEIGHT_50);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container_profile_activity);
        adContainer.addView(facebookAdView);
        facebookAdView.loadAd();
        admobInterstitial = getSharedPreferences("admobInterstitial", MODE_PRIVATE);
        MobileAds.initialize(ProfileActivity.this, getString(R.string.admob_app_id));
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
        }, 5, 1, TimeUnit.SECONDS);
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
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent edit = new Intent(ProfileActivity.this, EditProfileActivity.class);
                edit.putExtra("username", name.getText().toString());
                edit.putExtra("avatar", avatarLink);
                startActivity(edit);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(ProfileActivity.this, MainActivity.class);
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

    private void getConnectedUserData() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/getplayerdata", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String userName = jsonObject.getString("name");
                    String userEmail = jsonObject.getString("email");
                    int apoints = jsonObject.getInt("score");
                    String userPoints = String.valueOf(apoints);
                    String userImageUrl = jsonObject.getString("image");
                    String userMemberSince = jsonObject.getString("member_since");
                    // Set Header User Infos
                    name.setText(userName);
                    email.setText("Email : "+userEmail);
                    memberSince.setText("Member since : "+userMemberSince);
                    points.setText("Collected Points : "+userPoints);
                    avatarLink = userImageUrl;
                    Picasso.get().load(userImageUrl).fit().centerInside().into(profileImage);
                } catch(JSONException e){
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
                params.put("email", spUserEmail);
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
    public boolean onSupportNavigateUp() {
        Intent main = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(main);
        finish();
        return true;
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

}

