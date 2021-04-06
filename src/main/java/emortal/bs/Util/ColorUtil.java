package emortal.bs.Util;

import java.util.ArrayList;
import java.util.List;

public class ColorUtil {
    public static String color(String s) {
        return s.replaceAll("&", "§");
    }
    public static List<String> colorList(String... s) {
        final List<String> stringList = new ArrayList<>();
        for (String s1 : s) {
            stringList.add(color(s1));
        }
        return stringList;
    }
}
