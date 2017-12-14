## 摘要
* 上一篇以知乎网为例简单分享网络请求分析。这一篇主要分享一种应对反爬虫的方法，前端数据混淆。
## 目的
* 之前写https://github.com/wycm/zhihu-crawler项目的时候，需要用到免费的http代理，然后找到了这个 http://www.goubanjia.com/ 这个网站。现在需要把这个网站上的ip和port爬取下来，有兴趣的朋友也可以尝试自己爬取一下。
## 开始
* 打开这个网站首页，然后控制台查看ip和port的对应标签。
![](http://upload-images.jianshu.io/upload_images/5830895-166662036a68a8ac.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 如上图（图一），从控制台的标签中可以看出ip加了一些无关不显示的标签来混淆数据，这里混淆的原理其实很简单，通过标签的style="display:none"属性来达到混淆的目的，也就是包含这个属性的标签是不会显示在页面上的。知道了这一点就比较好处理了，只需要在解析的时候把包含style="display:none"属性的标签去掉。就可以轻松的拿到ip和port数据了。
* 代码如下
```
package com.cnblogs.wycm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URL;

/**
 *
 * 数据的解析采用的是Jsoup框架，Jsoup是一个操作HTML标签的Java库，它提供了非常方便的API来提取和操纵库，支持类似jquery的选择器来查找标签。
 * 由于请求比较单一，这里的网络请求并没有采用上一篇所使用HttpClient框架。直接通过Jsoup来执行http请求的。
 * 关于Jsoup的使用可以参考http://www.open-open.com/jsoup/
 *
 */
public class Chapter1 {
    public static void main(String[] args) throws IOException {
        Document document= Jsoup.parse(new URL("http://www.goubanjia.com/"), 10000);
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
}
/*
第一次运行打印结果:
183.129.246.228:8132
222.92.136.206:8987
54.238.186.100:8988
...
第二次运行打印结果：
183.129.246.228:8377
222.92.136.206:9059
54.238.186.100:8622
...
*/
```
* ip地址能够准确的拿到了，却发现port被做了混淆，而且每次返回的port还在动态改变。大家可以通过把浏览器的JavaScrip脚本关闭后，然后刷新这个网页。会发现每次的port都不一样。我们每次看到的正确port都是通过JavaScript脚本处理后的。如果采用普通爬虫的方式拿到的port都是错误的。现在要想拿到正确的port，可以通过分析它JavaScrip脚本还原数据的逻辑。
* 同样打开控制台->选择Sources->选择一行js代码打断点（点击行编号），如下图
![](http://upload-images.jianshu.io/upload_images/5830895-aa150ab7dee00d09.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 刷新网页—>页面Paused in debugger—>选择Elements->右键td节点->Break on...->subtree modifications。这两个步骤就是在设置断点调试，也就是在td节点发生改变的时候paused。
![](http://upload-images.jianshu.io/upload_images/5830895-17771b34ebc43f3c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 选择Sources->F8(继续执行)，这个时候又会有一次pause，也就是js脚本在还原正确port的时候（如下图）
![](http://upload-images.jianshu.io/upload_images/5830895-ad17fd4ba7733441.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 函数的调用栈有好多层，如何快速定位哪一个函数的技巧就是，看它局部变量表的变量变化，因为这里是port在发生改变，然后找到对应变量和对应逻辑函数。简单分析可以确定到port发生改变的函数是一个匿名函数，如下图
![](http://upload-images.jianshu.io/upload_images/5830895-55a376f91fee8519.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 格式化后，代码如下：
```
var _$ = ['\x2e\x70\x6f\x72\x74', "\x65\x61\x63\x68", "\x68\x74\x6d\x6c", "\x69\x6e\x64\x65\x78\x4f\x66", '\x2a', "\x61\x74\x74\x72", '\x63\x6c\x61\x73\x73', "\x73\x70\x6c\x69\x74", "\x20", "", "\x6c\x65\x6e\x67\x74\x68", "\x70\x75\x73\x68", '\x41\x42\x43\x44\x45\x46\x47\x48\x49\x5a', "\x70\x61\x72\x73\x65\x49\x6e\x74", "\x6a\x6f\x69\x6e", ''];
$(function() {
    $(_$[0])[_$[1]](function() {
        var a = $(this)[_$[2]]();
        if (a[_$[3]](_$[4]) != -0x1) {
            return
        }
        ;var b = $(this)[_$[5]](_$[6]);
        try {
            b = (b[_$[7]](_$[8]))[0x1];
            var c = b[_$[7]](_$[9]);
            var d = c[_$[10]];
            var f = [];
            for (var g = 0x0; g < d; g++) {
                f[_$[11]](_$[12][_$[3]](c[g]))
            }
            ;$(this)[_$[2]](window[_$[13]](f[_$[14]](_$[15])) >> 0x3)
        } catch (e) {}
    })
})
```
* 还原后如下：
```
var _$ = ['.port', "each", "html", "indexOf", '*', "attr", 'class', "split", " ", "", "length", "push", 'ABCDEFGHIZ', "parseInt", "join", ''];
$(function() {
    $('.port').each(function() {
        var a = $(this).html();
        if (a.indexOf('*') != -0x1) {
            return
        }
        ;var b = $(this).attr('class');
        try {
            b = (b.split(" "))[0x1];
            var c = b.split("");
            var d = c.length;
            var f = [];
            for (var g = 0x0; g < d; g++) {
                f.push('ABCDEFGHIZ'.indexOf(c[g]))
            }
            ;$(this).html(window.parseInt(f.join('')) >> 0x3)
        } catch (e) {}
    })
})
```
* 这段代码的逻辑，获取port标签的class属性值，取出属性中后面的几个大写字母，遍历该字符串，找出每次字符在'ABCDEFGHIZ'这个字符串中的索引，然后parseInt转换为整数，然后进行右移3位的操作。
* 完整代码实现
```
package com.cnblogs.wycm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

public class Chapter2 {
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
```
* maven依赖
```
 <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>1.10.2</version>
 </dependency>
```
## 总结
* 该篇文章简单分项了下如何应对前端混淆的反爬虫。关于这种反爬虫，还有其它的一些应对方式。如采用无头浏览器的方式，比如phantomjs框架。这种无头浏览器原本是用来做自动化测试的。它是基于webkit内核的，所以它可以较容易的爬取这种前端混淆的这种网站。一般来说浏览器能够正常访问到的数据，这种方式也可以比较容易爬取这些数据。当然这种方式的最大问题就是效率比较低。因为这种方式它每加载一个页面，都需要下载它的附加资源，如js脚本，脚本下载完成后，还要去执行js脚本。
* 我这里采用的方式是阅读js代码，得出前端混淆的逻辑，然后再通过目标语言来实现对应逻辑。这种方式如果针对一些简单的加密混淆还是很有用的。但是当遇到一些大型复杂的网站，如百度、微博等，需要抓取登录后的数据。这时候需要来手动模拟登录，相对来说，这种网站的模拟登录会更复杂，找各种登录参数来源。都会耗费大量精力。分析请求的成本会比较高。这种方式的优点就是爬取速度快，只获取目标数据。不需要额外网络请求成本。

>![](http://upload-images.jianshu.io/upload_images/5830895-6a8b96dde229c26c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
<br>一个程序员日常分享，包括但不限于爬虫、Java后端技术，欢迎关注。