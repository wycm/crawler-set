package com.github.wycm;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by wycm on 2018/11/19.
 */
public class DianpingCrawlerTest {
    @Test
    public void testJsoup(){
        String s = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/test.html"))).lines()
                .parallel().collect(Collectors.joining("\n"));
        Elements elements = Jsoup.parse(s).select("textPath[xlink:href='#1']");
        System.out.println(elements);
    }
    @Test
    public void testHttp() throws IOException {
        CloseableHttpResponse response = HttpClients.createDefault().execute(new HttpGet("http://s3plus.meituan.net/v1/mss_0a06a471f9514fc79c981b5466f56b91/svgtextcss/807789f715a7caed8e7c2475dcf94e20.svg"));
        System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));
    }
}