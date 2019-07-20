package com.zy.cache.aspect;

import com.youzan.ad.cps.biz.aop.lzm.executor.ICacheExecutor;
import com.youzan.ad.cps.common.util.JsonUtils;
import com.youzan.bigdata.util.LogUtils;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/** 
 * @Description:TODO
 * 类修改说明：无
 *
 * @author liaozhongmin
 * @date 2019/7/19 7:50 PM 
 * @version V1.0 
 */
@Slf4j
public class CacheProcess {

    /**
     * 缓存执行器
     */
    private ICacheExecutor cacheExecutor;

    /**
     * 缓存Key
     */
    private String cacheKey;

    /**
     * 过期时间
     */
    private Long expire;

    /**
     * 时间单位
     */
    private TimeUnit timeType;

    /**
     * 返回类型
     */
    private Type resultType;

    /**
     * 主流程处理器
     */
    private MainProcessor mainProcessor;

    public CacheProcess() {
    }
    public CacheProcess(ICacheExecutor cacheExecutor, String cacheKey, Long expire, TimeUnit timeType, Type resultType,MainProcessor mainProcessor) {
        this.cacheExecutor = cacheExecutor;
        this.cacheKey = cacheKey;
        this.expire = expire;
        this.timeType = timeType;
        this.resultType = resultType;
        this.mainProcessor = mainProcessor;
    }


    /**
     * 主函数:
     * 1.从缓存读取,并反序列化;
     * 2.如果缓存读取为空或者失败,回源查询;
     * 3.将回源结果序列化;
     * 4.返回结果;
     * @return
     */
    final public Object execute() throws Throwable {
        Object result = null;
        try {
            result = JsonUtils.jsonToBean(cacheExecutor.get(cacheKey),resultType.getClass());
        }catch (Exception e){
            LogUtils.warn(log,"反序列化缓存结果失败!",e, "cacheKey", cacheKey, "result", result);
        }
        boolean needBackToSource = result == null;
        LogUtils.debug(log,"是否需要回源!", "needBackToSource", needBackToSource);

        if(needBackToSource){

            Consumer<String> consumer = (param) -> {
                if(expire != null && expire > 0){
                    cacheExecutor.setWithExpire(cacheKey, param, expire,timeType);
                }else{
                    cacheExecutor.set(cacheKey, param);
                }
            };

            //执行查询数据库的业务逻辑
            result = mainProcessor.process();

            //如果查询数据库所得的结果不为空，则保存到数据库
            if(result != null){
                try {
                    String serializeRes = JsonUtils.objectToJson(result);
                    if(StringUtils.isNotEmpty(serializeRes)){
                        consumer.accept(serializeRes);
                    }
                }catch (Exception e){
                    LogUtils.warn(log,"序列化缓存结果失败!",e, "cacheKey", cacheKey, "result", result);
                }

            }
        }
        return result;
    }


}
