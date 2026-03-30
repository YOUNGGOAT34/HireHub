package com.goat.HireHub.job;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/job")
public class Controller {
     @GetMapping("/hello")
      public String hello(){
          return "Hello World";
      }
}
