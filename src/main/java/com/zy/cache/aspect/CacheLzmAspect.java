package com.zy.cache.aspect;

import com.youzan.ad.cps.biz.aop.lzm.annotation.CacheKey;
import com.youzan.ad.cps.biz.aop.lzm.annotation.CacheLzm;
import com.youzan.ad.cps.biz.aop.lzm.executor.RedisCacheExecutor;
import com.youzan.bigdata.util.BeanUtils;
import com.youzan.bigdata.util.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;

import com.zy.cache.aspect.executor.RedisCacheExecutor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

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
        CacheLzm cacheLzm = targetMethod.getAnnotation(CacheLzm.class);
        MainProcessor mainProcessor = () -> {
            Object result = ((ProceedingJoinPoint) joinPoint).proceed();
            return result;
        };

        Object result;
        boolean needCache = cacheLzm != null;
        LogUtils.info(log, "是否需要缓存处理!", "needCache", needCache, "method", BeanUtils.toString(targetMethod));

        if(needCache){
            //创建
            result = this.executeCreator(cacheLzm, targetMethod, mainProcessor,joinPoint.getArgs());
        }else{
            //未走缓存所以直接调用
            result = mainProcessor.process();
        }
        //返回结果
        return result;
    }

    protected Object executeCreator(CacheLzm cacheLzm, Method targetMethod, MainProcessor mainProcessor,Object[] args) throws Throwable {
        CacheProcess cacheProcess = null;
        String cacheKey = null;

        Long expire = 0L;
        try{
            //缓存失效时间
            expire = cacheLzm.expire();
            //获取返回类型
            Type type = targetMethod.getGenericReturnType();
            //获取缓存key
            cacheKey = generateKey(cacheLzm,targetMethod,args);

            //工厂 创建缓存执行器
            cacheProcess = new CacheProcess(redisCacheExecutor,cacheKey, expire, cacheLzm.timeType(),type,mainProcessor);
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
     * @param cacheLzm 缓存注解
     * @param targetMethod 目标方法
     * @param args 参数值
     * @return 返回完整的key值
     * @author liaozhongmin
     * @date  2019/7/19 2:05 PM
     */
    protected String generateKey(CacheLzm cacheLzm,Method targetMethod,Object[] args){
        Parameter[] parameters = targetMethod.getParameters();
        Map<String, Object> paramValueMap = this.getParamValueMap(parameters, args);

        String cacheKeyStr = "";
        for (Parameter parameter : parameters) {
            CacheKey annotation = parameter.getAnnotation(CacheKey.class);
            if (annotation != null){
                cacheKeyStr += paramValueMap.get(parameter.getName()) + cacheLzm.delimiter();
            }
        }
        cacheKeyStr = cacheKeyStr.substring(0,cacheKeyStr.length()-1);
        return cacheLzm.key() + cacheKeyStr;
    }



    /**
     * 获取方法中参数名和对应的值
     *
     * @param parameters 参数名列表
     * @param args 参数值列表
     * @return 返回参数名对应的值，Map集合
     * @author liaozhongmin
     * @date  2019/7/19 2:07 PM
     */
    private Map<String,Object> getParamValueMap(Parameter[] parameters,Object[] args){

        Map<String,Object> paramValueMap = new HashMap<>();
        int length = parameters.length;
        for (int i=0;i<length;i++) {
            paramValueMap.put(parameters[i].getName(),args[i]);
        }
        return paramValueMap;
    }
}
