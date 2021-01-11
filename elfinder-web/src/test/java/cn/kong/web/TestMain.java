package cn.kong.web;

import java.io.IOException;

/**
 * @author: gh
 * @date: 2021/04/12/0012 17:56
 * @description:
 */
public class TestMain {
    public static void main(String[] args) {
        try {
            new TestMain().methodA(5);
        } catch (IOException e) {
            System.out.println("caught IOException");
        } catch (Exception e) {
            System.out.println("caught Exception");
        }finally {
            System.out.println("no Exception");
        }
    }
    void methodA(int i) throws IOException {
        if (i % 2 != 0) {
            throw new IOException("methodA IOException");
        }
    }
}
