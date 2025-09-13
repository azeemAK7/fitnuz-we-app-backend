package com.fitnuz.project.Controllers;

import com.fitnuz.project.Payload.Response.AnalyticsResponse;
import com.fitnuz.project.Service.Definations.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    @Autowired
    AnalyticsService analyticsService;

    @GetMapping("/admin/app/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics(){
        AnalyticsResponse response =  analyticsService.getAnalyticsData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
