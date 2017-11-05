package org.shipstone.sandbox.docgen.gen;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shipstone.sandbox.docgen.DocgenApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author François Robert
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {DocgenApplication.class, SwaggerConfiguration.class})
public class GenSwaggerFileAndDoc {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Before
  public void initialization() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .alwaysDo(MockMvcResultHandlers.print())
        .build();
  }

  @Test
  public void generate() throws Exception {
    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/v2/api-docs")
            .accept(MediaType.APPLICATION_JSON_VALUE)
    )
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    String outputDir = System.getProperty("io.springfox.staticdocs.outputDir");
    Files.createDirectories(Paths.get(outputDir));
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDir, System.getProperty("io.springfox.staticdocs.outputFile")), StandardCharsets.UTF_8)){
      writer.write(result.getResponse().getContentAsString());
    }
  }

}
