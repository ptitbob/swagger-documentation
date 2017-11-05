# Génération de la documantation SWAGGER automatiquement

Le but de la manipulation est de généré automatiquement une documentation basé sur un descriptif SWAGGER (OpenAPI) généré depuis les annotations du code.

## Step 0

Création du module de l'API REST, avec comme "seule" dependance swagger celle qui permet l'utilisation des annotation de documentation de l'API par SWAGGER.

déclaration de la dependance SWAGGER (`springfox-swagger2`)

```xml
  <properties>
    ...
    <!-- Version de la dependance Springfox -->
    <springfox-swagger2.version>2.7.0</springfox-swagger2.version>
    ...
  </properties>

```
```xml
    <!-- SWAGGER -->
    <dependency>
      <!-- dependance permettant l'utilisation des anotation SWAGGER et la génération du descripttif -->
      <groupId>io.springfox</groupId>
      <artifactId>springfox-swagger2</artifactId>
      <version>${springfox-swagger2.version}</version>
    </dependency>
    <!-- SWAGGER -->
```

*Et evidement j'ai utilisé les annotations mise à ma disposition pour documenter mon endpoint :*

```java
...
@Api
public class HelloEndpoint {

  @ApiOperation(
      "Une API polie"
  )
  @GetMapping
  public String sayHello(
...
``` 

## Step 1 - Génération du fichier descriptif SWAGGER

> *Pour cela je vais utiliser la phase de test de mannière un peu particulière*
>
> Toutes les sources concernant cette génération se trouveront dans le repertoire de test `scr/test`.
>
> Et je vais crééer une configuration maven particulière afin de permettre la génération.

Dans un premier temps je créer la configuration spring afin de permettre d'exposer le endpoint swagger (`SwaggerConfiguration`) :
```java
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
```

Cette manupilation a pour effet de rendre disponible le endpoint SWAGGER à cette URL : `/v2/api-docs`.

Maintenant il s'agit d'interroger ce endpoint afin d'enregistrer le descriptif.
Je vais le faire à travers une classe de test (`GenSwaggerFileAndDoc`), et plus particulièrement via cette méthode : 
```java
  @Test
  public void generate() throws Exception {
    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/v2/api-docs")
            .accept(MediaType.APPLICATION_JSON_VALUE)
    )
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    String outputDir = System.getProperty("io.springfox.staticdocs.outputDir");
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDir, "swagger.json"), StandardCharsets.UTF_8)){
      writer.write(result.getResponse().getContentAsString());
    }
  }
```

Cette méthode utilise le framework de test Spring pour exposer un serveur sur un port aléatoire afin d'exposer l'API générée.
Une fois le serveur lancé, nous interrogeons cette URI afin d'y recupérer le descriptif SWAGGER au format JSON et de l'enregistrer dans un endroit spécifique.

Cependant, si vous faites un `mvn test`, vous ne verrez pas le fichier `swagger.json`.
C'est normal car springboot a configurer le plugin `maven-surefire-plugin` pour ne prendre en compte que les classes ayant comme pattern de nom `**/*test` et `**/*tests`.

C'est bien pratique, et cela va me permettre d'isoler ma génération de document dans un profil maven.
Je déclare donc un profil (non actif):
```xml
...
    <profile>
      <!-- Profile propre à la génération de la doc -->
      <id>documentation</id>
      <activation>
        <activeByDefault>false</activeByDefault>
      </activation>
      <properties>
        <!-- Repertoire de sortie pour le fichier swagger généré -->
        <swagger.output.dir>${project.build.directory}/swagger</swagger.output.dir>
        <swagger.output.file>${project.artifactId}-swagger.json</swagger.output.file>
        ...
      </properties>
...
``` 
Puis je déclare dans la section de build du profil ma configuration du plugin `maven-surefire-plugin` :

