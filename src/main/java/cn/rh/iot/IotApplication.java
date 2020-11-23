package cn.rh.iot;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.DeviceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class IotApplication {


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(IotApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext ctx = app.run(args);

//        //添加退出钩子
//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            @Override
//            public void run() {
//                log.info("-----IOT关闭-----");
//            }
//        }));
//
//        log.info("-----IOT启动-----");

//        IotConfig.getInstance();
//        while(true){
//            try{
//                Thread.sleep(3000);
//            }catch (InterruptedException ex){
//                ex.fillInStackTrace();
//            }
//        }
    }

}
