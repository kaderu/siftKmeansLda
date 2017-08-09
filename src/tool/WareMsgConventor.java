package tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangshangzhi on 2017/8/8.
 */
public class WareMsgConventor {

    private WareMsg wareMsg;

    private String[] keywords;

    private String[] titleCells;


    public WareMsgConventor(WareMsg wareMsg) {
        this.wareMsg = wareMsg;
        keywords = keywordsDealer(wareMsg.getKeywords());
        titleCells = titleCellsDealer(wareMsg.getTitle());
    }

    public long getWareId() {
        return wareMsg.getWareId();
    }

    public long getCateId() {
        return wareMsg.getCateId();
    }

    public String getBrandName() {
        return wareMsg.getBrandName();
    }

    public String getImgUri() {
        return wareMsg.getImgUri();
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String[] getTitleCells() {
        return titleCells;
    }

    private String[] keywordsDealer(String keywords) {
        String[] eles = keywords.split(",");
        List<String> list = new ArrayList<>();
        String[] keywordTerms;
        List<String> termList;
        for (String ele : eles) {
            if (wareMsg.getBrandName() != null &&
                    wareMsg.getBrandName().equals(ele.trim())) {
                continue;
            }
            keywordTerms = ele.replaceAll("[\\'\\|\\[\\]\\(\\)\\+\\-\\&\\/\\\\]", " ").split("\\s+");
            termList = new ArrayList<>();
            for (String term : keywordTerms) {
                if (term.length() >= 2 &&
                        !HasDigit(term)) {
                    termList.add(term);
                }
            }
            if (termList.size() <= 4) {
                StringBuffer fixKeywordBuffer = new StringBuffer();
                for (String term : termList) {
                    fixKeywordBuffer.append(term).append(" ");
                }
                if (!"".equals(fixKeywordBuffer.toString().trim())) {
                    list.add(fixKeywordBuffer.toString().trim());
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private String[] titleCellsDealer(String title) {
        // brandName drop
        if (wareMsg.getBrandName() != null) {
            if (title.toLowerCase().indexOf(wareMsg.getBrandName() + " ") != -1) { // fit full spell
                title = title.replaceAll(wareMsg.getBrandName() + " ", " ");
            } else if (wareMsg.getBrandName().indexOf(" ") != -1) { // split and check
                String[] upperBrandNameEles = wareMsg.getBrandName().toUpperCase().split(" ");
                for (String ele : upperBrandNameEles) {
                    if (title.indexOf(ele + " ") != -1) {
                        title = title.replaceAll(ele + " ", " ");
                    }
                }
            }
        }
        // keyword drop
//        if (keywords != null &&
//                keywords.length != 0) {
//            for (String keyword : keywords) {
//                if (title.indexOf(keyword) != -1) {
//                    title.replace(keyword, " ");
//                }
//            }
//        }

        // pair drop
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
            String curUnit = "";
            for (String unit : FileSteward.unitMap.keySet()) {
                if (title.contains("[0-9]+[\\s]+" + unit)) {
                    curUnit = unit;
                    break;
                }
            }
            title = title.substring(0, tailStart) + "\\s" + curUnit;
        }

        // TODO check
        // color drop
        title = title + " ";
        Set<String> colorSet = ColorDictProvider.getInstance().getColorSet();
        for (String color : colorSet) {
            if (title.indexOf(" " + color + " ") != -1) {
                title = title.replace(" " + color + " ", " ");
            } else if (title.indexOf("-" + color + " ") != -1) {
                title = title.replace("-" + color + " ", " ");
            }
        }

        String[] titleOriCells = title.replaceAll("[\\|\\[\\]\\(\\)\\*\\+\\-&/\\\\]", " ")
                .replaceAll("[0-9]+", " ").split("\\s+");
        List<String> celllist = new ArrayList<>();
        for (String cell : titleOriCells) {
            if (FileSteward.unitMap.containsKey(cell)) {
                celllist.add(FileSteward.unitMap.get(cell));
            } else if (cell.length() >= 3) {
                celllist.add(cell);
            }
        }
        return celllist.toArray(new String[celllist.size()]);
    }

    public boolean HasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    public static void main(String[] args) {
        String qwe = "MY BABY Hair & Body Wash-zszszszsz200ml(Free My Baby Wash\\lap)wqwr/qwzz";
        String[] titleOriCells = qwe.toLowerCase().replaceAll("[()\\-&/\\\\]", " ")
                .replaceAll("[0-9]+", " ").split("\\s+");
        for (String ele : titleOriCells) {
            System.out.println(ele);
        }
    }
}
