package com.workday.insights.timeseries.predict;

import com.workday.insights.timeseries.arima.Datasets;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ArimaTest{
    Tarin tarin = null;
    com.workday.insights.timeseries.arima.ArimaTest arimaTest = null;
    int p=0, d=0, q=200, P=0, D=0, Q=0, m=2;
    public void findParam(double [] train,double[] trues) {
        final int[] params = new int[]{0, 1, 2, 3,4,5,7,8,9,10,11,12};
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1.0;
        for(int p : params) for(int d : params) for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                final double rmse = arimaTest.commonTestCalculateRMSE("lsh_test",
                        train, trues, 6, p, d, q, P, D, Q, m);
                if (best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    System.out.printf(
                            "Better (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,P,D,Q,m);
                }
            } catch (final Exception ex) {
                System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }
        System.out.printf("Best (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n",
                best_rmse,best_p,best_d,best_q,best_P,best_D,best_Q,best_m);
       // double lsh = commonTestCalculateRMSE("lsh", Datasets.cscchris_val, Datasets.cscchris_answer, 6, best_p, best_d, best_q, best_P, best_D, best_Q, best_m);
    }

    @Before
    public void before(){
        tarin = new Tarin();
        arimaTest = new com.workday.insights.timeseries.arima.ArimaTest();
    }
    @Test
    public void test_ariam_pqd(){
        String name = "test_ariam_pqd";
        double [] trace = tarin.trace(Tarin.tracePath);
        double ratio = 0.8;
        int trainSize =2000,foreSize=1;

        double[]trainData = new double[trainSize];
        System.arraycopy(trace,(int)(trace.length*0.4)-trainSize,trainData,0,trainSize);
        double [] trueForecastData = new double[foreSize];
        System.arraycopy(trace,(int)(trace.length*0.4),trueForecastData,0,foreSize);
        int [] params = {0,1,2,3,4};
        int [] paramd = {0,1,2};
       // findParam(trainData,trueForecastData);
        //for(int p=0;p<=12;p++)


        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1.0;
        for(int p : params) for(int d : paramd) for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                double rmse = arimaTest.commonTestCalculateRMSE("x", trainData, trueForecastData, foreSize,p,d,q,P,D,Q,m);
                if (best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    System.out.printf(
                            "Better (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,P,D,Q,m);
                }
            }catch (Exception e){
                System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);
            }
       // double rmse = arimaTest.commonTestCalculateRMSE("x", trainData, trueForecastData, foreSize,2,0,1,0,0,0,1);
        System.out.printf("Final Best (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n",
                best_rmse,best_p,best_d,best_q,best_P,best_D,best_Q,best_m);

    }
    @Test
    public void testchangesize(){
        String name = "test_ariam_pqd";
        double [] trace = tarin.trace(Tarin.tracePath);
        double ratio = 0.8;

        int trainSize =10,foreSize=1;
        ArrayList<Double>allrmse = new ArrayList<>();
        for(;trainSize<=2000;trainSize++){
            double[]trainData = new double[trainSize];
            System.arraycopy(trace,(int)(trace.length*0.4)-trainSize,trainData,0,trainSize);
            double [] trueForecastData = new double[foreSize];
            System.arraycopy(trace,(int)(trace.length*0.4),trueForecastData,0,foreSize);
            int [] params = {0,1,2,3,4};
            int [] paramd = {0,1,2};
            double rmse = arimaTest.commonTestCalculateRMSE("x", trainData, trueForecastData, foreSize,1,0,0,0,1,0,2);
            allrmse.add(rmse);
        }
        for (Double aDouble : allrmse) {
            System.out.println(aDouble);
        }
//        double[]trainData = new double[trainSize];
//        System.arraycopy(trace,2000-trainSize,trainData,0,trainSize);
//        double [] trueForecastData = new double[foreSize];
//        System.arraycopy(trace,2000,trueForecastData,0,foreSize);
//        int [] params = {0,1,2,3,4};
//        int [] paramd = {0,1,2};
//        double rmse = arimaTest.commonTestCalculateRMSE("x", trainData, trueForecastData, foreSize,
//                1,0,0,0,1,0,2);
    }
}