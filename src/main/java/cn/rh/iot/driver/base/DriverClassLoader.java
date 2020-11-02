package cn.rh.iot.driver.base;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @Program: IOT_Controller
 * @Description: 通过文件加载类对象
 * @Author: Y.Y
 * @Create: 2020-09-21 08:42
 **/
public class DriverClassLoader extends ClassLoader{

    /*
     * @Description: 将类对象动态加载进应用程序
     * @Param: [file]
     * @Return: java.lang.Class<?>
     * @Author: Y.Y
     * @Date: 2020/9/24 8:59
     */
    public Class<?> LoadClassFromDisk(File file) throws ClassNotFoundException {
        String className = getClassName(file.getName());
        try {
            FileInputStream is = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            try {
                while ((len = is.read()) != -1) {
                    bos.write(len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] data = bos.toByteArray();
            is.close();
            bos.close();

            return defineClass(className, data, 0, data.length);

        } catch (IOException  e) {
            e.printStackTrace();
        }
        return super.findClass(className);
    }

    /*
     * @Description: 通过文件名称获取类名称（所以要求文件名与类名保持一致）
     * @Param: [filename]
     * @Return: java.lang.String
     * @Author: Y.Y
     * @Date: 2020/9/24 8:56
     */
    private String getClassName(String filename){
        int index=filename.lastIndexOf(".");
        if(index==-1) {
            return filename;
        }else{
            return filename.substring(0,index);
        }
    }
}
