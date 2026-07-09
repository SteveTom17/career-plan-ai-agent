package com.itheima.aiagent.controller;

import com.itheima.aiagent.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResponse<String> health(){
        return ApiResponse.success("health");
    }
}
