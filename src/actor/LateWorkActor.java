package actor;

import tool.LateWork;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshangzhi on 2017/8/14.
 */
public class LateWorkActor {


    public static void main(String[] args) {
        DocLdaActor.init();
//        clusterContent();
        wareCellContent();
    }

    // topN term in current leaf_cate and topic
    public static void clusterContent() {

        LateWork lateWork = new LateWork(DocLdaActor.ori_vs_cur_file);
        List<long[]> list = new ArrayList<long[]>() {{
            add(new long[]{75061322, 131});
            add(new long[]{75061322, 135});
            add(new long[]{75061322, 297});
//            add(new long[]{75061319, 240});
//            add(new long[]{75061333, 26});
//            add(new long[]{75061333, 27});
//            add(new long[]{75061333, 28});
//            add(new long[]{75061333, 31});
//            add(new long[]{75061333, 32});
//            add(new long[]{75061333, 34});
//            add(new long[]{75061333, 39});
//            add(new long[]{75061333, 40});
//            add(new long[]{75061333, 42});
//            add(new long[]{75061333, 43});
//            add(new long[]{75061333, 46});
//            add(new long[]{75061333, 47});
//            add(new long[]{75061333, 48});
//            add(new long[]{75061333, 51});
//            add(new long[]{75061333, 54});
//            add(new long[]{75061333, 56});
//            add(new long[]{75061333, 61});
//            add(new long[]{75061333, 65});
//            add(new long[]{75061333, 75});
//            add(new long[]{75061333, 79});
//            add(new long[]{75061333, 80});
//            add(new long[]{75061333, 82});
//            add(new long[]{75061333, 84});
//            add(new long[]{75061333, 88});
//            add(new long[]{75061333, 89});
//            add(new long[]{75061333, 94});
//            add(new long[]{75061333, 96});
//            add(new long[]{75061333, 97});
//            add(new long[]{75061333, 98});
        }};

        for (long[] array : list) {
            System.out.println("\n### cateId is: " + array[0] + " , topicId is: " + array[1]);
            lateWork.getPopularTerms(array[0], array[1]);
        }
    }

    public static void wareCellContent() {
        LateWork lateWork = new LateWork(DocLdaActor.ori_vs_cur_file);

        List<Long> wareIdList = new ArrayList<Long>() {{
            add(765648L);
            add(768905L);
//            add(680099L);
//            add(698451L);
//            add(698457L);
//            add(50005287L);
//            add(688456L);
        }};

        for (long wareId : wareIdList) {
            System.out.println("\n### wareId is: " + wareId);
            lateWork.getWareIndexComment(wareId);
        }
    }
}
