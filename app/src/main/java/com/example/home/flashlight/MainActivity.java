package com.example.home.flashlight;

//import android.Manifest;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraManager;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//public class MainActivity extends AppCompatActivity
//{
//
//    private ImageButton btnSwitch;
//    private static final int CAMERA_REQUEST = 50;
//    private boolean flashLightStatus = false;
//    private MediaPlayer mediaPlayer;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        btnSwitch = (ImageButton)findViewById(R.id.btnSwitch);
//
//        final boolean hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
//        final boolean isEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//
//       // btnSwitch.setEnabled(!isEnabled);
//
//        btnSwitch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                if(!isEnabled)
//                {
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},CAMERA_REQUEST);
//                }
//                else
//                {
//                    if(hasCameraFlash)
//                    {
//                        if (flashLightStatus) {
//                            flashLightOff();
//                        } else {
//                            flashLightOn();
//                        }
//                    }
//                    else
//                    {
//                        Toast.makeText(MainActivity.this,"No flash available on your device",Toast.LENGTH_LONG).show();
//                    }
//                }
//
//            }
//        });
//    }
//
//    private void flashLightOff()
//    {
//        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
//
//        try {
//            String cameraId = cameraManager.getCameraIdList()[0];
//            cameraManager.setTorchMode(cameraId,false);
//            flashLightStatus = false;
//            btnSwitch.setImageResource(R.drawable.btn_switch_off);
//        }
//        catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void flashLightOn()
//    {
//        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
//
//        try {
//            String cameraId = cameraManager.getCameraIdList()[0];
//            cameraManager.setTorchMode(cameraId, true);
//            flashLightStatus = true;
//            btnSwitch.setImageResource(R.drawable.btn_switch_on);
//        }
//        catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
//    {
//        switch (requestCode)
//        {
//            case CAMERA_REQUEST:
//                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                {
//                    btnSwitch.setEnabled(true);
//                }
//                else
//                {
//                    Toast.makeText(this,"Permission Denied for the Camera",Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
//
//
//
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//}








//===============================================================================================================================

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
  //  private boolean hasFlash;
    android.hardware.Camera.Parameters parameters;
    MediaPlayer mediaPlayer;
   // private boolean isOn = false;
    private boolean hasFlash;
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


