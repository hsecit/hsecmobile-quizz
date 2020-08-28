package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VerifyRegistrationByEmail extends AppCompatActivity {
    
    private EditText code;
    private Button verifyBtn;
    private String url;
    private int attempts;
    private SharedPreferences userSituation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_registration_by_email);
        userSituation = getSharedPreferences("userEmail",MODE_PRIVATE);
        code = (EditText) findViewById(R.id.verify_code);
        verifyBtn = (Button) findViewById(R.id.verify_button);
        url = getResources().getString(R.string.domain_name);
        sendVerificationCode();
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });
    }

    private void sendVerificationCode() {
        final String email = getIntent().getStringExtra("email");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/send/code", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success.equals("1")) {
                        Toast.makeText(VerifyRegistrationByEmail.this, "Verification Code Sent Successfully!", Toast.LENGTH_SHORT).show();
                    }
                } catch(JSONException e) {
                    e.getStackTrace();
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
                params.put("email", email);
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

    private void verifyCode() {
        final String email = getIntent().getStringExtra("email");
        final String codeStr = code.getText().toString().trim();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/send/code/verify", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success.equals("1")) {
                        Toast.makeText(VerifyRegistrationByEmail.this, "Account verified successfully!", Toast.LENGTH_SHORT).show();
                        registerUser();
                    } else {
                        if (attempts < 4) {
                            Toast.makeText(VerifyRegistrationByEmail.this, "Code is not valid!", Toast.LENGTH_SHORT).show();
                            attempts = attempts + 1;
                        } else {
                            Toast.makeText(VerifyRegistrationByEmail.this, "You failed your attempts to verify code!", Toast.LENGTH_SHORT).show();
                            Intent login = new Intent(VerifyRegistrationByEmail.this, RegisterActivity.class);
                            login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(login);
                            finish();
                        }
                    }
                } catch(JSONException e) {
                    e.getStackTrace();
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
                params.put("email", email);
                params.put("code", codeStr);
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

    private void registerUser() {
        final String enteredName = getIntent().getStringExtra("name");
        final String enteredEmail = getIntent().getStringExtra("email");
        final String enteredPassword = getIntent().getStringExtra("password");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/new", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    // No Errors
                    if (success.equals("1")){
                        // Change Shared Preferences
                        userSituation.edit().putString("userEmail", enteredEmail).apply();
                        // Message Success
                        Toast.makeText(VerifyRegistrationByEmail.this, "Registered Successfully!", Toast.LENGTH_LONG).show();
                        // Go To Home Page
                        Intent homePage = new Intent(VerifyRegistrationByEmail.this, ReferralCodeActivity.class);
                        startActivity(homePage);
                        finish();
                    }

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
}
