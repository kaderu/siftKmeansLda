package tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return FileSteward.getCellClusterList(prefixPath + "cellClusterTree.txt");
    }

    private Map<Integer, Integer> indexMapMaid(long leafCateId, long topicId) {
        return lateWork.getPopularTerms(leafCateId, topicId);
    }

    public double scoreMaid(Map<Integer, Integer> map1, Map<Integer, Integer> map2) {
        return  MapSimilaritySteward.getSimilarityScore(map1, map2);
    }

    public void maidCaptain() {
        List<CellCluster> list = getCellClusterList();
        Map<Long, List<CellCluster>> leafCateMap = new HashMap<>();
        for (CellCluster cluster : list) {
            if (cluster.getSize() < 5) { // drop pieces too small
                continue;
            }
            if (!leafCateMap.containsKey(cluster.getLeafCateId())) {
                leafCateMap.put(cluster.getLeafCateId(), new ArrayList<CellCluster>());
            }
            leafCateMap.get(cluster.getLeafCateId()).add(cluster);
        }

        Map<Integer, Integer> map = new HashMap<>();
        CellCluster cluster1;
        CellCluster cluster2;
        Map<Integer, Integer> indexMap1;
        Map<Integer, Integer> indexMap2;
        double score;
        for (Map.Entry<Long, List<CellCluster>> entry : leafCateMap.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                for (int j = 0; j < i; j++) {
                    cluster1 = entry.getValue().get(i);
                    cluster2 = entry.getValue().get(j);
                    indexMap1 = indexMapMaid(cluster1.getLeafCateId(), cluster1.getTopicId());
                    indexMap2 = indexMapMaid(cluster2.getLeafCateId(), cluster2.getTopicId());
                    score = scoreMaid(indexMap1, indexMap2);
                    if (score >= threshold) {
                        map.put(cluster1.getId(), cluster2.getId());
                    }
                }
            }
        }

        for (int id1 : map.keySet()) {
            System.out.println(id1 + "\t" + map.get(id1));
        }
    }
}
