package actor;

import tool.FileSteward;
import tool.Tokenizer;
import tool.WareMsgTranslate;

import java.util.*;

/**
 * Created by zhangshangzhi on 2017/8/11.
 */
public class DescribeActor {

    private static final Set<String> prepSet = new HashSet<String>() {{
        add("in");
        add("on");
        add("with");
        add("and");
        add("by");
        add("for");
        add("at");
        add("about");
        add("under");
        add("of");
        add("into");
        add("within");
        add("throughout");
        add("through");
        add("inside");
        add("outside");
        add("without");
        add("that");
    }};

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

            System.out.println("########### " + ware.getWareId());

            Map<Integer, Integer> wareTermMap = new HashMap<>();
            int index;

            /*
            let's make a interval discussion here for what count most for a ware doc.
             If keywords is of high-quanlity, then keyword count the most. Then is brandName, which connect similar wares together.
             If both above lose efficacy (like K:lego B:lego case), then we bank on title.
             But sometimes, title contribute little -- maybe it is just brandName plus version-number plus color. This case we should ask DESCRIBE for help.
             Then how ?
             Full-text helps little as a conclution of our test. But a CONTENT label always tell us what this ware is on earth. We like this label.
             At worset, nothing help above. Then we have to focus on describe content. As a commen sence, the first sentence would told us cat or dog, that's enough.
            */

