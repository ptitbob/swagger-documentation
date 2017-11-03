package org.shipstone.sandbox.doc.app.web;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author François Robert
 */
@RestController
@RequestMapping(path = "hello", produces = {TEXT_PLAIN_VALUE})
@Api
public class HelloEndpoint {

  @GetMapping
  public String sayHello(
      @RequestParam(value = "name", required = false, defaultValue = "stranger") String name
  ) {
    return "Hello " + name;
  }

}
