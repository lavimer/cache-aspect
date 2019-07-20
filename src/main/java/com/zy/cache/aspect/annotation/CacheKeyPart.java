package com.zy.cache.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * @Description:标注需要参与组成缓存key的参数
 * 类修改说明：无
 *
 * @author liaozhongmin
 * @date 2019/7/20 5:57 PM 
 * @version V1.0 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER})
@Inherited
public @interface CacheKeyPart {
}
