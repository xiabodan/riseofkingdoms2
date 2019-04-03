package com.example.opencvdemo;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

public class Input {
    private static final String TAG = Input.class.getSimpleName();
    private int inputSource;

    public Input() {
        inputSource = InputDevice.SOURCE_UNKNOWN;
    }

    public void inputTap(float x, float y) {
        inputSource = getSource(inputSource, InputDevice.SOURCE_TOUCHSCREEN);
        sendTap(inputSource, x, y);
        return;
    }

    private static final int getSource(int inputSource, int defaultSource) {
        return inputSource == InputDevice.SOURCE_UNKNOWN ? defaultSource : inputSource;
    }

    private void sendTap(int inputSource, float x, float y) {
        long now = SystemClock.uptimeMillis();
        injectMotionEvent(inputSource, MotionEvent.ACTION_DOWN, now, x, y, 1.0f);
        injectMotionEvent(inputSource, MotionEvent.ACTION_UP, now, x, y, 0.0f);
    }

    /**
     * Builds a MotionEvent and injects it into the event stream.
     *
     * @param inputSource the InputDevice.SOURCE_* sending the input event
     * @param action the MotionEvent.ACTION_* for the event
     * @param when the value of SystemClock.uptimeMillis() at which the event happened
     * @param x x coordinate of event
     * @param y y coordinate of event
     * @param pressure pressure of event
     */
    private void injectMotionEvent(int inputSource, int action, long when, float x, float y, float pressure) {
        final float DEFAULT_SIZE = 1.0f;
        final int DEFAULT_META_STATE = 0;
        final float DEFAULT_PRECISION_X = 1.0f;
        final float DEFAULT_PRECISION_Y = 1.0f;
        final int DEFAULT_EDGE_FLAGS = 0;
        MotionEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, DEFAULT_SIZE,
                DEFAULT_META_STATE, DEFAULT_PRECISION_X, DEFAULT_PRECISION_Y,
                getInputDeviceId(inputSource), DEFAULT_EDGE_FLAGS);
        event.setSource(inputSource);
        Log.i(TAG, "injectMotionEvent: " + event);
        InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }

    private int getInputDeviceId(int inputSource) {
        final int DEFAULT_DEVICE_ID = 0;
        int[] devIds = InputDevice.getDeviceIds();
        for (int devId : devIds) {
            InputDevice inputDev = InputDevice.getDevice(devId);
            if (inputDev.supportsSource(inputSource)) {
                return devId;
            }
        }
        return DEFAULT_DEVICE_ID;
    }
}
