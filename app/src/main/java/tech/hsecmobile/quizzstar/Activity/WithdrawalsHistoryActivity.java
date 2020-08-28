package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import tech.hsecmobile.quizzstar.Adapter.WithdrawalAdapter;
import tech.hsecmobile.quizzstar.Model.Withdrawal;
import tech.hsecmobile.quizzstar.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class WithdrawalsHistoryActivity extends AppCompatActivity {
    private WithdrawalAdapter withdrawalAdapter;
    private RecyclerView withdrawalsRecyclerView;
    private ArrayList<Withdrawal> withdrawalsArrayList;
    private RequestQueue queue;
    private String userEmail, userId;
    private TextView empty;
    SharedPreferences userSituationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawals_history);
        Toolbar toolbar = findViewById(R.id.toolbar_withdrawals);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        userSituationId = getSharedPreferences("userId",MODE_PRIVATE);
        userId = userSituationId.getString("userId", "");
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("userEmail");
        queue = Volley.newRequestQueue(this);
        empty = (TextView) findViewById(R.id.withdrawal_empty);
        withdrawalsRecyclerView = (RecyclerView) findViewById(R.id.withdrawals_recycler);
        withdrawalsArrayList = new ArrayList<>();
        withdrawalAdapter = new WithdrawalAdapter(this, withdrawalsArrayList);
        withdrawalsRecyclerView.setAdapter(withdrawalAdapter);
        withdrawalsRecyclerView.setHasFixedSize(true);
        withdrawalsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        getWithdrawalHistory();
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(WithdrawalsHistoryActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        super.onBackPressed();
    }

    private void getWithdrawalHistory() {
        String url = getResources().getString(R.string.domain_name)+"/api/players/"+userId+"/withdrawals";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            //Log.e("try", "im on try");
                            if (jsonArray == null || jsonArray.length() <= 0) {
                                empty.setVisibility(View.VISIBLE);
                            } else {
                                empty.setVisibility(View.GONE);
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject withdrawal = jsonArray.getJSONObject(i);
                                String gettedAmount = withdrawal.getString("amount");
                                int gettedPoints = withdrawal.getInt("points");
                                String gettedStatus = withdrawal.getString("status");
                                String gettedMethod = withdrawal.getString("method");
                                String gettedAccount = withdrawal.getString("account");
                                String gettedDate = withdrawal.getString("date");
                                withdrawalsArrayList.add(new Withdrawal(gettedAmount, gettedStatus, gettedMethod, gettedAccount, gettedDate,gettedPoints));
                            }
                            withdrawalAdapter.notifyDataSetChanged();

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
        Intent main = new Intent(WithdrawalsHistoryActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        return true;
    }
}
