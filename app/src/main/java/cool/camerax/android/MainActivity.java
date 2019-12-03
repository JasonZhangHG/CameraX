package cool.camerax.android;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.VideoCapture;
import androidx.camera.view.CameraView;

public class MainActivity extends AppCompatActivity {

    private CameraView mCameraView;
    private Button mTakePicView;
    private Button mStartRecordingView;
    private Button mSwitchCameraView;
    private ImageView mShowPicView;
    private VideoView mShowVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.view_camera);
        mTakePicView = findViewById(R.id.btn_take_pic);
        mShowPicView = findViewById(R.id.iv_show_pic);
        mStartRecordingView = findViewById(R.id.btn_start_recording);
        mShowVideoView = findViewById(R.id.vv_show_recording);
        mSwitchCameraView = findViewById(R.id.btn_switch_camera);
        //我们可以用 CameraView 的 bindToLifeCycle 方法将这个 View 与当前组件的生命周期绑定。
        mCameraView.bindToLifecycle(this);

        initTakePic();
        initStartRecording();
        initSwitchCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraX.unbindAll();
    }

    //拍照
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
                        LogUtils.d("MainActivity takePicture onImageSaved  file : " + file);
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
                        LogUtils.d("MainActivity takePicture onError  imageCaptureError : " + imageCaptureError + "  message : " + message +
                                " Throwable : " + cause);
                    }
                });
            }
        });
    }

    //录像
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
                            LogUtils.d("MainActivity startRecording onVideoSaved  file : " + file);
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
                            LogUtils.d("MainActivity startRecording onError  imageCaptureError : " + videoCaptureError + "  message : " + message +
                                    " Throwable : " + cause);
                        }
                    });
                }
            }
        });
    }

    public void initSwitchCamera() {
        //切换摄像头
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
}
