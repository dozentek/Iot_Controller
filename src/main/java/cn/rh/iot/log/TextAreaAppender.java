package cn.rh.iot.log;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.Setter;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Program: Iot_Controller
 * @Description: 将日志重定向到UI的TextArea中
 * @Author: Y.Y
 * @Create: 2020-10-02 21:52
 **/
@Plugin(name = "TextArea", category = "Core", elementType = "appender", printObject = true)
public class TextAreaAppender extends AbstractAppender {

    /**
     * @fields serialVersionUID
     */
    private static final long serialVersionUID = -830237775522429777L;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

    @Setter
    private TextArea txtLogViewer;

    //需要实现的构造方法，直接使用父类就行
    protected TextAreaAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
                               final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }


    @Override
    public void append(LogEvent event) {
        readLock.lock();
        try {
            //下面这个是要实现的自定义逻辑
            if(txtLogViewer!=null){
                Platform.runLater(() -> txtLogViewer.appendText(getLayout().toSerializable(event)+"\n"));
            }
        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        } finally {
            readLock.unlock();
        }
    }

    // 下面这个方法可以接收配置文件中的参数信息
    @PluginFactory
    public static TextAreaAppender createAppender(@PluginAttribute("name") String name,
                                                  @PluginElement("Filter") final Filter filter,
                                                  @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                  @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        if (name == null) {
            LOGGER.error("No name provided for MyCustomAppenderImpl");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new TextAreaAppender(name, filter, layout, ignoreExceptions);
    }

}
