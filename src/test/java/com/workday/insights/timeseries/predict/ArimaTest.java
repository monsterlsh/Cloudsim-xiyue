package com.workday.insights.timeseries.predict;

/*
 * Copyright (c) 2017-present, Workday, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the LICENSE file in the root repository.
 */





import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.Datasets;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;
import com.workday.insights.timeseries.timeseriesutil.ForecastUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ARIMA Tests
 */
public class ArimaTest {

    private final DecimalFormat df = new DecimalFormat("##.#####");

    private double[] commonTestLogic(final String name, final double[] dataSet,
                                     final double forecastRatio,
                                     int p, int d, int q, int P, int D, int Q, int m) {

        //Compute forecast and training size
        final int forecastSize = (int) (dataSet.length * forecastRatio);
        final int trainSize = dataSet.length - forecastSize;
        //Separate data set into training data and test data
        final double[] trainingData = new double[trainSize];
        System.arraycopy(dataSet, 0, trainingData, 0, trainSize);
        final double[] trueForecastData = new double[forecastSize];
        System.arraycopy(dataSet, trainSize, trueForecastData, 0, forecastSize);

        return commonTestSimpleForecast(name + " (common test)", trainingData, trueForecastData,
                forecastSize, p, d, q,
                P, D, Q, m);
    }

    private double forecastSinglePointLogic(final String name, final double[] dataSet,
                                            int p, int d, int q, int P, int D, int Q, int m) {
        //Compute forecast and training size
        final int forecastSize = 1;
        final int trainSize = dataSet.length - forecastSize;
        //Separate data set into training data and test data
        final double[] trainingData = new double[trainSize];
        System.arraycopy(dataSet, 0, trainingData, 0, trainSize);
        final double[] trueForecastData = new double[forecastSize];
        System.arraycopy(dataSet, trainSize, trueForecastData, 0, forecastSize);

        return commonTestSimpleForecast(name + " (forecast single point)", trainingData,
                trueForecastData, forecastSize, p, d, q,
                P, D, Q, m)[0];
    }

    private String dbl2str(final double value) {
        String rep = df.format(value);
        String padded = String.format("%15s", rep);
        return padded;
    }

    private double[] commonTestSimpleForecast(final String name, final double[] trainingData,
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
        sb.append("\n\nFitted Model RMSE: ").append(dbl2str(forecastResult.getRMSE()));
        sb.append("\n\n      TRUE DATA    |     LOWER BOUND          FORECAST       UPPER BOUND\n");

        for (int i = 0; i < forecast.length; ++i) {
            sb.append(dbl2str(trueForecastData[i])).append("    | ")
                    .append(dbl2str(lower[i])).append("   ").append(dbl2str(forecast[i]))
                    .append("   ").append(dbl2str(upper[i]))
                    .append("\n");
        }

        sb.append("\n");

        //Compute RMSE against true forecast data
        double temp = 0.0;
        for (int i = 0; i < forecast.length; ++i) {
            temp += Math.pow(forecast[i] - trueForecastData[i], 2);
        }
        final double rmse = Math.pow(temp / forecast.length, 0.5);
        double mape = 0.0;
        for (int i = 0; i < forecast.length; ++i) {
            mape += Math.pow(forecast[i] - trueForecastData[i], 2)/trueForecastData[i];
        }
        mape = mape/forecast.length;
        sb.append("RMSE = ").append(dbl2str(rmse)).append("\n\n");
        System.out.println(sb.toString());

        //mape
        sum += mape;
        n++;
        //if(rmse>20)
        rmsermse.add(mape);
        return forecast;
    }
    double sum = 0;
    int n=0;
    private double commonTestCalculateRMSE(final String name, final double[] trainingData,
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
        sb.append("\n\nFitted Model RMSE: ").append(dbl2str(forecastResult.getRMSE()));
        sb.append("\n\n      TRUE DATA    |     LOWER BOUND          FORECAST       UPPER BOUND\n");

        for (int i = 0; i < forecast.length; ++i) {
            sb.append(dbl2str(trueForecastData[i])).append("    | ")
                    .append(dbl2str(lower[i])).append("   ").append(dbl2str(forecast[i]))
                    .append("   ").append(dbl2str(upper[i]))
                    .append("\n");
        }

        sb.append("\n");

        //Compute RMSE against true forecast data
        double temp = 0.0;
        for (int i = 0; i < forecast.length; ++i) {
            temp += Math.pow(forecast[i] - trueForecastData[i], 2);
        }
        final double rmse = Math.pow(temp / forecast.length, 0.5);
        sb.append("RMSE = ").append(dbl2str(rmse)).append("\n\n");
        System.out.println(sb.toString());
        return rmse;
    }
    @Test
    public void lsh_test(){
        double[] dataArray = new double[] {0.11999, 0.04002666666666664, 0.03000333333333333, 0.02000333333333333, 0.049990000000000014, 0.05, 0.04000333333333333, 0.08998333333333333, 0.010026666666666673, 0.059983333333333326};

// Set ARIMA model parameters.
        int p = 2;
        int d = 0;
        int q = 2;
        int P = 1;
        int D = 1;
        int Q = 0;
        int m = 0;
//        p =1;
//        d =2;
//        q=0;
        int forecastSize = 3;

// Obtain forecast result. The structure contains forecasted values and performance metric etc.
        ForecastResult forecastResult = Arima.forecast_arima(dataArray, forecastSize, new ArimaParams(p, d, q, P, D, Q, m));

// Read forecast values
        double[] forecastData = forecastResult.getForecast(); // in this example, it will return { 2 }

// You can obtain upper- and lower-bounds of confidence intervals on forecast values.
// By default, it computes at 95%-confidence level. This value can be adjusted in ForecastUtil.java
        double[] uppers = forecastResult.getForecastUpperConf();
        double[] lowers = forecastResult.getForecastLowerConf();

// You can also obtain the root mean-square error as validation metric.
        double rmse = forecastResult.getRMSE();

// It also provides the maximum normalized variance of the forecast values and their confidence interval.
        double maxNormalizedVariance = forecastResult.getMaxNormalizedVariance();

// Finally you can read log messages.
        String log = forecastResult.getLog();
        System.out.println(log);
        for (double forecastDatum : forecastData) {
            System.out.print(forecastDatum+" ");
        }
        System.out.println();
        //System.out.println(forecastData.toString());
        System.out.println("rmse"+rmse);
    }

