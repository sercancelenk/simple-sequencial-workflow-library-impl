package byzas.example.simpleworkflow.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ext0280263 on 18.07.2020
 * @project IntelliJ IDEA
 */
@SpringBootTest
public class RegisterServiceTest {

    @Autowired
    RegisterService registerService;

    @Test
    public void registerMonoBlockingExceptionallyTest() {
        registerService.registerUserMono();
    }

    @Test
    public void registerFutureBlockingExceptionallyTest() {
        registerService.registerUserFuture();
    }

    @Test
    public void registerBlockingTest() {
        registerService.registerUser();
    }

}