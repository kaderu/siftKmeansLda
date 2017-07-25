package actor;

import org.knowceans.lda.LdaEstimate;
import tool.IndexSteward;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class DocLdaActor {

    public static final String prefix_path = "C:\\Users\\zhangshangzhi\\Desktop\\pic\\";
    public static final String wkbt_file_name = "wkbt.txt";
    public static final String wkbt_dict_file_name = "word_index.txt";
    public static final String lda_input_file_name = "wkbtLda.dat";
    public static final String lda_model_path_name = "wkbtLda.model";

    public static final int clusterNum = 10;

    public static String wkbt_file;
    public static String wkbt_dict_file;
    public static String lda_input_file;
    public static String da_model_path;


    public static void main(String[] args) {
        // step.1 determine a category_id, let's say, 75061382
        long categoryId = 75061382;

        // step.2 get wareId_keyword_brandName_title from mysql online, store as wkbt.txt in PATH C:\Users\zhangshangzhi\Desktop\pic\pic_75061382
        // help yourself do this.

        // step.3 gain index file named wkbtLda.dat which is input of LDA model
        IndexSteward.index(wkbt_file, wkbt_dict_file, lda_input_file);

        // step.4 work LDA and get lda.model
        args = new String[]{"est", "0.5", String.valueOf(clusterNum), "settings.txt",
                lda_input_file, "seeded", da_model_path};
        LdaEstimate.main(args);
    }

    private static void initalPath(long categoryId) {
        wkbt_file = prefix_path + "pic_" + categoryId + "\\" + wkbt_file_name;
        wkbt_dict_file = prefix_path + "pic_" + categoryId + "\\" + wkbt_dict_file_name;
        lda_input_file = prefix_path + "pic_" + categoryId + "\\" + lda_input_file_name;
        da_model_path = prefix_path + "pic_" + categoryId + "\\" + lda_model_path_name;
    }
}
