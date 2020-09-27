package config;

import cn.rh.iot.config.IotConfig;
import org.junit.Test;

/**
 * @Program: Iot_Controller
 * @Description: 配置文件解析测试
 * @Author: Y.Y
 * @Create: 2020-09-27 15:45
 **/
public class testConfig {

    @Test
    public void testFunc(){
        String filePath=System.getProperty("user.dir")+"\\out\\production\\resources\\Config.xml";
        boolean res=IotConfig.getInstance().load(filePath);

    }
}
