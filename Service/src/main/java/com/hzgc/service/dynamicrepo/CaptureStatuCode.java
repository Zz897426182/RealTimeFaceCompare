package com.hzgc.service.dynamicrepo;

public enum  CaptureStatuCode {
    THRESHOLDNULL("FEATURENULL");
    private String code;
    CaptureStatuCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
