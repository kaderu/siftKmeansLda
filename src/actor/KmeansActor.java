package actor;

import tool.FileSteward;
import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;

/**
 * Created by zhangshangzhi on 2017/8/8.
 */
public class KmeansActor {

    public static final int clusterNum = 150;

    public static void main(String[] args) {
        Instances ins = null;

        SimpleKMeans KM = null;
        DistanceFunction disFun = null;

        try {
            // 读入样本数据
            String gammaFilePath = FileSteward.getTargGammaFilePath("C:\\Users\\zhangshangzhi\\Desktop\\pic\\pic_75061316\\wkbtLda.model");
            File file = new File(gammaFilePath);
            ArffLoader loader = new ArffLoader();
            loader.setFile(file);
            ins = loader.getDataSet();

            // 初始化聚类器 （加载算法）
            KM = new SimpleKMeans();
            KM.setNumClusters(clusterNum);       //设置聚类要得到的类别数量
            KM.buildClusterer(ins);     //开始进行聚类
            System.out.println(KM.preserveInstancesOrderTipText());
            // 打印聚类结果
            System.out.println(KM.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
