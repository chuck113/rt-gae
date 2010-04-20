package com.rt;

import com.rt.util.MapUtils;

import java.util.List;
import java.util.Map;

public class CollectionsUtils {

    public static <K,V> Map<K,V> toJMap(scala.collection.immutable.Map scalaMap) {
        return (Map<K,V>) MapUtils.toJavaMap(scalaMap);
    }

     public static <K,V> Map<K,V> toJMap(scala.collection.immutable.Map scalaMap, Class<K> kClass, Class<V> vClass) {
        return (Map<K,V>) MapUtils.toJavaMap(scalaMap);
    }

    public static <T> List<T> toJList(scala.List scalaList) {
        return (List<T>) MapUtils.toJavaList(scalaList);
    }

    public static <T> List<T> toJList(scala.List scalaList, Class<T> clazz) {
        return (List<T>) MapUtils.toJavaList(scalaList);
    }
}
