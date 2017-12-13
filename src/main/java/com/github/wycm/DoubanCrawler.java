package com.github.wycm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 豆瓣selenium爬虫
 * 运行需要下载chromedirever,并修改代码中的chromedirver地址
 */
public class DoubanCrawler {
    private static WebDriver driver;
    static {
        System.setProperty("webdriver.chrome.driver", "D:/dev/selenium/chromedriver_V2.30/chromedriver_win32/chromedriver.exe");
        driver = new ChromeDriver();
    }
    public static void main(String[] args) throws InterruptedException {
        douban();
        driver.quit();
    }
    private static void douban(){
        driver.get("https://book.douban.com/subject_search?search_text=%E4%BA%92%E8%81%94%E7%BD%91&cat=1001");
        By by = By.cssSelector("div[id='root']");
        waitForLoad(driver, by);
        String pageSource = driver.getPageSource();
        Document document = Jsoup.parse(pageSource);
        Elements elements = document.select("a[class=title-text]");
        for(Element element: elements){
            System.out.println(element.text());
        }
    }
    /**
     * 等待元素加载，10s超时
     * @param driver
     * @param by
     */
    public static void waitForLoad(final WebDriver driver, final By by){
        new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                WebElement element = driver.findElement(by);
                if (element != null){
                    return true;
                }
                return false;
            }
        });
    }
}
