package org.hxbweixin.weixin.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hxbweixin.commons.domain.InMessage;
import org.hxbweixin.commons.domain.Image.ImageInMessage;
import org.hxbweixin.commons.domain.event.EventInMessage;
import org.hxbweixin.commons.domain.link.LinkInMessage;
import org.hxbweixin.commons.domain.location.LocationInMessage;
import org.hxbweixin.commons.domain.shortvideo.ShortVideoInMessage;
import org.hxbweixin.commons.domain.text.TextInMessage;
import org.hxbweixin.commons.domain.video.VideoInMessage;
import org.hxbweixin.commons.domain.voice.VoiceInMessage;

public class MessageTypeMapper {

	private static Map<String,Class<? extends InMessage>> typeMap=new ConcurrentHashMap<>();
	
	static {
		typeMap.put("text", TextInMessage.class);
		typeMap.put("image", ImageInMessage.class);
		typeMap.put("voice", VoiceInMessage.class);
		typeMap.put("video", VideoInMessage.class);
		typeMap.put("location", LocationInMessage.class);
		typeMap.put("shortvideo", ShortVideoInMessage.class);
		typeMap.put("link", LinkInMessage.class);
		
		typeMap.put("event", EventInMessage.class);
		//typeMap.put("event", EventInMessage.class);
	}
	@SuppressWarnings("unchecked")
	public static <T extends InMessage> Class<T> getClass(String type){
		return(Class<T>) typeMap.get(type);
	}
}