```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
      <systemPropertyVariables>
        <io.springfox.staticdocs.outputDir>${swagger.output.dir}</io.springfox.staticdocs.outputDir>
        <io.springfox.staticdocs.outputFile>${swagger.output.file}</io.springfox.staticdocs.outputFile>
        <io.springfox.staticdocs.snippetsOutputDir>${swagger.snippetOutput.dir}</io.springfox.staticdocs.snippetsOutputDir>
      </systemPropertyVariables>
      <includes>
        <!-- Inclusion que de la classe de génération de descritif Swagger -->
        <include>**/GenSwaggerFileAndDoc</include>
      </includes>
    </configuration>
  </plugin>
```

Et je le configure :

* Je n'inclus que ma classe de génération de fichier descriptif (pas la peine de jouer tous les tests, je ne veux que la documentation).
* J'y déclare aussi mes repertoire de sortie qui seront injecté comme propriété système, ce qui me permettra de les recupérer via `System.getProperty("io.springfox.staticdocs.outputDir")`.
* Et pour finir, je déclare le nom du fichier de sortie afin qu'il porte le nom de l'artifact (`${project.artifactId}-swagger.json`) et qu'il soit injecté de la même manière que le repertoire.

Et maintenant, je peux tranquillement faire `mvn clean install -P documentation` et regarder dans mon repertoire target la création de mon fichier `swagger.json`.

> *J'en ai aussi profité pour mettre en place un profil pour jouer mes tests d'intégrations* :)
>
> Tests qui peuvent être joué via `mvn clean install -P test-integration`

## Step 2 - Génération de la documentation générale

