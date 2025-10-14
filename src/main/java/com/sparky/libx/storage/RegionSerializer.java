package com.sparky.libx.storage;

import java.util.Map;

import com.sparky.libx.region.Region;

/**
 * Сериализатор регионов для сохранения в базе данных
 * @author Андрій Будильников
 */
public class RegionSerializer {
    
    /**
     * Сериализует регион в строку JSON
     * @param region регион для сериализации
     * @return строковое представление региона
     */
    public static String serialize(Region region) {
        Map<String, Object> data = region.serialize();

        return data.toString();
    }
    
    /**
     * Десериализует регион из строки
     * @param data строковое представление региона
     * @return десериализованный регион
     */
    public static Region deserialize(String data) {
        return null;
    }
}