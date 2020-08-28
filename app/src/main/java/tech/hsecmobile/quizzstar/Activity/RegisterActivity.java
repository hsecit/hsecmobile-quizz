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
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText name, email, password;
    private Button register;
    private ProgressBar mProgressBar;
    private String url;
    private TextView apiEmailError;
    private TextView apiNameError;
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
        setContentView(R.layout.activity_register);
        signin = (SignInButton) findViewById(R.id.sign_in_button);
        TextView textView = (TextView) signin.getChildAt(0);
        textView.setText("Google");
        userSituation = getSharedPreferences("userEmail",MODE_PRIVATE);
        name = (EditText) findViewById(R.id.register_name);
        email = (EditText) findViewById(R.id.register_email);
        password = (EditText) findViewById(R.id.register_password);
        register = (Button) findViewById(R.id.register_button);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        apiEmailError = (TextView) findViewById(R.id.api_email_error);
        apiNameError = (TextView) findViewById(R.id.api_name_error);
        TextView alreadyMember = (TextView) findViewById(R.id.already_member);
        loginButton = (LoginButton) findViewById(R.id.facebook_register_button);
        Button myLoginButtonFb = (Button) findViewById(R.id.facebook_register_button_custom);
        myLoginButtonFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
            }
        });
        alreadyMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go To Login Page
                Intent loginPage = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginPage);
                finish();
            }
        });
        validator = new AwesomeValidation(ValidationStyle.BASIC);
        url = getResources().getString(R.string.domain_name);
        setupRules();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validator.validate()) {
                    registerUser();
                    validator.clear();
                }
            }
        });
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                facebookLoginInformation(accessToken);
            }
            @Override
            public void onCancel() {
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

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            googleSignIn();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public void setupRules() {
        validator.addValidation(this, R.id.register_name, RegexTemplate.NOT_EMPTY, R.string.name_register_error);
        validator.addValidation(this, R.id.register_email, Patterns.EMAIL_ADDRESS, R.string.email_register_error);
        validator.addValidation(this, R.id.register_password, "[a-zA-Z0-9]{6,}", R.string.password_register_error);
    }

    private void googleSignIn() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(RegisterActivity.this);
        if (acct != null) {
            final String personName = acct.getDisplayName();
            final String personEmail = acct.getEmail();
            final Uri personPhoto = acct.getPhotoUrl();
            apiNameError.setVisibility(View.GONE);
            apiNameError.setText(null);
            apiEmailError.setVisibility(View.GONE);
            apiEmailError.setText(null);
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
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Registered Successfully!", Toast.LENGTH_LONG).show();
                            // Go To Home Page
                            Intent homePage = new Intent(RegisterActivity.this, ReferralCodeActivity.class);
                            startActivity(homePage);
                            finish();
                        }
                        // Device Exist
                        else if (success.equals("deviceError")){
                            Toast.makeText(RegisterActivity.this, "Only 1 account per device is allowed!", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.GONE);
                            register.setVisibility(View.VISIBLE);
                        }
                        else {
                            // Change Shared Preferences
                            userSituation.edit().putString("userEmail", personEmail).apply();
                            // Message Success
                            loginButton.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Welcome back!", Toast.LENGTH_LONG).show();
                            // Player already Exists , Go To Main Activity
                            Intent homePage = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(homePage);
                            finish();
                        }
                    } catch(JSONException e){
                        e.printStackTrace();
                        mProgressBar.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    mProgressBar.setVisibility(View.GONE);
                    register.setVisibility(View.VISIBLE);
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
            RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
    }

    private void registerUser() {
        final String enteredName = this.name.getText().toString().trim();
        final String enteredEmail = this.email.getText().toString().trim();
        final String enteredPassword = this.password.getText().toString().trim();
        apiEmailError.setVisibility(View.GONE);
        apiEmailError.setText(null);
        apiNameError.setVisibility(View.GONE);
        apiNameError.setText(null);
        mProgressBar.setVisibility(View.VISIBLE);
        register.setVisibility(View.GONE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/new/verify", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("1")){
                        mProgressBar.setVisibility(View.GONE);
                        Intent main = new Intent(RegisterActivity.this, VerifyRegistrationByEmail.class);
                        main.putExtra("name", enteredName);
                        main.putExtra("email", enteredEmail);
                        main.putExtra("password", enteredPassword);
                        startActivity(main);
                        finish();
                    }
                    // Name Exists
                    if (success.equals("nameError")){
                        mProgressBar.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                        String message = jsonObject.getString("message_name");
                        apiNameError.setVisibility(View.VISIBLE);
                        apiNameError.setText("* "+message);
                    }
                    // Device Exist
                    if (success.equals("deviceError")){
                        Toast.makeText(RegisterActivity.this, "Only 1 account per device is allowed!", Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                    }
                    // Email Exists
                    if (success.equals("emailError")){
                        mProgressBar.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                        String message = jsonObject.getString("message_email");
                        apiEmailError.setVisibility(View.VISIBLE);
                        apiEmailError.setText("* "+message);
                    }
                    // Email & Name Exists
                    if (success.equals("twoError")){
                        mProgressBar.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                        String messageName = jsonObject.getString("message_name");
                        String messageEmail = jsonObject.getString("message_email");
                        apiEmailError.setVisibility(View.VISIBLE);
                        apiEmailError.setText("* "+messageEmail);
                        apiNameError.setVisibility(View.VISIBLE);
                        apiNameError.setText("* "+messageName);
                    }

                } catch(JSONException e){
                    e.printStackTrace();
                    mProgressBar.setVisibility(View.GONE);
                    register.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mProgressBar.setVisibility(View.GONE);
                register.setVisibility(View.VISIBLE);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", enteredName);
                params.put("email", enteredEmail);
                params.put("password", enteredPassword);
                @SuppressLint("HardwareIds") final String device = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                params.put("device_id", device);
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

    private void facebookLoginInformation(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    // GET USER DATA
                    final String name = object.getString("name");

                    String email ="fb28386468464";
                    if(object.has("email")){
                        email = object.getString("email");
                    }
                    final String image = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    // New Player , Register Request & Go To Main Activity & Save Shared Prefs
                    apiEmailError.setVisibility(View.GONE);
                    apiEmailError.setText(null);
                    apiNameError.setVisibility(View.GONE);
                    apiNameError.setText(null);
                    final String finalEmail = email;
                    final String finalEmail1 = email;
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/new", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");
                                // No Errors
                                if (success.equals("1")){
                                    // Change Shared Preferences
                                    userSituation.edit().putString("userEmail", finalEmail).apply();
                                    // Message Success
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Registered Successfully!", Toast.LENGTH_LONG).show();
                                    // Go To Home Page
                                    Intent homePage = new Intent(RegisterActivity.this, ReferralCodeActivity.class);
                                    startActivity(homePage);
                                    finish();
                                } else if (success.equals("deviceError")){
                                    Toast.makeText(RegisterActivity.this, "Only 1 account per device is allowed!", Toast.LENGTH_LONG).show();
                                    mProgressBar.setVisibility(View.GONE);
                                    register.setVisibility(View.VISIBLE);
                                }
                                else {
                                    // Change Shared Preferences
                                    userSituation.edit().putString("userEmail", finalEmail).apply();
                                    // Message Success
                                    loginButton.setVisibility(View.GONE);
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Welcome back!", Toast.LENGTH_LONG).show();
                                    // Player already Exists , Go To Main Activity
                                    Intent homePage = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(homePage);
                                    finish();
                                }
                            } catch(JSONException e){
                                e.printStackTrace();
                                mProgressBar.setVisibility(View.GONE);
                                register.setVisibility(View.VISIBLE);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            mProgressBar.setVisibility(View.GONE);
                            register.setVisibility(View.VISIBLE);
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            int length = 10;
                            boolean useLetters = true;
                            boolean useNumbers = false;
                            params.put("name", name);
                            params.put("email", finalEmail1);
                            params.put("password", random());
                            params.put("image_url", image);
                            @SuppressLint("HardwareIds") final String device = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID);
                            params.put("device_id", device);
                            return params;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
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
}
