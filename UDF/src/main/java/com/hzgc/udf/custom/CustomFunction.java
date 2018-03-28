package com.hzgc.udf.custom;

import java.math.BigDecimal;

public class CustomFunction {
    private static final String SPLIT = ":";

    public double featureCompare(String currentFeatureStr, String historyFeatureStr) {
        float[] currentFeature = string2floatArray(currentFeatureStr);
        float[] historyFeature = string2floatArray(historyFeatureStr);
        if (currentFeature.length == 512 && historyFeature.length == 512) {
            return featureCompare(currentFeature, historyFeature);
        }
        return 0;
    }

    private static float[] string2floatArray(String feature) {
        if (feature != null && feature.length() > 0) {
            float[] featureFloat = new float[512];
            String[] strArr = feature.split(SPLIT);
            for (int i = 0; i < strArr.length; i++) {
                try {
                    featureFloat[i] = Float.valueOf(strArr[i]);
                } catch (Exception e) {
                    return new float[0];
                }
            }
            return featureFloat;
        }
        return new float[0];
    }
    private double featureCompare(float[] currentFeature, float[] historyFeature) {
        double similarityDegree = 0;
        double currentFeatureMultiple = 0;
        double historyFeatureMultiple = 0;
        if (currentFeature.length == 512 && historyFeature.length == 512) {
            for (int i = 0; i < currentFeature.length; i++) {
                similarityDegree = similarityDegree + currentFeature[i] * historyFeature[i];
                currentFeatureMultiple = currentFeatureMultiple + Math.pow(currentFeature[i], 2);
                historyFeatureMultiple = historyFeatureMultiple + Math.pow(historyFeature[i], 2);
            }
            double tempSim = similarityDegree / Math.sqrt(currentFeatureMultiple) / Math.sqrt(historyFeatureMultiple);
            double actualValue = new BigDecimal((0.5 + (tempSim / 2)) * 100).
                    setScale(2, BigDecimal.ROUND_HALF_UP).
                    doubleValue();
            if (actualValue >= 100) {
                return 100;
            }
            return actualValue;
        }
        return 0;
    }
}
