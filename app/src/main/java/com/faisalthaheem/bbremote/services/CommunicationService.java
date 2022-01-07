package com.faisalthaheem.bbremote.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.protobuf.Empty;

import java.util.Date;

import badbot.messages.BotServiceGrpc;
import badbot.messages.CmdOperationalMode;
import badbot.messages.Cmdtoggle;
import badbot.messages.Cmdvel;
import badbot.messages.Consts;
import badbot.messages.Message;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class CommunicationService extends Service {

    private final IBinder _binder = new CommunicationServiceBinder();
    private static final String TAG = "CommunicationService";
    private ManagedChannel _channel = null;
    BotServiceGrpc.BotServiceStub _stub = null;
    private Thread _connectionThread = null;
    private String _target_host = null;

    public SurfaceView get_canvas() {
        return _canvas;
    }

    public void set_canvas(SurfaceView _canvas) {
        this._canvas = _canvas;
    }

    private SurfaceView _canvas = null;

    public class CommunicationServiceBinder extends Binder
    {
        public CommunicationService getService()
        {
            return CommunicationService.this;
        }
    }



    @Override
    public void onCreate() {
        System.out.println("Communications service started");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _connectionThread.interrupt();
        System.out.println("Communications service destroyed");
    }

    @Override
    public int onStartCommand (Intent intent,
                               int flags,
                               int startId)
    {
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    public void sayHello()
    {
        Log.i(TAG, "hello World!");
    }

    public void setOpMode(Consts.OperationalMode mode){

        if(_stub == null){
            showToastMessage("Not connected");
            return;
        }

        CmdOperationalMode.CommandOperationalMode opMode = CmdOperationalMode.CommandOperationalMode.newBuilder()
                .setOpMode(mode)
                .build();

        Message.BadMessage msgOpMode = Message.BadMessage.newBuilder()
                .setMsgType(Consts.MessageType.CMD_OPERATIONAL_MODE)
                .setOpMode(opMode)
                .build();

        _stub.processCommand(msgOpMode, new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {

            }

            @Override
            public void onError(Throwable t) {

                Log.d(TAG, "Set operational mode command failed.");
                showToastMessage("Set operational mode command failed");
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "Set operational mode command succeeded.");
                showToastMessage("Set operational mode command succeeded");
            }
        });
    }

    public void setLidarState(boolean enabled)
    {
        txToggleCommand(Consts.Devices.LIDAR, enabled, "setLidarState");
    }

    public void setLidarRecording(boolean enabled)
    {
        txToggleCommand(Consts.Devices.LIDAR_RECORD, enabled, "setLidarRecording");
    }

    public void setAppSrcStatus(boolean enabled)
    {
        txToggleCommand(Consts.Devices.APPSRC, enabled, "setAppSrcStatus");
    }

    public void setMobileVideoStreamStatus(boolean enabled)
    {
        txToggleCommand(Consts.Devices.WS_VIDEO, enabled, "setMobileVideoStreamStatus");
    }

    public void toggleTelemetry(boolean enabled)
    {
        txToggleCommand(Consts.Devices.TELEMETRY, enabled, "toggleTelemetry");

        if(enabled && _stub != null)
        {
            _stub.subscribeToUpdates(Empty.newBuilder().build(), new StreamObserver< Message.BadMessage >(){

                /**
                 * Receives a value from the stream.
                 *
                 * <p>Can be called many times but is never called after {@link #onError(Throwable)} or {@link
                 * #onCompleted()} are called.
                 *
                 * <p>Unary calls must invoke onNext at most once.  Clients may invoke onNext at most once for
                 * server streaming calls, but may receive many onNext callbacks.  Servers may invoke onNext at
                 * most once for client streaming calls, but may receive many onNext callbacks.
                 *
                 * <p>If an exception is thrown by an implementation the caller is expected to terminate the
                 * stream by calling {@link #onError(Throwable)} with the caught exception prior to
                 * propagating it.
                 *
                 * @param value the value passed to the stream
                 */
                @Override
                public void onNext(Message.BadMessage value) {


                    Log.d(
                            TAG,
                            value.getMsgType().name()
                    );

                    Bundle bundle = new Bundle();
                    bundle.putByteArray("status", value.getStatus().toByteArray());

                    Intent notification_intent = new Intent("Bot-Status");
                    notification_intent.putExtras(bundle);

                    LocalBroadcastManager.getInstance(CommunicationService.this).sendBroadcast(notification_intent);

//                                if(value.getMsgType() == Consts.MessageType.VIDEO_FRAME)
//                                {
////                                    Log.d(TAG, "Video frame size is: " + value.getVideoFrame().getFrame().size());
//                                    if(_canvas != null)
//                                    {
//
//                                        SurfaceHolder holder = _canvas.getHolder();
//                                        Canvas surf = holder.lockCanvas();
////                                        Paint paint = new Paint();
////                                        paint.setStyle(Paint.Style.FILL);
////                                        paint.setColor(Color.RED);
////
////                                        Rect r = new Rect(10, 10, 200, 100);
////                                        surf.drawRect(r, paint);
//
//                                        Bitmap bmp = BitmapFactory.decodeByteArray(value.getVideoFrame().getFrame().toByteArray(), 0, value.getVideoFrame().getFrame().size());
//                                        //Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
//                                        surf.drawBitmap(bmp, 0,0, null);
//                                        //value.getVideoFrame().getFrame()
//
//                                        holder.unlockCanvasAndPost(surf);
//
//                                    }
//
//                                }
                }

                /**
                 * Receives a terminating error from the stream.
                 *
                 * <p>May only be called once and if called it must be the last method called. In particular if an
                 * exception is thrown by an implementation of {@code onError} no further calls to any method are
                 * allowed.
                 *
                 * <p>{@code t} should be a {@link StatusException} or {@link
                 * StatusRuntimeException}, but other {@code Throwable} types are possible. Callers should
                 * generally convert from a {@link Status} via {@link Status#asException()} or
                 * {@link Status#asRuntimeException()}. Implementations should generally convert to a
                 * {@code Status} via {@link Status#fromThrowable(Throwable)}.
                 *
                 * @param t the error occurred on the stream
                 */
                @Override
                public void onError(Throwable t) {
                    Log.d(TAG,"Failed to subscribe to updates.");
                    showToastMessage("Failed to subscribe to updates.");
                }

                /**
                 * Receives a notification of successful stream completion.
                 *
                 * <p>May only be called once and if called it must be the last method called. In particular if an
                 * exception is thrown by an implementation of {@code onCompleted} no further calls to any method
                 * are allowed.
                 */
                @Override
                public void onCompleted() {
                    Log.d(TAG,"Subscribed to updates.");
                    showToastMessage("Subscribed to updates.");
                }
            });

        }
    }

    private void txToggleCommand(Consts.Devices device, boolean enabled, String friendlyName)
    {
        if(_stub == null){
            showToastMessage("Not connected");
            return;
        }

        Cmdtoggle.CommandToggle cmdToggle = Cmdtoggle.CommandToggle.newBuilder()
                .setDevice(device)
                .setEnable(enabled)
                .build();

        Message.BadMessage msg = Message.BadMessage.newBuilder()
                .setMsgType(Consts.MessageType.CMD_TOGGLE)
                .setToggleCmd(cmdToggle)
                .build();

        _stub.processCommand(msg, new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {
                //never happens
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG,  friendlyName + " command failed.");
                showToastMessage( friendlyName + " command failed");
            }

            @Override
            public void onCompleted() {
                Log.d(TAG,  friendlyName + " command success.");
                showToastMessage( friendlyName + " command success");
            }
        });
    }

    public void txTeleop(int vx, int vy)
    {
        if(_stub == null){
            showToastMessage("Not connected");
            return;
        }

        Cmdvel.CmdVelocity cmdVelocity = Cmdvel.CmdVelocity.newBuilder()
                .setVX(vx)
                .setVY(vy)
                .build();


        Message.BadMessage msg = Message.BadMessage.newBuilder()
                .setMsgType(Consts.MessageType.CMD_TELEOP_MOVEMENT)
                .setCmdVel(cmdVelocity)
                .build();

        _stub.processCommand(msg, new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty value) {
                //never happens
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG,  "teleop command failed.");
            }

            @Override
            public void onCompleted() {
                Log.d(TAG,  "teleop command success.");
            }
        });
    }

    public void connectToHost(String host)
    {
        if(
            (_target_host == null || _target_host != host )
            &&
            _connectionThread !=null && _connectionThread.isAlive()
        ){
            //kill the connection thread as user asked us to connect to another host
            _connectionThread.interrupt();
            _connectionThread = null;
        }

        if(_connectionThread != null && _connectionThread.isAlive()){
            Log.d(TAG, "Another connection thread is already running. Will not start a new thread for connecting to " + host);
            return;
        }

        _connectionThread = new Thread() {
            @Override
            public void run() {
                try {
                    int retry_attempts = 0;
                    int retry_max_attempts = 5;
                    String status = "Connecting..";

                    do {
                        _target_host = host;

                        _channel = ManagedChannelBuilder
                                .forAddress(host, 5001)
                                .usePlaintext()
                                .build();

                        ConnectivityState state =
                                _channel.getState(true);

                        while (
                                state != ConnectivityState.READY
                                &&
                                retry_attempts++ < retry_max_attempts
                        ) {
                            status += ".";
                            txConnectivityStatus(status);

                            Log.i(TAG, "Trying to open channel to host " + host + " try # " + retry_attempts);
                            state = _channel.getState(true);

                            Thread.sleep(1000);
                        }

                        if(state != ConnectivityState.READY){
                            showToastMessage("Unable to connect to server, giving up.");
                            Log.e(TAG, "Unable to connect to server, giving up.");
                            txConnectivityStatus("Failed to connect to " + host);
                            return;
                        }

                        Log.d(TAG, "Connected to server.");
                        showToastMessage("Connected to server.");
                        txConnectivityStatus("Connected to " + host);

                        _stub = BotServiceGrpc.newStub(_channel);



                        //as channel has been created, enter the heartbeat loop
                        Message.BadMessage msg = Message.BadMessage.newBuilder()
                                .setMsgType(Consts.MessageType.HEARTBEAT_RC)
                                .build();
                        do {
                            if(_channel.getState(false) != ConnectivityState.READY)
                            {
                                //go back into trying to establish connection
                                break;
                            }

                            _stub.processCommand(msg, new StreamObserver<Empty>() {
                                @Override
                                public void onNext(Empty value) {
                                    //cannot happen
                                }

                                @Override
                                public void onError(Throwable t) {
                                    Log.d(TAG, "Heartbeat call completed with error.");
                                }

                                @Override
                                public void onCompleted() {
                                    Log.d(TAG, "Heartbeat call completed successfully.");
                                }
                            });
                            Thread.sleep(30000);

                        }while(true);

                        showToastMessage("Disconnected from server");
                        _stub = null;
                        _channel = null;

                    }while (true);
                }
                catch (Exception ex)
                {
                    Log.e(TAG, ex.getMessage());
                }
                finally {

                }
            }
        };
        _connectionThread.start();
    }

    private void showToastMessage(String message)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void txConnectivityStatus(String status)
    {
        Intent notification_intent = new Intent("Connectivity-Status");
        notification_intent.putExtra("status",status);

        LocalBroadcastManager.getInstance(CommunicationService.this).sendBroadcast(notification_intent);

    }
}


