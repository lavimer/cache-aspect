package com.zy.cache.aspect.utils;


import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.beans.BeanCopier;

import com.zy.cache.aspect.exception.ZyExcetion;

public class BeanUtils {

    private final static Map<String, Method> methodCache = new ConcurrentHashMap<>();

    private static ConcurrentMap<Class, ConcurrentMap<Class, BeanCopier>> beanCopierMap = new ConcurrentHashMap<>();

    /**
     * 找到 最原始的目标函数,去除掉aop等代理
     * @param method
     * @param target
     * @return
     */
    public static Method getTargetMethod(Method method, Object target){
        if(method == null || target == null){
            throw new ZyExcetion("参数为空");
        }
        String cacheKey = toString(method);
        //找到目标函数,有可能aop封装的,所以需要找到最终的函数
        Method targetMethod = methodCache.get(cacheKey);
        if (targetMethod == null) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
            if (targetClass == null) {
                targetClass = target.getClass();
            }
            targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            if (targetMethod == null) {
                targetMethod = method;
            }
            methodCache.put(cacheKey, targetMethod);
        }
        return targetMethod;
    }

    public static String toString(Method m){
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(m.getDeclaringClass().getName());
            sb.append("#");
            sb.append(m.toString());
            return sb.toString();
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 复制属性, 会自动缓存以加快速度,建议使用传入Class的方法
     *
     * @param src
     * @param dest
     * @return 复制的目标对象,注入如果src为null,则这里会返回null
     */
    public static Object copy(Object src, Object dest) {
        if (src == null) {
            return null;
        }
        if (dest == null) {
            throw new ZyExcetion("参数不能为空");
        }
        ConcurrentMap<Class, BeanCopier> innerMap = beanCopierMap.get(src.getClass());
        if (innerMap == null) {
            innerMap = new ConcurrentHashMap();
            ConcurrentMap<Class, BeanCopier> temp = beanCopierMap.putIfAbsent(src.getClass(), innerMap);
            if (temp != null) {
                innerMap = temp;
            }
        }
        BeanCopier beanCopier = innerMap.get(dest.getClass());
        if (beanCopier == null) {
            beanCopier = BeanCopier.create(src.getClass(), dest.getClass(), false);
            BeanCopier temp = innerMap.putIfAbsent(dest.getClass(), beanCopier);
            if (temp != null) {
                beanCopier = temp;
            }
        }
        beanCopier.copy(src, dest, null);
        return dest;
    }

    /**
     * 复制属性, 会自动缓存以加快速度
     *
     * @param src
     * @param destClass 目标类,要求该类必须有无参构造函数
     * @param <T>
     * @return
     */
    public static <T> T copy(Object src, Class<T> destClass) {
        if (src == null) {
            return null;
        }
        try {
            T dest = destClass.newInstance();
            copy(src, dest);
            return dest;
        } catch (Exception e) {
            throw new ZyExcetion(e.getMessage());
        }
    }

    /**
     * 把list中的每个对象都转换为目标类的对象
     *
     * @param srcList
     * @param destClass
     * @param <T>
     * @return
     */
    public static <T> List<T> copyList(List<?> srcList, Class<T> destClass) {
        if (srcList == null) {
            return Collections.emptyList();
        }
        List<T> retList = new ArrayList<>();
        for (Object src : srcList) {
            T destInstance = copy(src, destClass);
            retList.add(destInstance);
        }
        return retList;
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
    public static Map<String,Object> getParamValueMap(Parameter[] parameters,Object[] args){

        Map<String,Object> paramValueMap = new HashMap<>();
        int length = parameters.length;
        for (int i=0;i<length;i++) {
            paramValueMap.put(parameters[i].getName(),args[i]);
        }
        return paramValueMap;
    }
}
