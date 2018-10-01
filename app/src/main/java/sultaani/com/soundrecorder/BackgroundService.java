package sultaani.com.soundrecorder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {




    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("oo","Background OnCreate called...!");


        //Intent service = new Intent(getApplicationContext(), MyIntentService.class);
       //startService(service);

        Intent imageservice = new Intent(getApplicationContext(), ImageIntentService.class);
        startService(imageservice);

    }




}
