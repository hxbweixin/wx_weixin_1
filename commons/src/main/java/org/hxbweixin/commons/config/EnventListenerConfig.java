package org.hxbweixin.commons.config;

import java.util.ArrayList;
import java.util.List;

import org.hxbweixin.commons.domain.InMessage;
import org.hxbweixin.commons.domain.event.EventInMessage;
import org.hxbweixin.commons.service.JsonRedisSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

public interface EnventListenerConfig extends
	CommandLineRunner,
	DisposableBean{
	
	public final Object stopMonitor=new Object();
	
	
	@Override
	public default void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		new Thread(() -> {
			synchronized(stopMonitor) {
				try {
					stopMonitor.wait();
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	@Override
	public default void destroy() throws Exception {
		// TODO Auto-generated method stub
		synchronized (stopMonitor) {
			stopMonitor.notify();
		}
		
	}
	
	@Bean
	public default RedisTemplate<String,InMessage> inMessageTemplate(
			@Autowired RedisConnectionFactory redisConnectionFactory){
		RedisTemplate<String,InMessage> template=new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
	
//		template.setKeySerializer(new StringRedisSerialzer());
		template.setValueSerializer(new JsonRedisSerializer());
		
		return template;
	}
	
	@Bean
	public default MessageListenerAdapter messageListener(@Autowired RedisTemplate<String,InMessage> inMessageTemplate) {
		MessageListenerAdapter adapter=new MessageListenerAdapter();
		adapter.setSerializer(inMessageTemplate.getValueSerializer());
		
		adapter.setSerializer(inMessageTemplate.getValueSerializer());
		adapter.setDelegate(this);
		
		adapter.setDefaultListenerMethod("handle");
		
		return adapter;
	}
	
	@Bean
	public default RedisMessageListenerContainer messageListenerContainer(
			@Autowired RedisConnectionFactory redisConnectionFactory,
			@Autowired MessageListener l) {
		
		RedisMessageListenerContainer container=new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		
		List<Topic> topics=new ArrayList<>();
		
		topics.add(new ChannelTopic("weixin_1_event"));
		container.addMessageListener(l,topics);
		
		return container;
		
	}
	public void handle(EventInMessage msg);

}
