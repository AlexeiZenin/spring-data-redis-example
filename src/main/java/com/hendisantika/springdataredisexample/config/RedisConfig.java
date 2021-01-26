package com.hendisantika.springdataredisexample.config;

import com.hendisantika.springdataredisexample.queue.MessagePublisher;
import com.hendisantika.springdataredisexample.queue.MessagePublisherImpl;
import com.hendisantika.springdataredisexample.queue.MessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import java.util.Optional;

/**
 * Created by IntelliJ IDEA. Project : spring-data-redis-example User: hendisantika Email:
 * hendisantika@gmail.com Telegram : @hendisantika34 Date: 02/12/17 Time: 19.53 To change this
 * template use File | Settings | File Templates.
 */
@Configuration
public class RedisConfig {
  private static Logger log = LoggerFactory.getLogger(RedisConfig.class);

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      LettuceConnectionFactory redisConnectionFactory) {
    log.info(
        "Redis connection: {}",
        Optional.ofNullable(redisConnectionFactory.getSentinelConfiguration())
            .map(c -> redisConnectionFactory.getSentinelConnection().masters().toString())
            .orElseGet(
                () ->
                    redisConnectionFactory.getHostName() + ":" + redisConnectionFactory.getPort()));

    final RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
    return template;
  }

  @Bean
  MessageListenerAdapter messageListener() {
    return new MessageListenerAdapter(new MessageSubscriber());
  }

  @Bean
  RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
    final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(messageListener(), topic());
    return container;
  }

  @Bean
  MessagePublisher redisPublisher(RedisTemplate redisTemplate) {
    return new MessagePublisherImpl(redisTemplate, topic());
  }

  @Bean
  ChannelTopic topic() {
    return new ChannelTopic("pubsub:queue");
  }
}
