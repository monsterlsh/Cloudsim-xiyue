package com.workday.insights.timeseries.predict;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTrain {
    ExecutorService es=null;
    double[] traces;
    public static String path = "resources/instanceid_1.csv";
    int step;
    Tarin tarin = new Tarin();
    private CountDownLatch latch = null;
    int threadnum;
    @Before
    public void beforeClass() throws Exception {
        step = 5000;
        traces = tarin.trace(path);
        int size = Math.abs(traces.length/step);
        threadnum = size==0?1:size;
        es = Executors.newFixedThreadPool(threadnum);
        latch = new CountDownLatch(threadnum);
    }
    @Test
    public void train() throws IOException, InterruptedException {
        //window 200 20 1000 2000
        int start = 0,end =step,window=200,forecastSize=1;
        ThreadForTrain [] trains = new ThreadForTrain[threadnum];
        for(int i =0;i<trains.length;i++){
            trains[i] = new ThreadForTrain(traces,start,step,window,forecastSize);
            start+=step;
        }
        for (int i=0;i<threadnum;i++){
            es.execute(trains[i]);
        }
        latch.await();
        es.shutdown();
        StringBuilder builder = new StringBuilder();
        for(int i =0;i<trains.length;i++){
            builder.append(i).append(" ]  the best rmse is  ").append(trains[i].getBestrmse()).append(" , its pqd: ")
                    .append(trains[i].getBestarima().p).append(",")
                    .append(trains[i].getBestarima().d).append(",")
                    .append(trains[i].getBestarima().q).append(",")
                    .append(trains[i].getBestarima().P).append(",")
                    .append(trains[i].getBestarima().Q).append(",")
                    .append(trains[i].getBestarima().D).append(",")
                    .append(trains[i].getBestarima().m).append("\n");
            tarin.write(builder,"resources/threadParam");
        }

    }
}
