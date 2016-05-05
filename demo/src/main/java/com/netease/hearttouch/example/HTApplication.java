package com.netease.hearttouch.example;

import android.app.Application;
import android.content.Context;

import com.netease.hearttouch.router.HTLogUtil;
import com.netease.hearttouch.router.HTRouterHandler;
import com.netease.hearttouch.router.HTRouterHandlerParams;
import com.netease.hearttouch.router.HTRouterManager;

/**
 * @author hzshengxueming
 */

public class HTApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HTRouterManager.init();
        //注册绑定默认的降级页面
        String  customUrlKey = "customUrlKey"; //HTWebActivity接收参数key
        HTRouterManager.registerWebActivity(WebActivity.class, customUrlKey);
        //开启Debug模式，输出相应日志
        HTRouterManager.setDebugMode(true);
        //处理每次跳转监听 用户打点统计等
        HTRouterManager.setHtRouterHandler(new HTRouterHandler() {
            @Override
            public boolean handleRoute(Context context, HTRouterHandlerParams routerParams) {
                HTLogUtil.d("统计数据：" + context.getClass().getSimpleName() + "-->跳转url-->" + routerParams.url + "  参数intent" + routerParams.sourceIntent);
                //如果需要拦截或者改变跳转的目标可以直接改变url或者sourceIntent
//                routerParams.url = "http://www.kaola.com/pay?a=b&c=d";
                //如果返回true则表示由监听中进行处理，不需要HTRouter负责跳转
                return false;
            }
        });
    }
}
