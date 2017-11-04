package org.shipstone.sandbox.docgen.web;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author François Robert
 */
public class HelloEndpointTest {
  @Test
  public void sayHello() throws Exception {
    assertThat(new HelloEndpoint().sayHello("world"))
        .isNotBlank()
        .isEqualTo("Hello world");
  }

}