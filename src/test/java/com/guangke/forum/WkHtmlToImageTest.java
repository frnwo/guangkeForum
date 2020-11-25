package com.guangke.forum;

import java.io.IOException;

public class WkHtmlToImageTest {
    public static void main(String[] args) {
        String cmd = "D:\\soft\\WKHTMLTOPDF\\bin\\wkhtmltoimage https://www.baidu.com D:\\work\\data\\wk-images\\2.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
