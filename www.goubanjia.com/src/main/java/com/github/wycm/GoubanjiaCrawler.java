package com.github.wycm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

public class GoubanjiaCrawler {
    public static void main(String[] args) throws IOException {
        Document document= Jsoup.parse(new URL("http://www.goubanjia.com/"), 10000);
        setPort(document);
        //获取class='table'的table的所有子节点tr
        Elements elements = document.select("table[class=table] tr");
        for (int i = 1; i < elements.size(); i++){
            //获取td节点
            Element td = elements.get(i).select("td").first();
            /**
             * 查找所有style属性包含none字符串的标签（页面上未显示的标签），并移除
             * 包括以下两种
             * style=display: none;
             * style=display:none;
             */
            for(Element none : td.select("[style*=none;]")){
                none.remove();
            }
            //移除空格
            String ipPort = td.text().replaceAll(" ", "");
            //打印
            System.out.println(ipPort);
        }
    }

    /**
     * js代码port还原
     * @param doc
     */
    private static void setPort(Document doc){
        for (Element e : doc.select(".port")){//$('.port').each(function() {
            String a = e.text();//var a = $(this).html();
            if(a.indexOf("*") != -0x1){//if (a.indexOf('*') != -0x1) {
                return;
            }
            String b = e.attr("class");//var b = $(this).attr('class');
            b = b.split(" ")[0x1];//b = (b.split(" "))[0x1];
            String[] c = b.split("");//var c = b.split("");
            int d = b.length();//var d = c.length;
            StringBuilder f = new StringBuilder();//var f = [];
            for(int g = 0x0; g < d; g++){//for (var g = 0x0; g < d; g++) {
                f.append("ABCDEFGHIZ".indexOf(c[g]));//f.push('ABCDEFGHIZ'.indexOf(c[g]))
            }
            e.text(String.valueOf(Integer.valueOf(f.toString()) >> 0x3));//$(this).html(window.parseInt(f.join('')) >> 0x3)
        }
    }
}