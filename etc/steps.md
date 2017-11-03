#Génération de la documantation SWAGGER automatiquement

##Step 0

Création du module de l'API REST, avec comme "seule" dependance swagger celle qui permet l'utilisation des annotation de documentation de l'API par SWAGGER.

Dans notre cas : le module app.

##Step 1

Création qui servira a proprement parler de la génération du fichier descriptif Swagger.

Dans notre cas : le module doc.

pour cela, il faut qu'il invoque une dépendance vers le module applicatif : 

```xml
<dependency>
  <groupId>org.shipstone.sandbox</groupId>
  <artifactId>swagger-documentation-app</artifactId>
  <version>${project.version}</version>
</dependency>
```

La configuration se fera au niveau des tests.

> A Noter : ***Il est important que le package de base soit identique à celui de l'application***

On créer tout d'abord la classe de configuration pour l'exposition `SWAGGER` :

```java
@Configuration
@EnableSwagger2
public class DocumentationConfiguration {
```

puis nous y delcarons la méthode de génération du `Docket` pour la configuration `SWAGGER` : 

```java
@Bean
public Docket restApi() {
  return new Docket(DocumentationType.SWAGGER_2)
    .apiInfo(getApiInformation())
    .select()
    .apis(RequestHandlerSelectors.basePackage(getClass().getPackage().getName()))
    .paths(PathSelectors.any())
    .build();
}
```

La méthode permettant de générer les informations globale de l'API est très simple (pour notre exemple :) ) : 
```java
private ApiInfo getApiInformation() {
  return new ApiInfoBuilder()
    .title("Demo gen Doc")
    .description("Demonstration de génération de doc")
    .build();
}
```

*Maintenant que l'exposition SWAGGER est configuré, il faut maintenant passer à la génération du fichier descriptif*

Mais tout d'abord, un peu de configuration via le fichier projet (fichier `pom.xml`) : 

```xml
<properties>
<!-- Repertoire de sortie pour le fichier swagger généré -->
<swagger.output.dir>${project.build.directory}/swagger</swagger.output.dir>
<!-- repertoire des snippet de code -->
<swagger.snippetOutput.dir>${project.build.directory}/asciidoc/snippets</swagger.snippetOutput.dir>
</properties>
```

J'y defini les repertoires de sorties pour le fichier descriptif `SWAGGER` et les snippets. Repertoires qui seront affecté comme propriétés système par le plugin surfire : 
```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
      <systemPropertyVariables>
        <io.springfox.staticdocs.outputDir>${swagger.output.dir}</io.springfox.staticdocs.outputDir>
        <io.springfox.staticdocs.snippetsOutputDir>${swagger.snippetOutput.dir}</io.springfox.staticdocs.snippetsOutputDir>
      </systemPropertyVariables>
    </configuration>
  </plugin>
```

La génération proprement dite est très simple et s'appui sur le systeme de Mock de SpringMvc : 

```java
@RunWith(SpringRunner.class)
@SpringBootTest
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
```

Un petit `mvn clean test -P documentation` sur le projet racine et dans le repertoire de sortie du module `doc` le fichier swagger est généré (dans notre cas : `doc/target/swagger`).

Et c'est tout pour le premier step.

> J'ai inclus le module de doc dans un profil particulier, car le clean install global plante - je n'arrive pas à comprendre pourquoi car le test global passe sans problème...
> 
> Va comprendre Charles !!!

