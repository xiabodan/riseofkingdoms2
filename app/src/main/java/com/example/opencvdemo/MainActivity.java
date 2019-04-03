package com.example.opencvdemo;

import android.content.Context;
import android.content.res.AssetManager;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final double Zoom = 0.5;

    HandlerThread handlerThread;
    Handler mHandler;
    private boolean isProcessBusy = false;

    class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        // System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        // TextView tv = findViewById(R.id.sample_text);
        // tv.setText(stringFromJNI());

        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        javaCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

        handlerThread = new HandlerThread("ImageProcess");
        handlerThread.start();
        mHandler = new MyHandler(handlerThread.getLooper());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private JavaCameraView javaCameraView;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    // TODO
                    // javaCameraView.enableView();  // 不显示摄像头
                }
                break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    private void process() {
        String path = getFilesDir().getAbsolutePath();
        copyFilesFassets(this, "img", path);
        Map matchs = Template.getMatchs();
        Iterator iterator = matchs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)iterator.next();
            Log.i(TAG, "matching " + entry.getKey() + " " + entry.getValue());
            String file = getFilesDir().getAbsolutePath();
            String srcPath =  file + File.separator + entry.getValue();
            String templatePath =  file + File.separator + entry.getKey();
            try {
                ObjectMatch objectMatch = new ObjectMatch(srcPath, templatePath);
                long start = System.currentTimeMillis();
                Point point = objectMatch.match(null, Zoom);
                long cost = System.currentTimeMillis() - start;
                if (point != null) {
                    Log.i(TAG, "matched " + templatePath + " from " + srcPath + " " + point + " cost " + cost);
                }
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "matched error " + templatePath + " " + srcPath);
                e.printStackTrace();
            }
        }
    }

    public void startImageProcess(View view) {
        if (isProcessBusy) {
            Log.i(TAG, "process is busy");
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                isProcessBusy = true;
                Log.i(TAG, "process ...");
                process();
                isProcessBusy = false;
            }
        });
    }

    public void sendEvent(View view) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "sendEvent");
                    // Runtime.getRuntime().exec("send");
                    Runtime.getRuntime().exec("input tap 840 1886");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }

    public void screencap(View view) {
        /*
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "screencap");
                    Runtime.getRuntime().exec("screencap -p /sdcard/screencap.png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 4000); */

        // second solution
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                takeScreenshot();
                long end = System.currentTimeMillis();
                Log.i(TAG, "takeScreenshot cost " + (end - start) + " ms");
            }
        }, 4000);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "need load opencv manager");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String filedir = getFilesDir().getAbsolutePath();
            File copyed = new File(filedir + File.pathSeparator + "1.png");
            if (copyed.exists())
                return;
            String fileNames[] = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {  // dir
                File file = new File(newPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath + "/" + fileName);
                }
            } else {  // file
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while ((byteCount=is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    public void startProcess(View view) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                processModel(Template.createModel(Template.PROCESS.Farmland));
            }
        }, 10000);
    }

    private void processModel(Model model) {
        final int size = model.size();
        for (int i = 0; i < size; i++) {
            String object = model.getIndexOf(i);
            Point point = lookingForObject(object);
            if (point != null) {
                Input input = new Input();
                input.inputTap((float) point.x, (float) point.y);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap takeScreenshot() {
        ScreenShot screenShot = new ScreenShot(this);
        return screenShot.takeScreenshot();
    }

    private Point lookingForObject(String object) {
        String dataDir = getFilesDir().getAbsolutePath();
        Bitmap src = takeScreenshot();
        Log.i(TAG, "lookingForObject " + object + " src " + src.getConfig().toString());

        String templatePath =  dataDir + File.separator + object;
        ObjectMatch objectMatch = new ObjectMatch(src, templatePath);
        Point point = objectMatch.match(null, Zoom);
        if (point != null) {
            Log.i(TAG, "lookingForObject " + object + " at[" + point.x + ", " + point.y + "]");
            // Coordinate transformation
            final double x = point.x / Zoom;
            final double y = point.y / Zoom;  // (x, y): Image coordinate system
            // final double x1 = 1080 - y;
            // final double y1 = x;            // (x1, y1): input key event coordinate system
            Point newPoint = new Point(x, y);
            Log.i(TAG, "lookingForObject " + object + " at[" + newPoint.x + ", " + newPoint.y + "]");
            return newPoint;
        }
        return null;
    }
}
