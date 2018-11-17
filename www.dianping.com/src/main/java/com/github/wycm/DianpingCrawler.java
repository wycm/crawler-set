package com.github.wycm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

/**
 * 美团点评字体反爬
 */
public class DianpingCrawler {
    public static void main(String[] args) throws IOException {
        Document document= Jsoup.parse(new URL("http://www.dianping.com/shop/96231053"), 10000);
        String cssUrl = "http:" + document.select("link[href*=svgtextcss]").first().attr("href");
        String cssResponse = Jsoup.connect(cssUrl).execute().body();
//        System.out.println(document.toString());
        System.out.println(cssResponse);
    }
}