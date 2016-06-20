package com.hy.test;

/**
 * Created by cpazstido on 2016/6/20.
 */
public class StackOverFlow {
    private int i ;
    public void plus() {
        i++;
        plus();
    }
    public static void main(String[] args) {
        StackOverFlow stackOverFlow = new StackOverFlow();
        try {
            stackOverFlow.plus();
        } catch (Exception e) {
            System.out.println("Exception:stack length:"+stackOverFlow.i);
            e.printStackTrace();
        } catch (Error e) {
            System.out.println("Error:stack length:"+stackOverFlow.i);
            e.printStackTrace();

        }
    }
}
