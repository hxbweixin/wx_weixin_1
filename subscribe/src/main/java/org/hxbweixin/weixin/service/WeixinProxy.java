package org.hxbweixin.weixin.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import org.hxbweixin.commons.domain.User;
import org.hxbweixin.commons.domain.text.TextOutMessage;
import org.hxbweixin.commons.service.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WeixinProxy {
	
	private static final Logger LOG = LoggerFactory.getLogger(WeixinProxy.class);
	@Autowired
	private TokenManager tokenManager;
	@Autowired
	private ObjectMapper objectMapper;
	

	private HttpClient client=HttpClient.newBuilder()
			.version(Version.HTTP_1_1)
			.build();

	public User getUser(String account,String openId) {
		// TODO Auto-generated method stub
		String token=this.tokenManager.getToken(account);
		String url = "https://api.weixin.qq.com/cgi-bin/user/info"
				+ "?access_token=" + token
				+ "&openid=" + openId
				+ "&lang=zh_CN";
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.GET()
				.build();
		
		try {
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString(Charset.forName("UTF-8")));

			String body = response.body();
			LOG.trace("调用远程接口返回的内容 : \n{}", body);
			if (body.contains("errcode")) {
				LOG.error("调用远程接口出现问题：" + body);
			} else {
				User user = objectMapper.readValue(body, User.class);
				return user;
			}
		} catch (Exception e) {
			LOG.error("调用远程接口出现问题：" + e.getLocalizedMessage(), e);
		}
		return null;
	}

	public void sendText(String account, String openId, String content) {
		TextOutMessage msg=new TextOutMessage(openId, content);
		String token=this.tokenManager.getToken(account);
		try {
			String json=this.objectMapper.writeValueAsString(msg);
			String url="https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + token;
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.POST(BodyPublishers.ofString(json, Charset.forName("UTF-8")))
					.build();
			
			CompletableFuture<HttpResponse<String>> future
			= client.sendAsync(request, BodyHandlers.ofString(Charset.forName("UTF-8")));
			future.thenAccept(response -> {
				String body = response.body();
				LOG.trace("发送客服消息返回的内容 : \n{}", body);
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	
	
}
