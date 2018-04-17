package com.example.siyux.opencv;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.photo.Photo;
import android.widget.ImageView;
import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends Activity {

    //声明前后置摄像头  back为后置 pro为前置
    //surfaceView将图像显示在屏幕上
    SurfaceView proCamView, backCamView;
    SurfaceHolder backCamholder, proCamholder2;
    String TAG = "MainActivity";
    private Camera backcamera = null, camera2;
    Camera.Parameters parameters;
    Mat courImage=null;
    Mat tarImage=null;
    private ImageView mButton;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏设置
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);//要放到加载布局文件代码之前



        //绑定后摄预览控件
        backCamView = (SurfaceView) findViewById(R.id.surfaceview1);

        //绑定前摄预览控件
        proCamView = (SurfaceView) findViewById(R.id.surfaceview2);
        backCamholder = backCamView.getHolder();
        backCamholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        backCamholder.addCallback(new surfaceholderCallbackBack());

        proCamholder2 = proCamView.getHolder();
        proCamholder2.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        proCamholder2.addCallback(new surfaceholderCallbackFont());
        proCamholder2.setFormat(PixelFormat.TRANSPARENT);
        proCamView.setZOrderOnTop(true);
        mButton=(ImageView)findViewById(R.id.getPic);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

    }
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
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }



    /**
     * 后置摄像头回调
     */
    class surfaceholderCallbackBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 获取camera对象
            //获取摄像头数量
            int cameraCount = Camera.getNumberOfCameras();
            if (cameraCount > 0) {
                //打开后摄
                backcamera = Camera.open(0);
                try {
                    // 设置预览监听
                    backcamera.setPreviewDisplay(holder);
                    Camera.Parameters parameters = backcamera.getParameters();

                    //读取配置文件 确定是否要旋转方向
                    if (MainActivity.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        parameters.set("orientation", "portrait");
                        backcamera.setDisplayOrientation(90);
                        parameters.setRotation(90);
                    } else {
                        parameters.set("orientation", "landscape");
                        backcamera.setDisplayOrientation(0);
                        parameters.setRotation(0);
                    }

                    //设置后摄参数
                    backcamera.setParameters(parameters);
                    // 启动摄像头预览
                    backcamera.startPreview();
                    System.out.println("camera.startpreview");

                } catch (IOException e) {
                    e.printStackTrace();
                    backcamera.release();
                    System.out.println("camera.release");
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            backcamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        initCamera();// 实现相机的参数初始化
                        camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    }
                }
            });

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        // 相机参数的初始化设置
        private void initCamera() {
            parameters = backcamera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
            setDispaly(parameters, backcamera);
            backcamera.setParameters(parameters);
            backcamera.startPreview();
            backcamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
        }

        // 控制图像的正确显示方向
        private void setDispaly(Camera.Parameters parameters, Camera camera) {
            if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
                setDisplayOrientation(camera, 90);
            } else {
                parameters.setRotation(90);
            }

        }

        // 实现的图像的正确显示
        private void setDisplayOrientation(Camera camera, int i) {
            Method downPolymorphic;
            try {
                downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
                if (downPolymorphic != null) {
                    downPolymorphic.invoke(camera, new Object[]{i});
                }
            } catch (Exception e) {
                Log.e("Came_e", "图像出错");
            }
        }
    }



    public void takePicture() {
        backcamera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.e("error", "onPictureTaken");
                int width=backcamera.getParameters().getPreviewSize().width;
                int height=backcamera.getParameters().getPreviewSize().height;
                courImage=new Mat((int)(height*1.5),width, CvType.CV_8UC1);
                courImage.put(0,0,data);

            }
        });
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        camera2.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                int width=camera2.getParameters().getPreviewSize().width;
                int height=camera2.getParameters().getPreviewSize().height;
                tarImage=new Mat((int)(height*1.5),width, CvType.CV_8UC1);
                courImage.put(0,0,data);

            }
        });

//        backcamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                int width=backcamera.getParameters().getPreviewSize().width;
//                int height=backcamera.getParameters().getPreviewSize().height;
//                courImage=new Mat((int)(height*1.5),width, CvType.CV_8UC1);
//                courImage.put(0,0,data);
//            }
//        });
//        camera2.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                int width=camera2.getParameters().getPreviewSize().width;
//                int height=camera2.getParameters().getPreviewSize().height;
//                tarImage=new Mat((int)(height*1.5),width, CvType.CV_8UC1);
//                courImage.put(0,0,data);
//            }
//        });
        Uri path=Uri.parse(getPackageName()+R.drawable.mask);
        Mat mask=Imgcodecs.imread(path.getPath());


        Point poin=new Point(1500,3000);
        Mat result=new Mat();
        Photo.seamlessClone(tarImage,courImage,mask,poin,result,Photo.NORMAL_CLONE);


    }
    //前置摄像头
    class surfaceholderCallbackFont implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 获取camera对象
            int cameraCount = Camera.getNumberOfCameras();
            if (cameraCount == 2) {
                camera2 = Camera.open(1);
            }
            try {
                // 设置预览监听
                camera2.setPreviewDisplay(holder);
                Camera.Parameters parameters = camera2.getParameters();

                if (MainActivity.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait");
                    camera2.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    parameters.set("orientation", "landscape");
                    camera2.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
                camera2.setParameters(parameters);
                // 启动摄像头预览
                camera2.startPreview();
                System.out.println("camera.startpreview");

            } catch (IOException e) {
                e.printStackTrace();
                camera2.release();
                System.out.println("camera.release");
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera2.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        parameters = camera2.getParameters();
                        parameters.setPictureFormat(PixelFormat.JPEG);

                        setDispaly(parameters, camera2);
                        camera2.setParameters(parameters);
                        camera2.startPreview();
                        camera2.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
                        camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    }
                }
            });

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        // 控制图像的正确显示方向
        private void setDispaly(Camera.Parameters parameters, Camera camera) {
            if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
                setDisplayOrientation(camera, 90);
            } else {
                parameters.setRotation(90);
            }

        }


        // 实现的图像的正确显示
        private void setDisplayOrientation(Camera camera, int i) {
            Method downPolymorphic;
            try {
                downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
                if (downPolymorphic != null) {
                    downPolymorphic.invoke(camera, new Object[]{i});
                }
            } catch (Exception e) {
                Log.e("Came_e", "图像出错");
            }
        }
    }

}
