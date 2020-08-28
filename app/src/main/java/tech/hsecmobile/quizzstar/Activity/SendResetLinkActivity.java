package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SendResetLinkActivity extends AppCompatActivity {

    private EditText userEmail, codeVerification;
    private Button back, send, verify;
    private String url;
    private LinearLayout linear;
    private ProgressBar mProgressBar;
    AwesomeValidation validator;
    int attempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_reset_link);
        userEmail = (EditText) findViewById(R.id.send_reset_email);
        codeVerification = (EditText) findViewById(R.id.reset_code);
        back = (Button) findViewById(R.id.back_to_login_btn);
        send = (Button) findViewById(R.id.send_reset_btn);
        verify = (Button) findViewById(R.id.verify_code_btn);
        linear = (LinearLayout) findViewById(R.id.password_code_linear);
        url = getString(R.string.domain_name);
        validator = new AwesomeValidation(ValidationStyle.BASIC);
        verifyResetEmailFormat();
        mProgressBar = (ProgressBar) findViewById(R.id.password_view);
        Sprite foldingCube = new Wave();
        foldingCube.setColor(R.color.colorAccent);
        mProgressBar.setIndeterminateDrawable(foldingCube);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(SendResetLinkActivity.this, LoginActivity.class);
                login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validator.validate()) {
                    sendResetLinkByEmail();
                    validator.clear();
                } else {
                    Toast.makeText(SendResetLinkActivity.this, "Please Enter a valid Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });
    }

    private void verifyCode() {
        final String email = userEmail.getText().toString().trim();
        final String code = codeVerification.getText().toString().trim();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/password/reset/verify", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success.equals("1")) {
                        Toast.makeText(SendResetLinkActivity.this, "Code verified successfully!", Toast.LENGTH_SHORT).show();
                        Intent change = new Intent(SendResetLinkActivity.this, ChangePasswordActivity.class);
                        change.putExtra("email", email);
                        startActivity(change);
                        finish();
                    } else {
                        if (attempts < 4) {
                            Toast.makeText(SendResetLinkActivity.this, "Code is not valid!", Toast.LENGTH_SHORT).show();
                            attempts = attempts + 1;
                        } else {
                            Toast.makeText(SendResetLinkActivity.this, "You failed your attempts to verify code!", Toast.LENGTH_SHORT).show();
                            Intent login = new Intent(SendResetLinkActivity.this, LoginActivity.class);
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
                params.put("code", code);
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

    private void verifyResetEmailFormat() {
        validator.addValidation(this, R.id.send_reset_email, Patterns.EMAIL_ADDRESS, R.string.email_register_error);
    }

    private void sendResetLinkByEmail() {
        mProgressBar.setVisibility(View.VISIBLE);
        linear.setVisibility(View.GONE);
        verify.setVisibility(View.GONE);
        final String email = userEmail.getText().toString().trim();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/password/reset/send", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success.equals("1")) {
                        Toast.makeText(SendResetLinkActivity.this, "Reset Code Sent Successfully!", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                        linear.setVisibility(View.VISIBLE);
                        verify.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(SendResetLinkActivity.this, "This Email Address does not belong to any user!", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                } catch(JSONException e) {
                    e.getStackTrace();
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mProgressBar.setVisibility(View.GONE);
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
}
