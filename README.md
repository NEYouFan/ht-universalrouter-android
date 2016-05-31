# HT Universal Router页面管理框架
`HT Universal Router`是一种基于注解的Android页面管理框架，通过URL进行页面跳转管理，同时可以设置进入及退出动画，并提供页面降级及跳转拦截和重定向功能。
## 基本功能
- 通过`URL`进行`Activity`之间跳转
- 参数传递 （复杂类型参数传递）
- 进入退出动画资源自定义
- 页面降级 (注: 降级指的是`Native`无法匹配的`URL`可以转为通过`Webview`处理)
- `Webview`跳转`Native`识别
- `URL`中参数自动转化`Intent`参数
- 支持`startActivity`和`startActivityForResult`
- `URL`与`Activity`多对一映射
- 支持`Debug`模式，日志输出
- 支持适配Android M DeepLink
- 支持全局跳转数据，处理每次跳转监听 用户打点统计等功能
- 支持自定义`WebActivity`接收`URL`的`KEY`

## 用法

### AndroidManifest.xml
添加权限:

```
<uses-permission android:name="android.permission.INTERNET"/>
```
注册`HTRouterActivity`,根据实际修改`data`标签中的信息

```
<activity android:name="com.netease.hearttouch.router.HTRouterActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <!-- 为了支持android M -->
        <data android:host="www.kaola.com/" android:scheme="https" />
        <data android:host="www.kaola.com/" android:scheme="http" />
        <!-- 为了支持外部应用或者外部浏览器跳转本应用 -->
        <data android:host="www.kaola.com" android:scheme="kaola" />
    </intent-filter>
</activity>
```
相应的降级`WebActivity`,定义示例见后文。

```
<activity
    android:name=".WebActivity"
    android:label="@string/title_activity_web"
    android:theme="@style/AppTheme.NoActionBar"/>
```

### 初始化
在`Application`的`onCreate()`中添加如下信息，其中`HTRouterActivity`、`HTLogUtil`、`HTRouterManager`、`HTRouterHandler`、`HTRouterHandlerParams`编译后会自动生成（注：仅当有页面使用`@HTRouter`注解时,以上代码才会生成）。

- 降级页面接收的`URL`的`key` 
- 注册降级页面相应的`WebviewActivity`;
- 是否开启`debug`模式
- 每次跳转的监听，可以进行自定义处理,包括跳转的拦截和重定义等，可以作为拦截器使用，通过返回true来阻止本管理框架进行跳转，通过修改`HTRouterHandlerParams`中的内容来修改跳转的目标。

```
HTRouterManager.init();
//注册绑定默认的降级页面
StringcustomUrlKey = "customUrlKey"; //HTWebActivity接收参数key
HTRouterManager.registerWebActivity(WebActivity.class, customUrlKey);
//开启Debug模式，输出相应日志
HTRouterManager.setDebugMode(true);
HTRouterManager.setHtRouterHandler(new HTRouterHandler() {
    @Override
    public boolean handleRoute(Context context, HTRouterHandlerParams routerParams) {
       HTLogUtil.d("统计数据：" + context.getClass().getSimpleName() + "-->跳转url-->" + routerParams.url + "  参数intent" + routerParams.sourceIntent);
       //如果需要拦截或者改变跳转的目标可以直接改变url或者sourceIntent
       routerParams.url = "http://www.kaola.com/pay";
       //如果返回true则表示由监听中进行处理，不需要HTRouter负责跳转
       return false;
    }
    });
        
```

### 注解使用
`@HTRouter` 字段支持`Activity`类注解:

- `url`: 支持url数组 {"http://kaola.com","http://m.kaola.com"}
- `entryAnim` :`activity`进入动画 `R.anim.entry`
- `exitAnim` :`activity`退出动画 `R.anim.exit`

例如：

```
@HTRouter(url = {"http://www.kaola.com/pay"},entryAnim = R.anim.enter,exitAnim = R.anim.exit)
```

### 页面跳转方法
1. 使用`HTRouterManager.startActivity()`接口

    - `activity`: 当前页面的`activity`
    - `url`: 跳转的目标URL
    - `sourceIntent`: 传递进来一个intent，用于传递用户数据及启动模式等扩展
    - `isFinish`: 跳转后是否需要关闭当前页面

    例如`HTRouterManager.startActivity(this, "http://www.kaola.com/pay", null, false);`

