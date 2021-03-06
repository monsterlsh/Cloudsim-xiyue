package com.workday.insights.timeseries.predict;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.Datasets;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Tarin {
    static String wininput = "D:\\Data\\param.txt";
    static final String tracePath="/Users/lsh/Documents/ecnuIcloud/Cloudsim-xiyue/resources/instanceid_1.csv";
    static final String wintracePath = "resources/instanceid_90.csv";
    DecimalFormat df = new DecimalFormat("#.##");
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
    //??????????????????80%
    double [] iterator(double [] traces,int forecastSize,ArimaParams arimaParams,StringBuilder builder,double precent,int len,int start){

        double rmseAll = 0.0,mapeAll=0.0,smapeALL=0.0;
        double [] res = new double[4];
        res[3]=0;
        BigDecimal bigrmse = new BigDecimal(0.0);
        int size = (int)(traces.length*precent),k=size-start,ks=k;
        for(int i=start;i<size;i+=forecastSize){

            double[] origin = new double[len];
            double[] answer = new double[forecastSize];
            System.arraycopy(traces, i-len, origin, 0, origin.length);
            if(i<=size-forecastSize)
                System.arraycopy(traces, i, answer, 0, forecastSize);
            ForecastResult forecastResult = Arima.forecast_arima(origin, forecastSize, arimaParams);
            double[] forecast = forecastResult.getForecast();
            double temp = 0.0;
            double mape=0,smape=0;
            for (int j = 0; j < forecast.length; ++j) {
                //???rmse
                temp += Math.pow(forecast
                        [j] - answer[j], 2);
                //???mape

                mape += Math.abs(forecast
                        [j] - answer[j]);
                if(forecast[j]!=0 && answer[j]!=0){
                    smape += 2*mape/(forecast
                            [j] + answer[j]);
                }else smape=0;

                if(answer[j]!=0) mape /= answer[j];
            }
            mape = mape/forecast.length;
            final double rmse = Math.pow(temp / forecast.length, 0.5);
            rmseAll += rmse/k;
            mapeAll +=mape/k;
            smapeALL +=smape/k;
            bigrmse = bigrmse.add(new BigDecimal(rmse));
            if(rmseAll>30 || mapeAll>30) {
                res[3]=-1;
                break;
            }
        }
        rmseAll = bigrmse.doubleValue()/k;

        res[0]=rmseAll;
        res[1]=mapeAll;
        res[2]=smapeALL;
        return res;

    }

    @Test
    public void train_80_win_smape() throws IOException {

        double prenctile = 0.8;

        int forecastSize = 1;
        //double[] traces = trace("resources/instanceid_1.csv");
        double[] traces = trace(wintracePath);
        //20 2000
        int windows = 100;
        int start = windows;
        final int[] params = new int[]{2, 1, 0};
        final int[] paramd = new int[]{2, 1, 0};
        //final int[] paramP = new int[]{2, 1, 0};
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1,best_mape=-1,best_smape=-1;
        StringBuilder stringBuilder = new StringBuilder();
        int i=0,min=0;
        for(int p : params) for(int d : paramd) for(int q : params) for(int P : paramd)
            for(int D : paramd) for(int Q : paramd) for(int m : paramd) try {
                i++;
                ArimaParams arimaParams = new ArimaParams(p, d, q, P, D, Q, m);
                double [] res = iterator(traces, forecastSize, arimaParams, stringBuilder,prenctile,windows,start);
                final double rmse = res[0],mape = res[1];
                if(res[3]==-1){
                    System.out.printf(
                            "pass (RMSE,MAPE,SMAPE,p,d,q,P,D,Q,m)=(%f,%f,%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,mape,res[2],p,d,q,P,D,Q,m);
                    continue;
                }
                if ( best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    best_mape = mape;
                    best_smape = res[2];
                    min =i;

                    System.out.printf(
                            "Better (RMSE,MAPE,SMAPE,p,d,q,P,D,Q,m)=(%f,%f,%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,mape,res[2],p,d,q,P,D,Q,m);
                    stringBuilder.append(prenctile).append("%---windowsize=").append(windows).append("---RMSE:"+df.format(best_rmse)+", MAPE:"+df.format(best_mape)).append(", SMAPE:"+df.format(best_smape)+" ").
                            append("    param: ").append(p+",").append(d+",").append(q+",").append(P+",").append(D+",").append(Q+",").append(m+",and then \n");

                }

            } catch (final Exception ex) {
                //stringBuilder.append(" ==== wrong \n");
                //System.out.printf("\nInvalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }
        stringBuilder.append("Final Best (RMSE,MAPE,SMAPE,p,d,q,P,D,Q,m)=(").append(df.format(best_rmse)).append(", ").append(df.format(best_mape)).append(", ").append(df.format(best_smape)+",").append(best_p).append(","+best_q).append(","+best_d).append(","+best_P).append(","+best_D)
                .append(","+best_Q).append(","+best_m).append(")\n");
        stringBuilder.append("the best min is ").append(min);
        setListToString(stringBuilder);
        write(stringBuilder,"resources/res_1_80%_win=100-smape.txt");
    }
    @Test
    public void train_80_win_demo3() throws IOException {

        double prenctile = 0.8;

        int forecastSize = 1;
        double[] traces = trace(wintracePath);

        //20 2000
        int windows = 100;
        int start = windows;
        final int[] params = new int[]{2, 1, 0};
        final int[] paramd = new int[]{2, 1, 0};
        final int[] paramP = new int[]{2, 1, 0};
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1,best_mape=-1;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{2,1,0}: ").append(wintracePath).append("\n");
        int i=0,min=0;
        //int P=0, D=0, Q=0, m=0;
        //int m=0;
        for(int p : params) for(int d : paramd) for(int q : params)
            for(int P : paramd) for(int D : paramd) for(int Q : paramd)
                for(int m : paramd)
                try {
                i++;
                ArimaParams arimaParams = new ArimaParams(p, d, q, P, D, Q, m);
                double [] res = iterator(traces, forecastSize, arimaParams, stringBuilder,prenctile,windows,start);
                final double rmse = res[0],mape = res[1];
                if(res[2]==-1){
                    System.out.printf(
                            "pass (RMSE,MAPE,p,d,q,P,D,Q,m)=(%f,%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,mape,p,d,q,P,D,Q,m);
                    continue;
                }
                if ( best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;best_mape = mape;
                    min =i;

                    System.out.printf(
                            "Better (RMSE,MAPE,p,d,q,P,D,Q,m)=(%f,%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,mape,p,d,q,P,D,Q,m);
                    stringBuilder.append(prenctile).append("%---windowsize=").append(windows).append("---RMSE, MAPE"+df.format(best_rmse)+", "+df.format(best_mape)).
                            append("    param: ").append(p+",").append(d+",").append(q+",").append(P+",").append(D+",").append(Q+",").append(m+",and then \n");

                }

            } catch (final Exception ex) {
                //stringBuilder.append(" ==== wrong \n");
                //System.out.printf("\nInvalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }
        stringBuilder.append("Final Best (RMSE,MAPE,p,d,q,P,D,Q,m)=(").append(df.format(best_rmse)).append(", ").append(df.format(best_mape)).append(", ").append(best_p).append(","+best_q).append(","+best_d).append(","+best_P).append(","+best_D)
                .append(","+best_Q).append(","+best_m).append(")\n");
        stringBuilder.append("the best min is ").append(min);
        setListToString(stringBuilder);
        write(stringBuilder,"resources/res_90_80%_win=100.txt");
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
        final int[] paramd = new int[]{2,1, 0};
        System.arraycopy(traces,0,trainTest,0,trainTest.length);
        System.out.println("test now is "+wintracePath);
        for(int p : params) for(int d : paramd) for(int q : params)
            //for(int P : paramPQD)for(int D : paramPQD) for(int Q : paramPQD) for(int m : paramPQD)
                try {
                ArimaParams arimaParams = new ArimaParams(p, d, q, 0, 0, 0, 0);
                ForecastResult forecastResult = Arima.forecast_arima(trainTest, 1, arimaParams);
                double[] forecast = forecastResult.getForecast();
                double ans = traces[trainTest.length];
                double rmse = Math.pow(Math.abs(ans-forecast[0]),2);
                if(rmse<10)
                System.out.printf(
                        " (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,0, 0, 0, 0);
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
        ArimaParams arimaParams =  new ArimaParams(5,1,5,3,1,3,2);
        StringBuilder builder = new StringBuilder();
        int start = (int)(traces.length*0.8);
        try {
            double rmse = iterator(traces, forecastSize, arimaParams, builder,prenctile,2000,start)[0];
        }catch (Exception e){
            e.printStackTrace();
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


    double test_last_20(double [] traces,int forecastSize,ArimaParams arimaParams,StringBuilder builder){

        double rmseAll = 0.0,mapeAll=0.0,smapeALL=0.0;
        int k=traces.length-(int)(traces.length*0.8);
        double rmse=0,mape=0.0,smape=0.0;
        int lens =10;
        int x=0;
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
                    mape += Math.abs(forecast
                            [j] - answer[j]);
                    if(answer[j]==0 && forecast[j]==0) smape =0.0;
                    else smape = 2*mape/(forecast
                            [j] + answer[j]);
                    if(answer[j]!=0)mape/=answer[j];

                }
                rmse = Math.pow(temp / forecast.length, 0.5);
                mape = mape/forecast.length;
                smape /= forecast.length;
                if(rmse>500){
                    k--;
                    x++;
                    continue;
                }
                rmseAll += rmse;
                mapeAll+=mape;
                smapeALL+=smape;
                builder.append("[data I : " ).append(i).append(" answer: "+answer[0] + ",forecast :" +forecast[0]).append(" (rmse:" + df.format( rmse)).append(", mape: "+ df.format(mape)).append(", smape: "+ df.format(smape)).append(") ] \n");
            }
            catch (Exception e){
                System.out.println("wrong!!!!");
                //rmse = findParam(origin,answer,forecastSize)[0];
            }

        }
        rmseAll /= k;
        mapeAll /=k;
        smapeALL /= k;
        builder.append("the rmseaveg is : ").append(df.format(rmseAll)).append(",the mapeaveg is: ").append(df.format(mapeAll)).append(",the smapeaveg is:").append(df.format(smapeALL)).append("\n");
        System.out.println(builder);
        builder.append("the number of too large forecast in last 20% iteration is " + x);
        return rmseAll;
    }
    @Test
    public void wuqiong(){
        //65661
        //double[] traces = trace(wintracePath);
        ArimaParams arimaParams = new ArimaParams(2,0,0,1,1,2,3);
        int start = 65661;
        double[] origin = {66,
                133,
                137,
                58,
                87,
                141,
                158,
                200,
                133,
                54,
                112,
                104,
                75,
                145,
                108,
                58,
                91,
                120,
                100,
                141,
                125,
                95,
                79,
                91,
                100,
                141,
                70,
                91,
                108,
                79,
                95,
                170,
                87,
                100,
                87,
                58,
                79,
                175,
                87,
                50,
                104,
                83,
                70,
                120,
                116,
                58,
                100,
                145,
                116,
                79,
                133,
                112,
                133,
                229,
                262,
                350,
                304,
                316,
                329,
                241,
                229,
                275,
                154,
                95,
                127,
                158,
                116,
                116,
                111,
                105,
                100,
                100,
                95,
                120,
                116,
                87,
                100,
                87,
                108,
                124,
                104,
                100,
                79,
                95,
                104,
                145,
                62,
                79,
                100};
        //System.arraycopy(traces, start-origin.length, origin, 0, origin.length);
        double[] answer = {75};
        //System.arraycopy(traces, start, answer, 0, 1);
        ForecastResult forecastResult = Arima.forecast_arima(origin, 1, arimaParams);
        double[] forecast = forecastResult.getForecast();
        System.out.println(answer[0] + " " + forecast[0]);
    }
    @Test
    public void test_20_train() throws IOException {
        int forecastSize = 1;
        double[] traces = trace(wintracePath);
        //ArimaParams arimaParams = new ArimaParams(2,1,0,2,0,0,2);
        //2,0,0,1,1,2,3
        //1,1,5
        // 0,0,4
        // 1,1,5,0,0,0,0
        // 2,0,0,1,1,2,3  ok
        // 1,0,0,2,2,2,0 in_9
        // 2,2,2,2,2,2,0
        // 2,0,0,2,0,0,2
        //2,1,1,2,0,0,2 in_90
        // 2,1,2,2,2,2,0 in_90
        //2,0,0,2,0,0,2 in_90
        //0,1,0,2,1,1,2 in_1
        //0,1,0,2,1,1,2
        ArimaParams arimaParams = new ArimaParams(2,0,0,2,0,0,2);
        StringBuilder builder = new StringBuilder();
        builder.append("{ 2,0,0,2,0,0,2} in_90 test 20% win=100 \n ");
        test_last_20(traces,forecastSize,arimaParams,builder);
        //System.out.println(builder);
        builder.append("{ 2,0,0,2,0,0,2} in_90 test 20% win=100 \n ");
       write(builder,"resources/res_90_20%_win=10.txt");
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

