/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static com.squareup.javapoet.JavaFile.builder;
import static java.util.Collections.singleton;
import static javax.lang.model.SourceVersion.latestSupported;
import static javax.tools.Diagnostic.Kind.ERROR;


/**
 * 识别和预处理注解的类，会在编译期生成代码
 */
@AutoService(Processor.class)
public class HTRouterDispatchProcess extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private String packageName = "com.netease.hearttouch.router";
    private static final String ANNOTATION = "@" + HTRouter.class.getSimpleName();
    private static final int CODE_TYPE_HT_LOG_UTIL = 0;
    private static final int CODE_TYPE_HT_ROUTER_HANDLER = 1;
    private static final int CODE_TYPE_HT_ROUTER_MANAGER = 2;
    private static final int CODE_TYPE_HT_ROUTER_ACTIVITY = 3;
    private static final int CODE_TYPE_HT_ROUTER_HANDLER_PARAMS = 4;
    private static final Map<Integer, String> CODE_TYPE_MAP= new HashMap<Integer, String>() {
        {
            put(CODE_TYPE_HT_LOG_UTIL, "Couldn't generate HTLogUtil class");
            put(CODE_TYPE_HT_ROUTER_HANDLER, "Couldn't generate HTRouterListener class");
            put(CODE_TYPE_HT_ROUTER_MANAGER, "Couldn't generate HTRouterManager class");
            put(CODE_TYPE_HT_ROUTER_ACTIVITY, "Couldn't generate HTRouterActivity class");
            put(CODE_TYPE_HT_ROUTER_HANDLER_PARAMS, "Couldn't generate HTRouterHandlerParams class");
        }
    };

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(HTRouter.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    /**
     * 主要的处理注解的类，会拿到所有的注解相关的类
     *
     * @param annotations
     * @param roundEnv
     * @return 处理成功返回true
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<HTAnnotatedClass> annotatedClasses = new ArrayList<>();
        //获取所有通过HTRouter注解的项，遍历
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HTRouter.class)) {
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            //检测是否是支持的注解类型，如果不是里面会报错
            if (!isValidClass(annotatedClass)) {
                return true;
            }
            //获取到信息，把注解类的信息加入到列表中
            HTRouter htRouter = annotatedElement.getAnnotation(HTRouter.class);
            annotatedClasses.add(new HTAnnotatedClass(annotatedClass, htRouter.url(), htRouter.entryAnim(), htRouter.exitAnim()));
        }

        //生成各种类
        for (Integer type : CODE_TYPE_MAP.keySet()){
            try {
                generateCode(annotatedClasses, type);
            } catch (IOException e) {
                messager.printMessage(ERROR, CODE_TYPE_MAP.get(type));
            }
        }
        return true;
    }

    private boolean isValidClass(TypeElement annotatedClass) {

        if (!HTClassValidator.isPublic(annotatedClass)) {
            String message = String.format("Classes annotated with %s must be public.", ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        if (HTClassValidator.isAbstract(annotatedClass)) {
            String message = String.format("Classes annotated with %s must not be abstract.", ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }


        return true;
    }

    private void generateCode(List<HTAnnotatedClass> annotatedClasses, int type) throws IOException {
        if (annotatedClasses != null && annotatedClasses.size() == 0) {
            return;
        }
        TypeSpec generatedClass = null;
        switch (type){
            case CODE_TYPE_HT_LOG_UTIL:
                generatedClass = HTCodeGenerator.generateHTLogUtilClass(annotatedClasses);
                break;
            case CODE_TYPE_HT_ROUTER_HANDLER:
                generatedClass = HTCodeGenerator.generateHTRouterHandlerClass(packageName, annotatedClasses);
                break;
            case CODE_TYPE_HT_ROUTER_MANAGER:
                generatedClass = HTCodeGenerator.generateManagerClass(packageName, annotatedClasses);
                break;
            case CODE_TYPE_HT_ROUTER_ACTIVITY:
                generatedClass = HTCodeGenerator.generateHTRouterActivityClass(packageName, annotatedClasses);
                break;
            case CODE_TYPE_HT_ROUTER_HANDLER_PARAMS:
                generatedClass = HTCodeGenerator.generateHTRouterHandlerParamsClass(packageName, annotatedClasses);
                break;
            default:
                messager.printMessage(ERROR, "unsupported code type.");
                return;
        }
        JavaFile javaFile = builder(packageName, generatedClass).build();
        javaFile.writeTo(filer);
    }
}
