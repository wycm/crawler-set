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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 美团点评字体反爬
 * xb -> x = 0 - 18 * (columnNumber - 1), y = -7 - 30 * (lineNumber - 1)
 * ov -> x = -8 - 14 * (columnNumber - 1), y = -7 - 30 * (lineNumber - 1)
 */
public class DianpingCrawler {
    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        Document document= Jsoup.parse(new URL("http://www.dianping.com/shop/96231053"), 10000);
        String cssUrl = "http:" + document.select("link[href*=svgtextcss]").first().attr("href");
        String cssResponse = Jsoup.connect(cssUrl).execute().body();
//        System.out.println(document.toString());
        System.out.println(cssResponse);
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
        pattern = Pattern.compile("\\.[a-z]{2}-.*?\\{.*?\\}");
        matcher = pattern.matcher(cssResponse);
        List<String> cssList = new ArrayList<>();
        while (matcher.find()){
//            System.out.println(matcher.group(0));
            cssList.add(matcher.group(0));
        }
//        pattern = Pattern.compile("\\.[a-z]{2}-.*?\\{background:(.*?)\\.0px");
        cssList.sort((c1, c2) -> {
            int i = c1.substring(0, 3).compareTo(c2.substring(0, 3));
            if (i == 0){
                return c1.substring(8, 28).compareTo(c2.substring(8, 28));
            } else {
                return i;
            }
        });
        cssList.forEach(System.out::println);
    }
}