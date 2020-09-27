package cn.rh.iot;

import cn.rh.iot.config.IotConfig;

/**
 * @Program: Iot_Controller
 * @Description: 主程序
 * @Author: Y.Y
 * @Create: 2020-09-27 14:54
 **/
public class App {
    public static void main(String[] args) {
        String filePath=System.getProperty("user.dir")+"\\out\\production\\resources\\Config.xml";
        boolean res= IotConfig.getInstance().load(filePath);
    }
}