La documentation du projet est écrite suivant le format asciidoc ([Référence Asciidoc](http://asciidoctor.org/docs/asciidoc-syntax-quick-reference/)),
ce qui me permet de gérer la documentation du projet comme des sources (gestion de version etc...). 

> *dixit* [Wikipédia](https://fr.wikipedia.org/wiki/Asciidoc) : Asciidoc est un langage de balisage léger.
> Le texte du fichier source forme déjà un document lisible. Le programme de conversion asciidoc permet de transformer le document source au format XHTML, DocBook ou HTML.

Maintenant, je faire en sorte, toujours avec le profil documentation, de générer la doc attaché au projet.
Pour cela j'utilise le plugin `asciidoctor-maven-plugin`.

```xml
  <plugin>
    <groupId>org.asciidoctor</groupId>
    <artifactId>asciidoctor-maven-plugin</artifactId>
    <version>1.5.6</version>
    ...
```

Et comme je veux générer la documentation au format HTML & PDF, je dois inclure quelques dépendances : 

```xml
  <dependency>
    <groupId>org.asciidoctor</groupId>
    <artifactId>asciidoctorj-pdf</artifactId>
    <version>1.5.0-alpha.16</version>
  </dependency>
  <dependency>
    <groupId>org.jruby</groupId>
    <artifactId>jruby-complete</artifactId>
    <version>9.1.13.0</version>
  </dependency>
```

S'ensuit quelques lignes de configuration sans grandes difficultés que je vous laisse découvriri en parcourrant le fichier projet.

Ensuite, afin de générer les 2 types de sortie, il faut décrire deux processus d'execution, un pour la sortie HTML et un pour la sortie PDF 
(*chaque execution ne peut prendre en compte qu'un seul format*).

Pour la génération, proceder comme précédement : `mvn clean install -P documentation`.
Le resultat sera disponible sous le repertoire `target/documentation`.

## Step 3 - Génération de la documentation basé sur le fichier descriptif

> Tous mes modifications, comme pour l'étape 1 ne concerne que le profil `documentation`.

Maintenant je vais baser la documentation de l'API REST à partir du fichier swagger généré à l'étape 1.
Pour cela, j'utilise le plugin `swagger2markup-maven-plugin`,
mais comme celui-ci n'existe pas dans le repository maven de base, je déclare ma dépendance vers d'autres repository de plugins :

```xml
  <pluginRepositories>
    <pluginRepository>
      <id>jcenter-snapshots</id>
      <name>jcenter</name>
      <url>http://oss.jfrog.org/artifactory/oss-snapshot-local/</url>
    </pluginRepository>
    <pluginRepository>
      <id>jcenter-releases</id>
      <name>jcenter</name>
      <url>http://jcenter.bintray.com</url>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </pluginRepository>
  </pluginRepositories>
```  

J'en profite pour ajouter deux propriétés :

```xml
<swagger2markup.version>1.2.0</swagger2markup.version>
<swagger.input>${project.build.directory}/swagger/${swagger.output.file}</swagger.input>
```
Qui sont la version de mon plugin et la localisation du fichier descriptif SWAGGER précédement généré.

Ensuite je déclare **avant le plugin de traitement asciidoc** mon plugin et sa configuration en précisant qu'il doit s'executer pendant la phase de test.

Pour la génération, je créer un squelette de fichier asciidoc (`src/main/api-rest/index.doc`) :
```adoc
include::{generated}/overview.adoc[]
include::manual-content-01.adoc[]
include::{generated}/paths.adoc[]
include::{generated}/security.adoc[]
include::{generated}/definitions.adoc[]
include::manual-content-02.adoc[]
```
Ou j'inclus les doc générées via la balise `{generated}` et qui sont au nombre de 4 : 

* `overview.adoc` : le descriptif de l'API basé sur les informations de l'API qui ont été déclaré via la classe de généréation du fichier descriptif
* `paths.adoc` : les endpoints (méthode, path, paramètres, etc.).
* `security.adoc` : les informations lié à la sécurisation de l'API REST.
* `definitions.adoc` : Les définitions des ressources de l'API REST.

J'y inclus aussi 2 fichiers non générés afin d'ajouter des informations à la documentations, cela peut s'averer pratique (licences, contexte fonctionnel, etc.).

Il en me reste plus qu'a configurer la génération de la documentation via les fichiers asciidoc en configuration l'execution de 4 steps du plugin asccidoc, pour chacun des fichiers et des formats de sortie : 

```xml
  <execution>
    <id>technical-html</id>
    ...
    <configuration>
      <sourceDirectory>${asciidoctor.input.directory}/technical</sourceDirectory>
      ...
      <backend>html5</backend>
      <outputDirectory>${asciidoctor.html.output.directory}</outputDirectory>
      <outputFile>${asciidoctor.html.output.directory}/technical-documentation.html</outputFile>
    </configuration>
  </execution>
  <execution>
    <id>api-rest-html</id>
    <configuration>
      <sourceDirectory>${asciidoctor.input.directory}/api-rest</sourceDirectory>
      ...
      <backend>html5</backend>
      <outputDirectory>${asciidoctor.html.output.directory}</outputDirectory>
      <outputFile>${asciidoctor.html.output.directory}/api-documentation.html</outputFile>
    </configuration>
  </execution>
  <execution>
    <id>technical-pdf</id>
    ...
    <configuration>
      <sourceDirectory>${asciidoctor.input.directory}/technical</sourceDirectory>
      ...
      <backend>pdf</backend>
      <outputDirectory>${asciidoctor.pdf.output.directory}</outputDirectory>
      <outputFile>${asciidoctor.pdf.output.directory}/technical-documentation.pdf</outputFile>
    </configuration>
  </execution>
  <execution>
    <id>api-rest-pdf</id>
    ...
    <configuration>
      <sourceDirectory>${asciidoctor.input.directory}/api-rest</sourceDirectory>
      ...
      <backend>pdf</backend>
      <outputDirectory>${asciidoctor.pdf.output.directory}</outputDirectory>
      <outputFile>${asciidoctor.pdf.output.directory}/api-documentation.pdf</outputFile>
    </configuration>
  </execution>
```

Et voilà le tour est joué, vous avez une belle documentation, que vous pourrez deployer à l'envi selon vos besoin et ceux de votre projet !

*Have fun*
