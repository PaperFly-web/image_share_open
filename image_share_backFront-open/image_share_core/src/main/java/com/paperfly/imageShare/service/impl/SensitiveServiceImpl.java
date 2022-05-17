package com.paperfly.imageShare.service.impl;

import com.paperfly.imageShare.common.utils.SensitiveWordsUtil;
import com.paperfly.imageShare.dto.SensitiveDTO;
import com.paperfly.imageShare.service.SensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import toolgood.words.IllegalWordsSearch;
import toolgood.words.WordsSearchEx2;

import java.util.ArrayList;
import java.util.List;

@Service("sensitiveService")
@Slf4j
public class SensitiveServiceImpl implements SensitiveService {

    @Override
    public SensitiveDTO analysisText(String text) {
        SensitiveDTO sensitiveDTO = new SensitiveDTO();
        List<String> sensitiveTypeList = new ArrayList<>();
        String handleText = text;
        SensitiveWordsUtil sensitiveWordsUtil = SensitiveWordsUtil.getInstance();
        IllegalWordsSearch adWords = sensitiveWordsUtil.getAdWords();
        IllegalWordsSearch illWords = sensitiveWordsUtil.getIllWords();
        IllegalWordsSearch govWords = sensitiveWordsUtil.getGovWords();
        IllegalWordsSearch urlWords = sensitiveWordsUtil.getUrlWords();
        IllegalWordsSearch yellowWords = sensitiveWordsUtil.getYellowWords();
        boolean ad = adWords.ContainsAny(text);
        boolean ill = illWords.ContainsAny(text);
        boolean gov = govWords.ContainsAny(text);
        boolean url = urlWords.ContainsAny(text);
        boolean yellow = yellowWords.ContainsAny(text);
        if(ad){
            sensitiveDTO.setIll(true);
            sensitiveTypeList.add("ad");
            handleText = adWords.Replace(handleText);
        }
        if(ill){
            sensitiveDTO.setIll(true);
            sensitiveTypeList.add("ill");
            handleText = urlWords.Replace(handleText);
        }
        if(gov){
            sensitiveDTO.setIll(true);
            sensitiveTypeList.add("gov");
            handleText = govWords.Replace(handleText);
        }
        if(url){
            sensitiveDTO.setIll(true);
            sensitiveTypeList.add("url");
            handleText = urlWords.Replace(handleText);
        }
        if(yellow){
            sensitiveDTO.setIll(true);
            sensitiveTypeList.add("yellow");
            handleText = yellowWords.Replace(handleText);
        }
        sensitiveDTO.setType(sensitiveTypeList);
        sensitiveDTO.setHandleContent(handleText);
        sensitiveDTO.setOriginalContent(text);
        return sensitiveDTO;
    }
}
