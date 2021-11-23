package com.workday.insights.timeseries.predict;


import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Fliestring {
    @Test
    public void test (){
        String filename = "/home/linhao/sources/java_workpacle/alibaba_2018/intp_dir/instanceid_55.csv";
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str;
            while ((str = in.readLine()) != null) {
                System.out.println(str);
            }
            System.out.println(str);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    @Test
    public void test_arrycopy(){
        //System.out.println(Integer.parseInt("2.5"));
        int [] trace={1,2,3,4,5,6,7,8,9};
        double [] tmp = new double[5];
        System.arraycopy(trace,2,tmp,0,5);
        for (double i : tmp) {
            System.out.println(i);
        }
    }
    @Test
    public void test_double(){
        System.out.println((9.885235494512868E7)>50);
    }
}
