package com.workday.insights.timeseries.predict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IsPreidct {
        void acf(List<Double> TimeSeries){
                int total = 0, i=0,n=TimeSeries.size();
                double []timeArray = TimeSeries.stream().mapToDouble(j->j).toArray();
                List<List<Double>>zt = new ArrayList<List<Double>>();
                List<List<Double>>lzt= new ArrayList<List<Double>>();
                while(i<TimeSeries.size()){
                        total += timeArray[i];
                        double [] llarr = Arrays.copyOf(timeArray,n-i-1);
                        double [] larr = Arrays.copyOfRange(timeArray,i,n);
                        zt.add(Arrays.stream(larr).boxed().collect(Collectors.toList()));
                        lzt.add(Arrays.stream(llarr).boxed().collect(Collectors.toList()));
                        ++i;
                }
        }
}
