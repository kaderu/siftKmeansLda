package tool;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zhangshangzhi on 2017/7/25.
 */
public class PictureSteward {

    public static void main(String[] args) {
        String url = "http://localhost:8080/image/touxiang.png";
        String path = "F:/test.jpg";
        downloadPicture(url, path);
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
}
