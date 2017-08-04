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

    public void getPopularTerms(long leafCateId, long topicId) {
        Map<Integer, Long> wareIdList = new HashMap<>();
        int i = 0;
        for (Ware4LateWork ware : wareList) {
            if (ware.getLeafCateId() == leafCateId &&
                    ware.getTopicId() == topicId) {
                wareIdList.put(i, ware.getWareId());
            }
            i++;
        }

        Map<String, Integer> termCountMap = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> countIndexMap = FileSteward.getCountIndexMap(prefixPath + "wkbtLda.dat");
        Map<Integer, String> indexMap = FileSteward.getIndexMap(prefixPath + "word_index.txt");
        for (int ind : wareIdList.keySet()) {
            Map<Integer, Integer> cellMap = countIndexMap.get(ind);
            for (int termIndex : cellMap.keySet()) {
                String term = indexMap.get(termIndex);
                if (termCountMap.containsKey(term)) {
                    termCountMap.put(term, termCountMap.get(term) + cellMap.get(termIndex));
                } else {
                    termCountMap.put(term, cellMap.get(termIndex));
                }
            }
        }
        List<Map.Entry<String, Integer>> infoIds = new ArrayList<>(termCountMap.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
//                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });

        System.out.println("### size of ware list is: " + wareIdList.size());
        int count = 0;
        for (Map.Entry<String, Integer> entry : infoIds) {
            if (count < 30) {
                System.out.println(entry.getKey() + "\t" + entry.getValue());
            }
            count++;
        }
    }

    public void getWareIndexComment(long wareId) {
        Map<String, Integer> termCountMap = new HashMap<>();
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
            String term = indexMap.get(termIndex);
            if (termCountMap.containsKey(term)) {
                termCountMap.put(term, termCountMap.get(term) + cellMap.get(termIndex));
            } else {
                termCountMap.put(term, cellMap.get(termIndex));
            }
        }
        for (Map.Entry<String, Integer> entry : termCountMap.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
    }

    public static void main(String[] args) {

    }
}
