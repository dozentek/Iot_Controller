package core;

import cn.rh.iot.core.Device;
import cn.rh.iot.core.DeviceManager;
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
    public void testLoad(){
        DeviceManager manager=new DeviceManager();

//
//        String filepath=System.getProperty("user.dir")+"\\out\\production\\resources\\"+"deviceConfigTest.xml";
//        manager.Load(filepath);
//
//        Device device;
//        ArrayList<String> keys=manager.getKeyList();
//        for(int i=0;i<keys.size();i++){
//            System.out.println("设备名称："+manager.getDevice(keys.get(i)).getName());
//            System.out.println("设备类："+((Object)manager.getDevice(keys.get(i)).getDriver()).getClass().getName());
//        }
    }
}
