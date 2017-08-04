package tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class WareMsg {


    private long wareId;

    private String[] keywords;

    private String brandName;

    private String title;

    private String[] titleCells;

    private String imgUri;

    private long cateId;

    public long getWareId() {
        return wareId;
    }

    public void setWareId(long wareId) {
        this.wareId = wareId;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywordStr) {
        keywordStr = keywordStr.toLowerCase();
        String[] eles = keywordStr.split(",");
        List<String> list = new ArrayList<>();
        String[] keywordTerms;
        List<String> termList;
        for (String ele : eles) {
            if (brandName != null && brandName.equals(ele.trim())) {
                continue;
            }
            keywordTerms = ele.replaceAll("[\\'\\|\\[\\]\\(\\)\\+\\-\\&\\/\\\\]", " ").replaceAll("[0-9]+", "").split("\\s+");
            termList = new ArrayList<>();
            for (String term : keywordTerms) {
                if (term.length() >= 3) {
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
        this.keywords = list.toArray(new String[list.size()]);
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName.trim().toLowerCase();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        // brandName drop
        if (brandName != null) {
            if (title.toLowerCase().indexOf(brandName + " ") != -1) { // fit full spell
                title = title.toLowerCase().replaceAll(brandName, " ");
            } else if (brandName.indexOf(" ") != -1) { // split and check
                String[] upperBrandNameEles = brandName.toUpperCase().split(" ");
                for (String ele : upperBrandNameEles) {
                    if (title.indexOf(ele) != -1) {
                        title = title.replaceAll(ele, " ");
                    }
                }
                title = title.toLowerCase();
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
                if (title.toLowerCase().contains("[0-9]+[\\s]+" + unit)) {
                    curUnit = unit;
                    break;
                }
            }
            title = title.substring(0, tailStart) + "\\s" + curUnit;
        }

        // TODO check
        // color drop
        title = title.toLowerCase() + " ";
        Set<String> colorSet = ColorDictProvider.getInstance().getColorSet();
        for (String color : colorSet) {
            if (title.indexOf(" " + color + " ") != -1) {
                title = title.replace(" " + color + " ", " ");
            }
        }

        String[] titleOriCells = title.toLowerCase().replaceAll("[\\|\\[\\]\\(\\)\\*\\+\\-&/\\\\]", " ")
                .replaceAll("[0-9]+", " ").split("\\s+");
        List<String> celllist = new ArrayList<>();
        for (String cell : titleOriCells) {
            if (FileSteward.unitMap.containsKey(cell)) {
                celllist.add(FileSteward.unitMap.get(cell));
            } else if (cell.length() >= 3) {
                celllist.add(cell);
            }
        }
        titleCells = celllist.toArray(new String[celllist.size()]);
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public String[] getTitleCells() {
        return titleCells;
    }

    public long getCateId() {
        return cateId;
    }

    public void setCateId(long cateId) {
        this.cateId = cateId;
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
