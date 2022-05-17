package com.paperfly.imageShare.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ListUtil {
    /**
     * List转换为Str
     * @param list 集合
     * @param split 每个元素之间的分隔符
     * @return
     */
    public static String listToStr(List list,String split){
        StringBuffer strBuf = new StringBuffer();
        if(!isEmpty(list)){
            for (int i = 0; i < list.size(); i++) {
                if(i == list.size()-1){
                    strBuf.append(list.get(i));
                    continue;
                }
                strBuf.append(list.get(i)).append(split);
            }
        }
        return strBuf.toString();
    }

    public static List strToList(String str,String split){
        List list = new ArrayList<>();
        if(!EmptyUtil.empty(str)){
            final String[] splitArr = str.split(split);
            list.addAll(Arrays.asList(splitArr));
        }
        return list;
    }

    public static boolean isEmpty(List list){
        return EmptyUtil.empty(list);
    }

    public static boolean contains(Integer param,Integer... list){
        if(EmptyUtil.empty(list)){
            return false;
        }
        if(list.length<=0){
            return false;
        }
        for (Integer integer : list) {
            if (Objects.equals(param,integer)){
                return true;
            }
        }
        return false;
    }
    public static boolean contains(String param,String... list){
        if(EmptyUtil.empty(list)){
            return false;
        }
        if(list.length<=0){
            return false;
        }
        for (String str : list) {
            if (Objects.equals(param,str)){
                return true;
            }
        }
        return false;
    }
}
