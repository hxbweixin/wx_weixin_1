package subscribe;

import org.hxbweixin.commons.service.TokenManager;
import org.hxbweixin.weixin.SubscribeApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes= {SubscribeApplication.class})
public class TokenManagerTests {
	
	@Autowired
	private TokenManager tokenManager;
	
	@Test
	private void test() throws InterruptedException{
		Runnable task= ()->{
			String token =tokenManager.getToken(null);
			System.out.println("获取到的令牌："+token);
		};
		
		Thread t1=new Thread(task);
		Thread t2=new Thread(task);
		
		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
		
	}

}
