package com.workday.insights.timeseries.predict;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.Datasets;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Tarin {
    static String wininput = "D:\\Data\\param.txt";
    static final String tracePath="/Users/lsh/Documents/ecnuIcloud/Cloudsim-xiyue/resources/instanceid_1.csv";
    static final String wintracePath = "resources/instanceid_90.csv";
    List<ArimaParams> list = new ArrayList<>();
    public void setListToString(StringBuilder builder){
        if(list.isEmpty()){
            System.out.println("empty");
            return;
        }
        for (ArimaParams arimaParams : list) {
            builder.append("("+arimaParams.p+",").append(arimaParams.d+",").append(arimaParams.q+",")
                    .append(arimaParams.P+",").append(arimaParams.D+",").append(arimaParams.Q+",").append(arimaParams.m+")\n");
        }
    }
    public void fileWrite(String data){
        try{
            File file =new File(wininput);

            //if file doesnt exists, then create it
            if(!file.exists()){
                file.createNewFile();
            }

            //true = append file
            FileWriter fileWritter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.close();

            System.out.println("Done");

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    static List<double []>paramList = new ArrayList<>();



    public void  write(StringBuilder builder,String name)throws IOException{
        File file =new File(name);
        if(!file.exists()){
            file.createNewFile();
        }
        FileWriter fileWritter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
        bufferWritter.write(builder.toString());
        bufferWritter.close();

    }
    //一个文件中的80%
    double iterator(double [] traces,int forecastSize,ArimaParams arimaParams,StringBuilder builder,double precent,int len,int start,double bestrmse){

        double rmseAll = 0.0;
        BigDecimal bigrmse = new BigDecimal(0.0);
        int size = (int)(traces.length*precent),k=size-start;
        for(int i=start;i<size;i+=forecastSize){

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
//            if(rmseAll>50 || rmseAll > bestrmse) {
//                System.out.println("大于50 pass or 大于best");break;}
            //builder.append("[data I : " ).append(i).append("rmse:" + rmse).append("] \n");

        }
        double tmp = rmseAll;
        rmseAll = bigrmse.doubleValue();
        builder.append(" the rmse is : ").append(rmseAll).append("\n");
        if(rmseAll >100) rmseAll =110;
        return rmseAll;

    }


    @Test
    public void readInstance() throws IOException {
//        String filename = "resources/instanceid_c_4680.csv";
//        double [] ori = trace(filename);
//        for (int i = 0; i < ori.length; i++) {
//            System.out.println(ori[i]);
//        }
        File file =new File("resources/param.txt");
        if(!file.exists()){
            file.createNewFile();
        }
        FileWriter fileWritter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

        bufferWritter.close();
    }
    @Test
    public void fn(){
        double [] traces = trace(wintracePath);
        int x = (int)(traces.length*0.8);
        double [] trainTest = new double[x];
        final int[] params = new int[]{5,4,3,2, 1, 0};
        final  int[] paramPQD = new int[]{3,2,1};
        final int[] paramd = new int[]{1, 0};
        System.arraycopy(traces,0,trainTest,0,trainTest.length);
        for(int p : params) for(int d : paramd) for(int q : params) for(int P : paramPQD)
            for(int D : paramPQD) for(int Q : paramPQD) for(int m : paramPQD)try {
                ArimaParams arimaParams = new ArimaParams(p, d, q, P, D, Q, m);
                ForecastResult forecastResult = Arima.forecast_arima(trainTest, 1, arimaParams);
                double[] forecast = forecastResult.getForecast();
                double ans = traces[trainTest.length];
                double rmse = Math.pow(Math.abs(ans-forecast[0]),2);
                if(rmse<10)
                System.out.printf(
                        " (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,P,D,Q,m);
            }catch (Exception e){
               // System.out.println("xxxx");
            }

    }
    @Test
    public void test_single(){
        //2,0,0,2,0,0,2 0-20% rmseavg=25
        //3,1,3,3,3,3,0 rmseavg = 0.0048
        //4,1,4,4,4,4,0
        // 1,1,1,0,0,2,2 good?
        //0,0,2,0,0,2,1
        //0,0,2,0,0,1,2
        double prenctile = 1;
        int forecastSize = 1;
        double [] traces = trace(wintracePath);
        ArimaParams arimaParams =  new ArimaParams(0,0,2,0,0,2,1);
        StringBuilder builder = new StringBuilder();
        int start = (int)(traces.length*0.8);
        try {
            double rmse = iterator(traces, forecastSize, arimaParams, builder,prenctile,2000,start,50);
        }catch (Exception e){
            System.out.println("wrong");
        }
        //double rmse = iterator(traces, forecastSize, arimaParams, builder,prenctile,200,start,50);
        System.out.println(builder);
    }
    @Test
    public void getlist(){
        for (ArimaParams arimaParams : list) {
            System.out.println();
        }
    }
    //windows test
    @Test
    public void train_80_win() throws IOException {
        double prenctile = 0.8;

        int forecastSize = 1;
        double[] traces = trace(wintracePath);
        int start = (int)(traces.length*0.8);
        //20 2000
        int windows = 200;
        final int[] params = new int[]{4,3,2, 1, 0};
        final int[] paramd = new int[]{1, 0};
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1;
        StringBuilder stringBuilder = new StringBuilder();
        int i=0,min=0;
        for(int p : params) for(int d : paramd) for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                i++;
                ArimaParams arimaParams = new ArimaParams(p, d, q, P, D, Q, m);
                stringBuilder.append(i).append("th;").append(prenctile).append("%---windowsize=").append(windows).append("*****\n param: ").append(p+",").append(d+",").append(q+",").append(P+",").append(D+",").append(Q+",").append(m+",and then ");

                final double rmse = iterator(traces, forecastSize, arimaParams, stringBuilder,prenctile,windows,start,best_rmse);

                if (!(rmse>50) && best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    min =i;
                    if(best_rmse <5) best_rmse = 5;
                    if(rmse < 10) list.add(arimaParams);
                    System.out.printf(
                            "Better (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,P,D,Q,m);
                }

            } catch (final Exception ex) {
                //stringBuilder.append(" ==== wrong \n");
                System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }
            stringBuilder.append("Final Best (RMSE,p,d,q,P,D,Q,m)=(").append(best_rmse).append(","+best_q).append(","+best_d).append(","+best_P).append(","+best_D)
                            .append(","+best_Q).append(","+best_m).append(")\n");
            stringBuilder.append("the best min is ").append(min);
            setListToString(stringBuilder);
            //write(stringBuilder,"resources/result.txt");
    }

    double test_last_20(double [] traces,int forecastSize,ArimaParams arimaParams,StringBuilder builder){
        double rmseAll = 0.0;
        int k=traces.length-(int)(traces.length*0.8);
        double rmse=0;
        int lens =2000;
        for(int i=(int)(traces.length*0.8);i<traces.length;i+=forecastSize){
            double[] origin = new double[lens];
            double[] answer = new double[forecastSize];
            System.arraycopy(traces, i-origin.length, origin, 0, origin.length);
            if(i<=traces.length-forecastSize)
                System.arraycopy(traces, i, answer, 0, forecastSize);
            ForecastResult forecastResult = null;
            try{
                forecastResult = Arima.forecast_arima(origin, forecastSize, arimaParams);
                double[] forecast = forecastResult.getForecast();
                double temp = 0.0;
                for (int j = 0; j < forecast.length; ++j) {
                    temp += Math.pow(forecast
                            [j] - answer[j], 2);
                }
                rmse = Math.pow(temp / forecast.length, 0.5);
                rmseAll += rmse/k;
            }
            catch (Exception e){
                System.out.println("wrong!!!!");
                //rmse = findParam(origin,answer,forecastSize)[0];
            }


            k++;
            builder.append("[data I : " ).append(i).append("rmse:" + rmse).append("rmseALL: "+ rmseAll).append("] \n");
        }
        //rmseAll /= k;
        builder.append(" the rmse is : ").append(rmseAll).append("\n");
        System.out.println(builder);
        return rmseAll;
    }
    @Test
    public void test_20_train() throws IOException {
        String mac = wintracePath;
        int forecastSize = 1;
        double[] traces = trace(mac);
        //ArimaParams arimaParams = new ArimaParams(2,1,0,2,0,0,2);
        ArimaParams arimaParams = new ArimaParams(5,1,5,3,1,3,2);
        StringBuilder builder = new StringBuilder();

        test_last_20(traces,forecastSize,arimaParams,builder);
        write(builder,"resources/res_90_20%_test.txt");
    }

    public ArimaParams averageParam(){
        double [] params = new double[8];
        double paramSize=paramList.size();
        try {
            File file =new File(wininput);

            //if file doesnt exists, then create it
            if(!file.exists()){
                file.createNewFile();
            }

            //true = append file
            FileWriter fileWritter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);




            for (double[] doubles : paramList) {
                bufferWritter.write((int)doubles[0]);
                bufferWritter.write(",");
                for (int i = 1; i < doubles.length; i++) {
                    params[i-1] += doubles[i];
                    bufferWritter.write((int) doubles[i]);
                    bufferWritter.write(",");
                }
                bufferWritter.write("\n");
            }
            for (int i = 0; i < params.length; i++) {
                params[i] /= paramSize;
                bufferWritter.write((int) params[i]);
            }
            bufferWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  new ArimaParams((int)params[0],(int)params[1],(int)params[2],(int)params[3],(int)params[4],(int)params[5],(int)params[6]);
    }
    public void test_this_param(String file,int forecastSize,ArimaParams params){
        System.out.println("Test ********");
        double [] traces = trace(file);
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=(int)(traces.length*0.6);i<traces.length;i+=forecastSize){
            double[] origin = new double[Math.min(i,30)];
            double[] trueForecastData = new double[forecastSize];
            System.arraycopy(traces, Math.max(0,i-30), origin, 0, origin.length);

            if(i<traces.length-forecastSize)
                System.arraycopy(traces, i, trueForecastData, 0, forecastSize);
            ForecastResult forecastResult = Arima.forecast_arima(origin, forecastSize, params);
            double[] forecast = forecastResult.getForecast();
            double temp = 0.0;
            for (int j = 0; j < forecast.length; ++j) {
                temp += Math.pow(forecast[j] - trueForecastData[j], 2);
            }
            final double rmse = Math.pow(temp / forecast.length, 0.5);
            stringBuilder.append("origin: ");
            for (double v : origin) {
                stringBuilder.append(v).append(",");
            }
            stringBuilder.append("\n").append("trueForecastData: ");
            for (double v : trueForecastData) {
                stringBuilder.append(v).append(",");
            }
            stringBuilder.append("\n").append("forecast: ");
            for (double v : forecast) {
                stringBuilder.append(v).append(",");
            }
            stringBuilder.append("\n").append("rmse: ").append(rmse).append("\n\n");

            System.out.println(stringBuilder);
        }
    }
    public void train_single(String file,int forecastSize) {
        double [] traces = trace(file);
        for(int i=10;i<traces.length*0.6;i+=forecastSize){
            double[] origin = new double[Math.min(i,30)];
            double[] answer = new double[forecastSize];
            System.arraycopy(traces, Math.max(0,i-30), origin, 0, origin.length);
            if(i<traces.length-forecastSize)
                System.arraycopy(traces, i, answer, 0, forecastSize);
            double[] tmp = findParam(origin, answer, forecastSize);
            System.out.println(file + "end *********************************\n");
            String flinename = "D:\\Data\\param.txt";

            for (double v : tmp) {
                System.out.print(v+",");
            }
            System.out.println();
        }
        //System.out.println(Arrays.stream(traces).toArray());
        //System.out.println(subfile_name.substring(last)+" accuracy : ");
    }

    public double [] findParam(double [] origin,double [] trueForecastData,int forecastSize) {
        ForecastResult forecastResult = null;
        final int[] params = new int[]{0, 1, 2};
        int[] params_pq = new int[]{0,1,2,3,4,5,6,7};
        double [] res = new double[forecastSize];
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1.0;
        for(int p : params_pq) for(int d : params) for(int q : params_pq) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                 forecastResult = Arima
                        .forecast_arima(origin, forecastSize, new ArimaParams(p, d, q, P, D, Q, m));
                //Get forecast data and confidence intervals
                final double[] forecast = forecastResult.getForecast();
                final StringBuilder sb = new StringBuilder();
                sb.append("Input Params { ")
                        .append("p: ").append(p)
                        .append(", d: ").append(d)
                        .append(", q: ").append(q)
                        .append(", P: ").append(P)
                        .append(", D: ").append(D)
                        .append(", Q: ").append(Q)
                        .append(", m: ").append(m)
                        .append(" }");
                sb.append("\n\nFitted Model RMSE: ").append(forecastResult.getRMSE());
                sb.append("\n");

                //Compute RMSE against true forecast data
                double temp = 0.0;
                for (int i = 0; i < forecast.length; ++i) {
                    temp += Math.pow(forecast[i] - trueForecastData[i], 2);
                }
                final double rmse = Math.pow(temp / forecast.length, 0.5);
                sb.append("RMSE = ").append(rmse).append("\n\n");
                //System.out.println(sb.toString());
                if (best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    //maps.put(best_rmse,new double[p,d,q,P,D,Q,m]);
                    for (int i = 0; i < forecast.length; ++i) {
                        res[i] = forecast[i];
                    }
//                    System.out.printf(
//                            "Better (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,P,D,Q,m);
                }
            } catch (final Exception ex) {
//                System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }
        double [] params_array={best_rmse,best_p,best_d,best_q,best_P,best_D,best_Q,best_m};
        //ForecastResult forecastResult = Arima.forecast_arima(origin, forecastSize, new ArimaParams(best_p,best_d,best_q,best_P,best_D,best_Q,best_m));
        System.out.printf("Best (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", best_rmse,best_p,best_d,best_q,best_P,best_D,best_Q,best_m);
        //System.out.println(res);
        paramList.add(params_array);
        return params_array;
    }

    public double [] trace(String file) {
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
}

