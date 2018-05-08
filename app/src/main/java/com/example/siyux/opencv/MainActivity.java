package com.example.siyux.opencv;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class MainActivity extends Activity implements View.OnClickListener{
    String TAG="Main Activity";
    private BackCameraSurfaceView backSurfaceView;
    private FontCameraSurfaceView fontSurfaceView;;
    private ImageView takePicBtn;
    Mat courImage = null;
    Mat tarImage = null;
    //加载opencv
    private LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // OpenCV引擎初始化加载成功
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully.");
                    // 连接到Camera
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        // OpenCVLoader.initDebug()静态加载OpenCV库
        // OpenCVLoader.initAsync()为动态加载OpenCV库，即需要安装OpenCV Manager
        if (!OpenCVLoader.initDebug()) {
            Log.w(TAG, "static loading library fail,Using Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.w(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏设置
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);//要放到加载布局文件代码之前

        takePicBtn= (ImageView) findViewById(R.id.getPic);
        backSurfaceView = (BackCameraSurfaceView) findViewById(R.id.backSurfaceview);
        fontSurfaceView = (FontCameraSurfaceView) findViewById(R.id.fontSurfaceview);

        fontSurfaceView.setZOrderOnTop(true);
        takePicBtn.setOnClickListener(this);
        //绑定后摄预览控件


    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.getPic:
                courImage=backSurfaceView.takePicture();
                fontSurfaceView.takePicture();
                break;
            default:
                break;
        }
    }
}
