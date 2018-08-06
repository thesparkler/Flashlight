package com.example.home.flashlight;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


public class MainActivity extends AppCompatActivity {

    private ImageButton btnSwitch;
    android.hardware.Camera camera;
    private boolean isFlash = false;
    private boolean isFlashOn;
    android.hardware.Camera.Parameters parameters;
    MediaPlayer mediaPlayer;
    private boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // flash Switch button
        btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);

        /**
         * First check if device is supporting flashlight or not
         */
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            camera = android.hardware.Camera.open();
            parameters = camera.getParameters();
            isFlash = true;
        }

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFlash) {
                    if (!isOn) {
                        btnSwitch.setImageResource(R.drawable.btn_switch_on);

                        playSound();

                        parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                        camera.startPreview();
                        isOn = true;
                    } else {

                        playSound();
                        btnSwitch.setImageResource(R.drawable.btn_switch_off);
                        parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                        camera.stopPreview();
                        isOn = false;

                    }

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Error..");
                    builder.setMessage("Flashlight is not Available on this device... ");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            finish();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    private void playSound()
    {
        if(isFlash)
        {
            mediaPlayer = MediaPlayer.create(MainActivity.this,R.raw.light_switch_off);
        }
        else
        {
            mediaPlayer = MediaPlayer.create(MainActivity.this,R.raw.light_switch_on);
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

}


