package com.github.wycm;

import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangyang on 2017/8/22.
 */
public class WeiboCrawler {
    private static String CHECK_URL = "https://login.sina.com.cn/sso/prelogin.php?checkpin=1&entry=mweibo&su=MTMwODgyODA4NjA=&callback=jsonpcallback1503386116934";
    private static String LOGIN_URL = "https://passport.weibo.cn/sso/login";
    private static String POST_ARGS = "username=&password=&savestate=1&r=http%3A%2F%2Fm.weibo.cn%2F&ec=0&pagerefer=https%3A%2F%2Fpassport.weibo.cn%2Fsignin%2Fwelcome%3Fentry%3Dmweibo%26r%3Dhttp%253A%252F%252Fm.weibo.cn%252F&entry=mweibo&wentry=&loginfrom=&client_id=&code=&qq=&mainpageflag=1&hff=&hfp=";

    public static void main(String[] args) throws IOException {
        System.out.println(HttpClientUtil.get(CHECK_URL));

        HttpPost post = new HttpPost(LOGIN_URL);
        //该header必须要
        post.addHeader("Referer", "https://passport.weibo.cn/signin/login?entry=mweibo&res=wel&wm=3349&r=http%3A%2F%2Fm.weibo.cn%2F");
        HttpClientUtil.setHttpPostParams(post, queryToMap(POST_ARGS));
        String res = HttpClientUtil.getWebPage(post);
        System.out.println(res);

        System.out.println(HttpClientUtil.get("https://m.weibo.cn/"));
//        for(int i = 30499; i > 0; i--){
            String result = HttpClientUtil.get("https://m.weibo.cn/api/comments/show?id=4154417035431509&page=1");
            System.out.println(result);
//        }
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
