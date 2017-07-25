package tool;

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
        } catch (FileNotFoundException e) {
            System.out.println("找不到指定文件");
        } catch (IOException e) {
            System.out.println("读取文件失败");
        } finally {
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
        Map<String, Integer> dicMap = new HashMap<>();
        int i = 0;
        for (String ele : dictSet) {
            dicMap.put(ele, i++);
        }
        dicTranslateDealer(dicMap);

        // store dict
        FileSteward.storeDict(dicMap, dictFilePath);

        // make index map
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        Map<Integer, Integer> map;
        for (WareMsg wareMsg : wareMsgList) {
            map = new HashMap<>();
            for (String keyword : wareMsg.getKeywords()) {
                int wordIndex = dicMap.get(keyword);
                if (map.containsKey(wordIndex)) {
                    map.put(wordIndex, map.get(wordIndex) + 1);
                } else {
                    map.put(wordIndex, 1);
                }
            }
            mapList.add(map);
        }
        FileSteward.storeIndex(mapList, indexFilePath);
    }

    public static void dicTranslateDealer(Map<String, Integer> dicMap) {
        // TODO google translate
    }
}
