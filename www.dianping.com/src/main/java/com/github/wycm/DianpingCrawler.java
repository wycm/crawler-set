package com.github.wycm;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 美团点评字体反爬
 */
public class DianpingCrawler {
    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        Document document= Jsoup.parse(new URL("http://www.dianping.com/shop/96231053"), 10000);
        String cssUrl = "http:" + document.select("link[href*=svgtextcss]").first().attr("href");
        String cssResponse = Jsoup.connect(cssUrl).execute().body();
//        System.out.println(document.toString());
//        System.out.println(cssResponse);
        Pattern pattern = Pattern.compile("class\\^=\"(.*?)\".*?url\\((.*?)\\)");
        Matcher matcher = pattern.matcher(cssResponse);
        Map<String, String> urlMap = new HashMap<>();
        Map<String, String> svgMap = new HashMap<>();
        while (matcher.find()){
            String prefix = matcher.group(1);
            String url = "http:" + matcher.group(2);
            urlMap.put(prefix, url);
            svgMap.put(prefix, EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity()));
            System.out.println(prefix);
            System.out.println(url);
        }
    }
}