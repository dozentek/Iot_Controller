import cn.rh.iot.core.Device;
import cn.rh.iot.core.DeviceManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Program: Iot_Controller
 * @Description: 测试Mqtt通信
 * @Author: Y.Y
 * @Create: 2020-09-27 10:00
 **/
@Slf4j
public class testMqttChannel {

    @Test
    /*
     * @Description: 利用独立代码测试Mqtt订阅与发布
     * @Param: []
     * @Return: void
     * @Author: Y.Y
     * @Date: 2020/9/27 10:47
     */
    public void testMqttConn() {
        MqttClient client;
        MqttConnectOptions options;
        MqttClientPersistence persistence;

        persistence = new MemoryPersistence();
        options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName("");
        options.setPassword("".toCharArray());
        options.setConnectionTimeout(25);
        options.setKeepAliveInterval(5);

        try {
            client = new MqttClient("tcp://dozen.tech:1883", "BMS_1" , persistence);
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.info("====连接丢失====");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    log.info("Topic:"+topic+"|"+new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    log.info("====连接成功====");
                }
            });
            client.connect(options);

            client.subscribe("BMS/Ctrl", 0);
            log.info("订阅主题");
            client.publish("BMS/Info", "hello".getBytes(), 0, false);

        } catch (MqttException ex) {
            log.info("Mqtt错误,错误码:"+ex.getReasonCode());
        }
    }


}