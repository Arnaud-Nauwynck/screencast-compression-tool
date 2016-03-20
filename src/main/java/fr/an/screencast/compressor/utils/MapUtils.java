package fr.an.screencast.compressor.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class MapUtils {

    public static <K,T> ArrayList<T> getOrCreateKeyArrayList(Map<K,ArrayList<T>> map, K key) {
        ArrayList<T> res = map.get(key);
        if (res == null) {
            res = new ArrayList<>();
            map.put(key, res);
        }
        return res;
    }

    public static <K,T> HashSet<T> getOrCreateKeyHashSet(Map<K,HashSet<T>> map, K key) {
        HashSet<T> res = map.get(key);
        if (res == null) {
            res = new HashSet<>();
            map.put(key, res);
        }
        return res;
    }

    public static <K,K2,V2> HashMap<K2,V2> getOrCreateKeyHashMap(Map<K,HashMap<K2,V2>> map, K key) {
        HashMap<K2,V2> res = map.get(key);
        if (res == null) {
            res = new HashMap<>();
            map.put(key, res);
        }
        return res;
    }


}
