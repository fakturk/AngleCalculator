package netlab.fakturk.anglecalculator;

import static android.hardware.SensorManager.GRAVITY_EARTH;

/**
 * Created by fakturk on 19/04/2017.
 */

public class Newton
{
    float epsilon;
    AccSensorErrorData errorData;
    float[] calibration;


    public Newton(float epsilon)
    {

        this.epsilon = epsilon;
        init();

    }
    public Newton()
    {

        this.epsilon = 0.1f;
        init();



    }
    void init()
    {
        errorData = new AccSensorErrorData();
        calibration=new float[]{0,0,0};
    }
    void setCalibration(float[] cal)
    {
        for (int i = 0; i < 3; i++) {
//            calibration[i]=cal[i];
        }
    }

    float firstDerivative(float[] acc, float alpha)
    {
        float fd=0;
        for (int i = 0; i < 3; i++) {
            acc[i]=acc[i]+calibration[i];
        }
        float[] slopeOfError = errorData.getSlope("htc", alpha);
        float[] error = errorData.getError("htc", alpha);
        float cosTerm= (float) (Math.cos(Math.toRadians(alpha))*GRAVITY_EARTH);
        float sinTerm= (float) (Math.sin(Math.toRadians(alpha))*GRAVITY_EARTH);

        fd  =    2*(acc[0]-error[0])*slopeOfError[0]
                +2*(acc[1]+sinTerm-error[1])*(cosTerm-slopeOfError[1])
                +2*(acc[2]-cosTerm-error[2])*(sinTerm-slopeOfError[2]);
        return fd;

    }
    float secondDerivative(float[] acc, float alpha)
    {
        float sd=0;
        for (int i = 0; i < 3; i++) {
            acc[i]=acc[i]+calibration[i];
        }
        float[] slopeOfError = errorData.getSlope("htc", alpha);
        float[] error = errorData.getError("htc", alpha);
        float cosTerm= (float) (Math.cos(Math.toRadians(alpha))*GRAVITY_EARTH);
        float sinTerm= (float) (Math.sin(Math.toRadians(alpha))*GRAVITY_EARTH);
        sd  = (float)   (2*Math.pow(slopeOfError[0],2)
                        +2*Math.pow((cosTerm-slopeOfError[1]),2) + 2*(acc[1]+sinTerm-error[1])*(-sinTerm)
                        +2*Math.pow((sinTerm-slopeOfError[2]),2) + 2*(acc[2]-cosTerm-error[2])*(cosTerm)
                        );
        return sd;
    }
    float[] getAcc(float[] acc, float alpha)
    {
        float[] a=new float[]{0,0,0};
        float[] error = errorData.getError("htc", alpha);
        a[0]=acc[0]-error[0];
        a[1]= (float) (acc[1]- Math.sin(Math.toRadians(alpha))*GRAVITY_EARTH-error[1]);
        a[2]= (float) (acc[2]- Math.cos(Math.toRadians(alpha))*GRAVITY_EARTH-error[2]);
        return a;
    }
    float[] getAccWithoutError(float[] acc, float alpha)
    {
        float[] a=new float[]{0,0,0};
        float[] error = errorData.getError("htc", alpha);
        a[0]=acc[0];
        a[1]= (float) (acc[1]- Math.sin(Math.toRadians(alpha))*GRAVITY_EARTH);
        a[2]= (float) (acc[2]- Math.cos(Math.toRadians(alpha))*GRAVITY_EARTH);
        return a;
    }
    float iterate(float alphaZero,float[] acc)
    {
        float alphaK=0, alphaKPlusOne=0;

        for (int i = 0; i < 1000; i++)
        {
            float fd = firstDerivative(acc,alphaK);
            float sd = secondDerivative(acc,alphaK);
            sd = Math.abs(sd);
            if (sd<=0.001f)
            {
                sd = 0.001f;
            }
            alphaKPlusOne = alphaK-(fd/sd);
            if (Math.abs(alphaKPlusOne-alphaK)<epsilon)
            {
                break;
            }
//            while (alphaKPlusOne<0)
//            {
//                alphaKPlusOne+=360;
//            }
//            while(alphaKPlusOne>360)
//            {
//                alphaKPlusOne-=360;
//            }
//            if (Math.abs(alphaKPlusOne-alphaK)>2)
//            {
//                alphaKPlusOne = alphaK;
//            }

            alphaK = alphaKPlusOne;

        }

        return -alphaKPlusOne;
    }
}
