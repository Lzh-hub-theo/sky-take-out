package com.sky.perfomancetest.deductstock;

import com.sky.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/deduct")
public class TestController {
    @Autowired
    private TestService testService;

    @GetMapping("/noadam")
    public Result<Boolean> noadam(){
        return Result.success(testService.noadam());
    }

    @GetMapping("/adam")
    public Result<Boolean> adam(){
        return Result.success(testService.adam());
    }
}
