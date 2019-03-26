package com.github.wycm;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangyang on 2017/8/22.
 */
public class WeiboCrawler {
    private static final String CHECK_URL = "https://login.sina.com.cn/sso/prelogin.php?checkpin=1&entry=mweibo&su=MTMwODgyODA4NjA=&callback=jsonpcallback1503386116934";
    private static final String LOGIN_URL = "https://passport.weibo.cn/sso/login";
    private static final String POST_ARGS = "username=13268037201&password=password&savestate=1&r=http%3A%2F%2Fm.weibo.cn%2F&ec=0&pagerefer=https%3A%2F%2Fpassport.weibo.cn%2Fsignin%2Fwelcome%3Fentry%3Dmweibo%26r%3Dhttp%253A%252F%252Fm.weibo.cn%252F&entry=mweibo&wentry=&loginfrom=&client_id=&code=&qq=&mainpageflag=1&hff=&hfp=";
    private static final String KEYWORD_ARGS = "type=all&queryVal=${keyword}&luicode=10000011&lfid=106003type%3D1&title=${keyword}&containerid=100103type%3D1%26q%3D${keyword}";
    /**
     * 搜索url
     */
    public static void main(String[] args) throws IOException {
        String searchUrl = "https://m.weibo.cn/api/container/getIndex";
        String keyword = "联想电脑";
        System.out.println(HttpClientUtil.get(CHECK_URL));
        HttpPost post = new HttpPost(LOGIN_URL);
        //该header必须要
        post.addHeader("Referer", "https://passport.weibo.cn/signin/login?entry=mweibo&res=wel&wm=3349&r=http%3A%2F%2Fm.weibo.cn%2F");
        HttpClientUtil.setHttpPostParams(post, queryToMap(POST_ARGS));
        String res = HttpClientUtil.getWebPage(post);
        System.out.println(res);
        System.out.println(HttpClientUtil.get("https://m.weibo.cn/"));


        String searchArgs = KEYWORD_ARGS.replaceAll("\\$\\{keyword\\}", URLEncoder.encode(keyword, "utf-8"));
        searchUrl = searchUrl + "?" + searchArgs;
        String searchRes = HttpClientUtil.get(searchUrl);
        Pattern pattern = Pattern.compile("idstr\":\"(\\d+)\"");
        Matcher matcher = pattern.matcher(searchRes);
        while (matcher.find()){
            String commentId = matcher.group(1);
            System.out.println(commentId);
        }
//            String result = HttpClientUtil.get("https://m.weibo.cn/api/comments/show?id=4154417035431509&page=1");
//            System.out.println(result);
    }
    /**
     * returns the url parameters in a map
     * @param query
     * @return map
     */
    public static Map<String, String> queryToMap(String query){
        if (query == null){
            query = "";
        }
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }
}
