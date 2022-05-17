package com.paperfly.imageShare.common.utils;

import toolgood.words.IllegalWordsSearch;
import toolgood.words.WordsSearchEx2;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SensitiveWordsUtil {
    //广告敏感词
    private List<String> adListWords = new ArrayList<>();
    //政治类敏感词
    private List<String> govListWords = new ArrayList<>();
    //违法，涉枪等敏感词
    private List<String> illListWords = new ArrayList<>();
    //违规URL
    private List<String> urlListWords = new ArrayList<>();
    //色情敏感词
    private List<String> yellowListWords = new ArrayList<>();



    private IllegalWordsSearch adWords = new IllegalWordsSearch();
    private IllegalWordsSearch illWords = new IllegalWordsSearch();
    private IllegalWordsSearch govWords = new IllegalWordsSearch();
    private IllegalWordsSearch urlWords = new IllegalWordsSearch();
    private IllegalWordsSearch yellowWords = new IllegalWordsSearch();

    private volatile static SensitiveWordsUtil instance ;

    private SensitiveWordsUtil(){
        File adFile = new File(this.getResourcesFilePath("data/ad.txt"));
        File illFile = new File(this.getResourcesFilePath("data/ill.txt"));
        File govFile = new File(this.getResourcesFilePath("data/gov.txt"));
        File urlFile = new File(this.getResourcesFilePath("data/url.txt"));
        File yellowFile = new File(this.getResourcesFilePath("data/yellow.txt"));
        adWords.SetKeywords(loadKeywords(adFile));
        illWords.SetKeywords(loadKeywords(illFile));
        govWords.SetKeywords(loadKeywords(govFile));
        urlWords.SetKeywords(loadKeywords(urlFile));
        yellowWords.SetKeywords(loadKeywords(yellowFile));
    }

    public static SensitiveWordsUtil getInstance(){
        if (instance == null) {
            synchronized (SensitiveWordsUtil.class) {
                if (instance == null) {
                    instance = new SensitiveWordsUtil();
                }
            }
        }
        return instance;
    }

    private static List<String> loadKeywords(File file) {
        List<String> keyArray = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
            String s = null;
            while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
                keyArray.add(s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyArray;
    }

    private String getResourcesFilePath(String fileName){
        return this.getClass().getClassLoader().getResource(fileName).getPath();
    }

    public List<String> getAdListWords() {
        return adListWords;
    }

    public List<String> getGovListWords() {
        return govListWords;
    }

    public List<String> getIllListWords() {
        return illListWords;
    }

    public List<String> getUrlListWords() {
        return urlListWords;
    }

    public List<String> getYellowListWords() {
        return yellowListWords;
    }

    public IllegalWordsSearch getAdWords() {
        return adWords;
    }

    public IllegalWordsSearch getIllWords() {
        return illWords;
    }

    public IllegalWordsSearch getGovWords() {
        return govWords;
    }

    public IllegalWordsSearch getUrlWords() {
        return urlWords;
    }

    public IllegalWordsSearch getYellowWords() {
        return yellowWords;
    }
}
