package com.ranushan.spring;

import com.ranushan.annotation.Batch;
import com.ranushan.annotation.Run;
import org.springframework.stereotype.Service;

@Service
@Batch(name = "testRanu", interval = "5s", enableStatistics = true)
public class TestServiceImpl implements TestService {

    @Run
    @Override
    public void execute() {
        System.out.println("Hello World");
    }
}
