package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    private EditText loginEmail, loginPassword;
    private Button login;
    private ProgressBar progressBar;
    private String url,personImageUrl;
    private TextView apiLoginError;
    AwesomeValidation validator;
    SharedPreferences userSituation;
    CallbackManager callbackManager;
    private LoginButton loginButton;
    SignInButton signin;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signin = (SignInButton) findViewById(R.id.sign_in_button);
        TextView textView = (TextView) signin.getChildAt(0);
        textView.setText("Google");
        loginEmail = (EditText) findViewById(R.id.login_email);
        loginPassword = (EditText) findViewById(R.id.login_password);
        login = (Button) findViewById(R.id.login_button);
        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        Button myLoginButtonFb = (Button) findViewById(R.id.facebook_login_button_custom);
        myLoginButtonFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
        url = getResources().getString(R.string.domain_name);
        apiLoginError = (TextView) findViewById(R.id.api_login_error);
        userSituation = getSharedPreferences("userEmail",MODE_PRIVATE);
        validator = new AwesomeValidation(ValidationStyle.BASIC);
        TextView newMember = (TextView) findViewById(R.id.new_member);
        newMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerPage = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerPage);
                finish();
            }
        });
        setupRules();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validator.validate()) {
                    tryLogin();
                    validator.clear();
                }
            }
        });
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Retrieving access token using the LoginResult
                AccessToken accessToken = loginResult.getAccessToken();
                facebookLoginInformation(accessToken);
            }
            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }
            }
        });
        TextView forgotPassword = (TextView) findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reset = new Intent(LoginActivity.this, SendResetLinkActivity.class);
                startActivity(reset);
                finish();

            }
        });
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resulrCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resulrCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        super.onActivityResult(requestCode, resulrCode, data);
    }

    public static String random() {
        char[] chars1 = "ABCDEF012GHIJKL345MNOPQR678STUVWXYZ9".toCharArray();
        StringBuilder sb1 = new StringBuilder();
        Random random1 = new Random();
        for (int i = 0; i < 6; i++)
        {
            char c1 = chars1[random1.nextInt(chars1.length)];
            sb1.append(c1);
        }
        String generatedString = sb1.toString();
        return generatedString;
    }

    public void setupRules() {
        validator.addValidation(this, R.id.login_email, Patterns.EMAIL_ADDRESS, R.string.email_login_error);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            googleSignIn();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private void googleSignIn() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
        if (acct != null) {
            final String personName = acct.getDisplayName();
            final String personEmail = acct.getEmail();
            final Uri personPhoto = acct.getPhotoUrl();
            apiLoginError.setVisibility(View.GONE);
            apiLoginError.setText(null);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/new", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");
                        // No Errors
                        if (success.equals("1")){
                            // Change Shared Preferences
                            userSituation.edit().putString("userEmail", personEmail).apply();
                            // Message Success
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Registered Successfully!", Toast.LENGTH_LONG).show();
                            // Go To Home Page
                            Intent homePage = new Intent(LoginActivity.this, ReferralCodeActivity.class);
                            startActivity(homePage);
                            finish();
                        }
                        // Device Exist
                        else if (success.equals("deviceError")){
                            Toast.makeText(LoginActivity.this, "Only 1 account per device is allowed!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            login.setVisibility(View.VISIBLE);
                        }
                        else {
                            // Change Shared Preferences
                            userSituation.edit().putString("userEmail", personEmail).apply();
                            // Message Success
                            loginButton.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_LONG).show();
                            // Player already Exists , Go To Main Activity
                            Intent homePage = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(homePage);
                            finish();
                        }
                    } catch(JSONException e){
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                        login.setVisibility(View.VISIBLE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    login.setVisibility(View.VISIBLE);
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    int length = 10;
                    boolean useLetters = true;
                    boolean useNumbers = false;
                    params.put("name", personName);
                    params.put("email", personEmail);
                    params.put("password", random());
                    if (personPhoto!=null) {
                        final String personImageUrl = personPhoto.toString();
                        params.put("image_url", personImageUrl);
                    }
                    @SuppressLint("HardwareIds") final String device = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    params.put("device_id", device);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
    }

    private void facebookLoginInformation(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    // GET USER DATA
                    final String name = object.getString("name");
                    final String email = object.getString("email");
                    final String image = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    // New Player , Register Request & Go To Main Activity & Save Shared Prefs
                    apiLoginError.setVisibility(View.GONE);
                    apiLoginError.setText(null);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/new", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");
                                // No Errors
                                if (success.equals("1")){
                                    // Change Shared Preferences
                                    userSituation.edit().putString("userEmail", email).apply();
                                    // Message Success
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Registered Successfully!", Toast.LENGTH_LONG).show();
                                    // Go To Home Page
                                    Intent homePage = new Intent(LoginActivity.this, ReferralCodeActivity.class);
                                    startActivity(homePage);
                                    finish();
                                } else if (success.equals("deviceError")){
                                    Toast.makeText(LoginActivity.this, "Only 1 account per device is allowed!", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    login.setVisibility(View.VISIBLE);
                                }
                                else {
                                    // Change Shared Preferences
                                    userSituation.edit().putString("userEmail", email).apply();
                                    // Message Success
                                    loginButton.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_LONG).show();
                                    // Player already Exists , Go To Main Activity
                                    Intent homePage = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(homePage);
                                    finish();
                                }
                            } catch(JSONException e){
                                e.printStackTrace();
                                progressBar.setVisibility(View.GONE);
                                login.setVisibility(View.VISIBLE);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            progressBar.setVisibility(View.GONE);
                            login.setVisibility(View.VISIBLE);
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            int length = 10;
                            boolean useLetters = true;
                            boolean useNumbers = false;
                            params.put("name", name);
                            params.put("email", email);
                            params.put("password", random());
                            params.put("image_url", image);
                            @SuppressLint("HardwareIds") final String device = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID);
                            params.put("device_id", device);
                            return params;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                            10000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    private void tryLogin() {
        final String enteredEmail = this.loginEmail.getText().toString().trim();
        final String enteredPassword = this.loginPassword.getText().toString().trim();
        apiLoginError.setVisibility(View.GONE);
        apiLoginError.setText(null);
        progressBar.setVisibility(View.VISIBLE);
        login.setVisibility(View.GONE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/login", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("loggedSuccess")){
                        // Change Shared Preferences
                        userSituation.edit().putString("userEmail", enteredEmail).apply();
                        // Message Success
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Logged In Successfully!", Toast.LENGTH_LONG).show();
                        // Go To Home Page
                        Intent homePage = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(homePage);
                        finish();
                    }
                    // Email Problem
                    if (success.equals("emailError")){
                        progressBar.setVisibility(View.GONE);
                        login.setVisibility(View.VISIBLE);
                        String message = jsonObject.getString("email_error");
                        apiLoginError.setVisibility(View.VISIBLE);
                        apiLoginError.setText("* "+message);
                    }
                    // Password Problem
                    if (success.equals("passwordError")){
                        progressBar.setVisibility(View.GONE);
                        login.setVisibility(View.VISIBLE);
                        String message = jsonObject.getString("password_error");
                        apiLoginError.setVisibility(View.VISIBLE);
                        apiLoginError.setText("* "+message);
                    }
                } catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Catch : "+e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    login.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(LoginActivity.this, "ERROR : "+error.getMessage(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                login.setVisibility(View.VISIBLE);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", enteredEmail);
                params.put("password", enteredPassword);
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
