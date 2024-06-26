package ib.edu.heart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ArrayListObjectParser {

    public static String toJson(Object jsonObject) {
        return new Gson().toJson(jsonObject);
    }

    public static Object fromJson(String jsonString) {
        Type type = new TypeToken<ArrayList<CustomListElement>>() {
        }.getType();

        ArrayList<CustomListElement> list = (ArrayList<CustomListElement>) new Gson().fromJson(jsonString, type);
        return list;
    }

    public static Object fromJsonInt(String jsonString) {
        Type type = new TypeToken<List<Integer>>() {
        }.getType();

        List<Integer> list = (List<Integer>) new Gson().fromJson(jsonString, type);
        return list;
    }

}
