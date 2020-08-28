package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EarningsActivity extends AppCompatActivity {
    private TextView avalaibleBalance, avalaiblePoints, minWithdraw, todayDate, referralCode, clickToCopy;
    private String url,spUserEmail, currency;
    private Button requestWithdrawalsBtn, withdrawalsHistory;
    private int userPointsInt;
    private SharedPreferences admobBanner,userSituation,facebookBanner, bottomBannerType, facebookInterstitial, admobInterstitial;
    private AdView bannerAdView;
    private LinearLayout adsLinear;
    private RequestQueue queue;
    private String bannerBottomType;
    private com.facebook.ads.InterstitialAd facebookInterstitialAd;
    private InterstitialAd mInterstitialAd;
    private AdView bannerAdmobAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earnings);
        Toolbar toolbar = findViewById(R.id.toolbar_earnings);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        queue = Volley.newRequestQueue(this);
        admobBanner = getSharedPreferences("bannerID", MODE_PRIVATE);
        userSituation = getSharedPreferences("userEmail",MODE_PRIVATE);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        adsLinear = (LinearLayout) findViewById(R.id.earnings_ads_linear);
        bannerAdView = new AdView(this);
        bannerAdView.setAdUnitId(admobBanner.getString("admobBanner", ""));
        Log.e("admob", admobBanner.getString("admobBanner", ""));
        bannerAdView.setAdSize(AdSize.BANNER);
        adsLinear.addView(bannerAdView);
        adsLinear.setGravity(Gravity.CENTER_HORIZONTAL);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
        bannerAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                adsLinear.setVisibility(View.VISIBLE);
            }
        });
        avalaibleBalance = (TextView) findViewById(R.id.avalaible_balance);
        avalaiblePoints = (TextView) findViewById(R.id.avalaible_points);
        minWithdraw = (TextView) findViewById(R.id.min_points_to_withdraw);
        todayDate = (TextView) findViewById(R.id.today_date);
        referralCode = (TextView) findViewById(R.id.referral_code);
        requestWithdrawalsBtn = (Button) findViewById(R.id.request_withdrawals_btn);
        withdrawalsHistory = (Button) findViewById(R.id.withdrawals_history_btn);
        clickToCopy = (TextView) findViewById(R.id.click_here_to_copy);
        url = getResources().getString(R.string.domain_name);
        spUserEmail = userSituation.getString("userEmail", "");
        withdrawalsHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent history = new Intent(EarningsActivity.this, WithdrawalsHistoryActivity.class);
                history.putExtra("userEmail", spUserEmail);
                startActivity(history);
                finish();
            }
        });
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        todayDate.setText(formatter.format(date));
        getConnectedUserData();
        // Admob Banner Bottom
        bottomBannerType = getSharedPreferences("bottomBannerType",MODE_PRIVATE);
        bannerBottomType = bottomBannerType.getString("bottomBannerType", "");
        if (bannerBottomType.equals("admob")) {
            MobileAds.initialize(EarningsActivity.this, getString(R.string.admob_app_id));
            admobBanner = getSharedPreferences("admobBanner",MODE_PRIVATE);
            adsLinear = (LinearLayout) findViewById(R.id.banner_container_earnings_activity);
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
            LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container_earnings_activity);
            adContainer.addView(facebookAdView);
            facebookAdView.loadAd();
        }
        admobInterstitial = getSharedPreferences("admobInterstitial", MODE_PRIVATE);
        MobileAds.initialize(EarningsActivity.this, getString(R.string.admob_app_id));
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

    @Override
    public void onBackPressed() {
        Intent main = new Intent(EarningsActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        super.onBackPressed();
    }

    private void getSettings() {
        String url = getResources().getString(R.string.domain_name)+"/api/settings/all";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject settings = jsonArray.getJSONObject(i);
                                currency = settings.getString("currency");
                                final int min_to_withdraw = settings.getInt("min_to_withdraw");
                                double conversionRate = settings.getInt("conversion_rate");
                                double user_points = Integer.parseInt(avalaiblePoints.getText().toString());
                                final double balance = (double) (user_points/conversionRate);
                                avalaibleBalance.setText(String.format("%.2f", balance) + " "+currency);
                                minWithdraw.setText(getString(R.string.java_min_withdraw)+ String.valueOf(min_to_withdraw) + " "+currency);
                                if (min_to_withdraw>balance) {
                                    requestWithdrawalsBtn.setEnabled(true);
                                    requestWithdrawalsBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(EarningsActivity.this, "Minimum Withdrawal is "+String.valueOf(min_to_withdraw) + " "+currency, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    requestWithdrawalsBtn.setEnabled(true);
                                    requestWithdrawalsBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent requestWithdrawal = new Intent(EarningsActivity.this, RequestWithdrawalActivity.class);
                                            requestWithdrawal.putExtra("amount", String.format("%.2f", balance));
                                            requestWithdrawal.putExtra("points", avalaiblePoints.getText().toString());
                                            startActivity(requestWithdrawal);
                                            finish();
                                        }
                                    });
                                }
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
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void getConnectedUserData() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/getplayerdata", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    userPointsInt = jsonObject.getInt("score");
                    String userPoints = String.valueOf(userPointsInt);
                    String referral = jsonObject.getString("referral_code");
                    referralCode.setText(referral);
                    clickToCopy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("ReferralCode", referralCode.getText().toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(EarningsActivity.this, "Code Copied.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Set Header User Infos
                    avalaiblePoints.setText(userPoints);
                    getSettings();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Intent main = new Intent(EarningsActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        return true;
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
