package org.hxbweixin.commons.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.hxbweixin.commons.domain.ResponseError;
import org.hxbweixin.commons.domain.ResponseMessage;
import org.hxbweixin.commons.domain.ResponseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TokenMangerImpl implements TokenManager{
	
	private static final Logger LOG = LoggerFactory.getLogger(TokenMangerImpl.class);
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	@Qualifier("tokenRedisTemplate")
	private RedisTemplate<String,ResponseToken> tokenRedisTemplate;
	
	@Override
	public String getToken(String account) {
		// TODO Auto-generated method stub
		BoundValueOperations<String,ResponseToken> ops=tokenRedisTemplate.boundValueOps("weixin_access_token");
		ResponseToken token=ops.get();
		LOG.trace("获取令牌，结果： {}", token);
		if(token ==null) {
			//增加事务锁
			for(int i =0;i<10;i++) {
				Boolean locked=tokenRedisTemplate.opsForValue()
						
						.setIfAbsent("weixin_access_token_lock", new ResponseToken());
				LOG.trace("没有令牌，增加事务锁，结果：{}",locked);
				if(locked == true) {
					
					try {
						token=ops.get();
						if(token == null) {
							LOG.trace("再次检查令牌，还是没有，调用远程接口");
							token = getRemoToken(account);
							ops.getAndSet(token);
							ops.expire(token.getExpiresIn() - 60, TimeUnit.SECONDS);
						}else {
							LOG.trace("再次检查零牌，已经有令牌再Redis里面，直接使用");
						}
						break;
					}finally {
						LOG.trace("删除令牌事务锁");
						tokenRedisTemplate.delete("weixin_access_token_lock");
						synchronized(this) {
							this.notifyAll();
						}
					}
				}else {
					synchronized (this) {
						try {
							LOG.trace("其他线程锁定了令牌，无法获得锁，等待...");
							this.wait(1000*60);
						}catch(InterruptedException e) {
							LOG.error("等待获取分布式的事物锁出现问题:"+e.getLocalizedMessage(),e);
							break;
						}
					}
				}
			}
		}if(token != null) {
			return token.getAccessToken();
		}
		return null;
	}
	
	
	public ResponseToken getRemoToken(String account) {
		
		String appid="wx7329e75102933645";
		String appsecret="9d84d90c90229c8d772f7fed4ae761bb";
		
		String url="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
				+ "&appid="+appid
				+ "&secret="+appsecret;
		
		HttpClient hc=HttpClient.newBuilder()
				.version(Version.HTTP_1_1)
				.build();
		HttpRequest request=HttpRequest.newBuilder(URI.create(url))
				.GET()
				.build();
		ResponseMessage rm;
		try {
			
			HttpResponse<String> response=hc.send(request, BodyHandlers.ofString(Charset.forName("UTF-8")));
			String body=response.body();

			LOG.trace("调用远程接口返回的内容 : \n{}", body);
			
			if(body.contains("errcode")) {
				
				rm=objectMapper.readValue(body,ResponseError.class);
				rm.setStatus(2);
			}else {
				
				rm=objectMapper.readValue(body,ResponseToken.class);
				rm.setStatus(1);
			}
//			return rm;
			if(rm.getStatus()==1) {
//				return((ResponseToken)rm).getAccessToken();
				return((ResponseToken) rm);
			}
		
		}catch(Exception e) {
			throw new RuntimeException("无法获取令牌，因为："+e.getLocalizedMessage());
		}
		
		throw new RuntimeException("无法获取令牌，因为：错误代码="
				+((ResponseError)rm).getErrorCode()
				+"错误描述="+((ResponseError)rm).getErrorMessage());
	}

}