2. 还提供`HTRouterManager.startActivityForResult()`，参数与`HTRouterManager.startActivity()`相同，回调发生在`Activity`中。
3. 由于`Fragment`的`startActivityForResult()`的回调可以选择发生在`Fragment`或者`Fragment`所处的`Activity`中（通常直接调用`startActivityForResult()`发生在`fragment`中，而通过`getActivity().startActivityForResult()`则发生在Activity中），故另外提供`HTRouterManager.startActivityForResult(Fragment fragment, String url, Intent sourceIntent, boolean isFinish)`接口，该方法只有第一个参数不同。

### 参数获取
除了在页面跳转的`sourceIntent`中传入的自定义类型参数外,本框架还实现`URL`中参数字段的自动解析，解析后保存在`HashMap`中序列化后放在跳转的`Intent`参数中。用户可以用如下方式获取,需要进行强制类型转换：

```
Intent intent = getIntent();
HashMap<String, String> urlParamsMap = (HashMap<String, String>)intent.getSerializableExtra(HTRouterManager.HT_URL_PARAMS_KEY);
```

### 降级及升级WebActivity示例
* 降级`WebActivity`，`Native`处理不了的转`Webview`，只需要接收`url`参数后`loadUrl`一下。

    ```
    public class WebActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Intent intent = getIntent();
            String url = intent.getStringExtra("customUrlKey");
            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setLoadWithOverviewMode(true);
            String targetUrl = HTRouterManager.getHttpSchemaHostAndPath(url);
            HTLogUtil.d("打开链接=" + targetUrl);
            webView.loadUrl(targetUrl);
            setContentView(webView);
        }
    }
    ```

* 升级`WebActivity`，`webview`中的`url`先交给`native`处理，需要重载`shouldOverrideUrlLoading()`,处理`webview`中的`url`。

    ```
    public class WebActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //...
            //一些初始化...
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i("HT", "重新加载=" + url);
                    HTRouterEntry entity = HTRouterManager.findRouterEntryByUrl(url);
                    //为了防止匹配不上后循环跳，这里需要有个判断
                    if (entity != null) {
                        //匹配上之后
                        HTRouterManager.startActivity(WebActivity.this, url, null, false);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                }
            });
            //...
            //一些后续操作
        }
    }
    ```
    
### 进阶使用
本框架可以注册`HTRouterHandler`，进行跳转拦截或记录等处理,实现该接口并通过`HTRouterManager.setHtRouterHandler()`注册。返回值`true`代表拦截处理，则本框架不会进行后续的处理。返回`false`代表还需要本框架进行进一步跳转处理，同时可以修改`routerParams`中的`url`和`sourceIntent`来达到动态控制跳转的重定向。

```
public interface HTRouterHandler {
  boolean handleRoute(Context context, HTRouterHandlerParams routerParams);
}
```
## 集成
### Gradle
`app`的`build.grade`中添加

```
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'com.netease.hearttouch:ht-universalrouter-dispatch:0.1.0'
    apt 'com.netease.hearttouch:ht-universalrouter-dispatch-process:0.1.0'
}
```

`project`的`build.gradle`的`dependencies`下面添加

```
 classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
```

### maven
```
<dependencies>
  <dependency>
    <groupId>com.netease.hearttouch</groupId>
    <artifactId>ht-universalrouter-dispatch</artifactId>
    <version>0.1.0</version>
  </dependency>
  <dependency>
    <groupId>com.netease.hearttouch</groupId>
    <artifactId>ht-universalrouter-dispatch-process</artifactId>
    <version>0.1.0</version>
    <optional>true</optional>
  </dependency>
</dependencies>
```
## 代码混淆
如果要使用混淆，在引用工程的`proguard`文件中，添加如下代码:

```
-keep class com.netease.hearttouch.router.** { *; }
```

## 许可证
`HT Universal Router` 使用 `MIT` 许可证，详情见 [LICENSE](https://github.com/NEYouFan/ht-universalrouter-android/blob/master/LICENSE.txt) 文件。