package com.ranushan.spring;

import com.ranushan.annotation.Batch;
import com.ranushan.annotation.Run;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Batch(name = "testRanu", interval = "2s")
public class TestServiceImpl implements TestService {

    @Run
    @Override
    public void execute() {
        System.out.println("Hello World : " + Instant.now());
    }
}
