package cn.rh.iot;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.DeviceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class IotApplication extends Thread{

    public static void main(String[] args) {

        log.info("-----系统启动-----");
        IotApplication app=new IotApplication();
        String filePath=app.getClass().getResource("/Config.xml").getFile();
        boolean isOk= IotConfig.getInstance().load(filePath);

        if(!isOk){
            log.error("配置文件{}加载失败.",filePath);
            log.info("-----系统关闭-----");
            return;
        }

        app.start();

        while(true){
            try{
                Thread.sleep(3000);
            }catch (InterruptedException ex){
                ex.fillInStackTrace();
                log.info("-----系统关闭-----");
            }
        }
    }

    public void start(){
        new Thread(() -> {
            DeviceManager.getInstance().load(IotConfig.getInstance());
            ArrayList<String> deviceKeyList = DeviceManager.getInstance().getKeyList();
            for (String s : deviceKeyList) {
                DeviceManager.getInstance().getDevice(s).Start();
            }
        }).start();
    }
}
