package cn.rh.iot;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.DeviceManager;
import cn.rh.iot.driver.DriverManager;
import cn.rh.iot.log.TextAreaAppender;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import java.util.ArrayList;

@Slf4j
public class MainWindow extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            //添加shutdown hook处理函数
            Runtime.getRuntime().addShutdownHook(new Thread(this::closeHandler));

            //加载主窗口FXML配置
            Parent root =FXMLLoader.load(getClass().getResource("/MainWindow.fxml"));
            //显示主窗口
            {
                primaryStage.setTitle("设备监控窗口");
                primaryStage.setScene(new Scene(root, 800, 600));
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/mainIcon.png")));
                primaryStage.show();

                primaryStage.setOnCloseRequest(event -> {
                    closeHandler();
                    System.exit(0);
                });
            }

            initial(root);

        }catch (Exception ex){
            log.error("软件启动失败,原因:{}",ex.getMessage());
        }
    }

    private void closeHandler(){
        ArrayList<String> deviceKeyList = DeviceManager.getInstance().getKeyList();
        for (String s : deviceKeyList) {
            DeviceManager.getInstance().getDevice(s).Stop();
        }
        DeviceManager.getInstance().getGroup().shutdownGracefully();
        log.info("-----系统关闭-----");
    }

    private void initial(Parent root){
        new Thread(() -> {
            //初始化重定向Appender(log4j的）,以便能够在主窗口监控日志信息
            {
                Configuration logConfiguration = ((LoggerContext) LogManager.getContext(false)).getConfiguration();
                Appender appender = logConfiguration.getAppender("TextArea");

                if (appender == null) {
                    log.info("日志配置文件错误，缺少名为TextArea的Appender");
                    return;
                }
                TextAreaAppender tA=(TextAreaAppender)appender;
                TextArea logViewer=(TextArea)root.lookup("#txtLogViewer");
                tA.setTxtLogViewer(logViewer);
            }

            log.info("-----系统启动-----");

            //加载配置文件，启动各个设备
            {
                String filePath=getClass().getResource("/Config.xml").getFile();
                IotConfig.getInstance().load(filePath);
                DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
                DeviceManager.getInstance().load(IotConfig.getInstance());
                ArrayList<String> deviceKeyList = DeviceManager.getInstance().getKeyList();

                for (String s : deviceKeyList) {
                    DeviceManager.getInstance().getDevice(s).Start();
                }
            }

        }).start();
    }
}
