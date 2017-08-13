package actor;

import tool.FileSteward;
import tool.Tokenizer;
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
            int index;
            // let's make a interval discussion here for what count most for a ware doc.
            // If keywords is of high-quanlity, then keyword count the most. Then is brandName, which connect similar wares together.
            // If both above lose efficacy (like K:lego B:lego case), then we bank on title.
            // But sometimes, title contribute little -- maybe it is just brandName plus version-number plus color. This case we should ask DESCRIBE for help.
            // Then how ?
            // Full-text helps little as a conclution of our test. But a CONTENT label always tell us what this ware is on earth. We like this label.
            // At worset, nothing help above. Then we have to focus on describe content. As a commen sence, the first centence would told us cat or dog, that's enough.

            // keyword
            for (String ele : new HashSet<String>(Arrays.asList(ware.getKeywords().toLowerCase().split(",")))) {
                ele = castrate(ele, stopSet);
                if (!ele.isEmpty()) {
                    index = getTermIndex(ele, dictMap); // this will add ele to dicMap
                    addToMap(index, wareTermMap);
                    if (ele.contains(" ")) {
                        for (String cell : ele.split(" ")) {
                            if (FileSteward.HasDigit(cell) ||
                                    cell.isEmpty() ||
                                    stopSet.contains(cell)) {
                                continue;
                            }
                            index = getTermIndex(cell, dictMap);
                            addToMap(index, wareTermMap);
                        }
                    }
                }
            }

            // brandName
            index = getTermIndex(ware.getBrandName().toLowerCase(), dictMap);
            addToMap(index, wareTermMap);

            // title
            for (String ele : castrate(ware.getTitle()).toLowerCase().split(" ")) {
                if (FileSteward.HasDigit(ele) ||
                        ele.isEmpty() ||
                        stopSet.contains(ele) ||
                        ele.length() > 20) {
                    continue;
                }
                index = getTermIndex(ele, dictMap);
                addToMap(index, wareTermMap);
            }

            // describe
            if (ware.getDescribe().isEmpty()) { // if no describe, then it can not group with others, seperate it.
//                wareTermMap = new HashMap<Integer, Integer>(){{
//                    put(0, 1);
//                }};
            } else {
                for (String ele : ware.getDescribe().toLowerCase().split("\001")) {
                    for (String term : castrate(ele).split(" ")) {
                        if (stopSet.contains(term) ||
                                FileSteward.HasDigit(term) ||
                                term.isEmpty() ||
                                term.length() > 20) {
                            continue;
                        }
                        index = getTermIndex(term, dictMap);
                        if (type == 0) {
                            addToMap(index, wareTermMap);
                        }
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
        term = Tokenizer.token(term); // get root term
        if (indexMap.containsKey(term)) {
            return indexMap.get(term);
        } else {
//            int index = indexMap.size() + 1; // so we stipulate index start from 1
            int index = indexMap.size(); // so we stipulate index start from 1
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
        return input.replaceAll("amp;"," ").replaceAll("[^0-9a-zA-Z]"," ");
    }

    public static String castrate(String input, Set<String> set) {
        input = castrate(input);
        String[] inputArray = input.split(" ");
        for (int i = 0; i < inputArray.length; i++) {
            if (set.contains(inputArray[i])) {
                inputArray[i] = "";
            }
        }
        Arrays.sort(inputArray);
        StringBuffer result = new StringBuffer();
        for (String ele : inputArray) {
            if (!ele.trim().isEmpty()) {
                result.append(ele.trim()).append(" ");
            }
        }
        return result.toString().trim();
    }
}
