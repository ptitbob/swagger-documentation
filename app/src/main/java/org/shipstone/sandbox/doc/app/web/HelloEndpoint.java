package org.shipstone.sandbox.doc.app.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author François Robert
 */
@RestController
@RequestMapping("hello")
public class HelloEndpoint {

  @GetMapping
  public String sayHello(
      @RequestParam(value = "name", required = false, defaultValue = "stranger") String name
  ) {
    return "Hello " + name;
  }

}
