package core;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.DeviceManager;
import cn.rh.iot.driver.DriverManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

@Slf4j
/**
 * @Program: Iot_Controller
 * @Description: 测试DeviceManager对象
 * @Author: Y.Y
 * @Create: 2020-09-27 10:53
 **/
public class testDeviceManager {
    @Test
    public void testLoad() {
        String filepath = System.getProperty("user.dir") + "\\out\\production\\resources\\" + "Config.xml";
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        try {
            ArrayList<String> keys = DeviceManager.getInstance().getKeyList();
            for (int i = 0; i < keys.size(); i++) {
                System.out.println("设备名称：" + DeviceManager.getInstance().getDevice(keys.get(i)).getName());
                System.out.println("设备类：" + ((Object) DeviceManager.getInstance().getDevice(keys.get(i)).getDriver()).getClass().getName());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
