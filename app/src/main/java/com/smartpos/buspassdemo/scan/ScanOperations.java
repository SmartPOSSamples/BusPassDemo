package com.smartpos.buspassdemo.scan;


import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;

import com.cloudpos.scanserver.aidl.IScanService;
import com.cloudpos.scanserver.aidl.ScanParameter;
import com.cloudpos.scanserver.aidl.ScanResult;
import com.cloudpos.sdk.util.Debug;

public class ScanOperations implements IAIDLListener {

    private Handler handler;
    private Context context;
    private int index;
    private int mode;

    private IScanService scanService;
    private ServiceConnection scanConn;

    public ScanOperations(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void performScan(int index, int mode) {
        this.index = index;
        this.mode = mode;
        AidlController.getInstance().startScanService(context, this);
    }

    @Override
    public void serviceConnected(Object objService, ServiceConnection connection) {
        if (objService instanceof IScanService) {
            scanService = (IScanService) objService;
            scanConn = connection;

            switch (index) {
                case 6:
                    customScanWindow();
                    break;
                case 7:
                    scanTimeout();
                    break;
            }
        }
    }

    private void scanTimeout() {
        new Thread() {
            @Override
            public void run() {
                ScanParameter parameter = new ScanParameter();
                parameter.set(ScanParameter.KEY_DECODER_MODE, mode);
                parameter.set(ScanParameter.KEY_SCAN_TIME_OUT, 10000);
                parameter.set(ScanParameter.KEY_ENABLE_MIRROR_SCAN, true);

                try {
                    ScanResult result = scanService.scanBarcode(parameter);
                    sendText("Result : \n" + result);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService();
            }
        }.start();

    }


    /**
     * 自定义扫码框
     */
    private void customScanWindow() {
        new Thread() {
            @Override
            public void run() {
                ScanParameter parameter = new ScanParameter();
//                parameter.set(ScanParameter.KEY_SCAN_SECTION_BORDER_COLOR, Color.rgb(0xFF, 0x00, 0x00));
//                parameter.set(ScanParameter.KEY_SCAN_SECTION_CORNER_COLOR, Color.rgb(0x00, 0xFF, 0x00));
//                parameter.set(ScanParameter.KEY_SCAN_SECTION_LINE_COLOR, Color.rgb(0x00, 0x00, 0xFF));
//                parameter.set(ScanParameter.KEY_DISPLAY_SCAN_LINE, "no");
//                parameter.set(ScanParameter.KEY_SCAN_TIP_TEXT, "WIZARPOS");
//                parameter.set(ScanParameter.KEY_SCAN_TIP_TEXTCOLOR, Color.rgb(0xFF, 0x00, 0x00));
//                parameter.set(ScanParameter.KEY_SCAN_TIP_TEXTMARGIN, 0);
//                parameter.set(ScanParameter.KEY_SCAN_TIP_TEXTSIZE, 25);
//                parameter.set(ScanParameter.KEY_DECODER_MODE, mode);

                parameter.set(ScanParameter.KEY_SCAN_TIP_TEXT, "");

                int viewHeight = DeviceUtils.getScreenHeight(context);
                int viewTop = DeviceUtils.getScreenWidth(context);
                int stausHeight = DeviceUtils.getStausHeight(context);

                //preview interface
                parameter.set(ScanParameter.KEY_UI_WINDOW_TOP, 50);
                parameter.set(ScanParameter.KEY_UI_WINDOW_LEFT, 50);
                parameter.set(ScanParameter.KEY_UI_WINDOW_WIDTH, 60);
                parameter.set(ScanParameter.KEY_UI_WINDOW_HEIGHT, 60);

                parameter.set(ScanParameter.KEY_INDICATOR_LIGHT_STATE, true);
                parameter.set(ScanParameter.KEY_FLASH_LIGHT_STATE, true);
                parameter.set(ScanParameter.KEY_ENABLE_MIRROR_SCAN, true);
                parameter.set(ScanParameter.KEY_ENABLE_SWITCH_ICON, false);
                parameter.set(ScanParameter.KEY_ENABLE_FLASH_ICON, false);

                parameter.set(ScanParameter.KEY_SCAN_MODE, "overlay");
                try {
                    if (scanService == null) {
                        Debug.debug("Result:\n + scanService==null");
                        return;
                    }
                    ScanResult result = scanService.scanBarcode(parameter);

                    if (result.getResultCode() == 1) {
                        Debug.debug("Result:\n" + result);
                        handler.obtainMessage(1).sendToTarget();
                        handler.obtainMessage(3).sendToTarget();
                        SystemClock.sleep(1000);
                        handler.obtainMessage(2).sendToTarget();
                        SystemClock.sleep(500);
                    } else {
                        Debug.debug("Result:\n" + null);
                    }
                    if (result.getResultCode() == 1) {
                        customScanWindow();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
//                unbindService();
            }
        }.start();
    }

    public void unbindService() {
        if (scanService != null) {
            context.unbindService(scanConn);
            scanService = null;
            scanConn = null;
        }
    }

    public void stopScan() {
        if (scanService != null) {
            try {
                scanService.stopScan();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendText(String msg) {
        handler.obtainMessage(0, msg).sendToTarget();
    }

    private void appendText(String msg) {
        handler.obtainMessage(2, msg + "\n").sendToTarget();
    }


}
