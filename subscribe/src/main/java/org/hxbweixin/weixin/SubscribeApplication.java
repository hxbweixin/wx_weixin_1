package org.hxbweixin.weixin;

import java.util.ArrayList;
import java.util.List;

import org.hxbweixin.commons.config.EnventListenerConfig;
import org.hxbweixin.commons.domain.InMessage;
import org.hxbweixin.commons.domain.event.EventInMessage;
import org.hxbweixin.commons.processors.EventMessageProcessor;
import org.hxbweixin.commons.service.JsonRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;


@SpringBootApplication
@ComponentScan("org.hxbweixin")
@EnableJpaRepositories("org.hxbweixin")
@EntityScan("org.hxbweixin")
public class SubscribeApplication implements 
	EnventListenerConfig,
	ApplicationContextAware{
	
	private ApplicationContext ctx;
	
	@Override
	public void setApplicationContext (ApplicationContext applicationContext) throws BeansException{
		ctx=applicationContext;
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(SubscribeApplication.class);
	public void handle(EventInMessage msg) {
		String id=msg.getEvent().toLowerCase()+"MessageProcessor";
		try {
			EventMessageProcessor mp=(EventMessageProcessor) ctx.getBean(id);
			if(mp!=null ) {
			mp.onMessage(msg);
			}else {
				LOG.warn("Bean的ID{}无法调用对应的消息处理器：ID对应的Bean不存在",id,id);
			}
		}catch(Exception e) {
			LOG.warn("Bean的ID{}无法调用对应的消息处理器：{}",id,e.getMessage());
//			 
		}
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(SubscribeApplication.class, args);
//		System.out.println("Spring Boot 应用启动成功");
//		CountDownLatch countDownLatch=new CountDownLatch(1);
//		countDownLatch.await();
	}
}

