package org.shipstone.sandbox.docgen.web;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

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