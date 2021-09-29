package com.workday.insights.timeseries.predict;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AcfTest {
    List<Double> TimeSeries = new ArrayList<>();
    @Before
    public void before(){
        double [] arr = {13, 8, 15, 4, 4, 12, 11, 7, 14, 12};
        TimeSeries = Arrays.stream(arr).boxed().collect(Collectors.toList());
    }

    public List<Double> acf(List<Double> TimeSeries){
        TimeSeries = this.TimeSeries;
        int total = 0, i=1,n=TimeSeries.size();
        double []timeArray = TimeSeries.stream().mapToDouble(j->j).toArray();
        List<List<Double>>zt = new ArrayList<List<Double>>();
        List<List<Double>>lzt= new ArrayList<List<Double>>();
        while(i<TimeSeries.size()){
            total += timeArray[i-1];

            double [] larr = Arrays.copyOfRange(timeArray,i,n);
            zt.add(Arrays.stream(larr).boxed().collect(Collectors.toList()));

            double [] llarr = Arrays.copyOfRange(timeArray,0,n-i);
            if(llarr.length !=0 )
            lzt.add(Arrays.stream(llarr).boxed().collect(Collectors.toList()));
            ++i;
        }
        total += timeArray[timeArray.length-1];
        double avg = total / (timeArray.length*1.0);
        int k = 0,result_Deno =0;
        while(k<timeArray.length){
            result_Deno += Math.pow((timeArray[k]-avg),2);
            k++;
        }
//        System.out.println(result_Deno);
//        System.out.println(zt);
//        System.out.println(lzt);
        int p = 0, q=0;
        double result_Mole = 0;
        List<Double> acf = new ArrayList<>();
        while(p < zt.size() && !zt.get(p).isEmpty()){
            q = 0;
            result_Mole = 0;
            while (q < zt.get(p).size()){
                result_Mole += (zt.get(p).get(q)-avg)*(lzt.get(p).get(q)-avg);
                q++;
            }
            acf.add(result_Mole / result_Deno);
            p++;
        }
        //System.out.println(acf);
        return acf;
    }
    //
      void pacf(){

    }
    void argrelextrema(List<Double> data ){

    }
    void Perdictror(List<Double> tempNorm,double acfPara, double bMax, double bMin ){
        int periodFlag = 0;
        List<Double> acf = acf(tempNorm);
        
    }

    boolean isPredicter(double [] tempNorm){


        return false;
    }
}
