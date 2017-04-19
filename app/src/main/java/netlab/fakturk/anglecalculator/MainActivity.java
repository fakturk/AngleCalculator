package netlab.fakturk.anglecalculator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import static android.hardware.SensorManager.GRAVITY_EARTH;

public class MainActivity extends AppCompatActivity
{

    // Used to load the 'native-lib' library on application startup.
    static
    {
        System.loadLibrary("native-lib");
    }

    int degree=0;
    float[] accSDK, accNDK,gravity;
    float angle = 0, angleCorrected=0;
    AccSensorErrorData errorData;
    Gravity g;
    Orientation orientation;
    TextView angle_tv, angleCorrected_tv, difference_tv;
    Newton newton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorValue();
        startService(new Intent(this, SensorService.class));
        accSDK = new float[]{0,0,0};
        accNDK = new float[]{0,0,0};
        gravity = new float[3];
        g = new Gravity();
        orientation = new Orientation();
        newton = new Newton();
        angle_tv = (TextView) findViewById(R.id.angle_tv_value);
        angleCorrected_tv = (TextView) findViewById(R.id.angleCorrected_tv_value);
        difference_tv = (TextView) findViewById(R.id.difference_tv_value);

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
        float accNorm = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        gravity[0] = x * (GRAVITY_EARTH / accNorm);
        gravity[1] = y * (GRAVITY_EARTH / accNorm);
        gravity[2] = z * (GRAVITY_EARTH / accNorm);
        angle = (float) Math.toDegrees( g.angleBetweenGravity(gravity)) ;
        if(angle<0)
        {
            angle=360+angle;
        }
        angle_tv.setText(Float.toString(angle));
        angleCorrected = newton.iterate(angle,accNDK);
        angleCorrected_tv.setText(Float.toString(angleCorrected));
        difference_tv.setText(Float.toString(angle-angleCorrected));

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
