package workflow.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workflow.log.logs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    public static boolean writeStrToFile(String s,String filename)
    {
        //将流程定义XML保存为文件
        File file = new File(filename);
        try(FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            //convert string to byte array
            byte[] bytes = s.getBytes();
            //write byte array to file
            bos.write(bytes);
            bos.close();
            fos.close();

            return true;
        } catch (IOException e) {

            log.error(e.toString());
            return false;
        }
    }
}
