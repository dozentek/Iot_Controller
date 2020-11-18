package cn.rh.iot;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.DeviceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class IotApplication{

    public static void main(String[] args) {

        //添加退出钩子
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("-----IOT关闭-----");
            }
        }));

        log.info("-----IOT启动-----");

        IotApplication iot=new IotApplication();

        String filePath=iot.getClass().getResource("/Config.xml").getFile();
        boolean isOk= IotConfig.getInstance().load(filePath);

        if(!isOk){
            log.error("配置文件[{}]加载失败.",filePath);
            log.info("-----IOT关闭-----");
            return;
        }

        iot.start();

        while(true){
            try{
                Thread.sleep(3000);
            }catch (InterruptedException ex){
                ex.fillInStackTrace();
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
