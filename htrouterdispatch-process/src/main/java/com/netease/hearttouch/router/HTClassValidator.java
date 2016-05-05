/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author hzshengxueming
 */

final class HTClassValidator {
    /**
     * 判断类描述符是否是public的
     *
     * @param annotatedClass 需要判断的类
     * @return 如果是public的返回true，其他返回false
     */
    static boolean isPublic(TypeElement annotatedClass) {
        return annotatedClass.getModifiers().contains(PUBLIC);
    }

    /**
     * 判断类描述符是否是abstract的
     *
     * @param annotatedClass 需要判断的类
     * @return 如果是abstract的返回true，其他返回false
     */
    static boolean isAbstract(TypeElement annotatedClass) {
        return annotatedClass.getModifiers().contains(ABSTRACT);
    }
}
