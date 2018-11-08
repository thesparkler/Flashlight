package com.example.home.flashlight;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Locale;

public class FlashActivity extends AppCompatActivity {

    ImageView settings;
    private ToggleButton btnSwitch;
    android.hardware.Camera camera;
    private boolean isFlash = false;
    android.hardware.Camera.Parameters parameters;
    MediaPlayer mediaPlayer;
    StroboRunner sr;
    Thread t;
    int freq;
    private TextToSpeech myTTS;
    private SpeechRecognizer mySpeechRecognizer;
    ImageView imgMic;
    private ImageView imgTumbler;
    Animation animation;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeTextToSpeech();
        initializeSpeechRecognizer();

        // Instantiating widgets
        imgMic = (ImageView)findViewById(R.id.img_mic);
        imgTumbler = (ImageView)findViewById(R.id.img_tumbler);


        imgMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scaleDown();
                imgTumbler.setVisibility(View.VISIBLE);
                animBlink();
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                mySpeechRecognizer.startListening(intent);

            }
        });
        imgTumbler.setVisibility(View.INVISIBLE);



        // flash Switch button
        btnSwitch = (ToggleButton)findViewById(R.id.btnSwitch);

        /** First check if device is supporting flashlight or not **/
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            camera = android.hardware.Camera.open();
            parameters = camera.getParameters();
            isFlash = true;
        }

        btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                turnOnOff(isChecked);
            }
        });

        settings = (ImageView) findViewById(R.id.img_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FlashActivity.this);
                View parentView = getLayoutInflater().inflate(R.layout.modal_bs, null);
                bottomSheetDialog.setContentView(parentView);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                bottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics()));
                bottomSheetDialog.show();

                final TextView textView = (TextView) parentView.findViewById(R.id.tv_percent);
                SeekBar seekBar = (SeekBar) parentView.findViewById(R.id.seekbar);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        textView.setText(progress + "");
                        freq = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        });
    }

    private void playSound() {
        if (isFlash) {
            mediaPlayer = MediaPlayer.create(FlashActivity.this, R.raw.light_switch_off);
        } else {
            mediaPlayer = MediaPlayer.create(FlashActivity.this, R.raw.light_switch_on);
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

    private class StroboRunner implements Runnable
    {
        int freq;
        boolean stopRunning = false;

        @Override
        public void run() {
            Camera.Parameters paramsOn = camera.getParameters();
            Camera.Parameters paramsOff = parameters;
            paramsOn.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            paramsOff.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

            try{
                while(!stopRunning){
                    camera.setParameters(paramsOn);
                    camera.startPreview();
                    Thread.sleep(100 - freq);
                    camera.setParameters(paramsOff);
                    camera.startPreview();
                    Thread.sleep(freq);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void turnOnOff(boolean on)
    {
        if(on) {
            btnSwitch.setBackgroundDrawable(getDrawable(R.drawable.btn_switch_on));

            if (freq != 0) {
                sr = new StroboRunner();
                sr.freq = freq;
                t = new Thread(sr);
                t.start();
                return;

            }else{
                btnSwitch.setBackgroundDrawable(getDrawable(R.drawable.btn_switch_on));
                playSound();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
               // camera.startPreview();
            }


        }
        else if(!on){
            if(t != null){
                sr.stopRunning = true;
                btnSwitch.setBackgroundDrawable(getDrawable(R.drawable.btn_switch_off));

                t = null;
                return;
            }
            else{
                playSound();
                btnSwitch.setBackgroundDrawable(getDrawable(R.drawable.btn_switch_off));
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }
        camera.setParameters(parameters);
        camera.startPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     * ********************************** voice recognition code **********************************************
     */
    private void initializeTextToSpeech()
    {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size() == 0){
                    Toast.makeText(FlashActivity.this, "There is no TTS engine on your device", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    myTTS.setLanguage(Locale.ENGLISH);
                    speak("Hello! I am ready.");
                }

            }
        });
    }

    private void speak(String message)
    {
        if(Build.VERSION.SDK_INT >= 21)
        {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else{
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTTS.shutdown();
    }

    private void  initializeSpeechRecognizer(){
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            mySpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mySpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle results) {
                    List<String> res = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processResult(res.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    private void processResult(String command)
    {
        command = command.toLowerCase();

        if(command.contains("turn on the torch"))
        {
            speak("okay");
            btnSwitch.setBackgroundDrawable(getDrawable(R.drawable.btn_switch_on));
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
            playSound();
        }

        if(command.contains("turn off the torch")){
            speak("okay");
            playSound();
            btnSwitch.setBackgroundDrawable(getDrawable(R.drawable.btn_switch_off));
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
        }

        if(command.contains("what's your name")){
            speak("My name is smart torch");
        }

    }

    private void scaleDown(){
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scale_down);
        imgMic.startAnimation(animation);
    }

    private void animBlink(){
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_blink);
        imgTumbler.startAnimation(animation);
    }
}


