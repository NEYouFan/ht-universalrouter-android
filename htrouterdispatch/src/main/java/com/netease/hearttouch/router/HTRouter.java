/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HTRouter {
    /** activity对应url */
    String[] url();

    /** Activity进场动画资源 */
    int entryAnim() default 0;

    /** Activity出场动画资源 */
    int exitAnim() default 0;
}
