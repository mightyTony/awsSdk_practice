package com.example.awssdk_practice.s3.controller;

import com.amazonaws.services.mediaconvert.model.CreateJobResult;
import com.amazonaws.services.mediaconvert.model.JobSettings;
import com.example.awssdk_practice.s3.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @GetMapping("/create")
    public ResponseEntity<CreateJobResult> createdDashVideo(){

        CreateJobResult result = awsS3Service.beginJob();

        System.out.println("안되나");
        return ResponseEntity.ok(result);
    }

//    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
//    public ResponseEntity<byte[]> getMpdFIle(){
//
//
//    }
}
