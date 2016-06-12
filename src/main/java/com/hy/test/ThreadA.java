package com.hy.test;

/**
 * Created by cpazstido on 2016/6/12.
 */
public class ThreadA extends Thread {
    private static Integer flag = 0;
    public static void main(String[] args) throws InterruptedException {
        ThreadB b = new ThreadB();
        ThreadC c = new ThreadC();
        c.setName("c线程");
        b.setName("b线程");
        c.start();
        System.out.println(Thread.currentThread().getName()+" is start....");
        synchronized(c)
        {
            try
            {
                System.out.println("waiting for b1 to complete....");
                System.out.println("线程c的状态是:"+c.isAlive());
                c.wait();
                System.out.println("Completed.now back to "+ Thread.currentThread().getName());
                b.start();
            }
            catch(InterruptedException e)
            {
            }
        }
    }
}
