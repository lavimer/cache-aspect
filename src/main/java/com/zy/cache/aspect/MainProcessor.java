package com.zy.cache.aspect;


/**
 *  
 *  @Description:实现业务核心逻辑
 * 类修改说明：无
 * <p>
 *  @author liaozhongmin
 *  @date 2019/7/19 7:41 PM 
 *  @version V1.0 
 */
@FunctionalInterface
public interface MainProcessor {
    Object process() throws Throwable;
}
