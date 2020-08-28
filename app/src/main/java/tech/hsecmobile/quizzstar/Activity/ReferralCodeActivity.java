package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
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

public class ReferralCodeActivity extends AppCompatActivity {
    private Button send, skip;
    private EditText referralCode;
    private String url;
    private SharedPreferences userSituation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral_code);
        userSituation = getSharedPreferences("userEmail",MODE_PRIVATE);
        referralCode = (EditText) findViewById(R.id.referral_code);
        send = (Button) findViewById(R.id.referral_send);
        skip = (Button) findViewById(R.id.skip_referral);
        url = getResources().getString(R.string.domain_name);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skip = new Intent(ReferralCodeActivity.this, MainActivity.class);
                startActivity(skip);
                finish();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReferral();
            }
        });
    }

    private void sendReferral() {
        final String email = userSituation.getString("userEmail", "");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/referral/add", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success.equals("1")) {
                        Toast.makeText(ReferralCodeActivity.this, "Referral Code Added Successfully!", Toast.LENGTH_SHORT).show();
                        Intent main = new Intent(ReferralCodeActivity.this, MainActivity.class);
                        startActivity(main);
                        finish();
                    } else {
                        Toast.makeText(ReferralCodeActivity.this, "Referral Code Invalid!", Toast.LENGTH_SHORT).show();
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
                // Check that image is added
                params.put("email", email);
                params.put("referral", referralCode.getText().toString());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ReferralCodeActivity.this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }
}
