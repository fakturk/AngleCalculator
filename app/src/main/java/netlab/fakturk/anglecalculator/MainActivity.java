package netlab.fakturk.anglecalculator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{

    // Used to load the 'native-lib' library on application startup.
    static
    {
        System.loadLibrary("native-lib");
    }

    int degree=0;
    float[] accSDK, accNDK;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorValue();
        startService(new Intent(this, SensorService.class));
        accSDK = new float[]{0,0,0};
        accNDK = new float[]{0,0,0};

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                accSDK = (intent.getFloatArrayExtra("ACC_DATA"));
//

            }
        }, new IntentFilter(SensorService.ACTION_SENSOR_BROADCAST));
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void sensorValue();

    public void writeData(long time, float x, float y, float z)
    {
        accNDK[0]=x;
        accNDK[1]=y;
        accNDK[2]=z;
        calculateAngle(accNDK);

        String accStr =   x + " " + y + " " + z+ " " + accSDK[0] + " " + accSDK[1] + " " + accSDK[2]+"\n";

//        System.out.println(accStr);

    }
    void calculateAngle(float[] acc)
    {

    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
        stopService(new Intent(this,SensorService.class));
    }
    @Override
    protected void onPause() {

        super.onPause();
        stopService(new Intent(this,SensorService.class));


    }

    @Override
    protected void onResume() {

        super.onResume();
        startService(new Intent(this, SensorService.class));
    }

}
