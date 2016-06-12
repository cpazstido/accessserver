package com.hy.test;

/**
 * Created by cpazstido on 2016/6/12.
 */
class ThreadB extends Thread {
//    int total;
//    public void run() {
//        synchronized(this) {
//            System.out.println("ThreadB is running..");
//            for (int i=0; i<=100; i++ ) {
//                total += i;
//            }
//            //notify();
//            try {
//                sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("ThreadB is exit!");
//    }

    int total;
    public void run()
    {
        synchronized(this)
        {
            System.out.println(Thread.currentThread().getName()+" is running..");
            for(int i=0;i<10;i++)
            {
                total +=i;
            }
            System.out.println("total is "+total);
        }
    }
}
