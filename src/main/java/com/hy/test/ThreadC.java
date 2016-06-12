package com.hy.test;

/**
 * Created by cpazstido on 2016/6/12.
 */
public class ThreadC extends Thread {
    private Integer cc = 0;
    public ThreadC(){

    }
    public ThreadC(Integer cc){
        this.cc = cc;
    }
    int sum=1;
    public void run()
    {
        synchronized(this)
        {
            System.out.println(Thread.currentThread().getName()+" is running..");
            for(int i=1;i<10;i++)
            {
                sum *=i;
            }
            System.out.println("sum is "+sum);
            notify();
        }
        System.out.println("ThreadC is exit!");
    }
}
