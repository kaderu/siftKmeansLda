package tool;

import pic.algorithm.translate.TranslateUtil;

import java.io.*;
import java.util.*;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class IndexSteward {

    public static void index(String docPath, String dictFilePath, String indexFilePath) {
        List<WareMsg> wareMsgList = getWareMsgList(docPath);
        indexMaker(wareMsgList, dictFilePath, indexFilePath);
    }

    public static List<WareMsg> getWareMsgList(String path) {
        List<WareMsg> wareMsgList = new ArrayList<>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null; //用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        try {
            String str = "";
            String[] eles;
            WareMsg wareMsg;
            fis = new FileInputStream(path);
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                wareMsg = new WareMsg();
                eles = str.split("\t");
                wareMsg.setWareId(Long.parseLong(eles[0]));
                wareMsg.setKeywords(eles[1].split(","));
                wareMsg.setBrandName(eles[2]);
                wareMsg.setTitle(eles[3]);
                wareMsgList.add(wareMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
                isr.close();
                fis.close();
                // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
            } catch (IOException e) {
                e.printStackTrace();
            }
            return wareMsgList;
        }
    }

    public static void indexMaker(List<WareMsg> wareMsgList, String dictFilePath, String indexFilePath) {
        // make dict
        Set<String> dictSet = new TreeSet<>();
        for (WareMsg wareMsg : wareMsgList) {
            String[] array = wareMsg.getKeywords();
            dictSet.addAll(new ArrayList<>(Arrays.asList(array)));
        }
        Set<String> dicSet = new HashSet();
        int i = 0;
        for (String ele : dictSet) {
            dicSet.add(ele);
        }
        MultiLayerIndexMap dicMap = dicTranslateDealer(dicSet, "keyword_translate");
        dicMap.store(dictFilePath);

        // make index map
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        Map<Integer, Integer> map;
        for (WareMsg wareMsg : wareMsgList) {
            map = new HashMap<>();
            for (String keyword : wareMsg.getKeywords()) { // [mother care, baby bath, baby clean]
                List<Integer> wordIndexList = dicMap.get(keyword);
                for (int wordIndex : wordIndexList) {
                    if (map.containsKey(wordIndex)) {
                        map.put(wordIndex, map.get(wordIndex) + 1);
                    } else {
                        map.put(wordIndex, 1);
                    }
                }
            }
            mapList.add(map);
        }
        FileSteward.storeIndex(mapList, indexFilePath);
    }

    public static MultiLayerIndexMap dicTranslateDealer(Set<String> dicSet, String methodType) {
        switch (methodType) {
            case "keyword_translate":
                return dicTranslateMethodKeywordTranslate(dicSet);
            case "advance_keyword_translate":
                dicTranslateMethodAdvanceKeywordTranslate(dicSet);
        }
        return null;
    }

    // simply keyword translate
    public static MultiLayerIndexMap dicTranslateMethodKeywordTranslate(Set<String> dicSet) {
        Map<String, Integer> map = new HashMap<>();
        Map<String, Integer> translateMap = new HashMap<>();
        String uniformWord;
        try {
            for (String word : dicSet) {
                uniformWord = TranslateUtil.id2en(word.replaceAll("[-]"," ")).toLowerCase();
                if (!translateMap.containsKey(uniformWord)) {
                    translateMap.put(uniformWord, translateMap.size());
                }
                map.put(word, translateMap.get(uniformWord));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MultiLayerIndexMap(map);
    }

    // advance keyword translate, for keyword may get some spell mistakes or synonym exist, we check terms of keyword and get popular ones,
    // these short terms also put into keyword list,
    // and maybe, we can use word_net to figure out extended word pair.
//    public static Map<String, Integer> dicTranslateMethodAdvanceKeywordTranslate(Map<String, Integer> dicMap) {
//        Map<String, Integer> map = new HashMap<>();
//        Map<String, String> id2EnMap = new HashMap<>();
//        Set<String> translateSet = new HashSet<>();
//        Map<String, Integer> kernelWordCntMap = new HashMap<>();
//        Map<String, Integer> map = new HashMap<>();
//        Map<String, Integer> translateMap = new HashMap<>();
//        String uniformWord;
//        try {
//            for (String word : dicMap.keySet()) {
//                uniformWord = TranslateUtil.id2en(word.replaceAll("[-]"," ")).toLowerCase();
//                translateSet.add(uniformWord);
//                id2EnMap.put(word, uniformWord);
//            }
//
//            int kernelCnt = 0;
//            // calculate each kernel word weight
//            for (String word : translateSet) {
//                for (String ele : word.split(" ")) {
//                    if (!kernelWordCntMap.containsKey(ele)) {
//                        kernelWordCntMap.put(ele, 1);
//                    } else {
//                        kernelWordCntMap.put(ele, kernelWordCntMap.get(ele) + 1);
//                    }
//                    kernelCnt++;
//                }
//            }
//            double meanCnt = kernelCnt * 1.0 / kernelWordCntMap.size();
//            for (String kernelWord : kernelWordCntMap.keySet()) {
//                if (kernelWordCntMap.get(kernelWord) < meanCnt) {
//                    kernelWordCntMap.remove(kernelWord);
//                }
//            }
//
//            // put kernel word into index map
//            Set<String> containerSet = new HashSet<>();
//            String oriWord;
//            String transWord;
//            int index;
//            for (Map.Entry<String, String> entry : id2EnMap.entrySet()) {
//                oriWord = entry.getKey();
//                transWord = entry.getValue();
//                index = dicMap.get(oriWord);
//                if (!translateMap.containsKey(transWord)) {
//                    translateMap.put(transWord, index);
//                }
//                map.put(oriWord, translateMap.get(transWord));
//                List<String> kernelWordList = getHideKernelWord(kernelWordCntMap.keySet(), transWord);
//                for (String kernel : kernelWordList) {
//                    if (!translateMap.containsKey(kernel)) {
//                        translateMap.put(kernel, translateMap.size());
//                    }
//                    map.put(kernel, translateMap.get(kernel));
//                }
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return map;
//    }

    public static Map<String, Integer> dicTranslateMethodAdvanceKeywordTranslate(Set<String> dicSet) {
        Set<String> translateSet = new HashSet<>(); // set to collect cell word and make index
        Map<String, List<String>> ori2ExtendMap = new HashMap<>(); // map reflect every oriword to a list of cell word (after translate)
        Map<String, Integer> map = new HashMap<>();
        try {

            // step1. oriword translate
            for (String dictword : dicSet) {
                final String uniformWord = TranslateUtil.id2en(dictword.replaceAll("[-]", " ")).toLowerCase();
                translateSet.add(uniformWord);
                ori2ExtendMap.put(dictword, new ArrayList<String>(){{
                    add(uniformWord);
                }});
            }

            // step2. with oriword set, we process some popular cell words
            Map<String, Integer> popularCellWordCntMap = new HashMap<>(); // map keep popular cell word (single)
            int kernelCnt = 0;
            // calculate each kernel word weight
            for (String keyword : translateSet) {
                for (String ele : keyword.split(" ")) {
                    if (!popularCellWordCntMap.containsKey(ele)) {
                        popularCellWordCntMap.put(ele, 1);
                    } else {
                        popularCellWordCntMap.put(ele, popularCellWordCntMap.get(ele) + 1);
                    }
                    kernelCnt++;
                }
            }
            double meanCnt = kernelCnt * 1.0 / popularCellWordCntMap.size();
            for (String kernelWord : popularCellWordCntMap.keySet()) {
                if (popularCellWordCntMap.get(kernelWord) < meanCnt) {
                    popularCellWordCntMap.remove(kernelWord);
                }
            }

            // step3. extend translate word with popular cell word map
            for (Map.Entry<String, List<String>> entry : ori2ExtendMap.entrySet()) {
                List<String> popularCellWordList = getHideKernelWord(popularCellWordCntMap.keySet(), entry.getValue().get(0));
                entry.getValue().addAll(popularCellWordList);
                translateSet.addAll(popularCellWordList);
            }

            // step4. make index map
            int i = 0;
            for (String keyword : translateSet) {
                map.put(keyword, i++);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MultiLayerIndexMap(ori2ExtendMap, map);
    }

    private static List<String> getHideKernelWord(Set<String> wordSet, String keyword) {
        List<String> wordList = new ArrayList<>();
        for (String word : wordSet) {
            if (keyword.indexOf(word) != -1) {
                wordList.add(word);
            }
        }
        return wordList;
    }

    public static void main(String[] args) {
        System.out.println("Bath-Tub".replaceAll("-"," "));
    }
}
