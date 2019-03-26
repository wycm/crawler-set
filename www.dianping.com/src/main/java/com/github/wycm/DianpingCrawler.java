package com.github.wycm;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 美团点评字体反爬
 */
public class DianpingCrawler {
    public static void main(String[] args) throws IOException {
        getContent("http://www.dianping.com/shop/96231053");
    }
    private static void getContent(String detailUrl) throws IOException {
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                .build();
        String originalContent = EntityUtils.toString(httpClient.execute(new HttpGet(detailUrl)).getEntity());
        Document document= Jsoup.parse(originalContent);
        String cssUrl = "http:" + document.select("link[href*=svgtextcss]").first().attr("href");
        String cssResponse = Jsoup.connect(cssUrl).execute().body();
//        System.out.println(cssResponse);
        Pattern pattern = Pattern.compile("class\\^=\"(.*?)\".*?url\\((.*?)\\)");
        Matcher matcher = pattern.matcher(cssResponse);
        Map<String, String> urlMap = new HashMap<>();
        Map<String, String> svgMap = new HashMap<>();
        while (matcher.find()){
            String prefix = matcher.group(1);
            String url = "http:" + matcher.group(2);
            urlMap.put(prefix, url);
            svgMap.put(prefix, EntityUtils.toString(httpClient.execute(new HttpGet(url)).getEntity(), "utf-8"));
            System.out.println(prefix);
            System.out.println(url);
        }
        pattern = Pattern.compile("\\.[a-z]{2}-.*?\\{.*?\\}");
        matcher = pattern.matcher(cssResponse);
        List<CssBackground> cssList = new ArrayList<>();
        Pattern cssBackgroundPattern = Pattern.compile("(\\.([a-z]{2})-.*?)\\{background:(.*?)\\.0px (.*?)\\.0px");
        Matcher cssBackgroundMatch;
        while (matcher.find()){
            cssBackgroundMatch = cssBackgroundPattern.matcher(matcher.group(0));
            if (cssBackgroundMatch.find()){
                cssList.add(new CssBackground(cssBackgroundMatch.group(1), Integer.valueOf(cssBackgroundMatch.group(3)), Integer.valueOf(cssBackgroundMatch.group(4))));
            }
        }
        //对css分组排序
        cssList.sort((c1, c2) ->{
            int i = c1.getClassName().substring(0, 3).compareTo(c2.getClassName().substring(0, 3));
            if (i != 0){
                return i;
            } else {
                i = c2.getY().compareTo(c1.getY());;
                if (i != 0){
                    return i;
                } else {
                    return c2.getX().compareTo(c1.getX());
                }
            }
        });
        cssList.forEach(System.out::println);
        int xIndex = 0;
        int yIndex = 0;
        CssBackground lastCssBackground = null;
        //计算对应字体的坐标
        for(CssBackground c : cssList){
            if (lastCssBackground == null){
                lastCssBackground = c;
                continue;
            } else {
                if (!c.getClassName().substring(0, 3).equals(lastCssBackground.getClassName().substring(0, 3))){
                    xIndex = 0;
                    yIndex = 0;
                } else if (!c.getX().equals(lastCssBackground.getX()) && c.getY().equals(lastCssBackground.getY())){
                    c.setxIndex(++xIndex);
                    c.setyIndex(yIndex);
                } else if (c.getX().equals(lastCssBackground.getX()) && !c.getY().equals(lastCssBackground.getY())){
                    c.setxIndex(xIndex);
                    c.setyIndex(++yIndex);
                } else if (!c.getX().equals(lastCssBackground.getX()) && !c.getY().equals(lastCssBackground.getY())){
                    xIndex = 0;
                    c.setxIndex(xIndex);
                    c.setyIndex(++yIndex);
                }
                lastCssBackground = c;
            }
        }
        Map<String, Document> cacheDocumentMap = new HashMap<>();
        Map<String, CssBackground> cssBackgroundMap = new HashMap<>();
        String lastPrefix = "";
        cssList.stream().map(c -> {
            c.setSvgResponse(svgMap.get(c.getClassName().substring(1, 4)));
            if (!cacheDocumentMap.containsKey(c.getClassName().substring(0, 3))){
                cacheDocumentMap.put(c.getClassName().substring(0, 3), Jsoup.parse(c.getSvgResponse()));
            }
            c.setDocument(cacheDocumentMap.get(c.getClassName().substring(0, 3)));
            Document doc = c.getDocument();
            Element e = null;
            if ((c.getX() == -6 && c.getY() == -6) || (c.getX() % -12 == -7 && c.getY() == -6)){
                e = doc.select("text").first();
            } else if ((c.getX() == -7 && c.getY() == -7) || (c.getX() % 14 == -8 && c.getY() == -7)){
                e = doc.select("text").first();
            } else if (c.getX() % 6 == -1 && c.getY() == -6){
                e = doc.select("text").first();
            } else if (c.getX() % -12 == 0 && c.getY() % -30 == -6){
                e = doc.select("textPath[xlink:href='#" + (c.getyIndex() + 1) + "']").first();
            } else if (c.getX() % -14 == 0 && c.getY() % -30 == -7){
                e = doc.select("textPath[xlink:href='#" + (c.getyIndex() + 1) + "']").first();
            }
            if (c == null){
                //为上一个
                //CssBackground{className='.hy-GijB', x=-7, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-o8Bu', x=-19, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-7IxC', x=-31, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-8zQE', x=-43, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-PrgG', x=-55, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-Qbc8', x=-67, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-TnVD', x=-79, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-TqUO', x=-91, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-UkCG', x=-103, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //CssBackground{className='.hy-yOPP', x=-114, y=-6, xIndex=0, yIndex=0, actualFont='null'}
                //todo最后一个不满足规则
            }
            String text = e.text();
            c.setActualFont(text.substring(c.getxIndex(), c.getxIndex() + 1));
            cssBackgroundMap.put(c.getClassName().substring(1, c.getClassName().length()), c);
            return c;
        }).collect(Collectors.toList());
        //还原网页
        Pattern spanPattern = Pattern.compile("<span class=\"([a-z]{2}-[A-Za-z0-9]{4})\"></span>");
        Matcher contentMatcher = spanPattern.matcher(originalContent);
        while (contentMatcher.find()){
            String s1 = contentMatcher.group(0);
            String s2 = cssBackgroundMap.get(contentMatcher.group(1)).getActualFont();
            originalContent = originalContent.replace(s1, s2);
        }
        System.out.println(originalContent);
    }
    static class CssBackground{
        private String className;
        private Integer x;
        private Integer y;
        private int xIndex;
        private int yIndex;
        private String svgResponse;
        private String actualFont;
        private Document document;

        public CssBackground(String className, int x, int y) {
            this.className = className;
            this.x = x;
            this.y = y;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        public int getxIndex() {
            return xIndex;
        }

        public void setxIndex(int xIndex) {
            this.xIndex = xIndex;
        }

        public int getyIndex() {
            return yIndex;
        }

        public void setyIndex(int yIndex) {
            this.yIndex = yIndex;
        }

        public String getSvgResponse() {
            return svgResponse;
        }

        public void setSvgResponse(String svgResponse) {
            this.svgResponse = svgResponse;
        }

        public String getActualFont() {
            return actualFont;
        }

        public void setActualFont(String actualFont) {
            this.actualFont = actualFont;
        }

        public Document getDocument() {
            return document;
        }

        public void setDocument(Document document) {
            this.document = document;
        }

        @Override
        public String toString() {
            return "CssBackground{" +
                    "className='" + className + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    ", xIndex=" + xIndex +
                    ", yIndex=" + yIndex +
                    ", actualFont='" + actualFont + '\'' +
                    '}';
        }
    }
}