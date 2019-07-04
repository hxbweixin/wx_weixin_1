package org.hxbweixin.weixin.unsubscribe;

import java.util.Date;

import javax.transaction.Transactional;

import org.hxbweixin.commons.domain.User;
import org.hxbweixin.commons.domain.event.EventInMessage;
import org.hxbweixin.commons.processors.EventMessageProcessor;
import org.hxbweixin.commons.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("unsubscribeMessageProcessor")
public class UnsubscribeEventMessageProcessor implements EventMessageProcessor {
	

	
	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional
	public void onMessage(EventInMessage msg) {
		if(!msg.getEvent().equals("unsubscribe")) {
			return;
		}
		User user=this.userRepository.findByOpenId(msg.getFormUserName());
		if(user !=null) {
			user.setStatus(User.Status.IS_UNSUBSCRIBE);
			user.setUnsnbTime(new Date());
		}
		
	}

}
