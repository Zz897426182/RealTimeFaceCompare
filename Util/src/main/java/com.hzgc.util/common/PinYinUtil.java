package com.hzgc.util.common;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinYinUtil {
    private static HanyuPinyinOutputFormat defaultFormat = null;

    static {
        defaultFormat = new HanyuPinyinOutputFormat();
        //拼音小写
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //无音标方式；WITH_TONE_NUMBER：1-4数字表示英标；WITH_TONE_MARK：直接用音标符（必须WITH_U_UNICODE否则异常
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //用v表示ü
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    /**
     * 将文字转为汉语拼音
     *
     * @param chineseLanguage 要转成拼音的中文
     * @return 拼音
     */
    public static String toHanyuPinyin(String chineseLanguage) {
        char[] cl_chars = chineseLanguage.trim().toCharArray();
        StringBuilder pinyin = new StringBuilder();
        try {
            for (int i = 0; i < cl_chars.length; i++){
                if (String.valueOf(cl_chars[i]).matches("[\\u4e00-\\u9fa5]+")) {
                    pinyin.append(PinyinHelper.toHanyuPinyinStringArray(cl_chars[i], defaultFormat)[0] + " ");
                } else {
                    if (i < cl_chars.length - 1 && String.valueOf(cl_chars[i + 1]).matches("[\\u4e00-\\u9fa5]+")){
                        pinyin.append(cl_chars[i] + " ");
                    }else {
                        pinyin.append(cl_chars[i]);
                    }
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            System.out.println("字符不能转成汉语拼音");
        }
        return pinyin.toString();
    }

    public static String getFirstLetters(String chineseLanguage) {
        char[] cl_chars = chineseLanguage.toCharArray();
        StringBuilder pinyin = new StringBuilder();
        try {
            for (char cl_char : cl_chars) {
                String str = String.valueOf(cl_char);
                if (str.matches("[\u4e00-\u9fa5]+")) {
                    // 如果字符是中文,则将中文转为汉语拼音,并取第一个字母
                    pinyin.append(PinyinHelper.toHanyuPinyinStringArray(cl_char, defaultFormat)[0].substring(0, 1));
                } else if (str.matches("[0-9]+")) {
                    // 如果字符是数字,取数字
                    pinyin.append(cl_char);
                } else if (str.matches("[a-zA-Z]+")) {
                    // 如果字符是字母,取字母
                    pinyin.append(cl_char);
                } else {
                    // 否则不转换
                    pinyin.append(cl_char);//如果是标点符号的话，带着
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            System.out.println("字符不能转成汉语拼音");
        }
        return pinyin.toString();
    }
}
