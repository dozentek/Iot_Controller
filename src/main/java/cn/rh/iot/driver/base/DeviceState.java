package cn.rh.iot.driver.base;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceState {

    @Getter @Setter
    protected String info;

    @Getter @Setter
    protected int code;

    public DeviceState(int code,String info){
        this.code=code;
        this.info =info;
    }
}
