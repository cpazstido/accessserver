package com.hy.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cpazstido on 2016/6/20.
 */
public class ConstantOutOfMemory {
    public static void main(String[] args) throws Exception{
        try {
            List<String> strings = new ArrayList<String>();
            int i = 0;
            while(true){
                strings.add(String.valueOf(i++).intern());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
