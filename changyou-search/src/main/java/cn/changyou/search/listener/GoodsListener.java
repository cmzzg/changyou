package cn.changyou.search.listener;

import cn.changyou.search.service.SearchService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author xgl
 * @create 2019-12-27 0:03
 */
@Component
public class GoodsListener {
    @Autowired
    private SearchService searchService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "changyou.search.save.queue",durable = "true"),
            exchange = @Exchange(value = "changyou.item.exchange",ignoreDeclarationExceptions = "true"),
            key = {"item.insert","item.update"}
    ))
    public void save(Long id) throws IOException {
        if (id == null){
            return;
        }
        searchService.save(id);
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "changyou.search.delete.queue",durable = "true"),
            exchange = @Exchange(value = "changyou.item.exchange",ignoreDeclarationExceptions = "true"),
            key = {"item.delete"}
    ))
    public void delete(Long id) throws IOException {
        if (id == null){
            return;
        }
        searchService.delete(id);
    }
}
