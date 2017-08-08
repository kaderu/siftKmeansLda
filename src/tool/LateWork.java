package tool;

import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/3.
 */
public class LateWork {

    private List<Ware4LateWork> wareList;

    private String prefixPath;

    public LateWork(String path) { // oriVsCur.txt
        prefixPath = path.replace("oriVsCur.txt", "");
        if (wareList == null) {
            initWareList(path);
        }
    }

    private void initWareList(String path) {
        wareList = FileSteward.getWare4LateWorkList(path);
    }

    public Map<Integer, Integer> getPopularTerms(long leafCateId, long topicId) {
        Map<Integer, Long> wareIdList = new HashMap<>();
        int i = 0;
        for (Ware4LateWork ware : wareList) {
            if (ware.getLeafCateId() == leafCateId &&
                    ware.getTopicId() == topicId) {
                wareIdList.put(i, ware.getWareId());
            }
            i++;
        }

        Map<Integer, Integer> termCountMap = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> countIndexMap = FileSteward.getCountIndexMap(prefixPath + "wkbtLda.dat");
        Map<Integer, String> indexMap = FileSteward.getIndexMap(prefixPath + "word_index.txt");
        for (int ind : wareIdList.keySet()) {
            Map<Integer, Integer> cellMap = countIndexMap.get(ind);
            for (int termIndex : cellMap.keySet()) {
                if (termCountMap.containsKey(termIndex)) {
                    termCountMap.put(termIndex, termCountMap.get(termIndex) + cellMap.get(termIndex));
                } else {
                    termCountMap.put(termIndex, cellMap.get(termIndex));
                }
            }
        }


        // output
//        /*
        List<Map.Entry<Integer, Integer>> infoIds = new ArrayList<>(termCountMap.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o2.getValue() - o1.getValue());
//                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });
        System.out.println("### size of ware list is: " + wareIdList.size());
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : infoIds) {
            if (count < 30) {
                System.out.println(indexMap.get(entry.getKey()) + "\t" + entry.getValue());
            }
            count++;
        }
//        */

        return termCountMap;
    }

    public Map<Integer, Integer> getWareIndexComment(long wareId) {
        Map<Integer, Integer> termCountMap = new HashMap<>();
        int i = 0;
        for (Ware4LateWork ware : wareList) {
            if (ware.getWareId() == wareId) {
                break;
            }
            i++;
        }
        Map<Integer, Map<Integer, Integer>> countIndexMap = FileSteward.getCountIndexMap(prefixPath + "wkbtLda.dat");
        Map<Integer, String> indexMap = FileSteward.getIndexMap(prefixPath + "word_index.txt");
        Map<Integer, Integer> cellMap = countIndexMap.get(i);
        for (int termIndex : cellMap.keySet()) {

            if (termCountMap.containsKey(termIndex)) {
                termCountMap.put(termIndex, termCountMap.get(termIndex) + cellMap.get(termIndex));
            } else {
                termCountMap.put(termIndex, cellMap.get(termIndex));
            }
        }

        for (Map.Entry<Integer, Integer> entry : termCountMap.entrySet()) {
            System.out.println(indexMap.get(entry.getKey()) + "\t" + entry.getValue());
        }
        return termCountMap;
    }

    public static void main(String[] args) {

    }
}
