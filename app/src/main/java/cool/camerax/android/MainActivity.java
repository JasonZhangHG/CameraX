package cool.camerax.android;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.FlashMode;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.VideoCapture;
import androidx.camera.view.CameraView;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class MainActivity extends AppCompatActivity {

    private final static CameraXLog logger = new CameraXLog(MainActivity.class.getSimpleName());

    private CameraView mCameraView;
    private Button mTakePicView;
    private Button mStartRecordingView;
    private Button mSwitchCameraView;
    private Button mSwitchFlashView;
    private ImageView mShowPicView;
    private VideoView mShowVideoView;
    private int mClickFlashCount;
    private RtcEngine mRtcEngine;

    // 创建 SurfaceView 对象。
    private FrameLayout mLocalContainer;
    private SurfaceView surfaceViewMySelf;

    // 创建一个 SurfaceView 对象。
    private RelativeLayout mRemoteContainer;
    private SurfaceView mSurfaceViewOther;


    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // 注册 onJoinChannelSuccess 回调。
        // 本地用户成功加入频道时，会触发该回调。
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logger.d("onJoinChannelSuccess Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        // 注册 onFirstRemoteVideoDecoded 回调。
        // SDK 接收到第一帧远端视频并成功解码时，会触发该回调。
        // 可以在该回调中调用 setupRemoteVideo 方法设置远端视图。
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logger.d("onFirstRemoteVideoDecoded First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        // 注册 onUserOffline 回调。
        // 远端用户离开频道或掉线时，会触发该回调。
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logger.d("onUserOffline User offline, uid: " + (uid & 0xFFFFFFFFL));
//                    onRemoteUserLeft();
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mCameraView = findViewById(R.id.view_camera);
        mTakePicView = findViewById(R.id.btn_take_pic);
        mShowPicView = findViewById(R.id.iv_show_pic);
        mStartRecordingView = findViewById(R.id.btn_start_recording);
        mShowVideoView = findViewById(R.id.vv_show_recording);
        mSwitchCameraView = findViewById(R.id.btn_switch_camera);
        mSwitchFlashView = findViewById(R.id.btn_switch_flash);
        mRemoteContainer = findViewById(R.id.rl_show_rvc_video);
        mLocalContainer = findViewById(R.id.rl_local_rvc_video);
//        mCameraView.bindToLifecycle(this);
//        initTakePic();
//        initStartRecording();
//        initSwitchCamera();
//        initSwitchFlashView();
        initAgora();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraX.unbindAll();
        if (mRtcEngine != null) {
            // 离开当前频道。
            mRtcEngine.leaveChannel();
        }
        RtcEngine.destroy();
    }

    public void initTakePic() {
        mTakePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraView == null) {
                    return;
                }
                mCameraView.setCaptureMode(CameraView.CaptureMode.IMAGE);
                mCameraView.takePicture(initTakePicPath(), new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        logger.d("takePicture onImageSaved  file : " + file);
                        if (mShowPicView == null) {
                            return;
                        }
                        try {
                            Glide.with(MainActivity.this)
                                    .load(file)
                                    .into(mShowPicView);
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message,
                                        @Nullable Throwable cause) {
                        logger.d("takePicture onError  imageCaptureError : " + imageCaptureError + "  message : " + message +
                                " Throwable : " + cause);
                    }
                });
            }
        });
    }

    public void initStartRecording() {
        mStartRecordingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraView == null) {
                    return;
                }
                if (mCameraView.isRecording()) {
                    mCameraView.stopRecording();
                    mStartRecordingView.setText(ResourceUtil.getString(R.string.start_recording));
                } else {
                    mStartRecordingView.setText(ResourceUtil.getString(R.string.stop_recording));
                    mCameraView.setCaptureMode(CameraView.CaptureMode.VIDEO);
                    mCameraView.startRecording(initStartRecordingPath(), new VideoCapture.OnVideoSavedListener() {
                        @Override
                        public void onVideoSaved(@NonNull File file) {
                            logger.d("startRecording onVideoSaved  file : " + file);
                            if (mShowVideoView == null) {
                                return;
                            }
                            mShowVideoView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mShowVideoView.setVisibility(View.VISIBLE);
                                    mShowVideoView.setVideoPath(file.getAbsolutePath());
                                    mShowVideoView.start();
                                    mShowVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mediaPlayer) {
                                            mShowVideoView.start();
                                        }
                                    });
                                }
                            });
                        }

                        @Override
                        public void onError(@NonNull VideoCapture.VideoCaptureError videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            logger.d("startRecording onError  imageCaptureError : " + videoCaptureError + "  message : " + message +
                                    " Throwable : " + cause);
                        }
                    });
                }
            }
        });
    }

    public void initSwitchCamera() {
        mSwitchCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraView == null) {
                    return;
                }
                CameraX.LensFacing lensFacing = mCameraView.getCameraLensFacing();
                if (lensFacing == null) {
                    return;
                }
                if (lensFacing == CameraX.LensFacing.FRONT) {
                    mCameraView.setCameraLensFacing(CameraX.LensFacing.BACK);
                } else {
                    mCameraView.setCameraLensFacing(CameraX.LensFacing.FRONT);
                }
            }
        });
    }

    public void initSwitchFlashView() {
        mSwitchFlashView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraView == null) {
                    return;
                }

                int position = mClickFlashCount % 3;
                switch (position) {
                    case 0:
                        mSwitchFlashView.setText(ResourceUtil.getString(R.string.start_flash));
                        mCameraView.setFlash(FlashMode.ON);
                        break;
                    case 1:
                        mSwitchFlashView.setText(ResourceUtil.getString(R.string.auto_flash));
                        mCameraView.setFlash(FlashMode.AUTO);
                        break;
                    case 2:
                        mSwitchFlashView.setText(ResourceUtil.getString(R.string.stop_flash));
                        mCameraView.setFlash(FlashMode.OFF);
                        break;
                    default:
                        break;
                }
                mClickFlashCount++;
            }
        });
    }

    public File initTakePicPath() {
        String dir = Environment.getExternalStorageDirectory().getPath() + File.separator + "DCIM/CameraX/";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String path = dir + System.currentTimeMillis() + ".jpg";
        return new File(path);
    }

    public File initStartRecordingPath() {
        String dir = Environment.getExternalStorageDirectory().getPath() + File.separator + "DCIM/CameraX/";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String path = dir + System.currentTimeMillis() + ".mp4";
        return new File(path);
    }

    public void initAgora() {
        try {
            mRtcEngine = RtcEngine.create(this, getString(R.string.agora_app_id), mRtcEventHandler);
            setupLocalVideo();
            joinChannel();
        } catch (Exception e) {
            logger.d("initAgora " + Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupLocalVideo() {
        // 启用视频模块。
        mRtcEngine.enableVideo();
        surfaceViewMySelf = RtcEngine.CreateRendererView(getBaseContext());
        surfaceViewMySelf.setZOrderMediaOverlay(false);
        surfaceViewMySelf.setZOrderOnTop(false);
        mLocalContainer.addView(surfaceViewMySelf);
        // 设置本地视图。
        VideoCanvas localVideoCanvas = new VideoCanvas(surfaceViewMySelf, VideoCanvas.RENDER_MODE_HIDDEN, 1);
        mRtcEngine.setupLocalVideo(localVideoCanvas);
    }

    private void joinChannel() {
        // 使用 Token 加入频道。
        mRtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", 1);
    }

    private void setupRemoteVideo(int uid) {
        mSurfaceViewOther = RtcEngine.CreateRendererView(getBaseContext());
        surfaceViewMySelf.setZOrderOnTop(true);
        mSurfaceViewOther.setZOrderMediaOverlay(true);
        mRemoteContainer.addView(mSurfaceViewOther);
        // 设置远端视图。
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mSurfaceViewOther, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }
}
