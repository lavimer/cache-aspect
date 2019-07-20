package com.zy.cache.aspect.executor;


/** 
 * @Description:基于Redis的缓存执行器
 * 类修改说明：无
 *
 * @author liaozhongmin
 * @date 2019/7/19 7:42 PM 
 * @version V1.0 
 */
@Component
@Slf4j
public class RedisCacheExecutor implements ICacheExecutor {




    @Override
    public void set(String redisKey,String value) {

    }



    @Override
    public String get(String redisKey) {

        return null;
    }

    @Override
    public void setWithExpire(String redisKey, String value, long expire, TimeUnit unit) {

    }
}
