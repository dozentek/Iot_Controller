package driver;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.driver.base.DriverManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Program: Iot_Controller
 * @Description: 测试驱动管理类
 * @Author: Y.Y
 * @Create: 2020-09-27 16:03
 **/
public class testDriverManager {
    @Test
    public void testDriverLoader(){
        String filePath=System.getProperty("user.dir")+"\\out\\production\\resources\\Config.xml";
        boolean res=IotConfig.getInstance().load(filePath);
        Assert.assertTrue(res);
        res=DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        if(res) {
            System.out.println(DriverManager.getInstance().Count());
        }
    }
}