    private void commonAssertionLogic(double[] dataSet, double actualValue, double delta) {
        double lastTrueValue = dataSet[dataSet.length - 1];
        Assert.assertEquals(lastTrueValue, actualValue, delta);
    }

    @Test
    public void arma2ma_test() {
        double[] ar = {1.0, -0.25};
        double[] ma = {1.0, 2.0};
        int lag = 10;
        double[] ma_coeff = ForecastUtil.ARMAtoMA(ar, ma, lag);
        double[] true_coeff = {1.0, 2.0, 3.75, 3.25, 2.3125, 1.5, 0.921875, 0.546875, 0.31640625,
                0.1796875};

        Assert.assertArrayEquals(ma_coeff, true_coeff, 1e-6);
    }

    @Test(expected = RuntimeException.class)
    public void common_logic_fail_test() {
        commonTestLogic("simple12345", Datasets.simple12345, 0.1, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test(expected = RuntimeException.class)
    public void one_piont_fail_test() {
        forecastSinglePointLogic("simple12345", Datasets.simple12345, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void a10_test() {
        commonTestLogic("a10_test", Datasets.a10_val, 0.1, 3, 0, 0, 1, 0, 1, 12);
        double actualValue = forecastSinglePointLogic("a10_test", Datasets.a10_val, 3, 0, 0, 1, 0,
                1, 12);
        commonAssertionLogic(Datasets.a10_val, actualValue, 5.05);
    }

    @Test
    public void usconsumption_test() {
        commonTestLogic("usconsumption_test", Datasets.usconsumption_val, 0.1, 1, 0, 3, 0, 0, 1,
                42);
        double lastActualData = forecastSinglePointLogic("usconsumption_test",
                Datasets.usconsumption_val, 1, 0, 3, 0, 0, 1, 42);
        commonAssertionLogic(Datasets.usconsumption_val, lastActualData, 0.15);
    }

    @Test
    public void euretail_test() {
        commonTestLogic("euretail_test", Datasets.euretail_val, 0.1, 3, 0, 3, 1, 1, 0, 0);
        double lastActualData = forecastSinglePointLogic("euretail_test", Datasets.euretail_val, 3,
                0, 3, 1, 1, 0, 0);
        commonAssertionLogic(Datasets.euretail_val, lastActualData, 0.23);
    }

    @Test
    public void sunspots_test() {
        commonTestLogic("sunspots_test", Datasets.sunspots_val, 0.1, 2, 0, 0, 1, 0, 1, 21);
        double actualValue = forecastSinglePointLogic("sunspots_test", Datasets.sunspots_val, 2, 0,
                0, 1, 0, 1, 21);
        commonAssertionLogic(Datasets.sunspots_val, actualValue, 11.83);

    }

    @Test
    public void ausbeer_test() {
        commonTestLogic("ausbeer_test", Datasets.ausbeer_val, 0.1, 2, 0, 1, 1, 0, 1, 8);
        double actualValue = forecastSinglePointLogic("ausbeer_test", Datasets.ausbeer_val, 2, 0, 1,
                1, 0, 1, 8);
        commonAssertionLogic(Datasets.ausbeer_val, actualValue, 8.04);

    }

    @Test
    public void elecequip_test() {
        commonTestLogic("elecequip_test", Datasets.elecequip_val, 0.1, 3, 0, 1, 1, 0, 1, 6);
        double actualValue = forecastSinglePointLogic("elecequip_test", Datasets.elecequip_val, 3,
                0, 1, 1, 0, 1, 6);
        commonAssertionLogic(Datasets.elecequip_val, actualValue, 5.63);
    }

    @Test
    public void chicago_potholes_test() {
        commonTestLogic("chicago_potholes_test", Datasets.chicago_potholes_val, 0.1, 3, 0, 3, 0, 1,
                1, 14);
        double actualValue = forecastSinglePointLogic("chicago_potholes_test",
                Datasets.chicago_potholes_val, 3, 0, 3, 0, 1, 1, 14);
        commonAssertionLogic(Datasets.chicago_potholes_val, actualValue, 25.94);
    }

    @Test
    public void simple_data1_test() {
        final double forecast = forecastSinglePointLogic("simple_data1_test",
                Datasets.simple_data1_val, 3, 0, 3, 1, 1, 0, 0);
        assert (forecast == 2);
    }

    @Test
    public void simple_data2_test() {
        final double forecast = forecastSinglePointLogic("simple_data2_test",
                Datasets.simple_data2_val, 0, 0, 1, 0, 0, 0, 0);
        assert (forecast == 2);
    }

    @Test
    public void simple_data3_test() {
        final double[] forecast = commonTestSimpleForecast("simple_data3_test",
                Datasets.simple_data3_val, Datasets.simple_data3_answer, 7, 3, 0, 0, 1, 0, 1, 12);
        double lastActualData = forecast[forecast.length - 1];
        commonAssertionLogic(Datasets.simple_data3_answer, lastActualData, 0.31);
    }

    @Test
    public void cscchris_test() {
        final int[] params = new int[]{0, 1, 2, 3};
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1.0;
        for(int p : params) for(int d : params) for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                final double rmse = commonTestCalculateRMSE("cscchris_test",
                        Datasets.cscchris_val, Datasets.cscchris_answer, 6, p, d, q, P, D, Q, m);
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
    }
    public double [] findParam(double [] origin,double [] answer,int forecastSize) {
        final int[] params = new int[]{0, 1, 2, 3};
        int best_p, best_d, best_q, best_P, best_D, best_Q, best_m;
        best_p = best_d = best_q = best_P = best_D = best_Q = best_m = -1;
        double best_rmse = -1.0;
        for(int p : params) for(int d : params) for(int q : params) for(int P : params)
            for(int D : params) for(int Q : params) for(int m : params) try {
                final double rmse = commonTestCalculateRMSE("cscchris_test",
                        origin, answer, forecastSize, p, d, q, P, D, Q, m);
                if (best_rmse < 0.0 || rmse < best_rmse) {
                    best_rmse = rmse; best_p = p; best_d = d; best_q = q;
                    best_P = P; best_D = D; best_Q = q; best_m = m;
                    //maps.put(best_rmse,new double[p,d,q,P,D,Q,m]);
                    System.out.printf(
                            "Better (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n", rmse,p,d,q,P,D,Q,m);
                }
            } catch (final Exception ex) {
                System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);

            }
        System.out.printf("Best (RMSE,p,d,q,P,D,Q,m)=(%f,%d,%d,%d,%d,%d,%d,%d)\n",
                best_rmse,best_p,best_d,best_q,best_P,best_D,best_Q,best_m);

        double [] params_array={best_rmse,best_p,best_d,best_q,best_P,best_D,best_Q,best_m};
        return params_array;
    }
    double min_rmse,max_rmse ;
    double [] pdqPDQm ;
    Map<Double,Integer> maps;
    Map<Integer,Double> numsForRmse;
    List<List<Integer>> ans ;
    List<Double> rmsermse;
    @Before
    public  void before(){
        rmsermse = new ArrayList<>();
        ans = new ArrayList<>();
       min_rmse = Double.MAX_VALUE;
        pdqPDQm = new double[7];
        maps = new TreeMap<>();
        numsForRmse = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
    }
    @Test
    public void  test(){
        //String inputFolder = NonPowerAware.class.getClassLoader().getResource("workload/alibaba2017/instance_all").getPath();
        String inputFolder = "/home/linhao/sources/java_workpacle/alibaba_2018/intp_dir";
        System.out.println(inputFolder);
        File file = new File(inputFolder);

        List<List<Double>> ans = new ArrayList<>();
        tasks_files(file);
        for (double v : pdqPDQm) {
            System.out.println(v);
        }
        System.out.println("RMSE:"+min_rmse);
    }
    public void tasks_files(File file){
        File[] subfiles = file.listFiles();
        int nums = 10000;
        if (subfiles != null) {
            for (File subfile :subfiles ) {
                if(--nums <= 0) break;
                action(subfile);
            }
        }
        int idx;
        System.out.println((idx=maps.get(min_rmse)));
        for(double x : ans.get(idx)){
            System.out.print(x+",");
        }
    }

    @Test
    public void testaction(){

        String linuxpath = "/home/linhao/sources/java_workpacle/alibaba_2018/intp_dir/instanceid_9560.csv";
        String macpath = "/Users/lsh/Documents/ecnuIcloud/Cloudsim-xiyue/resources/instanceid_c_4680.csv";

        String inputFolder = macpath;


        System.out.println(inputFolder);
        File file = new File(inputFolder);
        //double [] traces = action(file);
        double[] traces = trace(file.toString());
        int idx=-1;
        if(!maps.isEmpty())
        System.out.println((idx=maps.get(min_rmse)));
//        for(double x : ans.get(idx)){
//            System.out.print(x+",");
//        }
        double rmse = 0;
        List<Double> rmsesss = new ArrayList<>();
        int p, d, q, P, D, Q, m;
        if(idx >=0){
             p=ans.get(idx).get(1);
             d=ans.get(idx).get(2);
             q=ans.get(idx).get(3);
             P=ans.get(idx).get(4);
             D=ans.get(idx).get(5);
             Q=ans.get(idx).get(6);
             m=ans.get(idx).get(7);
        }else {
            p=0;
            d=0;
            q=2;
            P=0;
            D=0;
            Q=0;
            m=1;
        }
        for(int i=100;i<traces.length;i++){
            double[] origin = new double[100];
            double[] answer = new double[3];
            System.arraycopy(traces, i-100, origin, 0, 100);
            if(i<traces.length-3){
                System.arraycopy(traces, i, answer, 0, 3);
                try {
                    double [] forecastResult = commonTestSimpleForecast("simplelsh",origin,answer,3,p,d,q,P,D,Q,m);

                    //System.out.println("*******RMSE***** "+rmsermse.get(rmsermse.size()-1));
//                    rmse = commonTestCalculateRMSE("cscchris_test",
//                            forecastResult, answer, 3, p, d, q, P, D, Q, m);
//                    rmsesss.add(rmse);
                }catch (Exception e){
                    System.out.printf("Invalid: (p,d,q,P,D,Q,m)=(%d,%d,%d,%d,%d,%d,%d)\n", p,d,q,P,D,Q,m);
                }

            }
//            for (Double aDouble : rmsermse) {
//                System.out.println(aDouble);
//            }

            System.out.println("*********************************");
        }

        System.out.println(sum/n);


        for (Double aDouble : rmsermse) {
            System.out.println(aDouble);
        }



    }
    public double [] action(File subfile){
        if (subfile.isDirectory()) tasks_files(subfile);
        //if (!subfile.isFile())return new int []{0};
        String subfile_name = subfile.toString();
        //int last = subfile_name.lastIndexOf("/");
        System.out.println();
        double[] traces = trace(subfile_name);
        for(int i=10;i<traces.length;i++){
            double[] origin = new double[10];
            double[] answer = new double[3];
            System.arraycopy(traces, i-10, origin, 0, 10);
            if(i<traces.length-3)
            System.arraycopy(traces, i, answer, 0, 3);

            double[] tmp = findParam(origin, answer, 3);
            int [] tmpint = new int[tmp.length];
            for(int j=0;i<tmp.length;j++){
                tmpint[j] = (int)tmp[j];
            }
            ans.add(Arrays.stream(tmpint).boxed().collect(Collectors.toList()) );
            maps.put(tmp[0],ans.size()-1);
            if (tmp[0] < min_rmse) {
                min_rmse = tmp[0];
                System.arraycopy(tmp, 1, pdqPDQm, 0, 7);
            }
            System.out.println("*********************************");
        }
    return traces;


        //System.out.println(subfile_name.substring(last)+" accuracy : ");
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