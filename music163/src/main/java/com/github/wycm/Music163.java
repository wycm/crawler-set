package com.github.wycm;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wycm
 */
public class Music163 {
    private static Logger logger = LoggerFactory.getLogger(Music163.class);

    //拷贝登录成功的浏览器原始cookie
    private final static String RAW_COOKIES = "cookie1=value1; cookie2=value2";
    private final static String CHROME_DRIVER_PATH = "/Users/wangyang/Downloads/chromedriver";
    //歌曲列表id
    private static String startId = "22336453";


    private static String userId = null;
    private static Set<String> playListSet = new HashSet<>();
    private static Pattern pattern = Pattern.compile("<span class=\"j-flag time\"><em>(.*?)</em>(.*?)</span>");
    private static Pattern songName = Pattern.compile("class=\"f-thide name fc1 f-fl\" title=\"(.*?)\"");
    private static ChromeOptions chromeOptions = new ChromeOptions();
    private static WebDriver driver = null;
    static {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        chromeOptions.addArguments("--no-sandbox");
    }
    public static void main(String[] args) throws InterruptedException {
        while (true){
            try {
                driver = new ChromeDriver(chromeOptions);
                playListSet.add(startId);
                invoke();
            } catch (Exception e){
                logger.error(e.getMessage(), e);
            } finally {
                driver.quit();
            }
            Thread.sleep(1000 * 10);
        }
    }

    /**
     * 初始化cookies
     */
    private static void initCookies(){
        Arrays.stream(RAW_COOKIES.split("; ")).forEach(rawCookie -> {
            String[] ss = rawCookie.split("=");
            Cookie cookie = new Cookie.Builder(ss[0], ss[1]).domain(".163.com").build();
            driver.manage().addCookie(cookie);
        });
    }
    private static void invoke() throws InterruptedException {
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
        String s = null;
        driver.get("http://music.163.com/");
        initCookies();
        driver.get("http://music.163.com/");
        s = driver.getPageSource();
        userId = group(s, "userId:(\\d+)", 1);
        driver.get("https://music.163.com/#/playlist?id=" + startId);
        driver.switchTo().frame("contentFrame");
        WebElement element = driver.findElement(By.cssSelector("[id=content-operation]>a:first-child"));
        element.click();
        ((JavascriptExecutor) driver).executeScript("window.open('about:blank')");
        ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(0));
        driver.switchTo().defaultContent();
        int i = 0;
        String lastSongName = "";
        int count = 0;
        while (true){
            if(i > Integer.MAX_VALUE - 2){
                break;
            }
            i++;
            s = driver.getPageSource();
            driver.switchTo().window(tabs.get(1)); //switches to new tab
            String songs = null;
            try{
                driver.get("https://music.163.com/user/home?id=" + userId);
                driver.switchTo().frame("contentFrame");
                songs = group(driver.getPageSource(), "累积听歌(\\d+)首", 1);
            } catch (TimeoutException e){
                logger.error(e.getMessage(), e);
            }
            driver.switchTo().window(tabs.get(0));
            Matcher matcher = pattern.matcher(s);
            Matcher songNameMatcher = songName.matcher(s);
            if (matcher.find() && songNameMatcher.find()){
                String songNameStr = songNameMatcher.group(1);
                if (!songNameStr.equals(lastSongName)){
                    count++;
                    lastSongName = songNameStr;
                }
                logger.info(songNameStr + "-" + matcher.group(1) + matcher.group(2) + "---当前播放第" + count + "首歌曲, 累计听歌:" + songs);
            } else {
                logger.info("解析歌曲播放记录或歌曲名失败");
            }
            Thread.sleep(1000 * 30);
        }
    }
    public static String group(String str, String regex, int index) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find() ? matcher.group(index) : "";
    }
}
