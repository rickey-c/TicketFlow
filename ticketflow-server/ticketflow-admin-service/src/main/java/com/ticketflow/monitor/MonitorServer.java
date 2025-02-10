package com.ticketflow.monitor;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractStatusChangeNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 服务监控处理
 * @Author: rickey-c
 * @Date: 2025/1/24 14:31
 */
@Slf4j
public class MonitorServer extends AbstractStatusChangeNotifier {

    private DingTalkMessage dingTalkMessage;

    private Expression text = new SpelExpressionParser()
            .parseExpression("#{instance.registration.name} (#{instance.id}) status changed from #{fromStatus} to #{toStatus}  #{instance.registration.healthUrl}", ParserContext.TEMPLATE_EXPRESSION);

    private String[] ignoreStatusChanges = new String[]{"UNKNOWN:UP", "DOWN:UP", "OFFLINE:UP"};

    public MonitorServer(DingTalkMessage dingTalkMessage, InstanceRepository repository) {
        super(repository);
        this.setIgnoreChanges(ignoreStatusChanges);
        this.dingTalkMessage = dingTalkMessage;
    }

    @Override
    protected boolean shouldNotify(InstanceEvent event, Instance instance) {
        if (event instanceof InstanceStatusChangedEvent) {
            InstanceStatusChangedEvent statusChange = (InstanceStatusChangedEvent) event;
            String from = this.getLastStatus(event.getInstance());
            String to = statusChange.getStatusInfo().getStatus();
            return Arrays.binarySearch(this.ignoreStatusChanges, from + ":" + to) < 0 && Arrays.binarySearch(this.ignoreStatusChanges, "*:" + to) < 0 && Arrays.binarySearch(this.ignoreStatusChanges, from + ":*") < 0;
        } else {
            return false;
        }
    }

    @Override
    protected Mono<Void> doNotify(final InstanceEvent event, final Instance instance) {
        InstanceStatusChangedEvent statusChange = (InstanceStatusChangedEvent) event;
        String from = this.getLastStatus(event.getInstance());
        String to = statusChange.getStatusInfo().getStatus();

        Map<String, Object> root = new HashMap<>(16);
        root.put("instance", instance);
        root.put("fromStatus", from);
        root.put("toStatus", to);
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.addPropertyAccessor(new MapAccessor());
        String message = text.getValue(context, String.class);
        return Mono.fromRunnable(() -> {
            log.info("Mono.fromRunnable执行 message:{}", message);
            dingTalkMessage.sendMessage(message);
        });
    }
}
