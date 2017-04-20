package netlab.fakturk.anglecalculator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
    float[] accSDK, accNDK,gravity, calibration, accCor, without;
    float[] velSDK, velNDK, velCor, velWithout;
    float[] disSDK, disNDK, disCor, disWithout;

    float angle = 0, angleCorrected=0, currentAngle=0;
    AccSensorErrorData errorData;
    Gravity g;
    Orientation orientation;
    TextView angle_tv, angleCorrected_tv, difference_tv, calibrate_tv, accNDK_tv,accSDK_tv, accCor_tv,sinCos_tv,without_tv;
    TextView velNDK_tv,velSDK_tv, velCor_tv,velWithout_tv;
    TextView disNDK_tv,disSDK_tv, disCor_tv,disWithout_tv;

    Newton newton;
    Button buttonCalibrate;


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorValue();
        startService(new Intent(this, SensorService.class));
        accSDK = new float[]{0,0,0};
        accNDK = new float[]{0,0,0};
        accCor = new float[]{0,0,0};
        without = new float[]{0,0,0};

        velSDK = new float[]{0,0,0};
        velNDK = new float[]{0,0,0};
        velCor = new float[]{0,0,0};
        velWithout = new float[]{0,0,0};

        disSDK = new float[]{0,0,0};
        disNDK = new float[]{0,0,0};
        disCor = new float[]{0,0,0};
        disWithout = new float[]{0,0,0};
        
        calibration = new float[]{0,0,0};
        gravity = new float[3];
        g = new Gravity();
        orientation = new Orientation();
        newton = new Newton();
        errorData = new AccSensorErrorData();
        angle_tv = (TextView) findViewById(R.id.angle_tv_value);
        angleCorrected_tv = (TextView) findViewById(R.id.angleCorrected_tv_value);
        difference_tv = (TextView) findViewById(R.id.difference_tv_value);
        calibrate_tv = (TextView) findViewById(R.id.calibrate_tv);
        accNDK_tv = (TextView) findViewById(R.id.accNDK_tv_value);
        accSDK_tv = (TextView) findViewById(R.id.accSDK_tv_value);
        accCor_tv = (TextView) findViewById(R.id.accCorr_tv_value);
        sinCos_tv = (TextView) findViewById(R.id.sinCos_tv);
        without_tv = (TextView) findViewById(R.id.without_tv_value);
        
        velNDK_tv = (TextView) findViewById(R.id.velNDK_tv_value);
        velSDK_tv = (TextView) findViewById(R.id.velSDK_tv_value);
        velCor_tv = (TextView) findViewById(R.id.velCorr_tv_value);
        velWithout_tv = (TextView) findViewById(R.id.velWithout_tv_value);

        disNDK_tv = (TextView) findViewById(R.id.disNDK_tv_value);
        disSDK_tv = (TextView) findViewById(R.id.disSDK_tv_value);
        disCor_tv = (TextView) findViewById(R.id.disCorr_tv_value);
        disWithout_tv = (TextView) findViewById(R.id.disWithout_tv_value);
        
        buttonCalibrate = (Button) findViewById(R.id.buttonCalibrate);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                accSDK = (intent.getFloatArrayExtra("ACC_DATA"));
//

            }
        }, new IntentFilter(SensorService.ACTION_SENSOR_BROADCAST));

        buttonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonCalibrate==v)
                {
                    float[] erroZero = errorData.getError("htc",0);
                    for (int i = 0; i < 3; i++) {
                        calibration[i]=gravity[i];
                    }
                    gravity[0]=0;
                    gravity[1]=0;
                    gravity[2]=GRAVITY_EARTH;
                    calibration[2]=calibration[2]-GRAVITY_EARTH;
                    calibrate_tv.setText(calibration[0]+", "+calibration[1]+", "+calibration[2]) ;
                    newton.setCalibration(calibration);
                }
            }
        });
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

        gravity[0] = x * (GRAVITY_EARTH / accNorm)-calibration[0];
        gravity[1] = y * (GRAVITY_EARTH / accNorm)-calibration[1];
        gravity[2] = z * (GRAVITY_EARTH / accNorm)-calibration[2];
        angle = (float) Math.toDegrees( g.angleBetweenGravity(gravity)) ;
        if(angle<0)
        {
            angle=360+angle;
        }
        angle_tv.setText(Float.toString(angle));
        sinCos_tv.setText(String.format("%.02f", Math.sin(Math.toRadians(angle))*GRAVITY_EARTH)+", "+String.format("%.02f", Math.cos(Math.toRadians(angle))*GRAVITY_EARTH));
        accCor=newton.getAcc(accNDK,angle);
        without=newton.getAccWithoutError(accNDK,angle);
        if (Math.abs(angle-currentAngle)>1) {
            angleCorrected = newton.iterate(angle, accNDK);
        }
        angleCorrected_tv.setText(Float.toString(angleCorrected));
        difference_tv.setText(Float.toString(angle-angleCorrected));

        accNDK_tv.setText(String.format("%.02f", x)+", "+String.format("%.02f", y)+", "+String.format("%.02f", z));
        accSDK_tv.setText(String.format("%.02f", accSDK[0])+", "+String.format("%.02f", accSDK[1])+", "+String.format("%.02f", accSDK[2]));
        accCor_tv.setText(String.format("%.02f", accCor[0])+", "+String.format("%.02f", accCor[1])+", "+String.format("%.02f", accCor[2]));
        without_tv.setText(String.format("%.02f", without[0])+", "+String.format("%.02f", without[1])+", "+String.format("%.02f", without[2]));

        for (int i = 0; i < 3; i++) {
            velNDK[i]+=accNDK[i]*0.01;
            velSDK[i]+=accSDK[i]*0.01;
            velCor[i]+=accCor[i]*0.01;
            velWithout[i]+=without[i]*0.01;
        }
        for (int i = 0; i < 3; i++) {
            disNDK[i]+=velNDK[i]*0.01;
            disSDK[i]+=velSDK[i]*0.01;
            disCor[i]+=velCor[i]*0.01;
            disWithout[i]+=velWithout[i]*0.01;
        }

        velNDK_tv.setText(String.format("%.02f", velNDK[0])+", "+String.format("%.02f", velNDK[1])+", "+String.format("%.02f", velNDK[2]));
        velSDK_tv.setText(String.format("%.02f", velSDK[0])+", "+String.format("%.02f", velSDK[1])+", "+String.format("%.02f", velSDK[2]));
        velCor_tv.setText(String.format("%.02f", velCor[0])+", "+String.format("%.02f", velCor[1])+", "+String.format("%.02f", velCor[2]));
        velWithout_tv.setText(String.format("%.02f", velWithout[0])+", "+String.format("%.02f", velWithout[1])+", "+String.format("%.02f", velWithout[2]));

        disNDK_tv.setText(String.format("%.02f", disNDK[0])+", "+String.format("%.02f", disNDK[1])+", "+String.format("%.02f", disNDK[2]));
        disSDK_tv.setText(String.format("%.02f", disSDK[0])+", "+String.format("%.02f", disSDK[1])+", "+String.format("%.02f", disSDK[2]));
        disCor_tv.setText(String.format("%.02f", disCor[0])+", "+String.format("%.02f", disCor[1])+", "+String.format("%.02f", disCor[2]));
        disWithout_tv.setText(String.format("%.02f", disWithout[0])+", "+String.format("%.02f", disWithout[1])+", "+String.format("%.02f", disWithout[2]));



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
