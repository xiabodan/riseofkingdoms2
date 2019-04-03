package com.example.opencvdemo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Model {
    private List<String> mProcess = new LinkedList<String>();

    Model(String[] processes) {
        for (String process : processes) {
            mProcess.add(process);
        }
    }

    public int size() {
        return mProcess.size();
    }

    public String getIndexOf(int i) {
        if (mProcess.size() > i) {
            return mProcess.get(i);
        }
        return null;
    }
}
