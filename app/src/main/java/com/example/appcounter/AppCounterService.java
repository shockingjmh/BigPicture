package com.example.appcounter;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import java.util.List;

/**
 * User: huhwook
 * Date: 2014. 1. 27.
 * Time: 오후 6:02
 */

public class AppCounterService extends Service {

    private final String LOG_NAME = AppCounterService.class.getSimpleName();

    private static Context mContext = null;
    private String recentComponentName;
    private ActivityManager mActivityManager;
    private ConnectServer connnect;
    private GpsInfo gpsInfo;

    private static DatabaseAppCounter db;

    private double longitude;
    private double latitude;

    private AlertDialog.Builder alert_confirm;

    @Override
    public void onCreate() {
        super.onCreate();

        if(mContext != this) {
            db = DatabaseAppCounter.getInstance(this.getApplicationContext());
            gpsInfo = new GpsInfo(this);
        }

        alert_confirm = new AlertDialog.Builder(this);

        Log.d(LOG_NAME, "onCreate() ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//로그 기록 추출
        if(connnect == null || !connnect.isAlive()) {
            Log.d(LOG_NAME, "null & Alive ");
            connnect = new ConnectServer(this);
            connnect.start();
        }
        Log.d(LOG_NAME, "onStartCommand() ");
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RecentTaskInfo> info = mActivityManager.getRecentTasks(1, Intent.FLAG_ACTIVITY_NEW_TASK);

        if (info != null) {
            ActivityManager.RecentTaskInfo recent = info.get(0);
            Intent mIntent = recent.baseIntent;
            ComponentName name = mIntent.getComponent();

            if (name.getPackageName().equals(recentComponentName)) {
            } else {
                Log.d(LOG_NAME, "onStartCommand() - name.getPackageName(): " + name.getPackageName());
                recentComponentName = name.getPackageName();
                db.insertAppInfo(name.getPackageName());
                Log.d(LOG_NAME, "Sqlite Insert - packageName: " + recentComponentName);

                if(gpsInfo.isGPSEnabled){
                    longitude = gpsInfo.getLongitude();
                    latitude = gpsInfo.getLatitude();
                }else{
                    longitude = 0;
                    latitude = 0;
                }

                if(recentComponentName.contains("launcher") == false && recentComponentName.contains("appcounter") == false) {
                    connnect.sendData(recentComponentName, longitude, latitude);
                }else if(connnect.receiveData() != null){
                    Log.d(LOG_NAME, "connect.isRecvData------------------");
                    alert_confirm.setTitle("Big Picture").setIcon(R.drawable.bg_main_icon)
                            .setMessage(connnect.receiveData()).setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener(){

                        public void onClick(DialogInterface dialog, int which){
                            Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(connnect.receiveData());
                            getApplicationContext().startActivity(intent);
                        };
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            return;
                        }
                    });

                    AlertDialog alert = alert_confirm.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_NAME, "onDestroy()");
        connnect.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}