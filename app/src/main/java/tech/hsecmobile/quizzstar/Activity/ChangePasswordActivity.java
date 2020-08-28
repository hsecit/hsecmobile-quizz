package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText newPassword, confirmPassword;
    private Button savePassword;
    String userEmail, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        url = getString(R.string.domain_name);
        newPassword = (EditText) findViewById(R.id.change_password_new);
        confirmPassword = (EditText) findViewById(R.id.confirm_password_new);
        savePassword = (Button) findViewById(R.id.save_new_password);
        Intent getIt = getIntent();
        userEmail = getIt.getStringExtra("email");
        savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        final String new_password = newPassword.getText().toString();
        final String confirm_password = confirmPassword.getText().toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+"/api/players/password/change", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success.equals("1")) {
                        Toast.makeText(ChangePasswordActivity.this, "Password Changed Successfully!", Toast.LENGTH_SHORT).show();
                        Intent login = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(login);
                        finish();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Password not confirmed!", Toast.LENGTH_SHORT).show();
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
                params.put("email", userEmail);
                params.put("new_password", new_password);
                params.put("confirm_password", confirm_password);
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
