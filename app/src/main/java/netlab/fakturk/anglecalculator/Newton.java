package netlab.fakturk.anglecalculator;

import static android.hardware.SensorManager.GRAVITY_EARTH;

/**
 * Created by fakturk on 19/04/2017.
 */

public class Newton
{
    float epsilon;
    AccSensorErrorData errorData;


    public Newton(float epsilon)
    {

        this.epsilon = epsilon;
        errorData = new AccSensorErrorData();

    }
    public Newton()
    {

        this.epsilon = 0.01f;
        errorData = new AccSensorErrorData();

    }

    float firstDerivative(float[] acc, float alpha)
    {
        float fd=0;
        float[] slopeOfError = errorData.getSlope("htc", alpha);
        float[] error = errorData.getError("htc", alpha);
        float cosTerm= (float) (Math.cos(alpha)*GRAVITY_EARTH);
        float sinTerm= (float) (Math.sin(alpha)*GRAVITY_EARTH);

        fd  =    2*(acc[0]-error[0])*slopeOfError[0]
                +2*(acc[1]+sinTerm-error[1])*(cosTerm-slopeOfError[1])
                +2*(acc[2]-cosTerm-error[2])*(sinTerm-slopeOfError[2]);
        return fd;

    }
    float secondDerivative(float[] acc, float alpha)
    {
        float sd=0;
        float[] slopeOfError = errorData.getSlope("htc", alpha);
        float[] error = errorData.getError("htc", alpha);
        float cosTerm= (float) (Math.cos(alpha)*GRAVITY_EARTH);
        float sinTerm= (float) (Math.sin(alpha)*GRAVITY_EARTH);
        sd  = (float)   (2*Math.pow(slopeOfError[0],2)
                        +2*Math.pow((cosTerm-slopeOfError[1]),2) + 2*(acc[1]+sinTerm-error[1])*(-sinTerm)
                        +2*Math.pow((sinTerm-slopeOfError[2]),2) + 2*(acc[2]-cosTerm-error[2])*(cosTerm)
                        );
        return sd;
    }
    float iterate(float alphaZero,float[] acc)
    {
        float alphaK=alphaZero, alphaKPlusOne=0;

        for (int i = 0; i < 1000; i++)
        {
            float fd = firstDerivative(acc,alphaK);
            float sd = secondDerivative(acc,alphaK);
            sd = Math.abs(sd);
            if (sd==0)
            {
                sd = 1;
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

        return alphaKPlusOne;
    }
}
