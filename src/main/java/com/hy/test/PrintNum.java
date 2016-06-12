package com.hy.test;

/**
 * Created by cpazstido on 2016/6/12.
 */
public class PrintNum {
    private byte[] lock = new byte[0];  //自定义锁对象，这样代价最小，也可已使用当前对象this
    public void demo() {
        PrintThread a = new PrintThread("a");
        PrintThread b = new PrintThread("b");
        a.start();
        b.start();
    }
    class PrintThread extends Thread {
        public PrintThread(String name) {
            this.setName(name);
        }
        public void run() {
            synchronized(lock) {
                for(int i =0; i < 100; i++) {
                    if(i % 10 == 0 && 0 != i) {
                        try {
                            lock.notify();       //唤醒另外一个进程
                            lock.wait();   //暂时释放资源
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(this.getName()+": "+i);
                }
                lock.notify();       //唤醒另外一个进程
            }
        }
    }
}