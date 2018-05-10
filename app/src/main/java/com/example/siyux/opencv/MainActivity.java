package com.example.siyux.opencv;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
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
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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





    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //申请读写内存卡的动态权限
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE );
        }
    }
            @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.getPic:
                backSurfaceView.takePicture();
                fontSurfaceView.takePicture();
                String tarfilePath = "/data/data/com.example.siyux.opencv/" + "tar" + ".jpg";
                String sourfilePath = "/data/data/com.example.siyux.opencv/" + "sour" + ".jpg";//照片保存路径
                Mat sour= Imgcodecs.imread(tarfilePath);
                Mat tar= Imgcodecs.imread(sourfilePath);
                Log.e("sour", String.valueOf(sour.width()));
                int x=1500;
                int y=3000;
                float scale=  0.1f;
                Point center=new Point(x*scale,y*scale);
                Uri path=Uri.parse(getPackageName()+"/"+R.drawable.mask);
               Mat mask=Imgcodecs.imread(path.getPath());
                Mat result=new Mat();
                Imgproc.resize(sour, sour, new Size(sour.width() * 0.5, sour.height() * 0.5));
                Imgproc.resize(mask, mask, new Size(mask.width() * 0.15, mask.height() * 0.15));
                Photo.seamlessClone(sour,tar,mask,center,result,Photo.NORMAL_CLONE);



//                float scale=  0.2f;
//                Imgproc.resize(sour, sour, new Size(sour.width() * scale, sour.height() * scale));

//                    FileInputStream tarstream = new FileInputStream(tarfilePath);
//                    Bitmap tarImage= BitmapFactory.decodeStream(tarstream);
//                    FileInputStream sourstream = new FileInputStream(sourfilePath);
//                    Bitmap sourImage= BitmapFactory.decodeStream(sourstream);



                break;
            default:
                break;
        }
    }
}
