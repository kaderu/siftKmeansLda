package tool;

import java.io.*;
import java.util.*;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class FileSteward {

    public static void delete(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.canRead()) {
            file.delete();
        } else if (file.isDirectory()) {
            String[] children = file.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                delete(file + "\\" + children[i]);
            }
            // 目录此时为空，可以删除
            file.delete();
        }
    }

    public static void storeDict(Map<String, Integer> map, String path) {
        delete(path);

        File file = new File(path);
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                bw.write(entry.getValue() + "\t" + entry.getKey() + "\r\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeIndex(List<Map<Integer, Integer>> mapList, String path) {
        delete(path);

        File file = new File(path);
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (Map<Integer, Integer> map : mapList) {
                bw.write("" + map.size());
                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    bw.write(" " + entry.getKey() + ":" + entry.getValue());
                }
                bw.write("\r\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static Map<Long, Integer> mergTopic2WareId(String gammaPath, String wkbtPath) {
        List<WareMsg> wareMsgList = getWareMsgList(wkbtPath);
        File gammaFiles = new File(gammaPath);
        File[] gammas = gammaFiles.listFiles();
        int index = 0;
        String maxIndex = "";
        for (File file : gammas) {
            if (!file.getName().endsWith("gamma")) {
                continue;
            }
            int curIndex = Integer.parseInt(file.getName().split("\\.")[0]);
            if (curIndex > index) {
                index = curIndex;
                maxIndex = file.getName().split("\\.")[0];
            }
        }
        String leastFilePath = gammaPath + "\\" + maxIndex + ".gamma";

        List<Integer> topicIdList = new ArrayList<>();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null; //用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        try {
            String str = "";
            String[] eles;
            fis = new FileInputStream(leastFilePath);
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                eles = str.split("\\s+");
                List<Double> list = new ArrayList<>();
                for (String ele : eles) {
                    list.add(Double.parseDouble(ele));
                }
                topicIdList.add(list.indexOf(Collections.max(list)));
            }
            br.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<Long, Integer> map = new TreeMap<>();
        if (wareMsgList.size() == topicIdList.size()) {
            for (int i = 0; i < wareMsgList.size(); i++) {
                map.put(wareMsgList.get(i).getWareId(), topicIdList.get(i));
            }
        }
        return map;
    }

    public static void main(String[] args) {
        String str = "00";
        System.out.println(Integer.parseInt(str));
    }
}
