/**
* Project Name:metl
* Date:2016年6月12日
* Copyright (c) 2016, jingma All Rights Reserved.
*/

package cn.benma666.kettle.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.benma666.constants.UtilConst;
import cn.benma666.kettle.mytuils.Db;
import cn.benma666.myutils.StringUtil;

import com.alibaba.fastjson.JSONObject;

/**
 * 字典管理 <br/>
 * date: 2016年6月12日 <br/>
 * @author jingma
 * @version 
 */
public class Dict {
	
	/**
	 * 字典缓存
	 */
	private static Map<String, Map<String,JSONObject>> dictCache = new HashMap<String, Map<String,JSONObject>>();

    /**
    * 清理缓存 <br/>
    * @author jingma
    */
    public static void clearCache(){
        dictCache.clear();
    }
    /**
    * 根据字典类别得到该类别字典的查询SQL <br/>
    * @author jingma
    * @param dictCategory 字典类别
    * @return 该类别字典的查询SQL
    */
    public static String dictCategoryToSql(String dictCategory) {
        String defaultVal = "select ocode ID,oname CN from sys_unify_dict where dict_category='"
                +dictCategory+"' and is_disable='"+UtilConst.WHETHER_FALSE
                +"' order by oorder asc";
        String result = dictCategory;
        //如果不是select开头的，则判断为使用同一字典方式。
        if(StringUtil.isNotBlank(dictCategory)&&!dictCategory.toLowerCase().startsWith("select")){
            String expand = Db.use(UtilConst.DS_SYS).queryStr(
                    "select expand from sys_unify_dict t "
                    + "where t.dict_category='DICT_CATEGORY' and is_disable=? and t.ocode=?", 
                    UtilConst.WHETHER_FALSE, dictCategory);
                try {
                    result = JSONObject.parseObject(expand).getString("sql");
                    if(StringUtil.isBlank(result)){
                        result = defaultVal;
                    }
                } catch (Exception e) {
                    //解析中报错时，采用默认查询语句
                    result = defaultVal;
                }
        }
        return result;
    }

    /**
    * 获取字典列表 <br/>
    * @author jingma
    * @param dictCategory 字典类别
    * @return 字典列表
    */
    public static Map<String, JSONObject> dictMap(String dictCategory){
    	Map<String, JSONObject> result = dictCache.get(dictCategory);
    	if(result==null){
            String expStr = dictCategoryToSql(dictCategory);
            String[] dict = Dict.parseDictExp(expStr);
            result = Db.use(dict[1]).findMap("id",dict[0]);
            dictCache.put(dictCategory, result);
    	}
        return result;
    }
    /**
    * 获取字典列表 <br/>
    * @author jingma
    * @param dictCategory 字典类别
    * @return 字典列表
    */
    public static List<JSONObject> dictList(String dictCategory){
        return new ArrayList<JSONObject>(dictMap(dictCategory).values());
    }
    /**
    * 获取字典对象 <br/>
    * @author jingma
    * @param dictCategory 字典类别
    * @param key 键
    * @return 对象
    */
    public static JSONObject dictObj(String dictCategory,String key){
        return dictMap(dictCategory).get(key);
    }
    /**
    * 获取字典对象 <br/>
    * @author jingma
    * @param dictCategory 字典类别
    * @param key 键
    * @return 对象
    */
    public static List<JSONObject> dictObjMore(String dictCategory,String key){
    	String[] keyArr = key.split(",");
    	List<JSONObject> result = new ArrayList<JSONObject>();
    	for(String k:keyArr){
    		result.add(dictMap(dictCategory).get(k));
    	}
    	return result;
    }
    /**
    * 获取字典值 <br/>
    * @author jingma
    * @param dictCategory 字典类别
    * @param key 键
    * @return 值
    */
    public static String dictValue(String dictCategory,String key){
        JSONObject dict = dictObj(dictCategory,key);
        String result;
		if(dict==null){
            result = key;
        }else{
        	result = dict.getString("cn");
        }
        return result;
    }
    /**
    * 获取字典值 <br/>
    * @author jingma
    * @param dictCategory 字典类别
    * @param key 键
    * @return 值
    */
    public static String dictValueMore(String dictCategory,String key){
        List<JSONObject> dictList = dictObjMore(dictCategory,key);
        String result = "";
		if(dictList.isEmpty()||dictList.get(0)==null){
            result = key;
        }else{
        	for(JSONObject obj : dictList){
        		result = result + "," + obj.getString("cn");
        	}
        	result = result.substring(1, result.length());
        }
        return result;
    }
    /**
    * 解析字典sql字符串 <br/>
    * 若没有指定数据库则默认使用metl库
    * @author jingma
    * @param exp
    * @return 0:sql,1:ds
    */
    public static String[] parseDictExp(String exp){
        if(StringUtil.isBlank(exp)){
            return null;
        }
        String[] result = new String[2];
        String[] strs = exp.split(";");
        result[0] = strs[0];
        if(strs.length>1){
            result[1] = strs[1].substring(3);
        }else{
            result[1] = UtilConst.DS_SYS;
        }
        return result;
    }
    /**
     * @return dictCache 
     */
    public static Map<String, Map<String, JSONObject>> getDictCache() {
        return dictCache;
    }
    /**
     * @param dictCache the dictCache to set
     */
    public static void setDictCache(Map<String, Map<String, JSONObject>> dictCache) {
        Dict.dictCache = dictCache;
    }
}
