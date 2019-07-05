package org.hxbweixin.commons.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;

import org.hxbweixin.commons.domain.ResponseError;
import org.hxbweixin.commons.domain.ResponseMessage;
import org.hxbweixin.commons.domain.ResponseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	private RedisTemplate<String,ResponseToken> tokenRedisTemplate;
	
	@Override
	public String getToken(String account) {
		// TODO Auto-generated method stub
		BoundValueOperations<String,ResponseToken> ops=tokenRedisTemplate.boundValueOps("weixin_access_token");
		ResponseToken token=ops.get();
		if(token ==null) {
			//增加事务锁
			Boolean locked=tokenRedisTemplate.opsForValue()
					.setIfAbsent("weixin_access_token_lock", new ResponseToken());
		}
		return null;
	}
	
	
	public String getRemoToken(String account) {
		
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
				return((ResponseToken)rm).getAccessToken();
			}
		
		}catch(Exception e) {
			throw new RuntimeException("无法获取令牌，因为："+e.getLocalizedMessage());
		}
		
		throw new RuntimeException("无法获取令牌，因为：错误代码="
				+((ResponseError)rm).getErrorCode()
				+"错误描述="+((ResponseError)rm).getErrorMessage());
	}

}
