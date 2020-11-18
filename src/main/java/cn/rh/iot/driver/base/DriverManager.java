package cn.rh.iot.driver.base;

import lombok.extern.slf4j.Slf4j;


/**
 * @Program: IOT_Controller
 * @Description: 加载并管理驱动程序类对象。根据构造函数传入的驱动程序类文件存储的目录地址，加载目录内全部的驱动程序类对象
 * @Author: Y.Y
 * @Create: 2020-09-18 12:22
 **/
@Slf4j
public class DriverManager {

    private final static DriverManager _instance=new DriverManager();

    public static DriverManager getInstance(){
        return _instance;
    }

    public IDriver getNewDriverObject(String classname) {

        try {
            Class<?> c = Class.forName(classname);
            if(c==null){
                return null;
            }else{
                try {
                    return (IDriver) c.newInstance();
                }catch ( InstantiationException| IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }catch (ClassNotFoundException ex){
            return null;
        }
    }

    public boolean IsExist(String className){
        try {
            boolean isPresent = (null != Class.forName(className));
            return isPresent;

        }catch (ClassNotFoundException ex){
            return false;
        }


    }
}
