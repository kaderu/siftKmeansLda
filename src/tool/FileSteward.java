package tool;

import actor.DocLdaActor;
import actor.KmeansActor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class FileSteward {

    public final static Map<String, String> unitMap = new HashMap<String, String>(){{
        put("ml", "ml");
//        put("l", "l");
        put("g", "gr");
        put("gr", "gr");
//        put("kg", "kgr");
//        put("kgr", "kgr");
        put("cc", "ml");
    }};

    public final static List<String[]> bracketList = new ArrayList<String[]>() {{
        add(new String[]{"(", ")"});
        add(new String[]{"[", "]"});
        add(new String[]{"{", "}"});
    }};

    private Map<String, String> addDictMap;
    private Map<String, String> addKeywordDictMap;

    public FileSteward() {
        addDictMap = new HashMap<>();
        addKeywordDictMap = new HashMap<>();
    }

    private Set<String> colorSet;

    public FileSteward(String colorDict) {
        Set<String> colorSet = new HashSet<>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            fis = new FileInputStream("color.dic");
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                colorSet.add(str.trim());
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
            this.colorSet = colorSet;
        }
    }

    public static Set<String> getBlockSet() {
        Set<String> set = new HashSet<>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            fis = new FileInputStream("color.dic");
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                set.add(str.trim());
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
            return set;
        }
    }

    public static void delete(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
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

        List<Map.Entry<String, Integer>> infoIds = new ArrayList<>(map.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o1.getValue() - o2.getValue());
            }
        });

        File file = new File(path);
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (Map.Entry<String, Integer> entry : infoIds) {
                bw.write(entry.getValue() + "\t" + entry.getKey() + "\r\n");
            }
            bw.close();
            fw.close();
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
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<WareMsgConventor> getWareMsgList(String path) {
        List<WareMsgConventor> wareMsgList = new ArrayList<>();

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
                wareMsg.setBrandName(eles[2]); // must set first, for some logic will base on it
                wareMsg.setWareId(Long.parseLong(eles[0]));
                wareMsg.setKeywords(eles[1]); // must set second, for some logic will base on it
                wareMsg.setTitle(eles[3]);
                if (eles.length >= 5) {
                    wareMsg.setImgUri(eles[4]);
                }
                if (eles.length >= 6) {
                    wareMsg.setCateId(Long.parseLong(eles[5]));
                }
                if (eles.length >= 8) {
                    wareMsg.setDescribe("NULL".equals(eles[7].trim()) ? "" : eles[7].trim());
                }
                wareMsgList.add(new WareMsgConventor(wareMsg));
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

    public static void storeTransWareList(String path, List<WareMsgTranslate> list) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            String str = "";
            String[] eles;
            fw = new FileWriter(path);
            bw = new BufferedWriter(fw);
            for (WareMsgTranslate transWare : list) {
                // wareId + keyword + brandName + title + describe + detail
                bw.write(transWare.getWareId() + "\t");
                bw.write(transWare.getKeywords() + "\t");
                bw.write(transWare.getBrandName() + "\t");
                bw.write(transWare.getTitle() + "\t");
                bw.write(transWare.getDescribe() + "\t");
                bw.write(transWare.getDetail() + "\n");
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // make a trick here: cut and title (maybe part of title) from describe
    public static List<WareMsgTranslate> getTransWareList(String path) {
        List<WareMsgTranslate> list = new ArrayList<>();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            String[] eles;
            WareMsgTranslate wareMsgTranslate;
            String[] describe;

            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                wareMsgTranslate = new WareMsgTranslate();
                eles = str.split("\t");
                // wareId + keyword + brandName + title + describe + detail
                wareMsgTranslate.setWareId(Long.parseLong(eles[0]));
                wareMsgTranslate.setKeywords(eles[1]);
                wareMsgTranslate.setBrandName(eles[2]);
                wareMsgTranslate.setTitle(eles[3]);
                if (eles.length >= 5) {
                    describe = eles[4].split("\001", 2);
                    // title or the main part of title
                    wareMsgTranslate.setTitle(describe[0]);
                    // part else
                    wareMsgTranslate.setDescribe(describe.length == 2 ? describe[1] : "");
                } else {
                    wareMsgTranslate.setDescribe("");
                }
                if (eles.length >= 6) {
                    wareMsgTranslate.setDetail(eles[5]);
                }
                list.add(wareMsgTranslate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Ware4LateWork> getWare4LateWorkList(String path) {
        List<Ware4LateWork> wareList = new ArrayList<>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            String[] eles;
            Ware4LateWork wareMsg;
            fis = new FileInputStream(path);
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                wareMsg = new Ware4LateWork();
                eles = str.split("\t");
                wareMsg.setWareId(Long.parseLong(eles[0]));
                wareMsg.setLeafCateId(Long.parseLong(eles[5]));
                wareMsg.setTopicId(Long.parseLong(eles[eles.length - 1]));
                wareList.add(wareMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return wareList;
        }
    }

    public static Map<String, Integer> getMergeCellMap(String path, String column) {
        Map<String, Integer> map = new HashMap<>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null; //用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        try {
            String str = "";
            String[] eles;
            String cateAndTopic;
            fis = new FileInputStream(path);
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                eles = str.split("\t");
                cateAndTopic = eles[0] + "_" + eles[1];
                if ("cate".equals(column)) {
                    map.put(cateAndTopic, Integer.parseInt(eles[2]) == 0 ? 999 : Integer.parseInt(eles[2]));
                } else if ("topic".equals(column)) {
                    map.put(cateAndTopic, Integer.parseInt(eles[3]) == 0 ? 999 : Integer.parseInt(eles[3]));
                }
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
            return map;
        }
    }

    public static List<CellCluster> getCellClusterList(String path) {
        List<CellCluster> cellList = new ArrayList<>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            String[] eles;
            CellCluster cellCluster;
            fis = new FileInputStream(path);
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            int i = 0;
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                cellCluster = new CellCluster();
                eles = str.split("\t");
                cellCluster.setId(++i);
                cellCluster.setSize(Integer.parseInt(eles[0]));
                cellCluster.setCateSize(Integer.parseInt(eles[1]));
                cellCluster.setTopicSize(Integer.parseInt(eles[2]));
                cellCluster.setLeafCateId(Long.parseLong(eles[3]));
                cellCluster.setTopicId(Integer.parseInt(eles[4]));
                cellCluster.setCatePercent(Double.parseDouble(eles[5]));
                cellCluster.setTopicPercent(Double.parseDouble(eles[6]));
                cellList.add(cellCluster);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return cellList;
        }
    }

    public static Map<Integer, Map<Integer, Integer>> getCountIndexMap(String path) {
        Map<Integer, Map<Integer, Integer>> map = new HashMap<>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            String str = "";
            String[] eles;
            Map<Integer, Integer> cellMap;

            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);

            int i = 0;
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                eles = str.split("\\s+");
                cellMap = new HashMap<>();
                for (String ele : eles) {
                    if (ele.contains(":")) {
                        String[] elePiece = ele.split(":");
                        cellMap.put(Integer.parseInt(elePiece[0]), Integer.parseInt(elePiece[1]));
                    }
                }
                map.put(i, cellMap);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static Map<Integer, String> getIndexMap(String path) {
        Map<Integer, String> map = new HashMap<>();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            String str = "";
            String[] eles;

            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);

            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                eles = str.split("\t");
                map.put(Integer.parseInt(eles[0]), eles[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    // for wanghui's ask
    public static void dealWareTitleFile(String path, String path2) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        BufferedWriter bw = null;

        FileSteward fileSteward = new FileSteward();
        Map<String, String> dictMap = fileSteward.readTranslateFile();
        try {
            String str = "";
            String[] eles;
            StringBuffer titleBuffer;
            fis = new FileInputStream(path);
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象

            fw = new FileWriter(new File(path2));
            bw = new BufferedWriter(fw);
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                eles = str.split("\t");
                String wareId = eles[0];
                String keywords = eles[1].toLowerCase();
                String title = eles[3].toLowerCase();
                titleBuffer = new StringBuffer(wareId + "\t");

                for (String ele : keywords.split(",")) {
                    String transStr = dictMap.get(ele) == null ? ele : dictMap.get(ele);
                    titleBuffer.append(transStr).append(",");
                }
                titleBuffer.append("\t");

                // bracket pair drop
                for (String[] bracketPair : FileSteward.bracketList) {
                    int start = 0;
                    int end = 0;
                    while ((start = title.indexOf(bracketPair[0])) != -1 &&
                            (end = title.indexOf(bracketPair[1])) != -1) {
                        title = title.substring(0, start) + " " + title.substring(end + 1, title.length());
                    }
                }
                // " - " tail drop
                int tailStart = 0;
                while ((tailStart = title.lastIndexOf(" - ")) != -1 && // has tail
                        tailStart > 15 && // body length
                        title.substring(0, tailStart).split(" ").length > 2) { // several word members
                    title = title.substring(0, tailStart);
                }

                title = title.replaceAll("[\\.\\|\\[\\]\\(\\)\\*\\+\\-&/\\\\]", " ");

                for (String ele : title.split("\\s+")) {
                    if (!HasDigit(ele)) {
                        String transStr = dictMap.get(ele) == null ? ele : dictMap.get(ele);
                        titleBuffer.append(transStr).append(" ");
                    }
                }
                bw.write(titleBuffer.toString() + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
                isr.close();
                fis.close();
                bw.close();
                fw.close();
                // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean HasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    public static String getTargGammaFilePath(String gammaPath) {
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
        return gammaPath + "\\" + maxIndex + ".gamma";

    }

    public static String getTargKmeansInputFilePath(String gammaPath, boolean needNormalize) {
        String gammaFilePath = getTargGammaFilePath(gammaPath);
        String kmeansInputFilePath = gammaFilePath.replace(".gamma", ".sigma");

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fis = new FileInputStream(gammaFilePath);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            fw = new FileWriter(kmeansInputFilePath);
            bw = new BufferedWriter(fw);
            String str;
            String[] eles;
            boolean hasKmeansHead = false;
            while ((str = br.readLine()) != null) {
                if (str.trim().isEmpty() ||
                        str.startsWith("@")) {
                    continue;
                }
                eles = str.split(" ");
                if (!hasKmeansHead) {
                    bw.write("@RELATION cate_remix\n\n");
                    for (int i = 1; i <= eles.length; i++) {
                        bw.write("@ATTRIBUTE " + i + " REAL\n");
                    }
                    bw.write("\n@data\n");
                    hasKmeansHead = true;
                }
                if (needNormalize) {
                    double[] topicWeight = new double[eles.length];
                    double squareSum = 0;
                    for (int i = 0; i < eles.length; i++) {
                        topicWeight[i] = Double.parseDouble(eles[i]);
                        squareSum += topicWeight[i] * topicWeight[i];
                    }
                    double distance = Math.sqrt(squareSum);
                    StringBuffer normalizeBuffer = new StringBuffer();
                    for (int i = 0; i < topicWeight.length; i++) {
                        normalizeBuffer.append(topicWeight[i] / distance).append(" ");
                    }
                    bw.write(normalizeBuffer.toString().trim() + "\n");
                } else {
                    bw.write(str + "\n");
                }
            }
            bw.close();
            fw.close();

            br.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kmeansInputFilePath;
    }

    public static Map<Long, Number> mergTopic2WareId(String gammaPath, String wkbtPath) {
        List<WareMsgConventor> wareMsgList = getWareMsgList(wkbtPath);
        String leastFilePath =  getTargGammaFilePath(gammaPath);

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

        Map<Long, Number> map = new TreeMap<>();
        if (wareMsgList.size() == topicIdList.size()) {
            for (int i = 0; i < wareMsgList.size(); i++) {
                map.put(wareMsgList.get(i).getWareId(), topicIdList.get(i));
            }
        }
        return map;
    }

    public static Map<Long, Number> mergLeafCate2WareId(String wkbtPath) {
        List<WareMsgConventor> wareMsgList = getWareMsgList(wkbtPath);
        Map<Long, Number> map = new TreeMap<>();
        for (WareMsgConventor ele : wareMsgList) {
            map.put(ele.getWareId(), ele.getCateId());
        }
        return map;
    }

    public static void copyFile(String path1, String path2) {
        File f1 = new File(path1);
        File f2 = new File(path2);
        int length=2097152;

        try {
            FileInputStream in=new FileInputStream(f1);
            FileOutputStream out=new FileOutputStream(f2);
            FileChannel inC = in.getChannel();
            FileChannel outC = out.getChannel();
            ByteBuffer b = null;

            while(true){
                if(inC.position() == inC.size()){
                    inC.close();
                    outC.close();
                }
                if((inC.size() - inC.position()) < length){
                    length = (int)(inC.size() - inC.position());
                }else
                    length=2097152;
                b = ByteBuffer.allocateDirect(length);
                inC.read(b);
                b.flip();
                outC.write(b);
                outC.force(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergTopic2LeafCateId(String path1, String path2, Map<Long, Number> map) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        FileWriter fw;
        BufferedWriter bw;
        try {
            String str = "";
            int topicIndex;
            fis = new FileInputStream(path1);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            fw = new FileWriter(new File(path2));
            bw = new BufferedWriter(fw);
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                String[] eles = str.split("\t");
                topicIndex = (int) map.get(Long.parseLong(eles[0]));
                bw.write(eles[0] + "\t"
                        + eles[1] + "\t"
                        + eles[2] + "\t"
                        + eles[3] + "\t"
                        + eles[4] + "\t"
                        + eles[5] + "\t"
                        + eles[6] + "\t"
                        + topicIndex + "\r\n");
            }
            br.close();
            isr.close();
            fis.close();
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergTopic2LeafCateId(String path1, String path2, List<Integer> list) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        FileWriter fw;
        BufferedWriter bw;
        try {
            String str = "";
            int topicIndex;
            fis = new FileInputStream(path1);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            fw = new FileWriter(new File(path2));
            bw = new BufferedWriter(fw);
            int index = 0;
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                topicIndex = list.get(index);
                bw.write(str + "\t" + topicIndex + "\r\n");
                index++;
            }
            br.close();
            isr.close();
            fis.close();
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<String> readStopSet() {
        Set<String> set = new HashSet<>();
        if (!new File("stop.dic").exists()) {
            return set;
        }
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            fis = new FileInputStream("stop.dic");
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                set.add(str.trim());
            }
            br.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }


    public Map<String, String> readTranslateFile() {
        Map<String, String> map = new HashMap<>();

        Map<String, String> mapTitle = dictReader("translate.dic");
        Map<String, String> mapKeyword = dictReader("keyword_translate.dic");
        // add for single leaf_cate 2017/8/9, small size of wordnet
        Map<String, String> mapWordnet = dictReader("C:\\Users\\zhangshangzhi\\Desktop\\pic\\pic_75061333\\private_dict.txt");

        map.putAll(mapTitle);
        map.putAll(mapKeyword);
        map.putAll(mapWordnet);


        return map;
    }

    public Map<String, String> dictReader(String path) {
        Map<String, String> map = new HashMap<>();
        if (!new File(path).exists()) {
            return map;
        }

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            String[] eles;
            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                eles = str.split("\t");
                map.put(eles[0], eles[1]);
            }
            br.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public void storeAddTranslateFile() {
        storeFile("translate.dic", addDictMap, 1);
        storeFile("keyword_translate.dic", addKeywordDictMap, 2);
    }

    private void storeFile(String dic, Map<String, String> map, int type) { // type 1 for title, type 2 for keyword
        Map<String, String> oriMap = dictReader(dic);
        if (type == 1) {
            mapMergeMultiTracker(oriMap, map);
        } else if (type == 2) {
            mapMergePasteTracker(oriMap, map);
        } else {
            oriMap.putAll(map);
        }

        File file = new File(dic);
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (String key : oriMap.keySet()) {
                bw.write(key + "\t" + oriMap.get(key) + "\n");
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void storeFile(String dic, Map<String, String> map) {
        File file = new File(dic);
        FileWriter fw;
        BufferedWriter bw;
        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            for (String key : map.keySet()) {
                bw.write(key + "\t" + map.get(key) + "\n");
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // while add translate map to ori, deal with case: paste for [keyword]
    private void mapMergePasteTracker(Map<String, String> oriMap, Map<String, String> mapAdd) {
        Map<String, List<String>> paste2OriMap = new HashMap<>();
        for (final String key : oriMap.keySet()) {
            if (!paste2OriMap.containsKey(key.replaceAll(" ", ""))) {
                paste2OriMap.put(key.replaceAll(" ", ""), new ArrayList<String>());
            }
            paste2OriMap.get(key.replaceAll(" ", "")).add(key);
        }

        List<String> colonKeywordList;
        String realKeyword;
        String realTranslateStr;
        for (final Map.Entry<String, String> entry : mapAdd.entrySet()) {
            if (paste2OriMap.containsKey(entry.getKey().replaceAll(" ", ""))) {
                colonKeywordList = paste2OriMap.get(entry.getKey().replaceAll(" ", ""));
                realKeyword = getRealKeyword(entry.getKey(), colonKeywordList);
                if (realKeyword.equals(entry.getKey())) { // the new comer is 真の物
                    realTranslateStr = entry.getValue();
                } else {
                    realTranslateStr = oriMap.get(realKeyword);
                }
                oriMap.put(entry.getKey(), realTranslateStr);
                for (String ele : colonKeywordList) {
                    oriMap.put(ele, realTranslateStr);
                }
                colonKeywordList.add(entry.getKey());
            } else {
                oriMap.put(entry.getKey(), entry.getValue());
                paste2OriMap.put(entry.getKey().replaceAll(" ", ""), new ArrayList<String>(){{add(entry.getKey());}});
            }
        }

        Map<String, List<String>> turnbackMap = new HashMap<>();
        for (String key : oriMap.keySet()) {
            if (!turnbackMap.containsKey(oriMap.get(key))) {
                turnbackMap.put(oriMap.get(key), new ArrayList<String>());
            }
            turnbackMap.get(oriMap.get(key)).add(key);
        }
        for (String value : turnbackMap.keySet()) {
            if (value.endsWith("s") &&
                    turnbackMap.containsKey(value.substring(0, value.length() - 1))) {
                for (String key : turnbackMap.get(value)) {
                    oriMap.put(key, value.substring(0, value.length() - 1));
                }
            }
        }
    }

    // while add translate map to ori, deal with case: multi-s for [title]
    private void mapMergeMultiTracker(Map<String, String> oriMap, Map<String, String> mapAdd) {
        Map<String, List<String>> singleMultiMap = new HashMap<>();
        for (String key : oriMap.keySet()) {
            if (!singleMultiMap.containsKey(oriMap.get(key))) {
                singleMultiMap.put(oriMap.get(key), new ArrayList<String>());
            }
            singleMultiMap.get(oriMap.get(key)).add(key);
        }
        String translateAdd;
        for (final String key : mapAdd.keySet()) {
            translateAdd = mapAdd.get(key);
            if (translateAdd.endsWith("s") &&
                    singleMultiMap.containsKey(
                            translateAdd.substring(0, translateAdd.length() - 1))) {
                oriMap.put(key, translateAdd.substring(0, translateAdd.length() - 1));
                if (singleMultiMap.containsKey(translateAdd)) {
                    for (String ele : singleMultiMap.get(translateAdd)) {
                        oriMap.put(ele, translateAdd.substring(0, translateAdd.length() - 1));
                    }
                }
                singleMultiMap.remove(translateAdd);
            } else if (singleMultiMap.containsKey(translateAdd + "s")) {
                oriMap.put(key, translateAdd);
                for (String ele : singleMultiMap.get(translateAdd + "s")) {
                    oriMap.put(ele, translateAdd);
                }
                singleMultiMap.remove(translateAdd + "s");
            }
        }
    }

    private String getRealKeyword(String keyword, List<String> list) {
        for (String ele : list) {
            if (ele.length() > keyword.length()) {
                keyword = ele;
            }
        }
        return keyword;
    }

    public Map<String, String> getAddDictMap() {
        return addDictMap;
    }

    public Map<String, String> getAddKeywordDictMap() {
        return addKeywordDictMap;
    }


    public Set<String> getColorSet() {
        return colorSet;
    }

    public static List<double[]> getKmeansKernelList(String path) {
        List<double[]> kernelList = new ArrayList<>();
        for (int i = 0; i < KmeansActor.clusterNum; i++) { //TODO fix
            kernelList.add(new double[DocLdaActor.clusterNum]);
        }
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        int kernelLen = 0;
        try {
            String str = "";
            String[] eles;
            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim())) {
                    continue;
                }
                eles = str.split("\\s+");
                kernelLen = eles.length;
                for (int i = 0; i < eles.length - 2; i++) {
                    kernelList.get(i)[Integer.parseInt(eles[0]) - 1] = Double.parseDouble(eles[i + 2]);
                }
            }
            br.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        for (double[] kernel : kernelList) {
//            System.arraycopy(kernel, 0, kernel, 0, kernelLen);
//        }

        return kernelList;
    }

    public static List<double[]> getGammaTopicSimlarList(String path) {
        List<double[]> gammaList = new ArrayList<>();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            String str = "";
            String[] eles;
            double[] gammaArray;
            fis = new FileInputStream(path);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((str = br.readLine()) != null) {
                if ("".equals(str.trim()) ||
                        str.startsWith("@")) {
                    continue;
                }
                eles = str.split("\\s+");
                gammaArray = new double[DocLdaActor.clusterNum];
                for (int i = 0; i < DocLdaActor.clusterNum; i++) {
                    gammaArray[i] = Double.parseDouble(eles[i]);
                }
                gammaList.add(gammaArray);
            }
            br.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gammaList;
    }

    public static void main(String[] args) {
        String str = "00";
        System.out.println(Integer.parseInt(str));
        String str2 = "123";
        String[] array = str2.split("\t", 2);
    }
}
