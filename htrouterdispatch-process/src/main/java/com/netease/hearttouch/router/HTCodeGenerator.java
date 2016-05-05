/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class HTCodeGenerator {

    private static final String MANAGER_CLASS_NAME = "HTRouterManager";
    private static final String LOG_CLASS_NAME = "HTLogUtil";
    private static final String ROUTER_HANDLER_CLASS_NAME = "HTRouterHandler";
    private static final String ROUTER_ACTIVITY_CLASS_NAME = "HTRouterActivity";
    private static final String ROUTER_HANDLER_PARAMS_CLASS_NAME = "HTRouterHandlerParams";

    public static TypeSpec generateManagerClass(String packageName, List<HTAnnotatedClass> classes) {

        TypeSpec.Builder builder = classBuilder(MANAGER_CLASS_NAME)
                .addModifiers(PUBLIC, FINAL);
        builder.addJavadoc("用于用户启动Activity或者通过URL获得可以跳转的目标\n");
        FieldSpec htUrlParamKey = FieldSpec
                .builder(String.class, "HT_URL_PARAMS_KEY",
                        PUBLIC, STATIC, FINAL)
                .initializer("\"ht_url_params_map\"").build();
        builder.addField(htUrlParamKey);

        FieldSpec mWebActivityClass = FieldSpec
                .builder(Class.class, "mWebActivityClass",
                        Modifier.PRIVATE, Modifier.STATIC)
                .build();
        builder.addField(mWebActivityClass);
        FieldSpec mWebExtraKey = FieldSpec
                .builder(String.class, "mWebExtraKey", PRIVATE, STATIC)
                .initializer("\"HTUrl\"")
                .build();
        builder.addField(mWebExtraKey);
        FieldSpec NATIVE = FieldSpec
                .builder(int.class, "NATIVE", PUBLIC, STATIC, FINAL)
                .initializer("1")
                .build();
        builder.addField(NATIVE);
        FieldSpec H5 = FieldSpec
                .builder(int.class, "H5", PUBLIC, STATIC, FINAL)
                .initializer("2")
                .build();
        builder.addField(H5);
        FieldSpec sHtRouterHandler = FieldSpec
                .builder(ClassName.get(packageName, ROUTER_HANDLER_CLASS_NAME), "sHtRouterHandler", PUBLIC, STATIC)
                .build();
        builder.addField(sHtRouterHandler);
        FieldSpec entries = FieldSpec
                .builder(ParameterizedTypeName.get(HashMap.class, String.class, HTRouterEntry.class), "entries",
                        Modifier.PRIVATE, Modifier.STATIC)
                .initializer("new $T<>()", TypeName.get(LinkedHashMap.class))
                .build();
        builder.addField(entries);

        MethodSpec.Builder initMethod = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);
        for (HTAnnotatedClass annotatedClass : classes) {
            ClassName activity = ClassName.bestGuess(annotatedClass.getActivity());
            for (String url : annotatedClass.getUrl()) {
                initMethod.addStatement("entries.put($S, new HTRouterEntry($T.class, $S, $L, $L))",
                        url, activity, url,
                        annotatedClass.getExitAnim(), annotatedClass.getEntryAnim());
            }
        }
        builder.addMethod(initMethod.build());


        MethodSpec.Builder setHtRouterHandler = MethodSpec.methodBuilder("setHtRouterHandler")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(ClassName.get(packageName, ROUTER_HANDLER_CLASS_NAME), "htRouterHandler")
                .addStatement("$N = $N", "sHtRouterHandler", "htRouterHandler");
        setHtRouterHandler.addJavadoc("设置监听接口，可以在每次进行跳转的时候监听\n");
        builder.addMethod(setHtRouterHandler.build());


        MethodSpec.Builder registerWebActivity = MethodSpec.methodBuilder("registerWebActivity")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(Class.class, "webActivityClass")
                .addStatement("registerWebActivity(webActivityClass, mWebExtraKey)");
        registerWebActivity.addJavadoc("设置当前无法处理的URL的降级Webview类，将会通过这个webview进行处理\n" +
                "@params webActivityClass 降级处理的webview的类型\n");
        builder.addMethod(registerWebActivity.build());

        MethodSpec.Builder registerWebActivity01 = MethodSpec.methodBuilder("registerWebActivity")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(Class.class, "webActivityClass")
                .addParameter(String.class, "webExtraKey")
                .beginControlFlow("if(webActivityClass == null || $T.isEmpty(webExtraKey))", ClassName.get("android.text", "TextUtils"))
                .addStatement("return")
                .endControlFlow()
                .addStatement("$N = $N", "mWebActivityClass", "webActivityClass")
                .addStatement("$N = $N", "mWebExtraKey", "webExtraKey");
        registerWebActivity01.addJavadoc("设置当前无法处理的URL的降级Webview类，将会通过这个webview进行处理\n" +
                "@params webActivityClass 降级处理的webview的类型\n" +
                "@params webExtraKey 自定义的传递给webview的参数的键\n");
        builder.addMethod(registerWebActivity01.build());

        MethodSpec.Builder getSchemaHostAndPathMethod = MethodSpec.methodBuilder("getSchemaHostAndPath")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(String.class, "url")
                .beginControlFlow("if($T.isEmpty(url))", ClassName.get("android.text", "TextUtils"))
                .addStatement("throw new IllegalArgumentException()")
                .endControlFlow()
                .addStatement("$T uri = $T.parse(url)", ClassName.get("android.net", "Uri"), ClassName.get("android.net", "Uri"))
                .addStatement("return uri.getScheme()+\"://\"+uri.getHost()+uri.getPath()");
        builder.addMethod(getSchemaHostAndPathMethod.build());

        MethodSpec.Builder getHostAndPathMethod = MethodSpec.methodBuilder("getHostAndPath")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(String.class, "url")
                .beginControlFlow("if($T.isEmpty(url))", ClassName.get("android.text", "TextUtils"))
                .addStatement("throw new IllegalArgumentException()")
                .endControlFlow()
                .addStatement("$T uri = $T.parse(url)", ClassName.get("android.net", "Uri"), ClassName.get("android.net", "Uri"))
                .addStatement("return uri.getHost()+uri.getPath()");
        builder.addMethod(getHostAndPathMethod.build());

        MethodSpec.Builder getParamsMethod = MethodSpec.methodBuilder("getParams")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(HashMap.class, String.class, String.class))
                .addParameter(String.class, "url")
                .addStatement("$T<$T,$T> params = new $T<>();", ClassName.get(HashMap.class), ClassName.get(String.class), ClassName.get(String.class), ClassName.get(LinkedHashMap.class))
                .addStatement("$T uri = $T.parse(url)", ClassName.get("android.net", "Uri"), ClassName.get("android.net", "Uri"))
                .addStatement("$T query = uri.getEncodedQuery()", ClassName.get(String.class))
                .beginControlFlow("if(query != null)")
                .addStatement("$T[] entries = query.split(\"&\")", ClassName.get(String.class))
                .beginControlFlow("for($T entry : entries)", ClassName.get(String.class))
                .addStatement("$T[] keys = entry.split(\"=\")", ClassName.get(String.class))
                .beginControlFlow("if(keys != null && keys.length >= 2)")
                .addStatement("params.put(keys[0],keys[1])")
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("return params");
        builder.addMethod(getParamsMethod.build());


        MethodSpec.Builder getHttpSchemaHostAndPathMethod = MethodSpec.methodBuilder("getHttpSchemaHostAndPath")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addParameter(String.class, "url")
                .addStatement("$T uri = $T.parse(url)", ClassName.get("android.net", "Uri"), ClassName.get("android.net", "Uri"))
                .addStatement("return url.replaceFirst(uri.getScheme(),\"http\")");
        builder.addMethod(getHttpSchemaHostAndPathMethod.build());

        MethodSpec.Builder getAnimIdMethod = MethodSpec.methodBuilder("getAnimIdMethod")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(int.class)
                .addParameter(Class.class, "activity")
                .addParameter(boolean.class, "isExitAnim")
                .beginControlFlow("for($T<$T, $T> entry: entries.entrySet())",
                        Map.Entry.class, String.class, HTRouterEntry.class)
                .beginControlFlow("if(entry.getValue().getActivity().toString().equals(activity.toString()))")
                .addStatement("return isExitAnim?entry.getValue().getExitAnim():entry.getValue().getEntryAnim()")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return 0");
        builder.addMethod(getAnimIdMethod.build());

        MethodSpec.Builder getRouterEntryMethod = MethodSpec.methodBuilder("getRouterEntry")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(String.class, "url")
                .returns(HTRouterEntry.class)
                        //.addStatement("$T hostAndPath = getHostAndPath(url)", String.class)
                .beginControlFlow("for($T<$T, $T> entry: entries.entrySet())",
                        Map.Entry.class, String.class, HTRouterEntry.class)
                        //.beginControlFlow("if(schemaHostAndPath.equals(getSchemaHostAndPath(entry.getValue().getUrl())))")
                .beginControlFlow("if(entry.getValue().matches(url))")
                .addStatement("return entry.getValue()")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return null");

        builder.addMethod(getRouterEntryMethod.build());

        MethodSpec.Builder findRouterEntryByUrlMethod = MethodSpec.methodBuilder("findRouterEntryByUrl")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "url")
                .returns(HTRouterEntry.class)
                .addStatement("$T hostAndPath = getHostAndPath(url)", String.class)
                .beginControlFlow("for($T<$T, $T> entry: entries.entrySet())",
                        Map.Entry.class, String.class, HTRouterEntry.class)
                        //.beginControlFlow("if(hostAndPath.equals(getHostAndPath(entry.getValue().getUrl())))")
                .beginControlFlow("if(entry.getValue().reverseMatches(url))")
                .addStatement("return entry.getValue()")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return null");
        findRouterEntryByUrlMethod.addJavadoc("通过URL找到可以跳转的页面的信息\n" +
                "@param url 需要进行匹配的URL\n" +
                "@return 返回匹配成功后的实体类，如果找不到会返回null\n");
        builder.addMethod(findRouterEntryByUrlMethod.build());

        MethodSpec.Builder startActivityMethod = MethodSpec.methodBuilder("startActivity")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.app", "Activity"), "activity")
                .addParameter(String.class, "url")
                .addParameter(ClassName.get("android.content", "Intent"), "sourceIntent")
                .addParameter(boolean.class, "isFinish")
                .addStatement("$T intent = null", ClassName.get("android.content", "Intent"))
                .addStatement("int exitAnim")
                .addStatement("int entryAnim")
                .addStatement("$T routerParams = new $T(url, sourceIntent)",
                        ClassName.get(packageName, "HTRouterHandlerParams"),
                        ClassName.get(packageName, "HTRouterHandlerParams"))
                .beginControlFlow("if (sHtRouterHandler != null && sHtRouterHandler.handleRoute(activity, routerParams))")
                .addStatement("return")
                .endControlFlow()
                .addStatement("url = routerParams.url")
                .addStatement("sourceIntent = routerParams.sourceIntent")
                .addStatement("HTRouterEntry entry = getRouterEntry(url)")
                .beginControlFlow("if(entry != null)")
                .addStatement("intent = processIntent(activity, sourceIntent, url, entry.getActivity())")
                .addStatement("activity.startActivity(intent)")
                .addStatement("exitAnim = $N(activity.getClass(),true)", getAnimIdMethod.build())
                .addStatement("entryAnim = $N(entry.getActivity(), false)", getAnimIdMethod.build())
                .beginControlFlow("if(isFinish)")
                .addStatement("activity.finish()")
                .endControlFlow()
                .addStatement("activity.overridePendingTransition(entryAnim,exitAnim)")
                .nextControlFlow("else if(mWebActivityClass != null)")
                .addStatement("intent = processIntent(activity, sourceIntent, url, mWebActivityClass)")
                .addStatement("intent.putExtra(mWebExtraKey,getHttpSchemaHostAndPath(url))")
                .addStatement("activity.startActivity(intent)")
                .addStatement("exitAnim = $N(activity.getClass(),true)", getAnimIdMethod.build())
                .addStatement("entryAnim = $N(mWebActivityClass, false)", getAnimIdMethod.build())
                .beginControlFlow("if(isFinish)")
                .addStatement("activity.finish()")
                .endControlFlow()
                .addStatement("activity.overridePendingTransition(entryAnim,exitAnim)")
                .endControlFlow();
        startActivityMethod.addJavadoc("通过URL启动一个页面\n" +
                "@param activity 当前需要进行跳转的activity\n" +
                "@param url 跳转的目标URL\n" +
                "@param sourceIntent 传递进来一个intent，用户数据及启动模式等扩展\n" +
                "@param isFinish 跳转后是否需要关闭当前页面\n");
        builder.addMethod(startActivityMethod.build());


        MethodSpec.Builder startActivityForResultMethod = MethodSpec.methodBuilder("startActivityForResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.app", "Activity"), "activity")
                .addParameter(String.class, "url")
                .addParameter(ClassName.get("android.content", "Intent"), "sourceIntent")
                .addParameter(boolean.class, "isFinish")
                .addParameter(int.class, "requestCode")
                .addStatement("$T intent = null", ClassName.get("android.content", "Intent"))
                .addStatement("int exitAnim")
                .addStatement("int entryAnim")
                .addStatement("$T routerParams = new $T(url, sourceIntent)",
                        ClassName.get(packageName, "HTRouterHandlerParams"),
                        ClassName.get(packageName, "HTRouterHandlerParams"))
                .beginControlFlow("if (sHtRouterHandler != null && sHtRouterHandler.handleRoute(activity, routerParams))")
                .addStatement("return")
                .endControlFlow()
                .addStatement("url = routerParams.url")
                .addStatement("sourceIntent = routerParams.sourceIntent")
                .addStatement("HTRouterEntry entry = getRouterEntry(url)")
                .beginControlFlow("if(entry != null)")
                .addStatement("intent = processIntent(activity, sourceIntent, url, entry.getActivity())")
                .addStatement("activity.startActivityForResult(intent,requestCode)")
                .addStatement("exitAnim = $N(activity.getClass(),true)", getAnimIdMethod.build())
                .addStatement("entryAnim = $N(entry.getActivity(), false)", getAnimIdMethod.build())
                .beginControlFlow("if(isFinish)")
                .addStatement("activity.finish()")
                .endControlFlow()
                .addStatement("activity.overridePendingTransition(entryAnim,exitAnim)")
                .nextControlFlow("else if(mWebActivityClass != null)")
                .addStatement("intent = processIntent(activity, sourceIntent, url, mWebActivityClass)")
                .addStatement("intent.putExtra(mWebExtraKey,getHttpSchemaHostAndPath(url))")
                .addStatement("activity.startActivityForResult(intent,requestCode)")
                .addStatement("exitAnim = $N(activity.getClass(),true)", getAnimIdMethod.build())
                .addStatement("entryAnim = $N(mWebActivityClass, false)", getAnimIdMethod.build())
                .beginControlFlow("if(isFinish)")
                .addStatement("activity.finish()")
                .endControlFlow()
                .addStatement("activity.overridePendingTransition(entryAnim,exitAnim)")
                .endControlFlow();
        startActivityForResultMethod.addJavadoc("通过URL启动一个页面,同时可以获得result回调\n" +
                "@param activity 当前需要进行跳转的activity\n" +
                "@param url 跳转的目标URL\n" +
                "@param sourceIntent 传递进来一个intent，用户数据及启动模式等扩展\n" +
                "@param isFinish 跳转后是否需要关闭当前页面\n");
        builder.addMethod(startActivityForResultMethod.build());


        MethodSpec.Builder startActivityForResultFragmentMethod = MethodSpec.methodBuilder("startActivityForResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.app", "Fragment"), "fragment")
                .addParameter(String.class, "url")
                .addParameter(ClassName.get("android.content", "Intent"), "sourceIntent")
                .addParameter(boolean.class, "isFinish")
                .addParameter(int.class, "requestCode")
                .addStatement("$T intent = null", ClassName.get("android.content", "Intent"))
                .addStatement("int exitAnim")
                .addStatement("int entryAnim")
                .addStatement("$T routerParams = new $T(url, sourceIntent)",
                        ClassName.get(packageName, "HTRouterHandlerParams"),
                        ClassName.get(packageName, "HTRouterHandlerParams"))
                .beginControlFlow("if (sHtRouterHandler != null && sHtRouterHandler.handleRoute(fragment.getActivity(), routerParams))")
                .addStatement("return")
                .endControlFlow()
                .addStatement("url = routerParams.url")
                .addStatement("sourceIntent = routerParams.sourceIntent")
                .addStatement("HTRouterEntry entry = getRouterEntry(url)")
                .beginControlFlow("if(entry != null)")
                .addStatement("intent = processIntent(fragment.getActivity(), sourceIntent, url, entry.getActivity())")
                .addStatement("fragment.startActivityForResult(intent,requestCode)")
                .addStatement("exitAnim = $N(fragment.getActivity().getClass(),true)", getAnimIdMethod.build())
                .addStatement("entryAnim = $N(entry.getActivity(), false)", getAnimIdMethod.build())
                .beginControlFlow("if(isFinish)")
                .addStatement("fragment.getActivity().finish()")
                .endControlFlow()
                .addStatement("fragment.getActivity().overridePendingTransition(entryAnim,exitAnim)")
                .nextControlFlow("else if(mWebActivityClass != null)")
                .addStatement("intent = processIntent(fragment.getActivity(), sourceIntent, url, mWebActivityClass)")
                .addStatement("intent.putExtra(mWebExtraKey,getHttpSchemaHostAndPath(url))")
                .addStatement("fragment.startActivityForResult(intent,requestCode)")
                .addStatement("exitAnim = $N(fragment.getActivity().getClass(),true)", getAnimIdMethod.build())
                .addStatement("entryAnim = $N(mWebActivityClass, false)", getAnimIdMethod.build())
                .beginControlFlow("if(isFinish)")
                .addStatement("fragment.getActivity().finish()")
                .endControlFlow()
                .addStatement("fragment.getActivity().overridePendingTransition(entryAnim,exitAnim)")
                .endControlFlow();
        startActivityForResultFragmentMethod.addJavadoc("通过URL启动一个页面,同时可以获得result回调，回调在fragment中\n" +
                "@param fragment 当前需要进行跳转的fragment\n" +
                "@param url 跳转的目标URL\n" +
                "@param sourceIntent 传递进来一个intent，用户数据及启动模式等扩展\n" +
                "@param isFinish 跳转后是否需要关闭当前页面\n");
        builder.addMethod(startActivityForResultFragmentMethod.build());

        MethodSpec.Builder setDebugMethod = MethodSpec.methodBuilder("setDebugMode")
                .addModifiers(PUBLIC, STATIC)
                .returns(void.class)
                .addParameter(boolean.class, "debug")
                .addStatement("$T.setDebugMode(debug)", ClassName.get(packageName, "HTLogUtil"));
        builder.addMethod(setDebugMethod.build());


        MethodSpec.Builder processIntentMethod = MethodSpec.methodBuilder("processIntent")
                .addModifiers(PRIVATE, STATIC)
                .returns(ClassName.get("android.content", "Intent"))
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(ClassName.get("android.content", "Intent"), "sourceIntent")
                .addParameter(String.class, "url")
                .addParameter(Class.class, "activityClass")
                .addStatement("$T intent = null", ClassName.get("android.content", "Intent"))
                .beginControlFlow("if(sourceIntent != null)")
                .addStatement("intent = ($T)sourceIntent.clone()", ClassName.get("android.content", "Intent"))
                .addStatement("intent.setClass(context, activityClass)")
                .nextControlFlow("else")
                .addStatement("intent = new Intent(context, activityClass)")
                .endControlFlow()
                .addStatement("intent.setData($T.parse(url))", ClassName.get("android.net", "Uri"))
                .addStatement("HashMap<String, String> paramsMap = getParams(url)")
                .beginControlFlow("if(paramsMap != null)")
                .addStatement("intent.putExtra(HT_URL_PARAMS_KEY, paramsMap)")
                .endControlFlow()
                .addStatement("return intent");
        builder.addMethod(processIntentMethod.build());

        return builder.build();
    }


    public static TypeSpec generateHTLogUtilClass(List<HTAnnotatedClass> classes) {
        TypeSpec.Builder builder = classBuilder(LOG_CLASS_NAME)
                .addModifiers(PUBLIC, FINAL);

        FieldSpec tag = FieldSpec
                .builder(String.class, "TAG", PRIVATE, STATIC, FINAL)
                .initializer("\"HT\"")
                .build();
        builder.addField(tag);

        FieldSpec sDebug = FieldSpec
                .builder(boolean.class, "sDebug", PRIVATE, STATIC)
                .initializer("false")
                .build();
        builder.addField(sDebug);

        MethodSpec.Builder setDugMode = MethodSpec.methodBuilder("setDebugMode")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(boolean.class, "debug")
                .addStatement("$N = $N", "sDebug", "debug");
        builder.addMethod(setDugMode.build());

        MethodSpec.Builder d = MethodSpec.methodBuilder("d")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(String.class, "message")
                .beginControlFlow("if(sDebug)")
                .addStatement("$T.d(TAG,message)", ClassName.get("android.util", "Log"))
                .endControlFlow();
        builder.addMethod(d.build());

        return builder.build();
    }

    public static TypeSpec generateHTRouterHandlerClass(String packageName, List<HTAnnotatedClass> classes) {
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(ROUTER_HANDLER_CLASS_NAME)
                .addModifiers(PUBLIC);
        MethodSpec.Builder handleMethod = MethodSpec.methodBuilder("handleRoute")
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(boolean.class)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(ClassName.get(packageName, "HTRouterHandlerParams"), "routerParams");
//                .addParameter(String.class, "url")
//                .addParameter(ClassName.get("android.content", "Intent"), "intent")
//                .addParameter(int.class, "type");
        builder.addMethod(handleMethod.build());
        return builder.build();
    }

    public static TypeSpec generateHTRouterActivityClass(String packageName, List<HTAnnotatedClass> classes) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(ROUTER_ACTIVITY_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get("android.app", "Activity"));

        MethodSpec.Builder onCreateMethod = MethodSpec.methodBuilder("onCreate")
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onCreate(savedInstanceState)")
                .addStatement("$T intent = getIntent()", ClassName.get("android.content", "Intent"))
                .beginControlFlow("if (intent != null && intent.getData()!= null)")
                .addStatement("$T.d(\"receive URL:\" + intent.getData().toString())", ClassName.get(packageName, "HTLogUtil"))
                .addStatement("$T url = intent.getData().toString()", String.class)
                .addStatement("$T.startActivity($T.this,url,intent,true)", ClassName.get(packageName, "HTRouterManager"), ClassName.get(packageName, "HTRouterActivity"))
                .nextControlFlow("else")
                .addStatement("finish()")
                .addStatement("$T.d(\"page error,needs URL format \")", ClassName.get(packageName, "HTLogUtil"))
                .addStatement("$T.makeText($T.this,\"page error\",Toast.LENGTH_SHORT).show()", ClassName.get("android.widget", "Toast"), ClassName.get(packageName, "HTRouterActivity"))
                .endControlFlow();
        builder.addMethod(onCreateMethod.build());
        return builder.build();
    }

    public static TypeSpec generateHTRouterHandlerParamsClass(String packageName, List<HTAnnotatedClass> classes) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(ROUTER_HANDLER_PARAMS_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC);

        FieldSpec urlFiled = FieldSpec
                .builder(String.class, "url", PUBLIC)
                .build();
        builder.addField(urlFiled);

        FieldSpec intentFiled = FieldSpec
                .builder(ClassName.get("android.content", "Intent"), "sourceIntent", PUBLIC)
                .build();
        builder.addField(intentFiled);

        MethodSpec.Builder constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(String.class, "url")
                .addParameter(ClassName.get("android.content", "Intent"), "sourceIntent")
                .addStatement("this.url = url")
                .addStatement("this.sourceIntent = sourceIntent");
        builder.addMethod(constructorMethod.build());

