/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import javax.lang.model.element.TypeElement;

/**
 * 用于记录被标注的类的设置
 * @author hzshengxueming
 */

public class HTAnnotatedClass {
    /**被标注的类的跳转URL*/
    public String[] url;
    /**被标注的类的类型信息*/
    public TypeElement typeElement;
    /**被标注的activity的名称*/
    public String activity;
    /**退出动画资源id*/
    public int exitAnim;
    /**进入动画资源id*/
    public int entryAnim;

    public HTAnnotatedClass(TypeElement typeElement, String[] url, int entryAnim, int exitAnim) {
        this.typeElement = typeElement;
        this.activity = typeElement.toString();
        this.url = url;
        this.entryAnim = entryAnim;
        this.exitAnim = exitAnim;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
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

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public String[] getUrl() {
        return url;
    }

    public void setUrl(String[] url) {
        this.url = url;
    }
}
