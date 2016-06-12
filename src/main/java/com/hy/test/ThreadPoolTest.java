package com.hy.test;

/**
 * Created by cpazstido on 2016/6/12.
 */
public class ThreadPoolTest {
    private byte[] lock = new byte[0];  //自定义锁对象，这样代价最小，也可已使用当前对象this
    public static void main(String[] args) {
//        PrintNum p = new PrintNum();
//        p.demo();

        ThreadB b = new ThreadB();
        b.start();//主线程中启动另外一个线程
        System.out.println("b is start....");
        //括号里的b是什么意思,应该很好理解吧
        synchronized(b) {
            try {
                System.out.println("Waiting for b to complete...");
                b.wait(20);//这一句是什么意思，究竟谁等待?
                System.out.println("ThreadB is Completed. Now back to main thread");
            }catch (InterruptedException e){}
        }
        System.out.println("Total is :" + b.total);
    }
}

