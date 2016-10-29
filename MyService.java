package felixgimeno.selftracking;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    final long delayedMilis = 60 * 1000;
    final Handler myHandler = new Handler();
    Runnable my_run = new Runnable() {
        public void run() {
            wd();
            Log.e("felix", "my_run executed");
            myHandler.postDelayed(my_run, delayedMilis);
        }
    };

    private void wd() {
        dbRow.writeData(getBaseContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("felix", "MyService onCreate called");
        my_run.run();
        Log.e("felix", "my_run called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO:
        return null;
    }

    @Override
    public void onDestroy() {
        myHandler.removeCallbacksAndMessages(my_run);
        super.onDestroy();
    }
}
