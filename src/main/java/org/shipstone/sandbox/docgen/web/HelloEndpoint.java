package org.shipstone.sandbox.docgen.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author François Robert
 */
@RestController
@RequestMapping(value = "hello", produces = {TEXT_PLAIN_VALUE})
@Api
public class HelloEndpoint {

  @ApiOperation(
      "Une API polie"
  )
  @GetMapping
  public String sayHello(
      @RequestParam(name = "name", required = false, defaultValue = "stranger") String name
  ) {
    return "Hello " + name;
  }

}
