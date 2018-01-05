package com.epam.brest.flux.app;

import com.epam.brest.flux.config.ReactiveConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import reactor.ipc.netty.NettyContext;

public class ReactiveApplication {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ReactiveConfig.class);
        context.refresh();

        NettyContext nettyContext = context.getBean(NettyContext.class);
        nettyContext.onClose().block();
    }
}
