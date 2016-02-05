package com.begentgroup.simplenetwork;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by dongja94 on 2015-11-27.
 */
public class MainHandler extends Handler {
    public static final int MESSAGE_SUCCESS = 1;
    public static final int MESSAGE_FAIL = 2;
    public static final int MESSAGE_PROGRESS = 3;

    public MainHandler() {
        super(Looper.getMainLooper());
    }

    @Override
    public void handleMessage(Message msg) {
        Processor p = (Processor)msg.obj;
        switch (msg.what) {
            case MESSAGE_SUCCESS :
                p.sendSuccess();
                break;
            case MESSAGE_FAIL :
                p.sendFail();
                break;
            case MESSAGE_PROGRESS :
                p.sendProgress();
                break;
        }
    }

    void sendSuccess(Processor p) {
        sendMessage(obtainMessage(MESSAGE_SUCCESS,p));
    }

    void sendFail(Processor p) {
        sendMessage(obtainMessage(MESSAGE_FAIL, p));
    }

    void sendProgress(Processor p) {
        sendMessage(obtainMessage(MESSAGE_PROGRESS, p));
    }
}
