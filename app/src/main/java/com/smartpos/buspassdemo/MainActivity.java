package com.smartpos.buspassdemo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;

import com.cloudpos.DeviceException;
import com.cloudpos.OperationResult;
import com.cloudpos.POSTerminal;
import com.cloudpos.card.Card;
import com.cloudpos.rfcardreader.RFCardReaderDevice;
import com.cloudpos.rfcardreader.RFCardReaderOperationResult;
import com.cloudpos.sdk.util.Debug;
import com.smartpos.buspassdemo.scan.ScanOperations;

public class MainActivity extends Activity {
    RFCardReaderDevice device = null;


    Activity mActivity;
    Context mContext;
    CycleWaitThread thread;
    boolean isRun;
    ScanOperations scanOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mActivity = this;

        initmSoundPool(mContext);
        dialog = new AlertDialog.Builder(mContext)
                .setView(R.layout.activity_pass)
                .create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startOperation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopOperation();
    }

    AlertDialog dialog;
    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (!mActivity.isFinishing()) {
                        dialog.show();
                    }
                    break;
                case 2:
                    dialog.dismiss();
                    break;
                case 3:
                    play();

                    break;
            }
        }
    };


    private SoundPool mSoundPool;
    private int soundID;


    public void initmSoundPool(Context mContext) {
        mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundID = this.mSoundPool.load(mContext, R.raw.beep, 1);
    }

    public synchronized void play() {
        mSoundPool.play(soundID, 0.1F, 0.1F, 0, 0, 1.0F);
    }

    public synchronized void release() {
//        boolean unload = mSoundPool.unload(soundID);
        mSoundPool.release();
    }


    private class CycleWaitThread extends Thread {


        @Override
        public void run() {
            int conut = 1;
            isRun = true;
            do {

                try {
                    if (device == null) {
                        device = (RFCardReaderDevice) POSTerminal.getInstance(mContext).getDevice("cloudpos.device.rfcardreader");
                    }
//                    if (conut > 5) {
//                        device.close();
//                        break;
//                    }
                    conut++;
                    device.open();
//                    RFCardReaderOperationResult operationResult = device.waitForCardPresent(TimeConstants.FOREVER);
                    RFCardReaderOperationResult operationResult = device.waitForCardPresent(2000);
                    if (operationResult.getResultCode() == OperationResult.SUCCESS) {
                        Card card = operationResult.getCard();
                        byte[] cardID = card.getID();
                        Debug.debug("pass id " + new String(cardID));
                        handler.obtainMessage(1).sendToTarget();
                        handler.obtainMessage(3).sendToTarget();
                        SystemClock.sleep(1000);
                        handler.obtainMessage(2).sendToTarget();
                        SystemClock.sleep(500);
                    }
                } catch (DeviceException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (device != null) {
                            device.close();
                            device = null;
                        }
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                }
            } while (isRun);
        }
    }

    @Override
    public void finish() {
        super.finish();
        stopOperation();
    }

    void startOperation(){
        try {
            thread = new CycleWaitThread();
            thread.start();

            scanOperations = new ScanOperations(mContext, handler);
            scanOperations.performScan(6, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopOperation(){
        try {
            isRun = false;
            if (scanOperations != null) {
                scanOperations.stopScan();
                scanOperations.unbindService();
            }
            device.close();
            release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}