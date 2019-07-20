package com.zy.cache.aspect.executor;

import java.util.concurrent.TimeUnit;

/**
 *  
 *  @Description:缓存执行器
 * 类修改说明：无
 * <p>
 *  @author liaozhongmin
 *  @date 2019/7/19 7:41 PM 
 *  @version V1.0 
 */
public interface ICacheExecutor {

    void set(String key, String value);

    String get(String key);

    void setWithExpire(String redisKey, String value, long expire, TimeUnit unit);
}
