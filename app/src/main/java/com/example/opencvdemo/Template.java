package com.example.opencvdemo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Template {
    private static final Map<String, String> matchs = new HashMap<String, String>();

    // 测试匹配集合
    public static Map<String, String> getMatchs() {
        return matchs;
    }

    static {
        matchs.put("_1_1.png", "1.png");
        matchs.put("_2_1.png", "2.png");
        matchs.put("_3_1.png", "3.png");
        matchs.put("_3_2.png", "3.png");
        matchs.put("_3_3.png", "3.png");
        matchs.put("_3_4.png", "3.png");
        matchs.put("_3_5.png", "3.png");
        matchs.put("_3_6.png", "3.png");
        matchs.put("_3_7.png", "3.png");
        matchs.put("_3_8.png", "3.png");
        matchs.put("_4_1.png", "4.png");
        matchs.put("_4_2.png", "4.png");
        matchs.put("_5_1.png", "5.png");
        matchs.put("_5_2.png", "5.png");
        matchs.put("_6_1.png", "6.png");
        matchs.put("_7_1.png", "7.png");
        matchs.put("_8_1.png", "8.png");
        matchs.put("_9_1.png", "9.png");
    }


    public enum PROCESS {
        Barbarian, Farmland, Sawmill, Stonemine, Goldmine
    }

    // 采集流程: 攻击野蛮人 采集农田 伐木场 石矿床 金矿床
    public static Model createModel(PROCESS mode) {
        switch (mode) {
            case Barbarian:
                return new Model(new String[]{"_2_1.png", "_3_6.png", "_3_1", "_6_1", "_7_1", "_8_1"});
            case Farmland:
                return new Model(new String[]{"_2_1.png", "_3_2.png", "_3_6.png", "_5_2.png", "_6_1.png", "_7_1.png", "_8_1.png"});
            case Sawmill:
                return new Model(new String[]{"_2_1.png", "_3_6.png", "_3_3", "_6_1", "_7_1", "_8_1"});
            case Stonemine:
                return new Model(new String[]{"_2_1.png", "_3_6.png", "_3_4", "_6_1", "_7_1", "_8_1"});
            case Goldmine:
                return new Model(new String[]{"_2_1.png", "_3_6.png", "_3_5", "_6_1", "_7_1", "_8_1"});
            default:
                return null;
        }
    }
}
