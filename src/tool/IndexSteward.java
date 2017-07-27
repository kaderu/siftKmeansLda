package tool;

import pic.algorithm.translate.TranslateUtil;

import java.util.*;

import static tool.IndexSteward.INDEX_TYPE.advance_keyword_translate;
import static tool.IndexSteward.INDEX_TYPE.keyword_translate;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class IndexSteward {

    public enum INDEX_TYPE {
        keyword_translate,
        advance_keyword_translate,
    }

    public static void index(String docPath, String dictFilePath, String indexFilePath) {
        List<WareMsg> wareMsgList = FileSteward.getWareMsgList(docPath);
//        indexMaker(wareMsgList, dictFilePath, indexFilePath);
        kbtIndexMaker(wareMsgList, dictFilePath, indexFilePath);
    }

    public static void indexMaker(List<WareMsg> wareMsgList, String dictFilePath, String indexFilePath) {
        // make dict
        Set<String> dictSet = new TreeSet<>();
        for (WareMsg wareMsg : wareMsgList) {
            String[] array = wareMsg.getKeywords();
            dictSet.addAll(new ArrayList<>(Arrays.asList(array)));
        }
        MultiLayerIndexMap dicMap = dicTranslateDealer(dictSet, advance_keyword_translate);
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

    // merge keyword, brandName and title
    public static void kbtIndexMaker(List<WareMsg> wareMsgList, String dictFilePath, String indexFilePath) {
        // make dict
        Set<String> dictSet = new TreeSet<>();
        for (WareMsg wareMsg : wareMsgList) {
            String[] array = wareMsg.getKeywords();
            dictSet.addAll(new ArrayList<>(Arrays.asList(array)));
            String branName = wareMsg.getBrandName();
            dictSet.add(branName);
            String[] titleCells = wareMsg.getTitleCells();
            dictSet.addAll(new ArrayList<>(Arrays.asList(titleCells)));
        }
        MultiLayerIndexMap dicMap = dicTranslateDealer(dictSet, advance_keyword_translate);
        dicMap.store(dictFilePath);
        // make index map
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        Map<Integer, Integer> map;
        for (WareMsg wareMsg : wareMsgList) {
            map = new HashMap<>();
            String[] keywordsArray = wareMsg.getKeywords();
            String brandName = wareMsg.getBrandName();
            String[] titleCells = wareMsg.getTitleCells();
            String[] totalArray = concatAll(keywordsArray, new String[]{brandName}, titleCells);

            for (String cell : totalArray) {
                List<Integer> wordIndexList = dicMap.get(cell);
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

    public static MultiLayerIndexMap dicTranslateDealer(Set<String> dicSet, Enum methodType) {
        if (methodType.equals(keyword_translate)) {
            return dicTranslateMethodKeywordTranslate(dicSet);
        } else if (methodType.equals(advance_keyword_translate)) {
            return dicTranslateMethodAdvanceKeywordTranslate(dicSet);
        }
        return null;
    }

    // simply keyword translate
    public static MultiLayerIndexMap dicTranslateMethodKeywordTranslate(Set<String> dicSet) {
        Map<String, Integer> map = new HashMap<>();
        Map<String, Integer> translateMap = new HashMap<>();
        String uniformWord;

        FileSteward fileSteward = new FileSteward();
        Map<String, String> dictMap = fileSteward.readTranslateFile();

        for (String word : dicSet) {
            uniformWord = getUniformWord(word, fileSteward, dictMap);

            if (!translateMap.containsKey(uniformWord)) {
                translateMap.put(uniformWord, translateMap.size());
            }
            map.put(word, translateMap.get(uniformWord));
        }
        fileSteward.storeAddTranslateFile();

        return new MultiLayerIndexMap(map);
    }

    public static MultiLayerIndexMap dicTranslateMethodAdvanceKeywordTranslate(Set<String> dicSet) {
        Set<String> translateSet = new HashSet<>(); // set to collect cell word and make index
        Map<String, List<String>> ori2ExtendMap = new HashMap<>(); // map reflect every oriword to a list of cell word (after translate)
        Map<String, Integer> map = new HashMap<>();

        FileSteward fileSteward = new FileSteward();
        Map<String, String> dictMap = fileSteward.readTranslateFile();

        String uniformWord;
        // step1. oriword translate
        for (String dictword : dicSet) {
            uniformWord = getUniformWord(dictword, fileSteward, dictMap);

            translateSet.add(uniformWord);
            final String uniWord = uniformWord;
            ori2ExtendMap.put(dictword, new ArrayList<String>(){{
                add(uniWord);
            }});
        }
        fileSteward.storeAddTranslateFile();

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
        Iterator it = popularCellWordCntMap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (popularCellWordCntMap.get(key) < meanCnt) {
                it.remove();//添加此行代码
                popularCellWordCntMap.remove(key);
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

        return new MultiLayerIndexMap(ori2ExtendMap, map);
    }

    private static String getUniformWord(String word, FileSteward fileSteward, Map<String, String> dictMap) {
        String uniformWord;
        if (dictMap.containsKey(word)) { // get dict from cache
            uniformWord = dictMap.get(word);
        } else { // get from google.com
            uniformWord = "";
            int i = 0;
            while (i < 3 && uniformWord.isEmpty()) {
                try {
                    uniformWord = TranslateUtil.id2en(word.replaceAll("[-]"," ")).toLowerCase();
                } catch (Exception e) {
                    i++;
                }
            }
            if (uniformWord.isEmpty()) {
                uniformWord = word;
            } else { // add to dictMap and increase_map, later store increasement to file
                dictMap.put(word, uniformWord);
                fileSteward.getAddDictMap().put(word, uniformWord);
            }
        }
        return uniformWord;
    }

    private static List<String> getHideKernelWord(Set<String> wordSet, String keyword) {
        List<String> wordList = new ArrayList<>();
        for (String word : wordSet) {
            if (word.length() <= 2) {
                continue;
            }
            if (!keyword.equals(word)) {
                if (keyword.indexOf(word) == 0 ||
                        keyword.endsWith(word)) {
                    wordList.add(word);
                }
            }
        }
        return wordList;
    }

    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("Bath-Tub".replaceAll("-"," "));
    }
}
