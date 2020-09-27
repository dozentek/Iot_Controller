package cn.rh.iot.driver;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;

@Slf4j
/**
 * @program: IOT_Controller
 * @description: 加载并管理驱动程序类对象。
 *               根据构造函数传入的驱动程序类文件存储的目录地址，加载目录内全部的驱动程序类对象
 * @author: Y.Y
 * @create: 2020-09-18 12:22
 **/
public class DriverManager {

    private String driverDir;
    private DriverClassLoader classLoader;
    private HashMap<String,Class> driverMap=new HashMap<String,Class>();

    /*
     * @Description: 构造函数
     * @Param: [driverDir 驱动程序所在目录]
     * @Return:
     * @Author: Y.Y
     * @Date: 2020/9/21 10:57
     */
    public DriverManager(String driverDir) throws Exception {

        this.driverDir=driverDir;
        classLoader=new DriverClassLoader();

        File dir=new File(driverDir);

        if(dir.exists()) {
            File[] files=dir.listFiles();
            for (File f:files) {
                if(!f.isDirectory())
                {
                    createDriver(f);
                }
            }
        }
        else {
            log.error("驱动程序文件目录不存在：["+driverDir+"]");
            throw new Exception("驱动程序文件目录不存在：["+driverDir+"]");
        }
    }

    /*
     * @Description: 获取类的新实例
     * @Param: [classname]  类的名称（全称）
     * @Return: cn.rh.iot.driver.IDriver
     * @Author: Y.Y
     * @Date: 2020/9/24 9:03
     */
    public IDriver getNewDriverObject(String classname){
        Class c=driverMap.get(classname);
        if(c==null) {
            return null;
        }
        else
        {
            try {
                IDriver driver = (IDriver) c.newInstance();
                return driver;
            }catch ( InstantiationException| IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /*
     * @description: 判断是否存在于className匹配的类对象
     * @Param: [className 类名称]
     * @Return: boolean
     * @Author: Y.Y
     * @Date: 2020/9/21 10:58
     */
    public boolean IsExist(String className){
        return driverMap.containsKey(className);
    }


    /*
     * @Description: 清空驱动类对象管理器中所有的类对象
     * @Param: []
     * @Return: void
     * @Author: Y.Y
     * @Date: 2020/9/24 10:19
     */
    public void Clear(){
        this.driverMap.clear();
    }

    /*
     * @description: 根据一个驱动程序文件创建对应的驱动程序类对象，并加入到该管理器中
     * @Param: [file  驱动程序文件对象]
     * @Return: void
     * @Author: Y.Y
     * @Date: 2020/9/21 10:59
     */
    private void createDriver(File file){

        try {
            Class c = classLoader.LoadClassFromDisk(file);

            if(c!=null)
            {
                if(!driverMap.containsKey(c.getName()))
                {
                    driverMap.put(c.getName(),c);
                }
            }

        }catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
}
