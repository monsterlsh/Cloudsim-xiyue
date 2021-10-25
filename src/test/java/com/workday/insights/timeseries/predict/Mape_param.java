package com.workday.insights.timeseries.predict;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Mape_param {
    private double commonTestCalculateMAPE(final String name, final double[] trainingData,
                                           final double[] trueForecastData, final int forecastSize,
                                           int p, int d, int q, int P, int D, int Q, int m) {

        //Make forecast

        final ForecastResult forecastResult = Arima
                .forecast_arima(trainingData, forecastSize, new ArimaParams(p, d, q, P, D, Q, m));
        //Get forecast data and confidence intervals
        final double[] forecast = forecastResult.getForecast();
        final double[] upper = forecastResult.getForecastUpperConf();
        final double[] lower = forecastResult.getForecastLowerConf();
        //Building output
        final StringBuilder sb = new StringBuilder();
        sb.append(name).append("  ****************************************************\n");
        sb.append("Input Params { ")
                .append("p: ").append(p)
                .append(", d: ").append(d)
                .append(", q: ").append(q)
                .append(", P: ").append(P)
                .append(", D: ").append(D)
                .append(", Q: ").append(Q)
                .append(", m: ").append(m)
                .append(" }");
        sb.append("\n");

        //Compute  against true forecast data
        double temp = 0.0;
        for (int i = 0; i < forecast.length; ++i) {
            temp += Math.abs(forecast[i] - trueForecastData[i])/trueForecastData[i];
        }
        final double mape = 100*temp/forecast.length;
        sb.append("\nThe mape of this Input_Param is : ").append(mape).append("\n");
        System.out.println(sb.toString());
        return mape;
    }
    //查找最优Params for finding the best mape
    //定阶分析 这里还没写
    public double [] findParam(double [] origin,double [] answer,int forecastSize) {
        final int[] params = new int[]{0, 1, 2, 3};
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_mape = -1.0;
        for(int p : params) for(int d : params) for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                final double mape = commonTestCalculateMAPE("xiyue_test",
                        origin, answer, forecastSize, p, d, q, P, D, Q, m);
                if (best_mape < 0.0 || mape < best_mape) {
                    best_mape = mape; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    System.out.printf(
                            "Better (MAPE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", mape,p,d,q,P,D,Q,m);
                }
            } catch (final Exception ex) {
                System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }
        System.out.printf("Best (MAPE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n",
                best_mape,best_p,best_d,best_q,best_P,best_D,best_Q,best_m);

        //double [] params_array={best_mape,best_p,best_d,best_q,best_P,best_D,best_Q,best_m};
        return new double[]{best_mape,best_p,best_d,best_q,best_P,best_D,best_Q,best_m};
    }

    //测试最优param matching the best MAPE
    @Test
    public void test_mape_findpqd(){
        final String winPath = "";
        final String linuxPath = "/home/linhao/sources/java_workpacle/alibaba_2018/intp_dir/instanceid_9560.csv";
        final String macPath = "/Users/lsh/Documents/ecnuIcloud/Cloudsim-xiyue/resources/instanceid_c_4680.csv";
        System.out.println(macPath);
        File file = new File(macPath);
        //get values of file
        double[] traces = value(file.toString());
        int p, d, q, P, D, Q, m,foresSize=3;
        int [][] parmas ={{}};
        List<List<Double>> mapOfFile = new ArrayList<>();
        double avg_MAPE = 0.0;
        double trainSize = traces.length * 0.6;
        int sizeOfhistory = 30;
        for(int i=sizeOfhistory;i<trainSize;i+=foresSize){
            //每隔5min
            final double[] origin = new double[i];
            final double[] answer = new double[foresSize];
            System.arraycopy(traces, 0, origin, 0, i);
            if(i<trainSize-foresSize){
                System.arraycopy(traces, i, answer, 0, foresSize);
                final double[] param = findParam(origin, answer, foresSize);
                mapOfFile.add(Arrays.stream(param).boxed().collect(Collectors.toList()));
                avg_MAPE += param[0];
//                p = (int)param[1];
//                d=(int)param[2];
//                q=(int)param[3];
//                P=(int)param[4];
//                D=(int)param[5];
//                Q=(int)param[6];
//                m=(int)param[7];
//                ForecastResult forecastResult = Arima.forecast_arima(origin, foresSize, new ArimaParams(p, d, q, P, D, Q, m));
//                double[] forecast = forecastResult.getForecast();
            }
        }
        StringBuilder sp = new StringBuilder();
        sp.append("#######################" ).
                append("\nAvg of \n").
                append(file).
                append("\n is : ").append(avg_MAPE);
        System.out.println(sp.toString());
    }
    //获取csv文件数据
    public double [] value(String file) {
        List<Integer> list = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            while ((str = in.readLine()) != null) {
                list.add(Integer.parseInt(str));
            }
            //System.out.println(str);
        } catch (IOException e) {
            System.out.println(e);
        }
        return list.stream().mapToDouble(i->i).toArray();
    }

    @Test
    public void train_main(){
         String winPath = "D:\\Data\\Traces";
         String linuxPath = "/home/linhao/sources/java_workpacle/alibaba_2018/intp_dir/instanceid_9560.csv";
         String macPath = "/Users/lsh/Documents/ecnuIcloud/Cloudsim-xiyue/resources/instanceid_c_4680.csv";
         File trace_file = new File(winPath);
         File [] files = trace_file.listFiles();
         int trainSize = (int)(files.length*0.6),testSize = (int)(files.length*0.2),yanzheng = testSize;
         File [] traning = new File[trainSize];
         File []  testing = new File[testSize];
         System.arraycopy(files,0,traning,0,testSize);
         final int[] params = new int[]{ 1, 2,0, 3};
         int d =0;
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_mape = Double.MAX_VALUE;
        for(int p : params)  for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params){
                int n = 0;
                try {
                for (File file : traning) {
                    if (file == null || !file.isFile()) continue;
                    double[] traces = value(file.toString());
                    int forecastSize = 3;
                    //选择param
                    if (p == 1) {
                        System.out.println();
                    }
                    double avg_MAPE = 0.0;
                    //double trainSize = traces.length * 0.6;
                    int sizeOfhistory = 30;
                    for (int i = sizeOfhistory; i < trainSize; i += forecastSize) {
                        //每隔5min
                        final double[] origin = new double[i];
                        final double[] answer = new double[forecastSize];

                            System.arraycopy(traces, 0, origin, 0, i);
                            System.arraycopy(traces, i, answer, 0, forecastSize);
                            final double mape = commonTestCalculateMAPE("xiyue_test",
                                    origin, answer, forecastSize, p, d, q, P, D, Q, m);
                            if (best_mape < 0.0 || mape < best_mape) {
                                best_mape = mape;
                                best_p = p;
                                best_d = d;
                                best_q = q;
                                best_P = P;
                                best_D = D;
                                best_Q = q;
                                best_m = m;
//                                System.out.printf(
//                                        "Better (MAPE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", mape,p,d,q,P,D,Q,m);
                            }
                            avg_MAPE += mape;
                            n++;


                        }

                        StringBuilder sp = new StringBuilder();
                        sp.append("#######################").
                                append("\nAvg of \n").
                                append(file).
                                append("\n is : ").append(avg_MAPE / n);
                        System.out.println(sp.toString());
                    }//for files end
                }catch (final Exception ex) {
                    System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);
                }
                System.out.printf("Better (MAPE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", best_mape,p,d,q,P,D,Q,m);
            }

    }
}
