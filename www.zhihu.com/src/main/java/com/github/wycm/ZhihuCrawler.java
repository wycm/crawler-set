package com.github.wycm;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;

public class ZhihuCrawler {
    private final static String RAW_COOKIES = "拷贝浏览器知乎cookie至此";
    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
    private static final CloseableHttpClient httpClient = HttpClients.custom()
            .setUserAgent(userAgent)
            //设置post默认重定向
            .setRedirectStrategy(new LaxRedirectStrategy())
            .build();
    private static final HttpClientContext httpClientContext = HttpClientContext.create();
    private static CookieStore cookieStore = new BasicCookieStore();


    static {
        for (String rawCookie : RAW_COOKIES.split("; ")){
            String[] s = rawCookie.split("=");
            BasicClientCookie cookie = new BasicClientCookie(s[0], s[1]);
            cookie.setDomain("zhihu.com");
            cookie.setPath("/");
            cookie.setSecure(false);
            cookie.setAttribute("domain", "zhihu.com");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, +5);
            cookie.setExpiryDate(calendar.getTime());
            cookieStore.addCookie(cookie);
        }
        httpClientContext.setCookieStore(cookieStore);
    }
    public static void main(String[] args) throws IOException {
        HttpGet httpGet = new HttpGet("https://www.zhihu.com");
        CloseableHttpResponse response = httpClient.execute(httpGet, httpClientContext);
        String s = EntityUtils.toString(response.getEntity());
        Document document = Jsoup.parse(s);
        Elements elements = document.select("div[class=ContentItem AnswerItem]");
        for (Element e : elements){
//            System.out.println(e);
            System.out.println(e.select("meta[itemprop=url]").first().attr("content"));
            System.out.println(e.select("meta[itemprop=name]").first().attr("content"));
        }
    }
}