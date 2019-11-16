package me.farazzappy.esp8266websocket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import java.util.Timer;
import java.util.TimerTask;

import hu.agta.rxwebsocket.RxWebSocket;
import io.github.controlwear.virtual.joystick.android.JoystickView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import okio.ByteString;

public class JoyStickActivity extends AppCompatActivity {

    private int[] ppm = new int[]{1100,1100,1100,1100,1100,1100,1100,1100};
    private int[] oldppm = new int[]{0,0,0,0,0,0,0,0};

    private RxWebSocket rxWebSocket;


    private class Sendalive extends TimerTask {

        @SuppressLint("CheckResult")
        @Override
        public void run() {
            byte sendframe[] = new byte[3];
            sendframe[0] = (byte) 0;
            sendframe[1] =  (byte) (ppm[0]>>8);
            sendframe[2] =  (byte) ppm[0];

            rxWebSocket.sendMessage(ByteString.of(sendframe)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            success -> Log.i("Websocket", "Message was sent " + success),
                            throwable -> Log.e("Websocket", "Error!" + throwable.getMessage())
                    );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joy_stick);

        start();

        Timer timer = new Timer();
        timer.schedule(new Sendalive(), 0, 1000);

        Switch aux1 = findViewById(R.id.aux1);
        Switch aux2 = findViewById(R.id.aux2);
        Switch aux3 = findViewById(R.id.aux3);
        Switch aux4 = findViewById(R.id.aux4);

        Button convertToHovercraft = findViewById(R.id.convertToHover);

        convertToHovercraft.setOnClickListener(v -> {
            rxWebSocket.sendMessage("hover").subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            success -> Log.i("Websocket", "Message was sent " + success),
                            throwable -> Log.e("Websocket", "Error!" + throwable.getMessage())
                    );
            startActivity(new Intent(JoyStickActivity.this, HoverCraftActivity.class));
            finish();
        });

        aux1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                ppm[4] = 2000;
            else
                ppm[4] = 1100;

            update();

//            rxWebSocket.sendMessage("get").subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            success -> Log.i("Websocket", "Message was sent " + success),
//                            throwable -> Log.e("Websocket", "Error!" + throwable.getMessage())
//                    );
        });

        aux2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                ppm[5] = 2000;
            else
                ppm[5] = 1100;

            update();

//            rxWebSocket.sendMessage("ret").subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            success -> Log.i("Websocket", "Message was sent " + success),
//                            throwable -> Log.e("Websocket", "Error!" + throwable.getMessage())
//                    );
        });

        aux3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                ppm[6] = 2000;
            else
                ppm[6] = 1100;

            update();
        });

        aux4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                ppm[7] = 2000;
            else
                ppm[7] = 1100;

            update();
        });


        final JoystickView joystickLeft = findViewById(R.id.joystickView_left);
        joystickLeft.setOnMoveListener((angle, strength) -> {

            joystickLeft.setAutoReCenterButton(false);

//                mTextViewAngleRight.setText(String.valueOf((int) (angle*0.71)));
//                mTextViewStrengthRight.setText(strength + "%");
//                mTextViewCoordinateRight.setText(
//                        String.format("x%05d:y%05d", map(joystickRight.getNormalizedX(), 0, 100, 1000, 2000), map(joystickRight.getNormalizedY(), 100, 0, 1000, 2000))
//                );

            ppm[1] = (int) map(joystickLeft.getNormalizedX(), 0, 100, 1100, 2000);
            ppm[0] = (int) map(joystickLeft.getNormalizedY(), 100, 0, 1100, 2000);

            update();
        });


        final JoystickView joystickRight = findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
//                mTextViewAngleRight.setText(String.valueOf((int) (angle*0.71)));
//                mTextViewStrengthRight.setText(strength + "%");
//                mTextViewCoordinateRight.setText(
//                        String.format("x%05d:y%05d", map(joystickRight.getNormalizedX(), 0, 100, 1000, 2000), map(joystickRight.getNormalizedY(), 100, 0, 1000, 2000))
//                );

                ppm[2] = (int) map(joystickRight.getNormalizedX(), 0, 100, 1100, 2000);
                ppm[3] = (int) map(joystickRight.getNormalizedY(), 100, 0, 1100, 2000);

                update();
            }
        });
    }

    @SuppressLint("CheckResult")
    private void start() {
        rxWebSocket = new RxWebSocket("ws://192.168.4.1:81");


        rxWebSocket.connect();

        rxWebSocket.onOpen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketOpenEvent -> {
                     Log.i("Websocket", "Connected");
                }, Throwable::printStackTrace);

        rxWebSocket.onClosed()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosedEvent -> {
                    Log.i("Websocket", "Closed");
                }, Throwable::printStackTrace);

        rxWebSocket.onClosing()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosingEvent -> {
                    Log.i("Websocket", "Closing");
                }, Throwable::printStackTrace);

        rxWebSocket.onTextMessage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketMessageEvent -> {
                    Log.i("Websocket", "Recieved :" + socketMessageEvent.getText());
                }, Throwable::printStackTrace);

        rxWebSocket.onFailure()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketFailureEvent -> {
                    Log.e("Websocket", "Failed : " + socketFailureEvent.getException().getLocalizedMessage());
                }, Throwable::printStackTrace);
    }


    @SuppressLint("CheckResult")
    private void update() {
        for(int i=0;i<8;i++) {
            if(ppm[i] != oldppm[i]) {
                oldppm[i] = ppm[i];
                byte sendframe[] = new byte[3];
                sendframe[0] = (byte) i;
                sendframe[1] =  (byte) (ppm[i]>>8);
                sendframe[2] =  (byte) ppm[i];

                rxWebSocket.sendMessage(ByteString.of(sendframe)).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> Log.i("Websocket", "Message was sent " + success),
                                throwable -> Log.e("Websocket", "Error!" + throwable.getMessage())
                        );
            }
        }
    }

    private long map(long x, long in_min, long in_max, long out_min, long out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}
