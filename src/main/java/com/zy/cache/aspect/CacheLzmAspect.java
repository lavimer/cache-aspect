package com.zy.cache.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import javax.annotation.Resource;
import com.zy.cache.aspect.annotation.CacheKeyPart;
import com.zy.cache.aspect.annotation.ZyCache;
import com.zy.cache.aspect.executor.RedisCacheExecutor;
import com.zy.cache.aspect.utils.BeanUtils;
import com.zy.cache.aspect.utils.LogUtils;
import java.lang.reflect.Parameter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


/** 
 * @Description:缓存拦截切面
 * 类修改说明：无
 *
 * @author liaozhongmin
 * @date 2019/7/20 7:50 PM 
 * @version V1.0 
 */
@Component
@Aspect
@Slf4j
public class CacheLzmAspect {

    /**
     * 当前切面使用Redis作为缓存拦截
     */
    @Resource
    private RedisCacheExecutor redisCacheExecutor;


    /**
     * 拦截被标记为 防并发的函数,对其进行防并发处理
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.zy.cache.aspect.annotation.ZyCache)")
    public Object around(JoinPoint joinPoint) throws Throwable {
        //获取目标方法
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Method targetMethod = BeanUtils.getTargetMethod(method, joinPoint.getTarget());

        //校验是否需要 设置并发锁
        ZyCache zyCache = targetMethod.getAnnotation(ZyCache.class);
        MainProcessor mainProcessor = () -> {
            Object result = ((ProceedingJoinPoint) joinPoint).proceed();
            return result;
        };

        Object result;
        boolean needCache = zyCache != null;
        if(needCache){
            //创建
            result = this.executeCreator(zyCache, targetMethod, mainProcessor,joinPoint.getArgs());
        }else{
            //未走缓存所以直接调用
            result = mainProcessor.process();
        }
        //返回结果
        return result;
    }

    protected Object executeCreator(ZyCache zyCache, Method targetMethod, MainProcessor mainProcessor,Object[] args) throws Throwable {
        CacheProcess cacheProcess = null;
        String cacheKey = null;

        Long expire = 0L;
        try{
            //缓存失效时间
            expire = zyCache.expire();
            //获取返回类型
            Type type = targetMethod.getGenericReturnType();
            //获取缓存key
            cacheKey = generateKey(zyCache,targetMethod,args);

            //工厂 创建缓存执行器
            cacheProcess = new CacheProcess(redisCacheExecutor,cacheKey, expire, zyCache.timeType(),type,mainProcessor);
        }catch (Exception e){
            //由于缓存处理的参数转换可能出错,所以捕获 使不影响主流程
            LogUtils.warn(log,"尝试走缓存框架时出错!", e, "cacheKey", cacheKey, "expire", expire);
        }

        //走缓存处理
        if(cacheProcess != null){
            return cacheProcess.execute();
        }else{
            //如果执行器创建出错的话, 不影响切点函数
            return mainProcessor.process();
        }
    }



    /**
     * 生成缓存的Key
     *
     * @param zyCache 缓存注解
     * @param targetMethod 目标方法
     * @param args 参数值
     * @return 返回完整的key值
     * @author liaozhongmin
     * @date  2019/7/19 2:05 PM
     */
    protected String generateKey(ZyCache zyCache,Method targetMethod,Object[] args){
        Parameter[] parameters = targetMethod.getParameters();
        Map<String, Object> paramValueMap = BeanUtils.getParamValueMap(parameters, args);

        String cacheKeyStr = "";
        for (Parameter parameter : parameters) {
            CacheKeyPart annotation = parameter.getAnnotation(CacheKeyPart.class);
            if (annotation != null){
                cacheKeyStr += paramValueMap.get(parameter.getName()) + zyCache.delimiter();
            }
        }
        cacheKeyStr = cacheKeyStr.substring(0,cacheKeyStr.length()-1);
        return zyCache.keyPrefix() + cacheKeyStr;
    }

}
