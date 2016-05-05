/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * 保存每个被标注的Activity的信息，包括可以匹配的URL，进入及退出动画等
 * 主要提供匹配URL功能，判断目标URL是否可以跳转到当前Activity
 *
 * @author hzshengxueming
 */

public class HTRouterEntry {
    /** URL参数值对应的正则表达式 */
    private static final String PARAM_VALUE = "([a-zA-Z0-9_#'!+%~,\\-\\.\\$]*)";
    /** URL参数键对应的正则表达式 */
    private static final String PARAM = "([a-zA-Z][a-zA-Z0-9_-]*)";
    /** 填充字段的正则，用于将{id}转换为实际匹配的正则表达式 */
    private static final String PARAM_REGEX = "%7B(" + PARAM + ")%7D";
    /** 正向匹配正则 */
    private final Pattern regex;
    /** 逆向匹配正则 */
    private final Pattern regexReverse;
    /** 跳转页面的类型信息 */
    private Class<?> activity;
    /** 本页面可以匹配的URL */
    private String url;
    /** 退场动画 */
    private int exitAnim;
    /** 进场动画 */
    private int entryAnim;

    /**
     * 构造一个用于保存URL与页面对应关系的类
     *
     * @param activity  目标页面的类型信息
     * @param url       用于匹配的URL信息
     * @param exitAnim  退出动画的资源id
     * @param entryAnim 进入动画的资源id
     */
    public HTRouterEntry(Class<?> activity, String url, int exitAnim, int entryAnim) {
        this.activity = activity;
        this.url = url;
        this.exitAnim = exitAnim;
        this.entryAnim = entryAnim;
        //替换掉URL中填充的占位信息，例如{id}
        this.regex = Pattern.compile(hostAndPath(url).replaceAll(PARAM_REGEX, PARAM_VALUE) + "$");
        this.regexReverse = Pattern.compile(hostAndPath(url).replaceAll(PARAM_REGEX, PARAM_VALUE) + "$");
    }

    /**
     * 构造一个用于保存URL与页面对应关系的类,默认进出场动画
     *
     * @param activity 目标页面的类型信息
     * @param url      用于匹配的URL信息
     */
    public HTRouterEntry(Class<?> activity, String url) {
        this.activity = activity;
        this.url = url;
        this.exitAnim = 0;
        this.entryAnim = 0;
        //替换掉URL中填充的占位信息，例如{id}
        this.regex = Pattern.compile(hostAndPath(url).replaceAll(PARAM_REGEX, PARAM_VALUE) + "$");
        this.regexReverse = Pattern.compile(hostAndPath(url).replaceAll(PARAM_REGEX, PARAM_VALUE) + "$");
    }

    /**
     * 进行正向匹配，判断传入的URL是否能跳转到当前页面
     *
     * @param inputUrl 需要进行判断的URL
     * @return 如果能跳转返回true，如果不能则返回false
     */
    public boolean matches(String inputUrl) {
        return inputUrl != null && regex.matcher(hostAndPath(inputUrl)).find();
    }

    /**
     * 进行反向匹配，判断传入的URL是否能跳转到当前页面，预留接口
     * 目的是忽略scheme进行匹配
     *
     * @param inputUrl 需要进行判断的URL
     * @return 如果能跳转返回true，如果不能则返回false
     */
    public boolean reverseMatches(String inputUrl) {
        return inputUrl != null && regexReverse.matcher(hostAndPath(inputUrl)).find();
    }


    private String hostAndPath(String url) {
        int postion = url.indexOf("://");
        //去掉scheme
        if (postion != -1) {
            url = url.substring(postion + "://".length());
        }
        //去掉参数
        postion = url.indexOf("?");
        if(postion != -1) {
            url = url.substring(0, postion);
        }
        String[] urls = url.split("/");
        //每一段都encode一下
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < urls.length; i++) {
            sb.append(URLEncoder.encode(urls[i]));
            if (i != urls.length - 1){
                sb.append('/');
            }
        }
        return sb.toString();

    }

    public Class<?> getActivity() {
        return activity;
    }

    public void setActivity(Class<?> activity) {
        this.activity = activity;
    }

    public int getEntryAnim() {
        return entryAnim;
    }

    public void setEntryAnim(int entryAnim) {
        this.entryAnim = entryAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public void setExitAnim(int exitAnim) {
        this.exitAnim = exitAnim;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
