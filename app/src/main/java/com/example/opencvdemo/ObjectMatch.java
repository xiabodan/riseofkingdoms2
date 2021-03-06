package com.example.opencvdemo;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;

import java.io.File;

class ObjectMatch {
    private static final String TAG = ObjectMatch.class.getSimpleName();

    Mat srcImage = null;
    Mat templateImage = null;
    Mat dstImage = null;

    // TM_SQDIFF = 0,
    // TM_SQDIFF_NORMED = 1,
    // TM_CCORR = 2,
    // TM_CCORR_NORMED = 3,
    // TM_CCOEFF = 4,
    // TM_CCOEFF_NORMED = 5
    int match_method = 0;

    ObjectMatch(Bitmap src, final String tempate) {
        File tempateFile = new File(tempate);
        if (!src.isRecycled() && tempateFile.exists()) {
            templateImage = Imgcodecs.imread(tempate);  // type = 16
            srcImage = new Mat();
            Utils.bitmapToMat(src, srcImage, false);
            Imgproc.cvtColor(srcImage, srcImage, Imgproc.COLOR_BGR2RGB,0);
        }
        match_method = Imgproc.TM_CCORR_NORMED;
    }

    ObjectMatch(final String src, final String tempate) {
        File srcFile = new File(src);
        File tempateFile = new File(tempate);
        if (srcFile.exists() && tempateFile.exists()) {
            srcImage = Imgcodecs.imread(src);
            templateImage = Imgcodecs.imread(tempate);
        }
        match_method = Imgproc.TM_CCORR_NORMED;
    }

    void setMethod(int method) {
        match_method = method;
    }

    public Point run(String outFile) {
        Log.i(TAG, "Running Template Matching with method " + match_method);

        // Create the result matrix
        int result_cols = srcImage.cols() - templateImage.cols() + 1;
        int result_rows = srcImage.rows() - templateImage.rows() + 1;
        dstImage = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // Do the Matching and Normalize
        Imgproc.matchTemplate(srcImage, templateImage, dstImage, match_method);
        Core.normalize(dstImage, dstImage, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        // Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(dstImage);

        Point matchLocation;
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLocation = mmr.minLoc;
        } else {
            matchLocation = mmr.maxLoc;
        }
        // Show me what you got
        Imgproc.rectangle(srcImage, matchLocation, new Point(matchLocation.x + templateImage.cols(),
                matchLocation.y + templateImage.rows()), new Scalar(0, 255, 0));
        // Save the visualized detection.
        if (outFile != null) {
            Log.i(TAG, "Writing "+ outFile);
            Imgcodecs.imwrite(outFile, srcImage);
        }

        Log.i(TAG, "matchLocation " + matchLocation);
        final double x = matchLocation.x + (templateImage.cols()/2);
        final double y = matchLocation.y + (templateImage.rows()/2);
        Point centerPoint = new Point(x, y);
        return centerPoint;
    }

    Point match(String outFile, double zoom) {
        if (srcImage == null || srcImage.empty()) {
            Log.e(TAG, "srcImage load fail");
            return null;
        }
        if (templateImage == null || templateImage.empty()) {
            Log.e(TAG, "templateImage load fail");
            return null;
        }

        Imgproc.pyrDown(templateImage, templateImage, new Size(templateImage.width() * zoom, templateImage.height() * zoom));
        Imgproc.pyrDown(srcImage, srcImage, new Size(srcImage.width() * zoom, srcImage.height() * zoom));

        Log.i(TAG, "image load success...");
        return run(outFile);
    }
}
