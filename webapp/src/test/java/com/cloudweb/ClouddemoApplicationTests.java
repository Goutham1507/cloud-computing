package com.cloudweb;

import com.cloudweb.controller.UserController;
import com.cloudweb.entity.User;
import com.cloudweb.repository.UserRepositry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest

class ClouddemoApplicationTests {

    @InjectMocks
    UserController userController;

    @Mock
    UserRepositry userRepositry;

    @Mock
    Environment environment;


    @Test
    public void testAddUse()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpHeaders headers = new HttpHeaders();
        String[]profile = {"aws"};
        headers.setBasicAuth("check@gmail.com","Check@1234567");
        User userRep = new User();
        when(userRepositry.findByEmailAddress(any())).thenReturn(userRep);
        userRep.setFirst_Name("check");
        userRep.setLast_Name("check_last");
        userRep.setEmailAddress("check@gmail.com");
        when(environment.getActiveProfiles()).thenReturn(profile);
        String generatedSecuredPasswordHash = BCrypt.hashpw("Check@1234567", BCrypt.gensalt(12));
        userRep.setPassword(generatedSecuredPasswordHash);
        ResponseEntity<String> responseEntity = userController.getUser(headers);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

}
