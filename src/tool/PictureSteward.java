package tool;

import actor.DocLdaActor;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class PictureSteward {

    public final static String picture_prefix_path = "http://img20.jd.id/Indonesia/s172x172_///img20.jd.id/Indonesia/";

    public static void main(String[] args) {
//        long categoryId = 75061382;
        long categoryId = 75061316;
        DocLdaActor.initalPath(categoryId);
        List<WareMsg> wareMsgList = FileSteward.getWareMsgList(DocLdaActor.wkbt_file);
        System.out.println("Begin picture download, wareMsgList size is " + wareMsgList.size());
        String url = "";
        String path = "";
        int i = 0;
        for (WareMsg wareMsg : wareMsgList) {
            url = picture_prefix_path + wareMsg.getImgUri();
            path = DocLdaActor.prefix_path + "pic_" + categoryId + "\\" + wareMsg.getWareId() + ".jpg";
                    downloadPicture(url, path);
            System.out.println("picture " + i++ + " download to local finished.");
        }
    }
    //链接url下载图片
    private static void downloadPicture(String onlineUrl, String localPath) {
        URL url = null;

        try {
            url = new URL(onlineUrl);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());

            String imageName = localPath; // like "F:/test.jpg"

            FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            byte[] context=output.toByteArray();
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void picturesRename(String path, Map<Long, Integer> map) {
        File file = new File(path);
        File [] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File oriFile = files[i];
            String oriFileName = oriFile.getName();   //根据后缀判断
            if (oriFileName.endsWith(".jpg")) {
                String newFileName = "";
                String[] elements = oriFile.getName().split("\\_|\\.");
                if (elements.length == 3) {
                    newFileName = map.get(Long.parseLong(elements[1])) + "_" + elements[1] + "." + elements[2];
                } else if (elements.length == 2) {
                    newFileName = map.get(Long.parseLong(elements[0])) + "_" + elements[0] + "." + elements[1];
                } else {
                    continue;
                }
                File newFile = new File(path + "\\" + newFileName);
                oriFile.renameTo(newFile);
            }
        }
    }

}
