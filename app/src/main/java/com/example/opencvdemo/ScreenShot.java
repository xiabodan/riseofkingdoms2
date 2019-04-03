package com.example.opencvdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.SurfaceControl;
import android.view.Surface;
import android.view.Display;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;

public class ScreenShot {
    private Bitmap mScreenBitmap;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    private WindowManager mWindowManager;
    private Matrix mDisplayMatrix;
    private Context mContext;

    ScreenShot(Context context) {
        mContext = context;
        // Inflate the screenshot layout
        mDisplayMatrix = new Matrix();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
    }

    private float getDegreesForRotation(int value) {
        switch (value) {
            case Surface.ROTATION_90:
                return 360f - 90f;
            case Surface.ROTATION_180:
                return 360f - 180f;
            case Surface.ROTATION_270:
                return 360f - 270f;
        }
        return 0f;
    }

    @TargetApi(26)
    Bitmap takeScreenshot() {
        // Take the screenshot
        float[] dims = {mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels};
        float degrees = getDegreesForRotation(mDisplay.getRotation());
        boolean requiresRotation = (degrees > 0);
        if (requiresRotation) {
            // Get the dimensions of the device in its native orientation
            mDisplayMatrix.reset();
            mDisplayMatrix.preRotate(-degrees);
            mDisplayMatrix.mapPoints(dims);
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }
        mScreenBitmap = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);
        if (mScreenBitmap == null) {
            return null;
        }
        if (requiresRotation) {
            // Rotate the screenshot to the current orientation
            Bitmap ss = Bitmap.createBitmap(mDisplayMetrics.widthPixels,
                    mDisplayMetrics.heightPixels, Bitmap.Config.ARGB_8888,
                    mScreenBitmap.hasAlpha(), mScreenBitmap.getColorSpace());
            Canvas c = new Canvas(ss);
            c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
            c.rotate(degrees);
            c.translate(-dims[0] / 2, -dims[1] / 2);
            c.drawBitmap(mScreenBitmap, 0, 0, null);
            c.setBitmap(null);
            // Recycle the previous bitmap
            mScreenBitmap.recycle();
            mScreenBitmap = ss;
        }
         mScreenBitmap.setHasAlpha(false);

        try {
            String fileDir = mContext.getFilesDir().getAbsolutePath();
            File bitmap = new File(fileDir + File.separator + "ScreenBitmap.png");
            FileOutputStream out = new FileOutputStream(bitmap);
            mScreenBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mScreenBitmap;
    }
}
