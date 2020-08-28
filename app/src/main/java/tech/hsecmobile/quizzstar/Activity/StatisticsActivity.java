package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;
import tech.hsecmobile.quizzstar.Adapter.CompletedAdapter;
import tech.hsecmobile.quizzstar.Adapter.ReferralAdapter;
import tech.hsecmobile.quizzstar.Model.Completed;
import tech.hsecmobile.quizzstar.Model.Referral;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatisticsActivity extends AppCompatActivity {
    private RequestQueue queue;
    private ScrollView myscroll;
    private RecyclerView completedRecyclerView, referralsRecyclerView;
    private CompletedAdapter completedAdapter;
    private ReferralAdapter mReferralAdapter;
    private ArrayList<Completed> completedsArrayList;
    private ArrayList<Referral> mReferralArrayList;
    private String userId,spUserEmail;
    SharedPreferences userSituationId,userSituation;
    private TextView userName, score, completedQuiz, referralsCount, emptyReferredTv, emptyCompletedTv;
    private CircleImageView userImage;
    private ImageView emptyReferredImage, emptyCompletedImage;
    private SharedPreferences facebookNative;
    private NativeAdLayout nativeAdLayout;
    private NativeBannerAd nativeBannerAd;
    private LinearLayout adView;
    private com.facebook.ads.InterstitialAd facebookInterstitialAd;
    private InterstitialAd mInterstitialAd;
    private SharedPreferences facebookInterstitial, admobInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = findViewById(R.id.statistics_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        userSituation = getSharedPreferences("userEmail",MODE_PRIVATE);
        spUserEmail = userSituation.getString("userEmail", "");
        userSituationId = getSharedPreferences("userId",MODE_PRIVATE);
        userId = userSituationId.getString("userId", "");
        nativeAdLayout = findViewById(R.id.statistics_native_banner_ad_container);
        facebookNative = getSharedPreferences("facebookNative", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(this.getApplicationContext());
        emptyReferredTv = (TextView) findViewById(R.id.empty_referred_users);
        emptyReferredImage = (ImageView) findViewById(R.id.empty_referred_image);
        emptyCompletedTv = (TextView) findViewById(R.id.empty_completed_users);
        emptyCompletedImage = (ImageView) findViewById(R.id.empty_completed_image);
        myscroll = (ScrollView) findViewById(R.id.all_completed_scroll);
        myscroll.fullScroll(View.FOCUS_DOWN);
        myscroll.setSmoothScrollingEnabled(false);
        userName = (TextView) findViewById(R.id.statistics_username);
        referralsCount = (TextView) findViewById(R.id.referrals_number);
        score = (TextView) findViewById(R.id.current_score_stat);
        completedQuiz = (TextView) findViewById(R.id.completed_quiz);
        userImage = (CircleImageView) findViewById(R.id.statistics_image);
        // Completed RecyclerView
        completedRecyclerView = (RecyclerView) findViewById(R.id.completed_recycler);
        completedsArrayList = new ArrayList<>();
        completedAdapter = new CompletedAdapter(this, completedsArrayList);
        completedRecyclerView.setAdapter(completedAdapter);
        completedRecyclerView.setHasFixedSize(true);
        completedRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        // Referral RecyclerView
        referralsRecyclerView = (RecyclerView) findViewById(R.id.referred_recycler);
        mReferralArrayList = new ArrayList<>();
        mReferralAdapter = new ReferralAdapter(this, mReferralArrayList);
        referralsRecyclerView.setAdapter(mReferralAdapter);
        referralsRecyclerView.setHasFixedSize(true);
        referralsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        getCompletedQuizes();
        getConnectedUserData();
        getReferrals();
        // Facebook Ads
        AudienceNetworkAds.initialize(this);
        loadNativeAd();
        admobInterstitial = getSharedPreferences("admobInterstitial", MODE_PRIVATE);
        MobileAds.initialize(StatisticsActivity.this, getString(R.string.admob_app_id));
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

    @Override
    public void onBackPressed() {
        Intent main = new Intent(StatisticsActivity.this, MainActivity.class);
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

    private void getReferrals() {
        String url = getResources().getString(R.string.domain_name)+"/api/players/"+userId+"/refers";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject refer = jsonArray.getJSONObject(i);
                                String name = refer.getString("name");
                                String email = refer.getString("email");
                                String image_url = refer.getString("image_url");
                                String date = refer.getString("date");
                                mReferralArrayList.add(new Referral(name, email, image_url, date));
                            }
                            mReferralAdapter.notifyDataSetChanged();
                            referralsCount.setText("Referred Players : "+ String.valueOf(mReferralArrayList.size()));
                            if (mReferralArrayList.size()<1) {
                                emptyReferredTv.setVisibility(View.VISIBLE);
                                emptyReferredImage.setVisibility(View.VISIBLE);
                            } else {
                                emptyReferredTv.setVisibility(View.GONE);
                                emptyReferredImage.setVisibility(View.GONE);
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
        queue.add(request);
    }

    private void getCompletedQuizes() {
        String url = getResources().getString(R.string.domain_name)+"/api/players/"+userId+"/quiz/completed";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject completed = jsonArray.getJSONObject(i);
                                String categoryName = completed.getString("category");
                                String categoryLevel = completed.getString("level");
                                int totalPoints = completed.getInt("total_points");
                                int earnedPoints = completed.getInt("earned_points");
                                int wastedPoints = totalPoints - earnedPoints;
                                int percentage = completed.getInt("percentage");
                                completedsArrayList.add(new Completed(categoryName, categoryLevel, totalPoints, earnedPoints, wastedPoints, percentage));
                            }
                            completedAdapter.notifyDataSetChanged();
                            completedQuiz.setText("Completed Quiz : "+ String.valueOf(completedsArrayList.size()));
                            if (completedsArrayList.size()<1) {
                                emptyCompletedTv.setVisibility(View.VISIBLE);
                                emptyCompletedImage.setVisibility(View.VISIBLE);
                            } else {
                                emptyCompletedTv.setVisibility(View.GONE);
                                emptyCompletedImage.setVisibility(View.GONE);
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
        queue.add(request);
    }

    private void getConnectedUserData() {
        String url = getResources().getString(R.string.domain_name)+"/api/players/getplayerdata";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String name = jsonObject.getString("name");
                    int myScore = jsonObject.getInt("score");
                    String userImageUrl = jsonObject.getString("image");
                    // Set Header User Infos
                    userName.setText(name);
                    score.setText(getString(R.string.java_question_current_score)+ String.valueOf(myScore));
                    Picasso.get().load(userImageUrl).fit().centerInside().into(userImage);
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

    private void loadNativeAd() {
        // Instantiate a NativeAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        nativeBannerAd = new NativeBannerAd(this, facebookNative.getString("facebookNative", ""));
        nativeBannerAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e("facebook", adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Race condition, load() called again before last ad was displayed
                if (nativeBannerAd == null || nativeBannerAd != ad) {
                    return;
                }
                // Inflate Native Banner Ad into Container
                inflateAd(nativeBannerAd);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });
        // load the ad
        nativeBannerAd.loadAd();
    }

    private void inflateAd(NativeBannerAd nativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Add the Ad view into the ad container.
        // nativeAdLayout = getView().findViewById(R.id.statistics_native_banner_ad_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        adView = (LinearLayout) inflater.inflate(R.layout.facebook_native_banner_layout, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        // Add the AdChoices icon
        RelativeLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(this, nativeBannerAd, nativeAdLayout);
        adOptionsView.setIconSizeDp(23);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        AdIconView nativeAdIconView = adView.findViewById(R.id.native_icon_view);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
        nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Intent main = new Intent(StatisticsActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        return true;
    }
}

