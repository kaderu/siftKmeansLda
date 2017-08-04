package tool;

/**
 * Created by zhangshangzhi on 2017/8/4.
 */
public class CellCluster {

    private int id;

    private int size;

    private long leafCateId;

    private int cateSize;

    private int topicId;

    private int topicSize;

    private double catePercent;

    private double topicPercent;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getLeafCateId() {
        return leafCateId;
    }

    public void setLeafCateId(long leafCateId) {
        this.leafCateId = leafCateId;
    }

    public int getCateSize() {
        return cateSize;
    }

    public void setCateSize(int cateSize) {
        this.cateSize = cateSize;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getTopicSize() {
        return topicSize;
    }

    public void setTopicSize(int topicSize) {
        this.topicSize = topicSize;
    }

    public double getCatePercent() {
        return catePercent;
    }

    public void setCatePercent(double catePercent) {
        this.catePercent = catePercent;
    }

    public double getTopicPercent() {
        return topicPercent;
    }

    public void setTopicPercent(double topicPercent) {
        this.topicPercent = topicPercent;
    }
}
