package com.zy.cache.aspect.utils;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;

/** 
 * @Description:基于FastJson的JSON工具类
 * 类修改说明：无
 *
 * @author liaozhongmin
 * @date 2019/6/20 8:01 PM 
 * @version V1.0 
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * 对象转换成json数据
     *
     * @param object 需要转换的对象
     * @return 返回json数据
     * @author liaozhongmin
     * @date  2019/6/20 8:20 PM
     */
    public static String objectToJson(Object object) {
        try {
            return JSON.toJSONString(object);
        } catch (Exception e) {
            logger.error("对象转换成json数据失败，msg={}",e.getMessage());
        }
        return null;
    }


    /**
     * 将Json数据转换成对象
     *
     * @param  jsonData json数据
     * @param  clazz 类
     * @return 具体的对象
     * @author liaozhongmin
     * @date  2019/6/20 8:17 PM
     */
    public static <T> T jsonToBean(String jsonData, Class<T> clazz) {

        if (StringUtils.isEmpty(jsonData)) {
            return null;
        }

        try {
            return JSON.parseObject(jsonData, clazz);
        } catch (Exception e) {
            logger.error("json数据转换成对象失败，msg={}",e.getMessage());
        }
        return null;
    }


    /**
     * 将json数据转换成对象列表
     *
     * @param jsonData json数据
     * @param clazz 类
     * @return 返回对象集合
     * @author liaozhongmin
     * @date  2019/6/20 8:21 PM
     */
    public static <T> List<T> jsonToList(String jsonData, Class<T> clazz) {

        if (StringUtils.isEmpty(jsonData)) {
            return null;
        }

        try {
            return JSON.parseArray(jsonData, clazz);
        } catch (Exception e) {
            logger.error("json数据转换成对象列表失败，msg={}",e.getMessage());
        }
        return null;
    }


}