//        MethodSpec.Builder getUrlMethod = MethodSpec.methodBuilder("getUrl")
//                .addModifiers(PUBLIC)
//                .returns(String.class)
//                .addStatement("return mUrl");
//        builder.addMethod(getUrlMethod.build());
//
//        MethodSpec.Builder setUrlMethod = MethodSpec.methodBuilder("setUrl")
//                .addModifiers(PUBLIC)
//                .returns(void.class)
//                .addParameter(String.class, "url")
//                .addStatement("mUrl = url");
//        builder.addMethod(setUrlMethod.build());
//
//        MethodSpec.Builder getTypeMethod = MethodSpec.methodBuilder("getType")
//                .addModifiers(PUBLIC)
//                .returns(String.class)
//                .addStatement("return mUrl");
//        builder.addMethod(getTypeMethod.build());
//
//        MethodSpec.Builder setTypeMethod = MethodSpec.methodBuilder("setType")
//                .addModifiers(PUBLIC)
//                .returns(void.class)
//                .addParameter(int.class, "type")
//                .addStatement("mType = type");
//        builder.addMethod(setUrlMethod.build());
//
//        MethodSpec.Builder getIntentMethod = MethodSpec.methodBuilder("getIntent")
//                .addModifiers(PUBLIC)
//                .returns(ClassName.get("android.content", "Intent"))
//                .addStatement("return sourceIntent");
//        builder.addMethod(getUrlMethod.build());
//
//        MethodSpec.Builder setUrlMethod = MethodSpec.methodBuilder("setUrl")
//                .addModifiers(PUBLIC)
//                .returns(void.class)
//                .addParameter(String.class, "url")
//                .addStatement("mUrl = url");
//        builder.addMethod(setUrlMethod.build());
        return builder.build();
    }
}
