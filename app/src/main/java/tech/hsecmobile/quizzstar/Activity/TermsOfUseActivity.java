package tech.hsecmobile.quizzstar.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import tech.hsecmobile.quizzstar.R;

import java.util.Locale;
import java.util.Objects;

public class TermsOfUseActivity extends AppCompatActivity {
    private TextView privacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_use);
        Toolbar toolbar = findViewById(R.id.terms_toolbar);
        toolbar.setTitle(getString(R.string.drawer_menu_terms));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        privacyPolicy = (TextView) findViewById(R.id.terms_text);
        privacyPolicy.setText(Html.fromHtml(getString(R.string.terms_of_use)));
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(TermsOfUseActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(main);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Intent main = new Intent(TermsOfUseActivity.this, MainActivity.class);
        startActivity(main);
        finish();
        return true;
    }

}
