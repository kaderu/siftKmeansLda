package actor;

import tool.FileSteward;
import tool.WareMsgTranslate;

import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/11.
 */
public class DescribeActor {

    public static void main(String[] args) {
        DocLdaActor.init();
        List<WareMsgTranslate> translateWareList = FileSteward.getTransWareList(DocLdaActor.wkbt_file.replace("wkbt.txt", "transWare.txt"));
        index(translateWareList);
        DocLdaActor.actor4Describe();
    }

    public static void index(List<WareMsgTranslate> translateWareList) {
        index(translateWareList, 0);
    }

    /**
     * type 0: full text index
     * type 1: ori index -- without describe
     * @param translateWareList
     */
    public static void index(List<WareMsgTranslate> translateWareList, int type) {
        List<Map<Integer, Integer>> indexWareList = new ArrayList<>();

        Set<String> stopSet = FileSteward.readStopSet();
        Map<String, Integer> dictMap = new HashMap<>();

        for (WareMsgTranslate ware : translateWareList) {
            Map<Integer, Integer> wareTermMap = new HashMap<>();
            Map<Integer, Integer> wareTermOriMap = new HashMap<>(); // for type 1
            int index;

            // keyword
            for (String ele : ware.getKeywords().toLowerCase().split(",")) {
                if (!ele.isEmpty()) {
                    index = getTermIndex(ele, dictMap); // this will add ele to dicMap
                    addToMap(index, wareTermMap);
                    for (String cell : ele.split(" ")) {
                        if (FileSteward.HasDigit(cell) ||
                                cell.isEmpty() ||
                                stopSet.contains(cell)) {
                            continue;
                        }
                        index = getTermIndex(ele, dictMap);
                        addToMap(index, wareTermMap);
                    }
                }
            }

            // brandName
            index = getTermIndex(ware.getBrandName().toLowerCase(), dictMap);
            addToMap(index, wareTermMap);

            // title
            for (String ele : ware.getTitle().toLowerCase().split(" ")) {
                if (FileSteward.HasDigit(ele) ||
                        ele.isEmpty() ||
                        stopSet.contains(ele)) {
                    continue;
                }
                index = getTermIndex(ele, dictMap);
                addToMap(index, wareTermMap);
            }

            // describe
            for (String ele : ware.getDescribe().toLowerCase().split("\001")) {
                for (String term : castrate(ele).split(" ")) {
                    if (stopSet.contains(term) ||
                            FileSteward.HasDigit(ele) ||
                            ele.isEmpty()) {
                        continue;
                    }
                    index = getTermIndex(term, dictMap);
                    if (type == 0) {
                        addToMap(index, wareTermMap);
                    }
                }
            }

            indexWareList.add(wareTermMap);
        }
        FileSteward.storeDict(dictMap, DocLdaActor.wkbt_dict_file);
        if (type == 0) {
            FileSteward.storeIndex(indexWareList, DocLdaActor.lda_input_file);
        } else {
            FileSteward.storeIndex(indexWareList, DocLdaActor.lda_input_file.replace("wkbtLda.dat", "wkbtLdaWithoutDescribe.dat"));
        }
    }

    public static int getTermIndex(String term, Map<String, Integer> indexMap) {
        if (indexMap.containsKey(term)) {
            return indexMap.get(term);
        } else {
            int index = indexMap.size();
            indexMap.put(term, index);
            return index;
        }
    }

    public static void addToMap(int termIndex, Map<Integer, Integer> map) {
        if (!map.containsKey(termIndex)) {
            map.put(termIndex, 1);
        } else {
            map.put(termIndex, map.get(termIndex) + 1);
        }
    }

    public static String castrate(String input) {
        return input.replaceAll("[^0-9a-zA-Z]"," ");
    }
}
