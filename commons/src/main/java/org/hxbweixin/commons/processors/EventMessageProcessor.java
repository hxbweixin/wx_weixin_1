package org.hxbweixin.commons.processors;

import org.hxbweixin.commons.domain.event.EventInMessage;

public interface EventMessageProcessor {
	
	public void onMessage(EventInMessage msg);
		
}
