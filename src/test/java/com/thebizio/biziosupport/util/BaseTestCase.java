package com.thebizio.biziosupport.util;

import com.thebizio.biziosupport.util.testcontainers.BaseTestContainer;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTestCase extends BaseTestContainer {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected DemoEntityGenerator demoEntityGenerator;

    @Autowired
    protected UtilTestService utilTestService;
}
