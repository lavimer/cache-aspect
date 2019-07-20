package com.zy.cache.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/** 
 * @Description:缓存注解
 * 类修改说明：无
 *
 * @author liaozhongmin
 * @date 2019/7/20 4:57 PM 
 * @version V1.0 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
@Inherited
public @interface ZyCache {

    /**
     * 缓存前缀
     */
    String keyPrefix();

    /**
     * 缓存过期时间
     */
    long expire() default -1;

    /**
     * 过期时间类型，默认为毫秒
     */
    TimeUnit timeType() default TimeUnit.MILLISECONDS;

    /**
     * 分隔符,默认为英文冒号
     */
    String delimiter() default ":";
}