            boolean msgEnough = false;
            // keyword
            String keywords = ware.getKeywords();
            if (keywords != null &&
                    !keywords.trim().isEmpty()) {
                keywords = keywords.toLowerCase().trim();
                for (String ele : new HashSet<String>(Arrays.asList(keywords.split("[\\s]{0,},[\\s]{0,}")))) {
                    ele = castrate(ele, stopSet);
                    System.out.println("keyword:\t" + ele);
                    if (!ele.isEmpty()) {
                        index = getTermIndex(ele, dictMap); // this will add ele to dicMap
                        addToMap(index, wareTermMap);
                        msgEnough = true; // we get kernel msg
                        if (ele.contains(" ")) {
                            for (String cell : ele.split("\\s+")) {
                                if (FileSteward.HasDigit(cell) ||
                                        stopSet.contains(cell)) {
                                    continue;
                                }
                                index = getTermIndex(cell, dictMap);
                                addToMap(index, wareTermMap);
                            }
                        }
                    }
                }
            }
            // brandName
//            index = getTermIndex(ware.getBrandName().toLowerCase(), dictMap);
//            addToMap(index, wareTermMap);
            // title
            if (!msgEnough &&
                    ware.getTitle() != null) {
                String title = ware.getTitle().toLowerCase().trim().replaceAll(ware.getBrandName() + " ", " "); // TODO more strict drop
                for (String ele : castrate(title).split("\\s+")) {
                    if (FileSteward.HasDigit(ele) ||
                            stopSet.contains(ele) || // here we drop color
                            ele.length() > 20) {
                        continue;
                    }
                    index = getTermIndex(ele, dictMap);
                    addToMap(index, wareTermMap);
                    msgEnough = true;
                }
            }
            // describe
            String describe = ware.getDescribe();
            if (describe != null &&
                    !describe.trim().isEmpty()) {
                describe = describe.toLowerCase().trim().replaceAll(ware.getBrandName() + " ", " ");;
                // focus on [Contents:]
                boolean aimAt = false;
                for (String ele : describe.split("\001")) {
                    if (ele.endsWith("Contents:")) { // we focus on next line
                        aimAt = true;
                        continue;
                    } else if (ele.contains("Contents:")) { // we focus on current line
                        aimAt = true;
                    }
                    if (aimAt) {
                        String contentKernel = getContentKernel(ele);
                        for (String term : castrate(contentKernel).split("\\s+")) {
                            if (stopSet.contains(term) ||
                                    FileSteward.HasDigit(term) ||
                                    term.length() > 20) {
                                continue;
                            }
                            index = getTermIndex(term, dictMap);
                            if (type == 0) {
                                addToMap(index, wareTermMap);
                            }
                        }
                        break;
                    }
                }
                // focus on first describe sentence
                if (!msgEnough) {
                    int sentenceLen = 0;
                    int curLen;
                    String targSentence = "";
                    for (String ele : describe.split("\001")) {
                        if ((curLen = castrate(ele).split("\\s+").length) > sentenceLen) {
                            sentenceLen = curLen;
                            targSentence = ele.trim();
                            if (curLen >= 20) { // if length is enough, we judge it the first sentence
                                break;
                            }
                        }
                    }
                    if (!targSentence.isEmpty()) {
                        String sentenceKernel = getsentenceKernel(targSentence, ware);
                        if (!sentenceKernel.isEmpty()) {
                            for (String term : castrate(sentenceKernel).split("\\s+")) {
                                if (stopSet.contains(term) ||
                                        FileSteward.HasDigit(term) ||
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
                }
            }


            /* // original logic
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
            */

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
        return input.replaceAll("amp;"," ").replaceAll("[^0-9a-zA-Z]"," ").trim();
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

    public static String getContentKernel(String content) {
        if (content.startsWith("Contents:")) {
            content = content.split(":", 2)[1].trim(); // case -- Contents: Milk powder container 1 piece
        }
        if (content.startsWith(",")) {
            content = content.split(":")[0].trim(); // case -- Contents: 1 bottle Wide Neck Milk with Dot Size 2-M, 1 Bottle Bottle with Soft Silicone Spout
        }
        // TODO more ..
        System.out.println("content:\t" + content);
        return content;
    }

    public static String getsentenceKernel(String sentence, WareMsgTranslate ware) {
        StringBuffer result = new StringBuffer();
        sentence = sentence.replace(ware.getBrandName(), " ");
        String[] sentenceArray = sentence.split("\\.");
        // first sentence
        if (sentenceArray[0].contains(" is a")) { // case -- Silvercross ... Britannia is a baby stroller made from quality materials so durable and durable.
            String str = sentenceArray[0].split(" is ", 2)[1];
            str = stopByPrep(str);
            result.append(str).append(" ");
        } else if (sentenceArray[0].startsWith("a ") ||
                sentenceArray[0].contains("an ")) {
            result.append(stopByPrep(sentenceArray[0])).append(" ");
        } else { // do not get a "is"
            int titleTermLen = ware.getTitle().split("\\s+").length;
            String[] firstsentenceArray = sentenceArray[0].split("\\s+");
            if (titleTermLen >= firstsentenceArray.length) { // sentence short than title, we guess it is useless
                return "";
            }
            for (int i = titleTermLen; i < firstsentenceArray.length; i++) { // TODO we'd better limit times of this loop
                if (!prepSet.contains(firstsentenceArray[i])) {
                    result.append(firstsentenceArray[i]).append(" ");
                } else {
                    break;
                }
            }
        }
        System.out.println("sentence 1st:\t" + result.toString().trim());
        // secend sentence
        if (sentenceArray.length > 1 &&
                sentenceArray[1].contains(" is ") &&
                sentenceArray[1].indexOf(" is ") <= 20) {
            result.append(sentenceArray[1].substring(0, sentenceArray[1].indexOf(" is ")));
            System.out.println("sentence 2nd:\t" + sentenceArray[1].substring(0, sentenceArray[1].indexOf(" is ")).trim());
        } else if (sentenceArray.length > 1 &&
                sentenceArray[1].contains(" are ") &&
                sentenceArray[1].indexOf(" are ") <= 20) {
            result.append(sentenceArray[1].substring(0, sentenceArray[1].indexOf(" are ")));
            System.out.println("sentence 2nd:\t" + sentenceArray[1].substring(0, sentenceArray[1].indexOf(" are ")).trim());
        }


        return result.toString().trim();
    }

    public static String stopByPrep(String sentence) {
        StringBuffer result = new StringBuffer();
        String[] arrays = sentence.split("\\s+");
        for (String ele :arrays) {
            if (!prepSet.contains(ele)) {
                result.append(ele).append(" ");
            } else {
                return result.toString().trim();
            }
        }
        return result.toString().trim();
    }
}
