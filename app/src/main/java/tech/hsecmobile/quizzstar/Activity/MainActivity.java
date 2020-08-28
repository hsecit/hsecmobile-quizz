package tech.hsecmobile.quizzstar.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import tech.hsecmobile.quizzstar.Adapter.CategoriesAdapter;
import tech.hsecmobile.quizzstar.Adapter.PlayersAdapter;
import tech.hsecmobile.quizzstar.Manager.MyApplication;
import tech.hsecmobile.quizzstar.Model.Category;
import tech.hsecmobile.quizzstar.Model.Player;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import tech.hsecmobile.quizzstar.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private InterstitialAd mInterstitialAd;
    private SharedPreferences fiftyFiftyOption, rewardVideoOption, questionTime , completedOption, admobInterstitial, facebookBanner, bottomBannerType, admobBanner, admobNative;
    private AdView bannerAdmobAdView;
    private LinearLayout adsLinear;
    private String bannerBottomType;
    private DrawerLayout drawer;
    private TextView currentUserName, currentEmail;
    private CircleImageView currentProfileImage;
    public String spUserEmail, userId, userName, userEmail, userImageUrl;
    SharedPreferences userSituationId, userSituation;
    GoogleSignInClient mGoogleSignInClient;
    private String url;
    ActionBarDrawerToggle toggle;
    private RequestQueue queue;
    private PlayersAdapter playersAdapter;
    private ArrayList<Player> playersArrayList;
    private CategoriesAdapter categoriesAdapter;
    private ArrayList<Category> categoriesArrayList;
    private String id, name;
    private UnifiedNativeAd nativeAd;
    private UnifiedNativeAdView adViewNative;
    private FrameLayout frameLayout;
    MyApplication mMyApplication;
    private SharedPreferences facebookInterstitial;
    private com.facebook.ads.InterstitialAd facebookInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMyApplication = MyApplication.getmInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        url = getResources().getString(R.string.domain_name);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        View header = navigationView.getHeaderView(0);
        Button logout = (Button) header.findViewById(R.id.logout);
        fiftyFiftyOption = getSharedPreferences("fiftyFiftyOption", Context.MODE_PRIVATE);
        rewardVideoOption = getSharedPreferences("rewardVideoOption", Context.MODE_PRIVATE);
        completedOption = getSharedPreferences("completedOption", Context.MODE_PRIVATE);
        questionTime = getSharedPreferences("seconds", MODE_PRIVATE);
        userSituationId = getSharedPreferences("userId",MODE_PRIVATE);
        userSituation = getSharedPreferences("userEmail",MODE_PRIVATE);
        admobNative = getSharedPreferences("admobNative",MODE_PRIVATE);
        currentUserName = (TextView) header.findViewById(R.id.current_user_name);
        currentEmail = (TextView) header.findViewById(R.id.current_user_email);
        currentProfileImage = (CircleImageView) header.findViewById(R.id.profile_image_header);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change Shared Preferences
                userSituation.edit().putString("userEmail", "").apply();
                userSituationId.edit().putString("userId", "").apply();
                // Logout Google
                if (mGoogleSignInClient != null) {
                    mGoogleSignInClient.signOut();
                }
                // Logout Facebook
                if (LoginManager.getInstance() != null) {
                    LoginManager.getInstance().logOut();
                }
                // Go To Login Page
                Intent loginPage = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginPage);
                finish();
            }
        });
        // Connected Player DATA
        spUserEmail = userSituation.getString("userEmail", null);
        currentUserName = (TextView) header.findViewById(R.id.current_user_name);
        currentEmail = (TextView) header.findViewById(R.id.current_user_email);
        currentProfileImage = (CircleImageView) header.findViewById(R.id.profile_image_header);
        getConnectedUserData();
        queue = Volley.newRequestQueue(this);
        Button viewAllCategories = (Button) findViewById(R.id.view_all);
        Button viewAllPlayers = (Button) findViewById(R.id.view_all_2);
        // Get Top Players
        RecyclerView playersRecyclerView = (RecyclerView) findViewById(R.id.top_10_player_recycler);
        playersArrayList = new ArrayList<>();
        playersAdapter = new PlayersAdapter(this, playersArrayList);
        playersRecyclerView.setAdapter(playersAdapter);
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        getTopPlayers();
        viewAllPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lead = new Intent(MainActivity.this, LeaderboardsActivity.class);
                startActivity(lead);
                finish();
            }
        });
        // Get Featured Categories
        RecyclerView categoriesRecyclerView = (RecyclerView) findViewById(R.id.featured_categories_recycler);
        categoriesArrayList = new ArrayList<>();
        categoriesAdapter = new CategoriesAdapter(this, categoriesArrayList);
        categoriesRecyclerView.setAdapter(categoriesAdapter);
        NestedScrollView categoriesScroll = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        categoriesScroll.fullScroll(View.FOCUS_DOWN);
        categoriesScroll.setSmoothScrollingEnabled(false);
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        getFeaturedCategories();
        // Single Item Click Listener
        viewAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cat = new Intent(MainActivity.this, CategoriesActivity.class);
                startActivity(cat);
                finish(); }
        });
        categoriesAdapter.setOnItemClickListener(new CategoriesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                id = categoriesArrayList.get(position).getId();
                name = categoriesArrayList.get(position).getTitle();
                Intent detailsIntent = new Intent(MainActivity.this, CategoryActivity.class);
                detailsIntent.putExtra("categoryId", id);
                detailsIntent.putExtra("categoryName", name);
                startActivity(detailsIntent);
                finish();
            }
        });
        admobInterstitial = getSharedPreferences("admobInterstitial", MODE_PRIVATE);
        MobileAds.initialize(MainActivity.this, getString(R.string.admob_app_id));
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
        }, 3, 1, TimeUnit.SECONDS);
        // Native Admob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        adViewNative = (UnifiedNativeAdView) this.getLayoutInflater()
                .inflate(R.layout.admob_native_ad_unified, null);
        frameLayout = findViewById(R.id.native_ad_home);
        refreshAd();
        // Admob Banner Bottom
        bottomBannerType = getSharedPreferences("bottomBannerType",MODE_PRIVATE);
        bannerBottomType = bottomBannerType.getString("bottomBannerType", "");
        if (bannerBottomType.equals("admob")) {
            MobileAds.initialize(MainActivity.this, getString(R.string.admob_app_id));
            admobBanner = getSharedPreferences("admobBanner",MODE_PRIVATE);
            adsLinear = (LinearLayout) findViewById(R.id.banner_container_main_activity);
            bannerAdmobAdView = new AdView(this);
            bannerAdmobAdView.setAdUnitId(admobBanner.getString("admobBanner", ""));
            bannerAdmobAdView.setAdSize(AdSize.FULL_BANNER);
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
        } else if(bannerBottomType.equals("facebook")) {
            facebookBanner = getSharedPreferences("facebookBanner",MODE_PRIVATE);
            AudienceNetworkAds.initialize(this);

            com.facebook.ads.AdView facebookAdView = new com.facebook.ads.AdView(this, facebookBanner.getString("facebookBanner", null), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container_main_activity);
            adContainer.addView(facebookAdView);
            facebookAdView.loadAd();
        }
        getQuestionTimeAndCompletedOption();
    }

    private void prepareInterstitialAd() {
        AudienceNetworkAds.initialize(this);

        facebookInterstitialAd = new com.facebook.ads.InterstitialAd(this, facebookInterstitial.getString("facebookInterstitial", ""));
        facebookInterstitialAd.loadAd();
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
        VideoController vc = nativeAd.getVideoController();
        if (vc.hasVideoContent()) {
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    super.onVideoEnd();
                }
            });
        }
    }

    private void refreshAd() {
        AdLoader.Builder builder = new AdLoader.Builder(this,admobNative.getString("admobNative", null) );
        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            // OnUnifiedNativeAdLoadedListener implementation.
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                populateUnifiedNativeAdView(unifiedNativeAd, adViewNative);
                frameLayout.removeAllViews();
                frameLayout.addView(adViewNative);
            }

        });
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);
        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void getConnectedUserData() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/getplayerdata", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    userName = jsonObject.getString("name");
                    userEmail = jsonObject.getString("email");
                    userId = String.valueOf(jsonObject.getInt("id"));
                    userImageUrl = jsonObject.getString("image");
                    // Register User ID In Shared Prefs
                    userSituationId.edit().putString("userId", userId).apply();
                    userSituation.edit().putString("userEmail", userEmail).apply();
                    // Set Header User Infos
                    currentUserName.setText(userName);
                    currentEmail.setText(userEmail);
                    Picasso.get().load(userImageUrl).fit().centerInside().into(currentProfileImage);
                } catch(JSONException e){
                    Log.e("Error ", e.getMessage());
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

    private void getTopPlayers() {
        String url = getResources().getString(R.string.domain_name)+"/api/players/top10";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject player = jsonArray.getJSONObject(i);
                                String name = player.getString("name");
                                String email = player.getString("email");
                                String imageUrl = player.getString("image");
                                String memberSince = player.getString("member_since");
                                int points = player.getInt("score");
                                playersArrayList.add(new Player(name, email, memberSince, imageUrl, points));
                            }
                            playersAdapter.notifyDataSetChanged();
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
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private void getFeaturedCategories() {
        String url = getResources().getString(R.string.domain_name)+"/api/categories/featured";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject category = jsonArray.getJSONObject(i);
                                String name = category.getString("name");
                                String imageUrl = category.getString("imageUrl");
                                String id = String.valueOf(category.getInt("id"));
                                categoriesArrayList.add(new Category(name, imageUrl,id));
                            }
                            categoriesAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.statistics :
                Intent stats = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(stats);
                finish();
                break;
            case R.id.instructions :
                Intent instr = new Intent(MainActivity.this, InstructionsActivity.class);
                startActivity(instr);
                finish();
                break;
            case R.id.categories :
                Intent cat = new Intent(MainActivity.this, CategoriesActivity.class);
                startActivity(cat);
                finish();
                break;
            case R.id.profile :
                Intent prof = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(prof);
                finish();
                break;
            case R.id.ranking :
                Intent leader = new Intent(MainActivity.this, LeaderboardsActivity.class);
                startActivity(leader);
                finish();
                break;
            case R.id.privacy :
                Intent priv = new Intent(MainActivity.this, PrivacyActivity.class);
                startActivity(priv);
                finish();
                break;
            case R.id.terms_of_use :
                Intent terms = new Intent(MainActivity.this, TermsOfUseActivity.class);
                startActivity(terms);
                finish();
                break;
            case R.id.invite_friends :
                Intent inviteIntent = new Intent(MainActivity.this, InviteFriendsActivity.class);
                startActivity(inviteIntent);
                finish();
                break;
            case R.id.report :
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Report a Bug");
                builder.setMessage("To report a bug or a problem in this application, please contact us via Email" +"\n\n Thank You!");
                builder.setPositiveButton("Send Email",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
                                emailSelectorIntent.setData(Uri.parse("mailto:"));
                                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email)});
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.drawer_menu_report_bug));
                                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                emailIntent.setSelector( emailSelectorIntent );
                                if( emailIntent.resolveActivity(getPackageManager()) != null )
                                    startActivity(emailIntent);
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.share :
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, "Download this APP From : http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                startActivity(Intent.createChooser(intent, "Share Now"));
                break;
            case R.id.contact_us :
                Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
                emailSelectorIntent.setData(Uri.parse("mailto:"));

                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email)});
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                emailIntent.setSelector( emailSelectorIntent );
                if( emailIntent.resolveActivity(getPackageManager()) != null )
                    startActivity(emailIntent);
                break;
            case R.id.rate :
                smartRating();
                break;
            case R.id.exit :
                finishAndRemoveTask();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
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
        if (toggle.onOptionsItemSelected(item)) {
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

    private void prepareInterstitialAdmobAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(admobInterstitial.getString("admobInterstitial", ""));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void getQuestionTimeAndCompletedOption() {
        String url = getResources().getString(R.string.domain_name)+"/api/settings/all";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject settings = jsonArray.getJSONObject(i);
                                final int question_time = settings.getInt("question_time");
                                final String completedOptionStr = settings.getString("completed_option");
                                final String fiftyFiftyOptionStr = settings.getString("fifty_fifty");
                                final String rewardVideoOptionStr = settings.getString("video_reward");
                                questionTime.edit().putInt("seconds", question_time).apply();
                                completedOption.edit().putString("completedOption", completedOptionStr).apply();
                                fiftyFiftyOption.edit().putString("fiftyFiftyOption", fiftyFiftyOptionStr).apply();
                                rewardVideoOption.edit().putString("rewardVideoOption", rewardVideoOptionStr).apply();
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
}