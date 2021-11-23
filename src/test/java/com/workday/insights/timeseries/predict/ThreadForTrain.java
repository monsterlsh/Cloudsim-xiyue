package com.workday.insights.timeseries.predict;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;

import java.math.BigDecimal;

public class ThreadForTrain extends Thread{
    private double [] traces;
    private double [] trainTraces;
    private double [] trueTrainTraces;
    private int StartIndex;
    private int step;
    private int forecastSize;
     static int [] params ={0,1,2,3,4,5} ;
    static int [] paramd={0,1,2};
    private int trianWindows;

    public ArimaParams getBestarima() {
        return bestarima;
    }

    public void setBestarima(ArimaParams bestarima) {
        this.bestarima = bestarima;
    }

    private ArimaParams bestarima;
    public double getBestrmse() {
        return bestrmse;
    }

    public void setBestrmse(double bestrmse) {
        this.bestrmse = bestrmse;
    }

    private double bestrmse;
    public ThreadForTrain(){}

    public ThreadForTrain(double[] traces, int paramDex, int step,int trianWindows
                            ,int forecastSize) {
        this.trianWindows =trianWindows;
        this.traces = traces;
        this.StartIndex = paramDex;
        this.step = step;
        this.forecastSize = forecastSize;
        this.trainTraces = new double[step];
        System.arraycopy(this.traces, this.StartIndex, this.trainTraces, 0, this.step);
    }
    double iterator(double [] traces,int forecastSize,ArimaParams arimaParams,StringBuilder builder,int peri){
        double rmseAll = 0.0;
        BigDecimal bigrmse = new BigDecimal(0.0);
        int size = (traces.length),k=size-peri,len=peri;
        for(int i=len;i<traces.length;i+=forecastSize){
            double[] origin = new double[len];
            double[] answer = new double[forecastSize];
            System.arraycopy(traces, i-len, origin, 0, origin.length);
            if(i<=traces.length-forecastSize)
                System.arraycopy(traces, i, answer, 0, forecastSize);
            ForecastResult forecastResult = Arima.forecast_arima(origin, forecastSize, arimaParams);
            double[] forecast = forecastResult.getForecast();
            double temp = 0.0;
            for (int j = 0; j < forecast.length; ++j) {
                temp += Math.pow(forecast
                        [j] - answer[j], 2);
            }
            final double rmse = Math.pow(temp / forecast.length, 0.5)/k;
            rmseAll += rmse;
            bigrmse = bigrmse.add(new BigDecimal(rmse));
            //builder.append("[data I : " ).append(i).append("rmse:" + rmse).append("] \n");
        }
        double tmp = rmseAll;
        rmseAll = bigrmse.doubleValue();
        builder.append(" the rmse is : ").append(rmseAll).append("\n");
        return rmseAll;

    }
    public ArimaParams test_ratio(){
        ArimaParams arimaParams = null;
        int i=0;
        StringBuilder stringBuilder = new StringBuilder();
        double best_rmse = -1.0;
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        for(int p : params) for(int d : paramd) for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                i++;
                ArimaParams xarimaParams = new ArimaParams(p, d, q, P, D, Q, m);
                stringBuilder.append("******");
                stringBuilder.append(i).append("th;******\n param: ").append(p+",").append(d+",").append(q+",").append(P+",").append(D+",").append(Q+",").append(m+",and then ");

                final double rmse = iterator(traces, forecastSize, xarimaParams, stringBuilder,this.trianWindows);
                if (best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    System.out.printf(
                            "【线程" + this.getId() + " 训练】 " + "Better (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,P,D,Q,m);
                }

            } catch (final Exception ex) {
                stringBuilder.append(" ==== wrong \n");
                System.out.printf("【线程" + this.getId() + " 训练】 " + "Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }

        System.out.printf("【线程" + this.getId() + " 训练】 " +"Final Best (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n",
                best_rmse,best_p,best_d,best_q,best_P,best_D,best_Q,best_m);
        //stringBuilder.append("the best min is ").append(min);
        stringBuilder.append("????");
        arimaParams = new ArimaParams(best_p,best_d,best_q,best_P,best_D,best_Q,best_m);
        setBestrmse(best_rmse);
        return arimaParams;
    }
    @Override
    public void run() {
        System.out.println("线程" + this.getId() + " 训练");
        bestarima = test_ratio();
        System.out.println("线程" + this.getId() + "  endddd");
    }
}
