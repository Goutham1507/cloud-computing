package com.cloudweb.repository;

import com.cloudweb.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepositry extends CrudRepository<User, Long> {

    User findByEmailAddress(String email_address);



}