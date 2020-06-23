package com.puty;

import com.puty.project.system.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;

@SpringBootTest
public class Test {

    @Autowired
    private ISysDeptService deptService;


    @org.junit.Test
    public void test () throws ParseException {
        depts();
    }

    /**
     *  类别
     */
    public void depts () throws ParseException {

    }
}
