package org.shipstone.sandbox.doc.app.generator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shipstone.sandbox.doc.app.DocumentationConfiguration;
import org.shipstone.sandbox.doc.app.SwaggerDocumentationAppApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author François Robert
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SwaggerDocumentationAppApplication.class, DocumentationConfiguration.class})
@AutoConfigureMockMvc
public class GenDocByTest {

  @Autowired
  private MockMvc mockMvc;

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
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDir, "swagger.json"), StandardCharsets.UTF_8)){
      writer.write(result.getResponse().getContentAsString());
    }
  }

}
