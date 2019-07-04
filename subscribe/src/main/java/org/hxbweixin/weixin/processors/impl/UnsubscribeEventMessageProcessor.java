package org.hxbweixin.weixin.processors.impl;

import org.hxbweixin.commons.domain.event.EventInMessage;
import org.hxbweixin.commons.processors.EventMessageProcessor;
import org.springframework.stereotype.Service;

@Service("unsubscribeMessageProcessor")
public class UnsubscribeEventMessageProcessor implements EventMessageProcessor {
	
	@Override
	public void onMessage(EventInMessage msg) {
		// TODO Auto-generated method stub
		
	}
}
