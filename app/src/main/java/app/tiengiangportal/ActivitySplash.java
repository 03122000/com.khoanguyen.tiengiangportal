/*
Create by Nguyen Nguyen
 */
package app.tiengiangportal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

import app.tiengiangportal.data.SharedPref;
import app.tiengiangportal.loader.GcmLoader;
import app.tiengiangportal.utils.Callback;
import app.tiengiangportal.utils.PermissionUtil;
import app.tiengiangportal.utils.Tools;

public class ActivitySplash extends AppCompatActivity {

    private SharedPref sharedPref;
    private View parent_view;
    final TimerTask task = new TimerTask() {
        @Override
        public void run() {
            startMainActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        parent_view = findViewById(R.id.parent_view);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        sharedPref = new SharedPref(this);
        Tools.initImageLoader(getApplicationContext());

        parent_view.setBackgroundColor(sharedPref.getThemeColorInt());
        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    private void startProvisioningGcm(){
        if(sharedPref.isNeedRegisterGcm() && Tools.cekConnection(this)){
            ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
            // register gcm
            new GcmLoader(this, new Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    new Timer().schedule(task, 1000);
                }

                @Override
                public void onError(String result) {
                    new Timer().schedule(task, 1000);
                }
            }).execute("");
        } else {
            try{
                // Show splash screen for 1 seconds
                new Timer().schedule(task, 1000);
            }catch (Exception e){}
        }

    }

    private void startMainActivity() {
        // go to the main activity
        Intent i = new Intent(ActivitySplash.this, ActivityMain.class);
        startActivity(i);
        // kill current activity
        finish();
    }

    @Override
    protected void onResume() {
        if(!PermissionUtil.isAllPermissionGranted(this)){
            showDialogPermission();
        }else{
            startProvisioningGcm();
        }
        super.onResume();
    }

    private void showDialogPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_permission));
        builder.setMessage(getString(R.string.dialog_content_permission));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                PermissionUtil.goToPermissionSettingScreen(ActivitySplash.this);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Timer().schedule(task, 1000);
            }
        });
        builder.show();
    }
}
