package org.shipstone.sandbox.docgen.gen;

import org.shipstone.sandbox.docgen.DocgenApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author François Robert
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

  @Bean
  public Docket restApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(getApiInformation())
        .select()
        .apis(RequestHandlerSelectors.basePackage(DocgenApplication.class.getPackage().getName()))
        .paths(PathSelectors.any())
        .build();
  }

  private ApiInfo getApiInformation() {
    return new ApiInfoBuilder()
        .title("Demo gen Doc")
        .description("Demonstration de génération de doc")
        .build();
  }
}
