package tool;

import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/4.
 */
public class MergeSteward {

    private LateWork lateWork;
    private double threshold;
    private String prefixPath;

    public MergeSteward(String path) {
        lateWork = new LateWork(path);
        prefixPath = path.replace("oriVsCur.txt", "");
        threshold = 0.5; // TODO
    }

    private List<CellCluster> getCellClusterList() {
        List<CellCluster> list = FileSteward.getCellClusterList(prefixPath + "cellClusterTree.txt");
        return list;
    }

    private Map<Integer, Integer> indexMapMaid(long leafCateId, long topicId) {
        return lateWork.getPopularTerms(leafCateId, topicId);
    }

    public double scoreMaid(Map<Integer, Integer> map1, Map<Integer, Integer> map2) {
        return  MapSimilaritySteward.getSimilarityScore(map1, map2);
    }

    public void maidCaptain(String columnStr) {
        List<CellCluster> list = getCellClusterList();

        Map<Integer, List<Integer>> map = new TreeMap<>();
        CellCluster cluster1;
        CellCluster cluster2;
        Map<Integer, Integer> indexMap1;
        Map<Integer, Integer> indexMap2;
        double score;
        if ("cate".equals(columnStr)) {
            Map<Long, List<CellCluster>> leafCateMap = new HashMap<>();
            for (CellCluster cluster : list) {
//            if (cluster.getSize() < 5) { // drop pieces too small
//                continue;
//            }
                if (!leafCateMap.containsKey(cluster.getLeafCateId())) {
                    leafCateMap.put(cluster.getLeafCateId(), new ArrayList<CellCluster>());
                }
                leafCateMap.get(cluster.getLeafCateId()).add(cluster);
            }
            for (Map.Entry<Long, List<CellCluster>> entry : leafCateMap.entrySet()) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    for (int j = 0; j < i; j++) {
                        cluster1 = entry.getValue().get(i);
                        cluster2 = entry.getValue().get(j);
                        indexMap1 = indexMapMaid(cluster1.getLeafCateId(), cluster1.getTopicId());
                        indexMap2 = indexMapMaid(cluster2.getLeafCateId(), cluster2.getTopicId());
                        score = scoreMaid(indexMap1, indexMap2);
                        if (score >= threshold) {
                            if (!map.containsKey(cluster2.getId())) {
                                map.put(cluster2.getId(), new ArrayList<Integer>());
                            }
                            map.get(cluster2.getId()).add(cluster1.getId());
                        }
                    }
                }
            }
        } else if ("topic".equals(columnStr)) {
            Map<Integer, List<CellCluster>> topicMap = new HashMap<>();
            for (CellCluster cluster : list) {
                if (!topicMap.containsKey(cluster.getTopicId())) {
                    topicMap.put(cluster.getTopicId(), new ArrayList<CellCluster>());
                }
                topicMap.get(cluster.getTopicId()).add(cluster);
            }
            for (Map.Entry<Integer, List<CellCluster>> entry : topicMap.entrySet()) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    for (int j = 0; j < i; j++) {
                        cluster1 = entry.getValue().get(i);
                        cluster2 = entry.getValue().get(j);
                        indexMap1 = indexMapMaid(cluster1.getLeafCateId(), cluster1.getTopicId());
                        indexMap2 = indexMapMaid(cluster2.getLeafCateId(), cluster2.getTopicId());
                        score = scoreMaid(indexMap1, indexMap2);
                        if (score >= threshold) {
                            if (!map.containsKey(cluster2.getId())) {
                                map.put(cluster2.getId(), new ArrayList<Integer>());
                            }
                            map.get(cluster2.getId()).add(cluster1.getId());
                        }
                    }
                }
            }
        }

        Set<Set<Integer>> setList = new HashSet<>();
        Set<Integer> sinSet = new HashSet<>();
        Map<Integer, Integer> idMap = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            if (sinSet.contains(entry.getKey())) {
                continue;
            }
            Set<Integer> curSet = new TreeSet<>();
            curSet.add(entry.getKey());
            loopSin(map, entry.getValue(), curSet);
            setList.add(curSet);
            sinSet.addAll(curSet);

            for (int curId : curSet) {
                if (!idMap.containsKey(curId)) {
                    idMap.put(curId, 1);
                } else {
                    idMap.put(curId, idMap.get(curId) + 1);
                }
            }
        }

        Set<Set<Integer>> finalSet = new HashSet<>();
        for (int cellId : idMap.keySet()) {
            if (idMap.get(cellId) > 1) {
                Set<Integer> oriSet = null;
                Iterator<Set<Integer>> it = setList.iterator();
                while (it.hasNext()) {
                    Set<Integer> set = it.next();
                    if (set.contains(cellId)) {
                        if (oriSet == null) {
                            oriSet = set;
                        } else {
                            oriSet.addAll(set);
                            it.remove();
                        }
                    }
                }
                finalSet.add(oriSet);
            }
        }
        finalSet.addAll(setList);
//        for (Set<Integer> set : finalList) {
//            for (int id : set) {
//                System.out.print(id + " ");
//            }
//            System.out.println("");
//        }

        Map<Integer, Integer> printMap = new HashMap<>();
        int ind = 0;
        for (Set<Integer> set : finalSet) {
            ind++;
            for (int id : set) {
                printMap.put(id, ind);
            }
        }
        for (int i = 1; i < list.size() + 1; i++) {
            if (printMap.containsKey(i)) {
                System.out.println(printMap.get(i));
            } else {
                System.out.println(0);
            }

        }
    }

    private static void loopSin(Map<Integer, List<Integer>> map, List<Integer> oriSinList, Set<Integer> set) {
        for (int oriSin : oriSinList) {
            set.add(oriSin);
            if (map.containsKey(oriSin)) {
                loopSin(map, map.get(oriSin), set);
            }
        }
    }

    public static void main(String[] args) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        List<Integer> list1 = new ArrayList<Integer>(){{
            add(10);
            add(11);
        }};
        List<Integer> list2 = new ArrayList<Integer>(){{
            add(20);
            add(21);
        }};
        map.put(1, list1);
        map.put(10, list2);
        loopSin(map, new ArrayList<Integer>(){{add(1);}}, new HashSet<Integer>());
    }
}
