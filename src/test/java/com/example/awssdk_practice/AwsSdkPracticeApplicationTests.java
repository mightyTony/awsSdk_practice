package com.example.awssdk_practice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AwsSdkPracticeApplicationTests {

    @Test
    void contextLoads() {
    }

    // ec2 가 아닌 로컬에서 테스트 하기용
    static {
        System.setProperty("com.amazonaws.sdk.disableEc2Metadata", "true");
    }
}
