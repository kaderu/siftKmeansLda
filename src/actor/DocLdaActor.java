package actor;

import org.knowceans.lda.LdaEstimate;
import tool.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class DocLdaActor {

    public static final String prefix_path = "C:\\Users\\zhangshangzhi\\Desktop\\pic\\";
    public static final String wkbt_file_name = "wkbt.txt";
    public static final String wkbt_dict_file_name = "word_index.txt";
    public static final String lda_input_file_name = "wkbtLda.dat";
    public static final String lda_model_path_name = "wkbtLda.model";
    public static final String ori_vs_cur_file_name = "oriVsCur.txt";

    public static final int clusterNum = 300;

    public static String wkbt_file;
    public static String wkbt_dict_file;
    public static String lda_input_file;
    public static String da_model_path;
    public static String ori_vs_cur_file;


    public static void main(String[] args) {

//        actor();
//        watchActor();
          lateWorkActor();

//
//        long categoryId = 75061316;
//        initalPath(categoryId);
//
//        Map<Long, Number> map = FileSteward.mergTopic2WareId(da_model_path, wkbt_file);
//        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);

//        Map<Long, Number> map = FileSteward.mergTopic2WareId(da_model_path, wkbt_file);
//        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);

//        lateWorkActor();
    }

    public static void actor() {
        // step.1 determine a category_id, let's say, 75061382
//        long categoryId = 75061382;
        long categoryId = 75061316;
        initalPath(categoryId);
        System.out.println("**************************");
        System.out.println("step.1 initalPath finish...");

        // step.2 get wareId_keyword_brandName_title from mysql online, store as wkbt.txt in PATH C:\Users\zhangshangzhi\Desktop\pic\pic_75061382
        // help yourself do this.

        // step.3 gain index file named wkbtLda.dat as input of LDA model
        IndexSteward.index(wkbt_file, wkbt_dict_file, lda_input_file);
        System.out.println("**************************");
        System.out.println("step.3 gain index file finish...");

        // step.4 work LDA and get lda.model
        FileSteward.delete(da_model_path);
        String[] args = new String[]{"est", "0.5", String.valueOf(clusterNum), "settings.txt",
                lda_input_file, "seeded", da_model_path};
        LdaEstimate.main(args);
        System.out.println("**************************");
        System.out.println("step.4 LDA work finish...");

        // step.5 if we have prepared picture locally, then this method will help group them.
        Map<Long, Number> map = FileSteward.mergTopic2WareId(da_model_path, wkbt_file);
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);
        System.out.println("**************************");
        System.out.println("step.5 picture group finish...");

        // step.6 aim to compare status of clustering, put orignal and current status to an file, namely, oriVsCur.csv
        FileSteward.mergTopic2LeafCateId(wkbt_file, ori_vs_cur_file, map);
        System.out.println("**************************");
        System.out.println("step.6 compare status finish, see in oriVsCur.csv ...");
    }

    // this actor use to rename picture with leaf_category_id
    public static void watchActor() {
        long categoryId = 75061316;
        initalPath(categoryId);
        Map<Long, Number> map = FileSteward.mergLeafCate2WareId(wkbt_file);
        PictureSteward.picturesRename(prefix_path + "pic_" + categoryId, map);
    }

    public static void initalPath(long categoryId) {
        wkbt_file = prefix_path + "pic_" + categoryId + "\\" + wkbt_file_name;
        wkbt_dict_file = prefix_path + "pic_" + categoryId + "\\" + wkbt_dict_file_name;
        lda_input_file = prefix_path + "pic_" + categoryId + "\\" + lda_input_file_name;
        da_model_path = prefix_path + "pic_" + categoryId + "\\" + lda_model_path_name;
        ori_vs_cur_file = prefix_path + "pic_" + categoryId + "\\" + ori_vs_cur_file_name;
    }

    // get dealed title wanghui need
    public static void wareTitleFile() {
        FileSteward.dealWareTitleFile(wkbt_file, wkbt_file.replace("wkbt.txt", "wkbt_ex.txt"));
    }

    // topN term in current leaf_cate and topic
    public static void lateWorkActor() {
        long categoryId = 75061316;
        initalPath(categoryId);

        LateWork lateWork = new LateWork(ori_vs_cur_file);

        List<long[]> list = new ArrayList<long[]>() {{
            add(new long[]{75061340, 20});
            add(new long[]{75061340, 68});
//            add(new long[]{75061333, 16});
//            add(new long[]{75061333, 17});
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

//        /*
        for (long[] array : list) {
            System.out.println("\n### cateId is: " + array[0] + " , topicId is: " + array[1]);
            lateWork.getPopularTerms(array[0], array[1]);
        }
//        */

        List<Long> wareIdList = new ArrayList<Long>() {{
            add(50004531L);
            add(699399L);
            add(694780L);
            add(694787L);
            add(688456L);
        }};

        /*
        for (long wareId : wareIdList) {
            System.out.println("\n### wareId is: " + wareId);
            lateWork.getWareIndexComment(wareId);
        }
        */
    }

    // put piece together
    public static void putTogether() {
        long categoryId = 75061316;
        initalPath(categoryId);
        MergeSteward mergeSteward = new MergeSteward(ori_vs_cur_file);
        mergeSteward.maidCaptain();
    }
}
