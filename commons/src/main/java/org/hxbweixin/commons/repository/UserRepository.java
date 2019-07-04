package org.hxbweixin.commons.repository;

import org.hxbweixin.commons.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,String>{

	
	
	User findByOpenId(String openId);

}
