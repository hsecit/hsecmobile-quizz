package tech.hsecmobile.quizzstar.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import tech.hsecmobile.quizzstar.Manager.PermissionManager;
import tech.hsecmobile.quizzstar.R;

import java.util.Locale;

public class PermissionActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    private static final int REQUEST_WRITE_PERMISSION = 786;
    private Button btnAllowPermissions;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION : {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED   ) {
                    new PermissionManager(PermissionActivity.this).writePreference();
                    Intent goToMain  =  new Intent(getApplicationContext(), SplashScreenActivity.class);
                    goToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(goToMain);
                    finish();
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale locale = new Locale(getApplicationContext().getResources().getString(R.string.app_lang));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        if (new PermissionManager(PermissionActivity.this).checkPreference()) {
            loadMainActivity();
        }else {
            Toast.makeText(this, "something wrong in permmision", Toast.LENGTH_LONG).show();
        }
        btnAllowPermissions = (Button) findViewById(R.id.allow_permissions_btn);
        btnAllowPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    private void requestPermission() {
        Log.d("versioncode", String.valueOf(Build.VERSION.SDK_INT)+"--->"+ String.valueOf(Build.VERSION_CODES.M));
        Log.d("versioncode", String.valueOf(Build.VERSION.SDK_INT)+"--->"+ String.valueOf(Build.VERSION_CODES.Q));

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(PermissionActivity.this, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            Log.d("permissions","add permissions");
        }else {
            Log.d("permissions","build version < build code");
        }
    }

    private void loadMainActivity() {
        startActivity(new Intent(PermissionActivity.this, SplashScreenActivity.class));
        finish();
    }
}





