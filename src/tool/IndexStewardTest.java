package tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.IndexSteward.concatAll;

/**
 * Created by zhangshangzhi on 2017/8/2.
 */
public class IndexStewardTest {

    public static void main(String[] args) {
        MultiLayerIndexMap dicMap = null;
        // make index map
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        Map<Integer, Integer> map;
        map = new HashMap<>();

        WareMsg wareMsg = new WareMsg();

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
    }
}
