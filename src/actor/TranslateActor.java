package actor;

import pic.algorithm.translate.TranslateUtil;
import tool.FileSteward;
import tool.ForkJoinPoolProcessor;
import tool.WareMsgConventor;
import tool.WareMsgTranslate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Created by zhangshangzhi on 2017/8/10.
 */
public class TranslateActor {

    public static void main(String[] args) {
        DocLdaActor.init();
        List<WareMsgConventor> wareList = FileSteward.getWareMsgList(DocLdaActor.wkbt_file);
//        List<WareMsgTranslate> transWareList = forkJoinMethod(wareList);
        List<WareMsgTranslate> transWareList = singleTaskMethod(wareList);

        FileSteward.storeTransWareList(DocLdaActor.wkbt_file.replace("wkbt.txt", "transWare.txt"), transWareList);
    }


    public static List<WareMsgTranslate> forkJoinMethod(List<WareMsgConventor> wareList) {
        List<WareMsgTranslate> list = new ArrayList<>();
        ForkJoinPool bussinessPool = new ForkJoinPool();
        Map<Long, List<Object>> map = null;
        Future<Map<Long, List<Object>>> bussinessFuture = bussinessPool.submit(new ForkJoinPoolProcessor(0, wareList.size(), 2, wareList));
        try {
            map = bussinessFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bussinessPool.shutdown();
        for (long id : map.keySet()) {
            list.add(new WareMsgTranslate((WareMsgConventor) map.get(id).get(0)));
        }
        return list;
    }

    public static List<WareMsgTranslate> singleTaskMethod(List<WareMsgConventor> wareList) {
        FileSteward fileSteward = new FileSteward("");
        Map<String, String> transMap = fileSteward.dictReader("describ.dic");

        List<WareMsgTranslate> list = new ArrayList<>();

        int i = 1;
        for (WareMsgConventor ware : wareList) {
            translate(ware, transMap);
            list.add(new WareMsgTranslate(ware));
            System.out.println("######### " + i++ + "th ware finished translate.");
        }
        fileSteward.storeFile("describ.dic", transMap);
        return list;
    }

    public static void translate(WareMsgConventor ware, Map<String, String> transMap) {
        // describe
        String describe = ware.getDescribe();
        if (describe != null &&
                !"".equals(describe.trim())) {
            String[] elements = describe.split("[\\s][\\s][\\s]+");
            StringBuffer describeBuffer = new StringBuffer();
            for (int i = 0; i < elements.length; i++) {
                if (elements[i] == null ||
                        "".equals(elements[i].trim()) ||
                        "detail".equals(elements[i].trim().toLowerCase()) ||
                        "detail:".equals(elements[i].replaceAll("\\s+", "").toLowerCase())) { // "detail" this single phrase we don't need
                    continue;
                }
                String translateStr = translate(elements[i], transMap);
                if (!translateStr.isEmpty()) {
                    describeBuffer.append(translateStr).append("\001");
                }
            }
            ware.setDescribe(describeBuffer.toString());
        }

        // title
        String title = ware.getTitle();
        if (title != null &&
                !title.trim().isEmpty()) {
            ware.setTitle(translate(title, transMap));
        }

        // keywords
        String[] keywords = ware.getKeywords();
        if (keywords != null &&
                keywords.length > 0) {
            for (int i = 0; i < keywords.length; i++) {
                String translateStr = translate(keywords[i], transMap);
                if (!translateStr.isEmpty()) {
                    keywords[i] = translateStr;
                }
            }
            ware.setKeywords(keywords);
        }
    }


    public static void translate(WareMsgConventor ware) {
        String describe = ware.getDescribe();
        if (describe != null ||
                !"".equals(describe.trim())) {
            String[] elements = describe.split("[\\s][\\s][\\s]+");
            StringBuffer describeBuffer = new StringBuffer();
            for (int i = 0; i < elements.length; i++) {
                if ("".equals(elements[i].trim()) ||
                        "detail".equals(elements[i].trim().toLowerCase()) ||
                        "detail:".equals(elements[i].replaceAll("\\s+", "").toLowerCase())) { // "detail" this single phrase we don't need
                    continue;
                }
                String translateStr = translate(elements[i]);
                if (!translateStr.isEmpty()) {
                    describeBuffer.append(translateStr).append("\001");
                }
            }
            ware.setDescribe(describeBuffer.toString());
        }


        String[] keywords = ware.getKeywords();
        for (int i = 0; i < keywords.length; i++) {
            String translateStr = translate(keywords[i]);
            if (!translateStr.isEmpty()) {
                keywords[i] = translateStr;
            }
        }
        ware.setKeywords(keywords);
    }


    /*
    public static void translate(WareMsgConventor ware) {
        String describe = ware.getDescribe();
        if (describe != null ||
                !"".equals(describe.trim())) {
            String trans = translate(describe);
            ware.setDescribe(trans);
        }

        String[] keywords = ware.getKeywords();
        for (int i = 0; i < keywords.length; i++) {
            String translateStr = translate(keywords[i]);
            if (!translateStr.isEmpty()) {
                keywords[i] = translateStr;
            }
        }
        ware.setKeywords(keywords);
    }
    */

    public static String translate(String str, Map<String, String> map) {
        if (map.containsKey(str)) {
            return map.get(str);
        }
        String trans = "";
        int i = 0;
        while (i < 3 && trans.isEmpty()) {
            try {
                trans = TranslateUtil.id2en(str);
            } catch (Exception e) {
                i++;
                if (i == 3) {
                    System.out.println(str);
                }
            }
        }
        if (trans.isEmpty()) {
            return str;
        } else {
            map.put(str, trans);
            return trans;
        }
    }

    public static String translate(String str) {
        String trans = "";
        int i = 0;
        while (i < 3 && trans.isEmpty()) {
            try {
                trans = TranslateUtil.id2en(str);
            } catch (Exception e) {
                i++;
                if (i == 3) {
                    System.out.println(str);
                }
            }
        }
        if (trans.isEmpty()) {
            return str;
        } else {
            return trans;
        }
    }
}
