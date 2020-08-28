package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

public class InstructionsActivity extends AppCompatActivity {
    private ScrollView myscroll;
    private RequestQueue queue;
    private TextView tv6, tv2, tv10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        Toolbar toolbar = findViewById(R.id.instructions_toolbar);
        toolbar.setTitle(getString(R.string.drawer_menu_instructions));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        myscroll = (ScrollView) findViewById(R.id.instructions_scroll);
        myscroll.fullScroll(View.FOCUS_DOWN);
        myscroll.setSmoothScrollingEnabled(false);
        queue = Volley.newRequestQueue(this);
        tv6 = (TextView) findViewById(R.id.tv6);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv10 = (TextView) findViewById(R.id.tv10);
        getSettings();
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(InstructionsActivity.this, MainActivity.class);
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
                                int referral_register_points = settings.getInt("referral_register_points");
                                int question_time = settings.getInt("question_time");
                                int min_to_withdraw = settings.getInt("min_to_withdraw");
                                String currrency = settings.getString("currency");
                                tv2.setText(getString(R.string.fragment_instructions_2) + " "+ String.valueOf(referral_register_points));
                                tv6.setText(getString(R.string.fragment_instructions_6) + " "+ String.valueOf(question_time));
                                tv10.setText(getString(R.string.fragment_instructions_10) + " "+ String.valueOf(min_to_withdraw) + currrency + ".");
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Intent main = new Intent(InstructionsActivity.this, MainActivity.class);
        startActivity(main);
        finish();
        return true;
    }

}

