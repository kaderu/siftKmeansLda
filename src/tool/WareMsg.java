package tool;

import java.util.ArrayList;
import java.util.List;

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

    public long getWareId() {
        return wareId;
    }

    public void setWareId(long wareId) {
        this.wareId = wareId;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        String[] titleOriCells = title.toLowerCase().replaceAll("[\\(\\)\\-&/\\\\]", " ")
                .replaceAll("[0-9]+", " ").split("\\s+");
        List<String> celllist = new ArrayList<>();
        for (String cell : titleOriCells) {
            if (cell.length() >= 3) {
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

    public static void main(String[] args) {
        String qwe = "MY BABY Hair & Body Wash-zszszszsz200ml(Free My Baby Wash\\lap)wqwr/qwzz";
        String[] titleOriCells = qwe.toLowerCase().replaceAll("[()\\-&/\\\\]", " ")
                .replaceAll("[0-9]+", " ").split("\\s+");
        for (String ele : titleOriCells) {
            System.out.println(ele);
        }
    }
}
