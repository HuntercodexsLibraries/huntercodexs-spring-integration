# huntercodexs-spring-integration
Library to help developers make integration easily

![Java](https://img.shields.io/badge/Lang-Java-green)
![Version](https://img.shields.io/badge/Version-21-blue)
![Spring](https://img.shields.io/badge/Framework-SpringBoot-orange)
![Release](https://img.shields.io/badge/Release-1.0.0-red)

> TIP: Use the repository https://github.com/huntercodexs/java-spring-boot-sample-integration to test this library or get 
> some idea in how to use this library.

> NOTE: This is an official documentation for this library

# Resources

![integration-banner.png](files/img/integration-banner.png)

- [Mustache](#mustache)
- [Global Handler Interceptor](#global-handler-interceptor)
- [OpenAPI (Swagger)](#openapi)
- [Codegen (openapi-generator-maven-plugin)](#codegen-openapi-generator-maven-plugin)
- [Feign](#feign)
- [Circuit Breaker](#circuit-breaker)
- [Rate Limit (API)](#rate-limit-for-apis)
- [Rate Limit (Service Bus)](#rate-limit-service-bus)
- [MongoDB](#mongodb)
- [Redis](#redis)
- [Kafka](#kafka)
- [Service Bus](#service-bus)
- [RabbitMQ](#rabbitmq)
- [SQS](#sqs)

# Como Usar

Para Usar essa biblioteca de recursos para integracao siga os seguintes passos

1. Importe a bilbioteca para sua aplicacao

```xml
        <dependency>
            <groupId>com.huntercodexs</groupId>
            <artifactId>huntercodexs-spring-integration</artifactId>
            <version>1.0.0</version>
        </dependency>
```

2. Adicione a anotacao @EnableIntegration

```java
package com.huntercodexs.sample;

import com.huntercodexs.integration.core.annotation.EnableIntegration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableIntegration("com.huntercodexs.sample")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Indique o package para ser escaneado pela biblioteca conforme mostrado no exemplo acima "com.huntercodexs.sample" que  
por sua vez deve estar de acordo com a estrutura de aplicacao, conforme segue exemplo de aplicacao real:

```
.
├── LICENSE
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── huntercodexs
│   │   │           └── sample
│   │   │               ├── api
│   │   │               │   ├── InvalidApiClientSimulation.java
│   │   │               │   └── UserApiClientSimulation.java
│   │   │               ├── Application.java
│   │   │               ├── component
│   │   │               │   ├── exception
│   │   │               │   │   ├── Impl1.java
│   │   │               │   │   ├── Impl2.java
│   │   │               │   │   ├── Impl3.java
│   │   │               │   │   └── Impl4.java
│   │   │               │   └── interceptor
│   │   │               │       ├── OrgInterceptorImpl.java
│   │   │               │       └── UserInterceptorImpl.java
│   │   │               ├── config
│   │   │               │   ├── RateLimitActionMessageImpl.java
│   │   │               │   └── RateLimitActionUserImpl.java
│   │   │               ├── controlller
│   │   │               │   ├── RabbitProducerSimulation.java
│   │   │               │   ├── RateLimitControllerSimulation.java
│   │   │               │   ├── RateLimitServiceBusConsumerSimulation.java
│   │   │               │   ├── ServiceBusProducerSimulation.java
│   │   │               │   ├── SqsProducerSimulation.java
│   │   │               │   ├── UserControllerSimulation.java
│   │   │               │   └── UsersApiControllerSimulation.java
│   │   │               ├── dto
│   │   │               │   ├── ProcessMessageSimulation.java
│   │   │               │   ├── UserRequestSimulation.java
│   │   │               │   └── UserResponseSimulation.java
│   │   │               ├── messaging
│   │   │               │   ├── kafka
│   │   │               │   │   ├── consumer
│   │   │               │   │   │   ├── KafkaConsumer1IntegrationProcessImpl.java
│   │   │               │   │   │   ├── KafkaConsumer2IntegrationProcessImpl.java
│   │   │               │   │   │   └── KafkaConsumer.java
│   │   │               │   │   ├── dto
│   │   │               │   │   │   └── User.java
│   │   │               │   │   └── producer
│   │   │               │   │       ├── component
│   │   │               │   │       │   ├── KafkaProducerMessage1Impl.java
│   │   │               │   │       │   └── KafkaProducerMessage2Impl.java
│   │   │               │   │       ├── controller
│   │   │               │   │       │   └── KafkaController.java
│   │   │               │   │       └── service
│   │   │               │   │           └── KafkaService.java
│   │   │               │   ├── rabbitmq
│   │   │               │   │   ├── consumer
│   │   │               │   │   │   ├── OrderCreatedRetryStrategy.java
│   │   │               │   │   │   ├── OrderCreatedStrategy.java
│   │   │               │   │   │   ├── UserRegisteredRetryStrategy.java
│   │   │               │   │   │   └── UserRegisteredStrategy.java
│   │   │               │   │   ├── dto
│   │   │               │   │   │   ├── OrderEvent.java
│   │   │               │   │   │   └── UserEvent.java
│   │   │               │   │   └── producer
│   │   │               │   │       └── ProducerSample.java
│   │   │               │   ├── servicebus
│   │   │               │   │   ├── consumer
│   │   │               │   │   │   ├── list
│   │   │               │   │   │   │   └── impl
│   │   │               │   │   │   │       ├── ServiceBusProcessorIntegrationList1Impl.java
│   │   │               │   │   │   │       ├── ServiceBusProcessorIntegrationList2Impl.java
│   │   │               │   │   │   │       └── ServiceBusProcessorIntegrationList3Impl.java
│   │   │               │   │   │   └── single
│   │   │               │   │   │       └── impl
│   │   │               │   │   │           └── ServiceBusProcessorIntegrationSingleImpl.java
│   │   │               │   │   └── producer
│   │   │               │   │       └── ServiceBusProducerSample.java
│   │   │               │   └── sqs
│   │   │               │       ├── consumer
│   │   │               │       │   ├── SqsConsumer1Sample.java
│   │   │               │       │   └── SqsConsumer2Sample.java
│   │   │               │       └── producer
│   │   │               │           └── SqsProducerSample.java
│   │   │               └── retry
│   │   │                   └── mongo
│   │   │                       ├── controller
│   │   │                       │   └── UserController.java
│   │   │                       ├── database
│   │   │                       │   ├── entity
│   │   │                       │   │   └── UserEntity.java
│   │   │                       │   └── repository
│   │   │                       │       └── UserRepository.java
│   │   │                       ├── dto
│   │   │                       │   ├── UserRequestDto.java
│   │   │                       │   └── UserResponseDto.java
│   │   │                       └── service
│   │   │                           └── UserService.java
│   │   └── resources
│   │       ├── application-default.properties
│   │       ├── application-deploy.properties
│   │       ├── application.properties
│   │       ├── certs
│   │       │   ├── broker.keystore.jks
│   │       │   ├── broker.truststore.jks
│   │       │   ├── key_password
│   │       │   ├── keystore_password
│   │       │   ├── README
│   │       │   └── truststore_password
│   │       ├── feign
│   │       │   ├── openapi
│   │       │   │   └── USER-DATA-SAMPLE-API.yaml
│   │       │   └── templates
│   │       │       ├── apiClient.mustache
│   │       │       └── clientConfiguration.mustache
│   │       ├── logback.xml
│   │       ├── messages_en.properties
│   │       └── openapi
│   │           ├── templates
│   │           │   ├── api.mustache
│   │           │   └── model.mustache
│   │           └── USER-SAMPLE-API.yaml
```

A partir de agora a biblioteca esta pronta para ser utilizada e consumida em sua aplicacao, tendo disponiveis todos 
os recursos presentes nela.

A seguir iremos descrever item a item dos recursos que compoe essa biblioteca, sendo que para cada um deles voce podera 
encontrar alem de uma explicacao detalhada exemplos de uso e implementacoes diversas.

# Mustache

Mustache é um mecanismo de template leve e sem lógica, utilizado para gerar arquivos de texto a partir de modelos
predefinidos. Ele permite separar a estrutura do documento dos dados, facilitando a criação de classes padronizadas e
pré-formatadas. No contexto de projetos Java e contratos, o Mustache pode ser empregado para automatizar a geração de
código, documentação ou contratos, garantindo consistência, agilidade e controle sobre o processo. Ao definir
templates, é possível criar rapidamente estruturas reutilizáveis, reduzindo erros manuais e acelerando o
desenvolvimento, especialmente em integrações e padronizações de APIs.

Voce pode personalizar os arquivos de configuracao mustache de acordo com suas necessidades, mas para iniciar do zero
basta usar os arquivos disponiveis em `src/main/resources/support/openapi/templates` e/ou `src/main/resources/support/feign/templates`,
ressaltando que os templates em openapi/templates sao usados para geracao de contratos da API com seus consumidores e
os arquivos em feign/templates sao usados para gerar a propria integracao nas aplicacoes consumidores, contendo em sua
estrutura circuit breakers e o proprio feign.

# Global Handler Interceptor

A biblioteca possui um recurso que implementa um interceptor global de exceções para aplicações Spring Boot. Ele
centraliza o tratamento de erros lançados durante o processamento das requisições, capturando exceções específicas
e genéricas, e retornando respostas padronizadas para o cliente. Dessa forma, garante maior controle, padronização
e clareza nas mensagens de erro, além de facilitar a manutenção e o monitoramento do sistema.

A resposta padrao é definida pela classe `CustomResponseExceptionHandler` a qual possui a seguinte estrutura

```java
@Setter
@Getter
public class CustomResponseExceptionHandler {

    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tracker;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> errors;

    public CustomResponseExceptionHandler(String message, String code, String tracker, List<String> errors) {
        this.message = message;
        this.tracker = tracker;

        if (Objects.equals(code, "0") || isNull(code) || code.isEmpty()) {
            this.code = null;
        } else {
            this.code = code;
        }

        this.errors = errors;
        if (isNull(errors) || errors.isEmpty()) {
            this.errors = null;
        }
    }
}
```

Um exemplo de resposta e dado abaixo

```json
{
  "message": "Limit of requests exceeded for Integration",
  "timestamp": "2025-12-26T17:36:09.473261608",
  "code": "503",
  "tracker": "30649506-90e2-4a6c-8c48-846a7c095bb8",
  "errors": [
    "Integration Retries Exceeded: 3"
  ]
}
```

As excessões tratadas pela biblioteca sao listadas a seguir (esses itens fazem parte do enum GlobalEnumIntegration)

| #  | Item                                                         | Codigo HTTP      | Descricao                                        |
|----|--------------------------------------------------------------|------------------|--------------------------------------------------|
| 1  | CUSTOM_EXCEPTION_INTERCEPTOR                                 | 400              | Requisição inválida                              |
| 2  | METHOD_ARGUMENT_VALIDATION_EXCEPTION_INTERCEPTOR_400         | 400              | Argumento de método inválido                     |
| 3  | HTTP_MESSAGE_NOT_READABLE_EXCEPTION_INTERCEPTOR_400          | 400              | Mensagem HTTP não legível                        |
| 4  | MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION_INTERCEPTOR_400  | 400              | Parâmetro de requisição ausente                  |
| 5  | CONSTRAINT_VIOLATION_EXCEPTION_INTERCEPTOR_400               | 422              | Violação de restrição                            |
| 6  | HANDLER_METHOD_VALIDATION_EXCEPTION_INTERCEPTOR_400          | 400              | Validação de método handler                      |
| 7  | HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION_INTERCEPTOR_405  | 405              | Método HTTP não suportado                        |
| 8  | HTTP_MEDIA_TYPE_NOT_SUPPORTED_EXCEPTION_INTERCEPTOR_415      | 415              | Tipo de mídia não suportado                      |
| 9  | CIRCUIT_BREAKER_CALL_NOT_PERMITTED_EXCEPTION_INTERCEPTOR_503 | 503              | Circuit breaker aberto                           |
| 10 | REST_CLIENT_EXCEPTION_INTERCEPTOR_502                        | 502              | Erro no cliente REST                             |
| 11 | DATA_ACCESS_RESOURCE_FAILURE_EXCEPTION_INTERCEPTOR_500       | 500              | Falha de acesso a recurso de dados               |
| 12 | RATE_LIMIT_EXCEEDED_EXCEPTION_INTERCEPTOR_429                | 429              | Limite de requisições excedido                   |
| 13 | INTEGRATION_RETRY_EXCEEDED_EXCEPTION_INTERCEPTOR_503         | 503              | Limite de tentativas de integração excedido      |
| 14 | RUNTIME_EXCEPTION_INTERCEPTOR_500                            | 500              | Exceção em tempo de execução                     |
| 15 | NULL_POINTER_EXCEPTION_INTERCEPTOR_5XX                       | 500              | NullPointerException                             |
| 16 | GENERIC_EXCEPTION_INTERCEPTOR_5XX                            | 500              | Exceção genérica                                 |

Sempre que uma exception suportada pela biblioteca for lancada ela sera interceptada pelo Handler Global Exception 
resultando em uma resposta padronizada conforme mencionado acima. Entretanto voce pode precisar ou querer criar algum 
tipo de tratamento para determinada excessão, como por exemplo um '404 NotFound'. Nesses casos é possivel fazer a 
implementação da interface GlobalExceptionInterceptorIntegration em seu codigo para tratar da maneira mais adequada 
o erro, conforme os exemplo a seguir

- METHOD_ARGUMENT_VALIDATION_EXCEPTION_INTERCEPTOR_400

```java
package com.huntercodexs.sample.component.exception;

import com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration;
import com.huntercodexs.integration.handler.interfaces.GlobalExceptionInterceptorIntegration;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration.METHOD_ARGUMENT_VALIDATION_EXCEPTION_INTERCEPTOR_400;

@Component
public class Impl1 implements GlobalExceptionInterceptorIntegration {
    @Override
    public boolean supports(GlobalEnumIntegration value) {
        return value.equals(METHOD_ARGUMENT_VALIDATION_EXCEPTION_INTERCEPTOR_400);
    }

    @Override
    public String message() {
        return "Mensagem";
    }

    @Override
    public String trackerId() {
        return "8329083290";
    }

    @Override
    public String code() {
        return "1";
    }

    @Override
    public List<String> errors(Object exception) {
        return List.of("Erro 1", "Erro 2");
    }
}
```

- HTTP_MESSAGE_NOT_READABLE_EXCEPTION_INTERCEPTOR_400

```java
package com.huntercodexs.sample.component.exception;

import com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration;
import com.huntercodexs.integration.handler.interfaces.GlobalExceptionInterceptorIntegration;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration.HTTP_MESSAGE_NOT_READABLE_EXCEPTION_INTERCEPTOR_400;

@Component
public class Impl2 implements GlobalExceptionInterceptorIntegration {
    @Override
    public boolean supports(GlobalEnumIntegration value) {
        return value.equals(HTTP_MESSAGE_NOT_READABLE_EXCEPTION_INTERCEPTOR_400);
    }

    @Override
    public String message() {
        return "Mensagem";
    }

    @Override
    public String trackerId() {
        return "8329083290";
    }

    @Override
    public String code() {
        return "2";
    }

    @Override
    public List<String> errors(Object exception) {
        return List.of("Erro 1", "Erro 2");
    }
}

```

- RUNTIME_EXCEPTION_INTERCEPTOR_500

```java
package com.huntercodexs.sample.component.exception;

import com.huntercodexs.integration.handler.interfaces.GlobalExceptionInterceptorIntegration;
import com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration.RUNTIME_EXCEPTION_INTERCEPTOR_500;

@Component
public class Impl3 implements GlobalExceptionInterceptorIntegration {
    @Override
    public boolean supports(GlobalEnumIntegration value) {
        return value.equals(RUNTIME_EXCEPTION_INTERCEPTOR_500);
    }

    @Override
    public String message() {
        return "Mensagem";
    }

    @Override
    public String trackerId() {
        return "8329083290";
    }

    @Override
    public String code() {
        return "3";
    }

    @Override
    public List<String> errors(Object exception) {
        System.out.println(exception);
        return List.of("Erro 1", "Erro 2");
    }
}
```

- CIRCUIT_BREAKER_CALL_NOT_PERMITTED_EXCEPTION_INTERCEPTOR_503

```java
package com.huntercodexs.sample.component.exception;

import com.huntercodexs.integration.handler.interfaces.GlobalExceptionInterceptorIntegration;
import com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.huntercodexs.integration.handler.enumerator.GlobalEnumIntegration.CIRCUIT_BREAKER_CALL_NOT_PERMITTED_EXCEPTION_INTERCEPTOR_503;

@Component
public class Impl4 implements GlobalExceptionInterceptorIntegration {
    @Override
    public boolean supports(GlobalEnumIntegration value) {
        return value.equals(CIRCUIT_BREAKER_CALL_NOT_PERMITTED_EXCEPTION_INTERCEPTOR_503);
    }

    @Override
    public String message() {
        return "Mensagem";
    }

    @Override
    public String trackerId() {
        return "8329083290";
    }

    @Override
    public String code() {
        return "4";
    }

    @Override
    public List<String> errors(Object exception) {
        return List.of("Erro 1", "Erro 2", "Erro 3");
    }
}

```

Todos os exemplos acima apresentam uma solucao para casos onde e necessario, gerar uma mensagem especifica, um tracerId, 
um codigo, assim como tambem tratar os erros e retorna-los em uma lista de erros que sera apresentado ao usuario com 
o modelo de resposta apresentado

```json
{
  "message": "Mensagem",
  "timestamp": "2025-12-26T17:36:09.473261608",
  "code": "4",
  "tracker": "8329083290",
  "errors": [
    "Erro 1",
    "Erro 2",
    "Erro 3"
  ]
}
```

O Global Handler Exception esta disponivel e ativo para toda a aplicação, não sendo possivel desabilita-lo, sendo assim 
é altamente recomendavel que voce implemente tratamento de erros especificos quando assim for necessário.

# OpenAPI

OpenAPI é uma especificação que define um padrão para descrever APIs REST de forma estruturada e compreensível tanto 
para humanos quanto para máquinas. O documento OpenAPI, geralmente escrito em YAML ou JSON, detalha os endpoints, 
métodos HTTP, parâmetros, respostas, autenticação e outros aspectos da API.

![swagger-sample.png](files/img/swagger-sample.png)

Swagger é um conjunto de ferramentas que facilita a criação, visualização e validação de documentos OpenAPI. 
Com Swagger UI, por exemplo, é possível apresentar a documentação de forma interativa ao time, permitindo testes e 
explorações dos endpoints diretamente pelo navegador.

A definição do documento OpenAPI deve ser feita logo no início do projeto, envolvendo o time para garantir que todos 
compreendam os contratos da API. Após a validação, o documento pode ser incluído no repositório, normalmente na raiz 
ou em uma pasta específica (ex: `docs/openapi.yaml`), mais comumente usada `src/main/resources/openapi`. A implementação
pode ser automatizada usando bibliotecas como Springdoc OpenAPI, que geram o documento a partir do código-fonte, 
mantendo a documentação sempre atualizada e acessível.

Para utilizar os recursos do openapi de forma correta sera necessario implementar as seguintes configuracoes:

1. Adicione o plugin maven para buildar os sources no arquivo pom.xml

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>add-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>target/generated/swagger/gen</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

2. Adicione tambem o plugin maven do openapi generator, conforme mostrado abaixo

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.16.0</version>
    <executions>

        <!--OPENAPI Sample-->
        <execution>
            <id>users</id>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <inputSpec>./src/main/resources/openapi/USER-SAMPLE-API.yaml</inputSpec>
                <generatorName>spring</generatorName>
                <modelPackage>com.huntercodexs.api.users.model</modelPackage>
                <apiPackage>com.huntercodexs.api.users.api</apiPackage>
                <configOptions>
                    <useJakartaEe>true</useJakartaEe>
                    <useSpringBoot3>true</useSpringBoot3>
                    <generateSupportingFiles>true</generateSupportingFiles>
                    <sourceFolder>gen</sourceFolder>
                    <interfaceOnly>true</interfaceOnly>
                    <skipDefaultInterface>true</skipDefaultInterface>
                    <java21>true</java21>
                    <serializableModel>true</serializableModel>
                </configOptions>
                <templateDirectory>./src/main/resources/openapi/templates</templateDirectory>
            </configuration>
        </execution>

        <!--FEIGN Sample-->
        <execution>
            <id>users-data</id>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <inputSpec>./src/main/resources/feign/openapi/USER-DATA-SAMPLE-API.yaml</inputSpec>
                <modelPackage>com.huntercodexs.integration.users_data.model</modelPackage>
                <apiPackage>com.huntercodexs.integration.users_data.api</apiPackage>
                <generatorName>spring</generatorName>
                <library>spring-cloud</library>
                <configHelp/>
                <configOptions>
                    <useJakartaEe>true</useJakartaEe>
                    <useSpringBoot3>true</useSpringBoot3>
                    <performBeanValidation>true</performBeanValidation>
                    <sourceFolder>gen</sourceFolder>
                    <java21>true</java21>
                    <useTags>true</useTags>
                    <title>usersData</title> <!-- Used for feign bean name and uri property-->
                </configOptions>
                <templateDirectory>./src/main/resources/feign/templates</templateDirectory>
            </configuration>
        </execution>

    </executions>
</plugin>
```

Repare que existem duas configuracoes (execution) dentro do bloco do plugin openapi-generator, uma para OPENAPI e outro 
para o FEIGN, sendo o primeiro utilizado para definicoes de contratos da aplicacao que implementa a biblioteca e o 
segundo para integracao com outro servico ou API, ambos serao tratados como contratos que cada aplicacao tem para que 
ela seja consumida por outra aplicacao.

Na patrica teremos o seguinte cenario:

![openapi.png](files/img/openapi.png)

Olhando a imagem acima podemos ver que o contrato OPENAPI (swagger) do MS1 dentro do NAMESPACE 2 esta sendo usado pelos 
micro servicos MS1, MS2 e MS3 do NAMESPACE 1 como integracao, ou seja, o contrato foi gerado pelo MS1 do NAMESPACE 2 e 
entao deve ser obedecido pelas aplicacoes que precisam integrar com ele. O mesmo acontece dentro do NAMESPACE 2, onde 
o MS2 possui um contrato OPENAPI (swagger) que esta sendo consumido pelo MS1 dentro do mesmo NAMESPACE (2).

Isso e muito util para lidar com situacoes onde existe a dificuldade em gerir grandes quantidades de equipes que por 
sua vez precisam consumir servicos uns dos outros.

Voltando a falar sobre a estrutura de configuracao do arquivo pom.xml, vamos observar o que temos dentro do bloco de 
configuracao para definicoes de contrato da API

```xml
<!--OPENAPI Sample-->
<execution>
    <id>users</id>
    <goals>
        <goal>generate</goal>
    </goals>
    <configuration>
        <inputSpec>./src/main/resources/openapi/USER-SAMPLE-API.yaml</inputSpec>
        <generatorName>spring</generatorName>
        <modelPackage>com.huntercodexs.api.users.model</modelPackage>
        <apiPackage>com.huntercodexs.api.users.api</apiPackage>
        <configOptions>
            <useJakartaEe>true</useJakartaEe>
            <useSpringBoot3>true</useSpringBoot3>
            <generateSupportingFiles>true</generateSupportingFiles>
            <sourceFolder>gen</sourceFolder>
            <interfaceOnly>true</interfaceOnly>
            <skipDefaultInterface>true</skipDefaultInterface>
            <java21>true</java21>
            <serializableModel>true</serializableModel>
        </configOptions>
        <templateDirectory>./src/main/resources/openapi/templates</templateDirectory>
    </configuration>
</execution>
```

Observe que a configuracao e composta pelos seguintes campos: id, inputSpec, modelPackage, apiPackage, configOptions e
templateDirectory, sendo elas descritas a seguir:

- id: Define uma identificacao do bloco em questao, podendo ser utilizada para definir regras especificas
- inputSpec: Define o caminho para o arquivo que contem as especificacoes da API (arquivo yaml)
- modelPackage: Define onde os model devem ser gerados pelo codegen, esses model sao configurados no arquivo YAML
- apiPackage: Define onde as interfaces de apis devem ser geradas para consumo nas aplicacoes, tambem configurado no arquivo YAML
- configOptions: Contem detalhes importantes para controle de geracao dos arquivos de integracao
- templateDirectory: Define onde esta o arquivo de template para geracao das classes de APIs e Models (nao obrigatorio)

Agora vamos observar o que temos no bloco de configuracoes do pom.xml para integracoes FEIGN

```xml
<!--FEIGN Sample-->
<execution>
    <id>users-data</id>
    <goals>
        <goal>generate</goal>
    </goals>
    <configuration>
        <inputSpec>./src/main/resources/feign/openapi/USER-DATA-SAMPLE-API.yaml</inputSpec>
        <modelPackage>com.huntercodexs.integration.users_data.model</modelPackage>
        <apiPackage>com.huntercodexs.integration.users_data.api</apiPackage>
        <generatorName>spring</generatorName>
        <library>spring-cloud</library>
        <configHelp/>
        <configOptions>
            <useJakartaEe>true</useJakartaEe>
            <useSpringBoot3>true</useSpringBoot3>
            <performBeanValidation>true</performBeanValidation>
            <sourceFolder>gen</sourceFolder>
            <java21>true</java21>
            <useTags>true</useTags>
            <title>usersData</title> <!-- Used for feign bean name and uri property-->
        </configOptions>
        <templateDirectory>./src/main/resources/feign/templates</templateDirectory>
    </configuration>
</execution>
```

Observe que a configuracao e composta pelos seguintes campos: id, inputSpec, modelPackage, apiPackage, configOptions e
templateDirectory, sendo elas descritas a seguir:

- id: Define uma identificacao do bloco em questao, podendo ser utilizada para definir regras especificas
- inputSpec: Define o caminho para o arquivo que contem as especificacoes da API (arquivo yaml)
- modelPackage: Define onde os model devem ser gerados pelo codegen, esses model sao configurados no arquivo YAML
- apiPackage: Define onde as interfaces de apis devem ser geradas para consumo nas aplicacoes, tambem configurado no arquivo YAML
- configOptions: Contem detalhes importantes para controle de geracao dos arquivos de integracao
  - Dentro desse campo temos o title que merece atencao dedicada e sera apresentado na sessao FEIGN
- templateDirectory: Define onde esta o arquivo de template para geracao das classes de APIs e Models (nao obrigatorio)

A diferenca entre a configuracao do OPENAPI (swagger) para o OPENAPI (feign) esta no caminho de cada um, onde para o 
feign temos openapi/feign e no caminho dos packages temos ".integration.", alem do caminho especifico para templates 
de geracao de arquivos de integracao FEIGN especificos para isso feign/templates.

Essa configuracao e basica, mas serve para a maioria dos casos, agora vamos falar sobre o arquivo de especificacao de 
dados e integracacao, o arquivo YAML.

![OpenApi](https://img.shields.io/badge/OpenApi-ArquivoYAML-purple)

Este arquivo YAML contém a especificação OpenAPI (Swagger) usada para definir contratos de integração com um 
serviço/API. Ele deve descrever endpoints, esquemas de requisição/resposta, parâmetros, códigos de status e requisitos 
de segurança necessários para seus templates de codegen. Mantenha o arquivo versionado e estável (por exemplo em 
`src/main/resources/openapi`) e atualize a configuração do `openapi-generator` no `pom.xml` sempre que o contrato 
mudar. Artefatos gerados (clientes, interfaces de servidor) dependem dessa especificação para manter integrações 
consistentes.

O arquivo de especificacao OPENAPI para contrato e integracao possui as seguintes definicoes estruturais:

```yaml
openapi: 3.0.0

info:
  title: USER SAMPLE API
  version: 1.0.0
  description: API for managing Users
  contact:
    name: Huntercodexs API team
    email: support@huntercodexs.com
    
servers:
  - url: http://localhost:8080/huntercodexs/api/v1/management
    description: STG
  - url: http://localhost:8080/huntercodexs/api/v1/management
    description: QA
  - url: http://localhost:8080/huntercodexs/api/v1/management
    description: UAT
  - url: http://localhost:8080/huntercodexs/api/v1/management
    description: PROD

paths:
  /users:
    post:
    get:
    put:
    delete:
    path:
      
components:
  schemas:
  responses:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

**info**: Dentro de info sao definidos detalhes sobre a API, como versao por exemplo, porem meramente informativo

**servers**: Em servers podem ser  informados os ambientes em que a API vai estar disponivel com suas respectivas urls

**paths**: O path e usado para definir os enpoints da API, com seus metodos HTTP, objetos de entradas e saidas e ate 
mesmo os parametros que a API espera receber.

**components**: Ja em components, podemos definir a estrutura dos objetos, e aqui onde criamos os DTOs de entrada e 
saida, para assim compor o contrato da API.

A seguir um exemplo real de um arquivo YAML Swagger:

> NOTA: Voce pode usar o Swagger Editor para validar seu arquivo YAML no link https://editor.swagger.io/

```yaml
openapi: 3.0.0

info:
  title: USER SAMPLE API
  version: 1.0.0
  description: API for managing Users
  contact:
    name: Huntercodexs API team
    email: support@huntercodexs.com

servers:
  - url: http://localhost:8080/huntercodexs/api/v1/management
    description: STG
  - url: http://localhost:8080/huntercodexs/api/v1/management
    description: QA
  - url: http://localhost:8080/huntercodexs/api/v1/management
    description: UAT
  - url: http://localhost:8080/huntercodexs/api/v1/management
    description: PROD

paths:
  /users:
    post:
      tags:
        - Users Management
      summary: Create a new User
      operationId: createNewUser
      description: |
        ## Responsibilities
        * Creates a new User entity in the system.

        ---
        
        ## Requirements
        * To access this endpoint, the client must have one of the following role sets:
          * `ADMIN` and `USER` and `AUTH-BASIC`

      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUserRequest'
      responses:
        '201':
          description: User successfully created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CreateUserResponse'

        '400':
          description: |
            Bad Request
            
            This endpoint can throw the following errors:
            
            ```
            [{"code": "400", "message": "Username is required."}]
            ```

          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'

        '401':
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '403':
          description: Forbidden
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '422':
          description: |
            Unprocessable Entity
            
            This endpoint can throw the following errors:
            
            ```
            [{ "code": "422001", "message": "The user already exists." }]
            ```

          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '500':
          description: |
            Internal Server Error
            
            This endpoint can throw the following errors:

            ```
            [{ "code": "500001", "message": "Failed to integrate with User Data Sample API." }]
            ```

          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        'default':
          description: Unexpected Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
    get:
      tags:
        - Users Management
      summary: List all Users
      operationId: getAllUsers
      description: |
        ## Responsibilities
        * Retrieves a paginated list of all Users available to the authenticated admin user.
        * Supports filtering and pagination to optimize performance and usability.

        ---
        
        ## Requirements
        * To access this endpoint, the client must have one of the following role sets:
          * `ADMIN` and `USER` and `AUTH-BASIC`

      security:
        - bearerAuth: []
      parameters:
        - name: limit
          in: query
          required: false
          schema:
            type: integer
            default: 20
          description: Maximum number of items to return
        - name: offset
          in: query
          required: false
          schema:
            type: integer
            default: 0
          description: Starting index for pagination
        - name: name
          in: query
          required: false
          schema:
            type: string
          description: Filter by User name
      responses:
        '200':
          description: Users retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UsersResponsePagination'
        "206":
          description: Partial Content
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UsersResponsePagination'
        '401':
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '403':
          description: Forbidden
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '500':
          description: Internal Server Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        'default':
          description: Unexpected Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
  /users/{userId}:
    get:
      tags:
        - Users Management
      summary: Get details of a User by Id
      operationId: getUserById
      description: |
        ## Responsibilities
        * Retrieves a unique User by Id available to the authenticated admin user.

        ---
        
        ## Requirements
        * To access this endpoint, the client must have one of the following role sets:
          * `ADMIN` and `USER` and `AUTH-BASIC`

      security:
        - bearerAuth: []
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
          description: User Id
      responses:
        '200':
          description: User details retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'
        '403':
          description: Forbidden
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '404':
          description: |
            Not Found
            
            This endpoint can throw the following errors:
            
            ```
            [{"code": "404001", "message": "User not found."}]
            ```
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '500':
          description: Internal Server Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        'default':
          description: Unexpected Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
    delete:
      x-rate-limit-enabled: true
      tags:
        - Users Management
      summary: Delete a User by Id
      operationId: deleteUserById
      description: |
        ## Responsibilities
        * Delete a unique User by Id available to the authenticated admin user.

        ---
        
        ## Requirements
        * To access this endpoint, the client must have one of the following role sets:
          * `ADMIN` and `USER` and `AUTH-BASIC`

      security:
        - bearerAuth: []
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
          description: User Id
      responses:
        '204':
          description: User deleted successfully
        '401':
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '404':
          description: |
            Not Found
            
            This endpoint can throw the following errors:
            
            ```
            [{"code": "404001", "message": "User not found."}]
            ```
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '500':
          description: Internal Server Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        'default':
          description: Unexpected Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
    patch:
      tags:
        - Users Management
      summary: Update partial data of User by Id
      operationId: patchUserById
      description: |
        ## Responsibilities
        * Update a partial data of a unique User by Id available to the authenticated admin user.

        ---
        
        ## Requirements
        * To access this endpoint, the client must have one of the following role sets:
          * `ADMIN` and `USER` and `AUTH-BASIC`

      security:
        - bearerAuth: []
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
          description: User Id
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserUpdateRequest'
      responses:
        '204':
          description: No content
        '400':
          description: |
            Bad Request
            
            This endpoint can throw the following errors:
            
            ```
            [{"code": "400", "message": "Bad Request"}]
            [{"code": "400001", "message": "At least one attribute is required to perform the update."}]
            ```
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '401':
          description: Unauthorized
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '404':
          description: |
            Not Found
            
            This endpoint can throw the following errors:
            
            ```
            [{"code": "404001", "message": "User not found."}]
            ```
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '422':
          description: |
            Unprocessable Entity
            
            This endpoint can throw the following errors:
            
            ```
            [{ "code": "422001", "message": "The name already exists." }]
            ```
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        '500':
          description: Internal Server Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'
        'default':
          description: Unexpected Error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorModel'

components:
  schemas:
    CreateUserRequest:
      type: object
      required:
        - name
        - email
      properties:
        name:
          type: string
          example: Username
          minLength: 3
        email:
          type: string
          example: user@email.com

    CreateUserResponse:
      type: object
      properties:
        id:
          type: string
          example: '5a2d6db58b9a2900088237f1'

    UsersResponsePagination:
      required:
        - users
        - pagination
      type: object
      properties:
        users:
          type: array
          items:
            $ref: '#/components/schemas/UserResponse'
        pagination:
          $ref: '#/components/schemas/Pagination'

    UserResponse:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        email:
          type: string
          x-field-extra-annotation: '@JsonInclude(JsonInclude.Include.NON_NULL)'
          #x-field-extra-annotation: '@JsonProperty("id")'
        createdAt:
          type: string

    UserUpdateRequest:
      type: object
      properties:
        name:
          type: string
          example: Updated User name
        email:
          type: string
          example: Updated User email

    ErrorModel:
      type: array
      items:
        type: object
        required:
          - code
          - message
        properties:
          code:
            type: string
            example: "code"
          message:
            type: string
            example: "message"

    Pagination:
      type: object
      properties:
        offset:
          type: number
        limit:
          type: number
        totalCount:
          type: number
        totalPages:
          type: number
        links:
          type: array
          description: Links for next page, previous page, first page and last page
          items:
            $ref: '#/components/schemas/PaginationLink'

    PaginationLink:
      required:
        - href
        - rel
      type: object
      properties:
        href:
          type: string
          example: 'http://localhost:8080/pagination-sample/v1/resource?limit=1&offset=0'
        rel:
          type: string
          description: Expected values are **Last**, **Previous**, **Next** or **First**

  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

O exemplo pode tambem ser encontrado no caminho desse repositorio em `src/main/resources/support/openapi`.

![OpenApi](https://img.shields.io/badge/OpenApi-MavenCommand-blue)

Agora que voce configurou todos os arquivos relacionados ao OPENAPI, basta rodar o comando na sua IDE

```shell
mvn clean install -DskipTests
```

A saida/conclusao devera ser algo como mostrado a seguir

***Gerado a partir da IDE IntelliJ***
![mvncleaninstall.png](files/img/mvncleaninstall.png)

Apos a conclusao do comando, serao gerados todos os arquivos necessarios para iniciar a programacao da API e utilizar 
os arquivos resultados como base para integracao de outras aplicacoes com a aplicacao atual. Os arquivos gerados estarao 
disponiveis no caminho `target/generated-sources/openapi/gen` conforme ilustrado a seguir

![targetsample.png](files/img/targetsample.png)

> DICA: Caso a pasta gen nao esteja marcada como "Generated Resources Root", clique com o botao direito sobre ela e 
> marque ela como tal, pois a IDE IntelliJ ou outra qualquer pode nao encontrar esses arquivos automaticamente.

![OpenApi](https://img.shields.io/badge/OpenApi-Implementacao-green)

Agora com todos os arquivos e configuracoes criados, podemos dar inicio a programacao da API, criando os controllers 
e implementando as interfaces que foram gerados a partir do arquivo YAML, Sendo assim vamos iniciar criando o controller 
UsersControllerSimulation.java

```java
package com.huntercodexs.sample.controlller;

import com.huntercodexs.api.users.api.UsersApi;
import com.huntercodexs.api.users.model.*;
import com.huntercodexs.integration.ratelimit.annotation.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation")
public class UsersApiControllerSimulation implements UsersApi {

    @Override
    public ResponseEntity<CreateUserResponse> createNewUser(CreateUserRequest createUserRequest) {
        System.out.println("Creating new user: " + createUserRequest);
        return null;
    }
}
```

Observe que ao implementar `UsersApi.java` em nossa aplicacao automaticamente temos acesso aos metodos contidos dentro 
dessa interface, assim como os objetos e detalhes pertinentes a ela, e tudo isso foi gerado a partir do arquivo YAML.

Se voce der uma espiada nesse metodo `createNewUesr` vera que realmente foi tudo o que voce definiou no arquivo YAML e 
seu conteudo sera mais ou menos igual a

```java
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-12-23T15:06:39.938308216-03:00[America/Sao_Paulo]", comments = "Generator version: 7.16.0")
@Validated
@Tag(name = "Users Management", description = "the Users Management API")
public interface UsersApi {

    public static final String PATH_CREATE_NEW_USER = "/users";

    /**
     * POST /users : Create a new User
     * ## Responsibilities * Creates a new User entity in the system.  ---  ## Requirements * To access this endpoint, the client must have one of the following role sets:   * &#x60;ADMIN&#x60; and &#x60;USER&#x60; and &#x60;AUTH-BASIC&#x60; 
     *
     * @param createUserRequest  (required)
     * @return User successfully created (status code 201)
     *         or Bad Request  This endpoint can throw the following errors:  &#x60;&#x60;&#x60; [{\&quot;code\&quot;: \&quot;400\&quot;, \&quot;message\&quot;: \&quot;Username is required.\&quot;}] &#x60;&#x60;&#x60;  (status code 400)
     *         or Unauthorized (status code 401)
     *         or Forbidden (status code 403)
     *         or Unprocessable Entity  This endpoint can throw the following errors:  &#x60;&#x60;&#x60; [{ \&quot;code\&quot;: \&quot;422001\&quot;, \&quot;message\&quot;: \&quot;The user already exists.\&quot; }] &#x60;&#x60;&#x60;  (status code 422)
     *         or Internal Server Error  This endpoint can throw the following errors:  &#x60;&#x60;&#x60; [{ \&quot;code\&quot;: \&quot;500001\&quot;, \&quot;message\&quot;: \&quot;Failed to integrate with User Data Sample API.\&quot; }] &#x60;&#x60;&#x60;  (status code 500)
     *         or Unexpected Error (status code 200)
     */
    @Operation(
            operationId = "createNewUser",
            summary = "Create a new User",
            description = "## Responsibilities * Creates a new User entity in the system.  ---  ## Requirements * To access this endpoint, the client must have one of the following role sets:   * `ADMIN` and `USER` and `AUTH-BASIC` ",
            tags = {"Users Management"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "User successfully created", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = CreateUserResponse.class))
                    }),
                    @ApiResponse(responseCode = "400", description = "Bad Request  This endpoint can throw the following errors:  ``` [{\"code\": \"400\", \"message\": \"Username is required.\"}] ``` ", content = {
                            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorModelInner.class)))
                    }),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = {
                            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorModelInner.class)))
                    }),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorModelInner.class)))
                    }),
                    @ApiResponse(responseCode = "422", description = "Unprocessable Entity  This endpoint can throw the following errors:  ``` [{ \"code\": \"422001\", \"message\": \"The user already exists.\" }] ``` ", content = {
                            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorModelInner.class)))
                    }),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error  This endpoint can throw the following errors:  ``` [{ \"code\": \"500001\", \"message\": \"Failed to integrate with User Data Sample API.\" }] ``` ", content = {
                            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorModelInner.class)))
                    }),
                    @ApiResponse(responseCode = "default", description = "Unexpected Error", content = {
                            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorModelInner.class)))
                    })
            },
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @RequestMapping(
            method = RequestMethod.POST,
            value = UsersApi.PATH_CREATE_NEW_USER,
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<CreateUserResponse> createNewUser(
            @Parameter(name = "CreateUserRequest", description = "", required = true) @Valid @RequestBody CreateUserRequest createUserRequest
    );
}
```

Agora e so criar as Services e demais classes para tratar a regra de negocio da aplicacao.

# Codegen (openapi-generator-maven-plugin)

O Codegen do OpenAPI, utilizando o plugin `openapi-generator-maven-plugin`, permite gerar código-fonte automaticamente 
a partir de templates YAML do Swagger. Essa abordagem facilita a padronização e agilidade na criação de APIs, pois os 
contratos definidos em YAML são convertidos em implementações Java, reduzindo erros e acelerando o desenvolvimento. 
A configuração do plugin no Maven possibilita customizar templates e integrar o processo de geração ao ciclo de build 
do projeto.

Esse recurso ja foi apresentado anteriormente em Como Usar, sendo assim nao necessita de mais explicacoes no momento.

# Feign

Feign é um cliente HTTP declarativo para Java, utilizado para simplificar chamadas a APIs REST. Ele permite definir 
interfaces que representam endpoints remotos, tornando o consumo de serviços externos mais simples e integrado ao 
Spring Boot. Na prática, basta criar uma interface anotada com `@FeignClient` e declarar os métodos correspondentes às 
requisições desejadas, sem necessidade de implementar lógica de comunicação manual.

Aqui comeca a principal fonte de integracao da biblioteca huntercodexs-spring-integration, onde temos possibilidade de 
implementar de forma simples e objetiva integracoes entre APIs utilizando REST. Para isso vamos pontuar os seguintes 
topicos que iremos tratar para uma implementacao satisfatoria do Feign:

![Feign](https://img.shields.io/badge/Feign-Definicao-orange)

Apos a definicao de contrato da API, que nada mais e do que definir como a API sera servida, qual metodo HTTP 
sera utilizado e quais objetos de entrada e saida serao processados. Esse arquivo ja foi explicado anteriormente e nao
necessita mais delongas a cerca de sua estrutura e utilidade, mas tenha em mente que ele sera a peca chave para iniciar 
os desenvolvimento de toda a aplicacao em questao.

![Feign](https://img.shields.io/badge/Feign-Configuracao-blue)

A configuracao e feita atraves dos seguintes arquivos

1 - application.propoerties: Nesse arquivo sera necessario definir as seguintes propriedades

```properties
# FEIGN CONFIG
huntercodexs.integration.client.config.logging.enabled=true
huntercodexs.integration.client.config.retryer.period=3000
huntercodexs.integration.client.config.retryer.max-period=10000
huntercodexs.integration.client.config.retryer.max-attempts=3
huntercodexs.integration.client.config.proxy.enable=false
huntercodexs.integration.client.config.proxy.host=localhost
huntercodexs.integration.client.config.proxy.port=8080
```
Cada uma dessas propriedades sera utilizada durante a programacao da aplicacao, em um momento oportuno e adequado tudo 
isso sera explicado em detalhes

2 - pom.xml: Ja nesse arquivo sera necessario (conforme dito anteriormente) as seguintes configuracoes

```xml
<!--FEIGN Sample-->
<execution>
    <id>users-data</id>
    <goals>
        <goal>generate</goal>
    </goals>
    <configuration>
        <inputSpec>./src/main/resources/feign/openapi/USER-DATA-SAMPLE-API.yaml</inputSpec>
        <modelPackage>com.huntercodexs.integration.users_data.model</modelPackage>
        <apiPackage>com.huntercodexs.integration.users_data.api</apiPackage>
        <generatorName>spring</generatorName>
        <library>spring-cloud</library>
        <configHelp/>
        <configOptions>
            <useJakartaEe>true</useJakartaEe>
            <useSpringBoot3>true</useSpringBoot3>
            <performBeanValidation>true</performBeanValidation>
            <sourceFolder>gen</sourceFolder>
            <java21>true</java21>
            <useTags>true</useTags>
            <title>usersData</title> <!-- Used for feign bean name and uri property-->
        </configOptions>
        <templateDirectory>./src/main/resources/feign/templates</templateDirectory>
    </configuration>
</execution>
```

Um ponto importante e que para cada integracao devera ser criado um bloco de configuracao como esse, como por exemplo: 
Imagine que temos duas integracoes, uma de usuario e outra de dados, entao teriamos dois arquivos YAML e duas configuracoes 
dentro do arquivo pom.xml

```xml
<!--FEIGN Sample-->
<execution>
    <id>user</id>
    <goals>
        <goal>generate</goal>
    </goals>
    <configuration>
        <inputSpec>./src/main/resources/feign/openapi/USER-SAMPLE-API.yaml</inputSpec>
        <modelPackage>com.huntercodexs.integration.user.model</modelPackage>
        <apiPackage>com.huntercodexs.integration.user.api</apiPackage>
        <generatorName>spring</generatorName>
        <library>spring-cloud</library>
        <configHelp/>
        <configOptions>
            <useJakartaEe>true</useJakartaEe>
            <useSpringBoot3>true</useSpringBoot3>
            <performBeanValidation>true</performBeanValidation>
            <sourceFolder>gen</sourceFolder>
            <java21>true</java21>
            <useTags>true</useTags>
            <title>users</title> <!-- Used for feign bean name and uri property-->
        </configOptions>
        <templateDirectory>./src/main/resources/feign/templates</templateDirectory>
    </configuration>
</execution>

<execution>
    <id>data</id>
    <goals>
        <goal>generate</goal>
    </goals>
    <configuration>
        <inputSpec>./src/main/resources/feign/openapi/DATA-SAMPLE-API.yaml</inputSpec>
        <modelPackage>com.huntercodexs.integration.data.model</modelPackage>
        <apiPackage>com.huntercodexs.integration.data.api</apiPackage>
        <generatorName>spring</generatorName>
        <library>spring-cloud</library>
        <configHelp/>
        <configOptions>
            <useJakartaEe>true</useJakartaEe>
            <useSpringBoot3>true</useSpringBoot3>
            <performBeanValidation>true</performBeanValidation>
            <sourceFolder>gen</sourceFolder>
            <java21>true</java21>
            <useTags>true</useTags>
            <title>data</title> <!-- Used for feign bean name and uri property-->
        </configOptions>
        <templateDirectory>./src/main/resources/feign/templates</templateDirectory>
    </configuration>
</execution>
```

![Feign](https://img.shields.io/badge/Feign-Recursos-red)

A biblioteca huntercodexs-spring-integration oferece muito mais do que simplesmente integrar APIs e microservicos, ela 
oferece uma serie de recursos uteis para implementacoes complexas como interceptores, proxy, retentativas, logs dentre 
outros, a seguir a lista de recursos:

- Proxy

Ative o proxy antes de fazer o uso do mesmo com a propriedade

```properties
huntercodexs.integration.client.config.proxy.enable=true
```

Apos ativar o proxy configure o host e a porta, por exemplo

```properties
huntercodexs.integration.client.config.proxy.host=myproxy.com
huntercodexs.integration.client.config.proxy.port=8080
```

Por padrao a biblioteca aplica o proxy (quando ativo) nos seguintes profiles:

```text
"local", "dev", "develop", "development", "default", "stage", "prod", "production"
```

- Retry

O recurso de retentativa considera a seguinte configuracao

```properties
huntercodexs.integration.client.config.retryer.period=3000 (ms)
huntercodexs.integration.client.config.retryer.max-period=10000 (ms)
huntercodexs.integration.client.config.retryer.max-attempts=3
```

Com essas propriedades e possivel criar um sistema de retentativa eficiente e escalavel, sendo esses os valores default 
para o retryer do Feign. A retentativa e disparada automaticamente quando e detectada alguma falha durante a requisicao 
de uma API.

Ainda e possivel implementar a interface RetryInterceptorIntegration.java para personlizacao de comportamento da API 
quando a quantidade de tentativas e excedida, conforme mostrado a seguir

```java
package com.huntercodexs.sample.component.interceptor;

import com.huntercodexs.integration.core.interfaces.RetryInterceptorIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.huntercodexs.integration.core.constants.CoreIntegrationConstants.CORE_RETRYER_HANDLER_EXCEPTION_CUSTOM;

@Component
public class RetryInterceptorCustomSimulation implements RetryInterceptorIntegration {

    private static final Logger log = LoggerFactory.getLogger(RetryInterceptorCustomSimulation.class);

    @Override
    public boolean supports(Object value) {
        return value.toString().equals(CORE_RETRYER_HANDLER_EXCEPTION_CUSTOM);
    }

    @Override
    public void execute() {
        log.info("This is a CUSTOM retry interceptor integration");
    }
}
```

Quando essa abordagem e usada, temos entao a possibilidade de executar alguma operacao antes de finalizar as tentativas, 
o log nessa situacao pode parecer com algo assim

```text
2025-12-23 17:32:31.819 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.c.ClientInterceptorConfigIntegration.retrieveClientToken - No interceptor found for target: invalid-api
2025-12-23 17:32:31.820 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.logger.HttpLoggerIntegration.logRequest - Request sent - method: POST | url: http://localhost:8085/api/users/create | headers: {Content-Length=53, Content-Type=application/json} | body: {"name":"Username Test","email":"username@email.com"}
2025-12-23 17:32:31.825 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.retry.RetryLoggerIntegration.continueOrPropagate - Retrying request - 1/3 | method: POST | url: http://localhost:8085/api/users/create | message: Connection refused executing POST http://localhost:8085/api/users/create
2025-12-23 17:32:33.827 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.c.ClientInterceptorConfigIntegration.retrieveClientToken - No interceptor found for target: invalid-api
2025-12-23 17:32:33.827 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.logger.HttpLoggerIntegration.logRequest - Request sent - method: POST | url: http://localhost:8085/api/users/create | headers: {Content-Length=53, Content-Type=application/json} | body: {"name":"Username Test","email":"username@email.com"}
2025-12-23 17:32:33.828 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.retry.RetryLoggerIntegration.continueOrPropagate - Retrying request - 2/3 | method: POST | url: http://localhost:8085/api/users/create | message: Connection refused executing POST http://localhost:8085/api/users/create
2025-12-23 17:32:37.829 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.c.ClientInterceptorConfigIntegration.retrieveClientToken - No interceptor found for target: invalid-api
2025-12-23 17:32:37.830 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.logger.HttpLoggerIntegration.logRequest - Request sent - method: POST | url: http://localhost:8085/api/users/create | headers: {Content-Length=53, Content-Type=application/json} | body: {"name":"Username Test","email":"username@email.com"}
2025-12-23 17:32:37.831 [http-nio-8080-exec-1] [WARN ] [jereelton-acer-nitro] c.h.i.c.retry.RetryLoggerIntegration.continueOrPropagate - Limit of retries reached, (tries: 3/3) | method: POST | url: http://localhost:8085/api/users/create | message: Connection refused executing POST http://localhost:8085/api/users/create
2025-12-23 17:32:37.831 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.s.c.i.RetryInterceptorCustomSimulation.execute - This is a CUSTOM retry interceptor integration
2025-12-23 17:32:37.834 [http-nio-8080-exec-1] [ERROR] [jereelton-acer-nitro] c.h.i.handler.GlobalExceptionHandler.handleIntegrationRetryAttemptsExceededException - Limit of requests exceeded for Integration: Integration Retries Exceeded: 3
2025-12-23 17:32:37.834 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.handler.GlobalExceptionHandler.buildErrorResponse - No tracker provided; generated automatically: f0024aec-8fd8-4219-813f-fd44f635e095
2025-12-23 17:32:37.835 [http-nio-8080-exec-1] [ERROR] [jereelton-acer-nitro] c.h.i.handler.GlobalExceptionHandler.logException - [f0024aec-8fd8-4219-813f-fd44f635e095] 503 SERVICE_UNAVAILABLE - Limit of requests exceeded for Integration | errors=[Integration Retries Exceeded: 3]
```

Repare no trecho de log "This is a CUSTOM retry interceptor integration" onde foi implementado o codigo.

- Interceptor

Um dos recursos mais uteis dessa biblioteca chamada huntercodexs-spring-integration e o interceptor, com ele e possivel 
interceptar uma requisicao antes mesmo de ser disparada pela aplicacao, efetuando algum alteracao especifica como 
adicionar uma nova header, ou encriptar algum dado.

Apenas para ilustrar e clarear um pouco sobre o assunto, vamos criar dois interceptores para controlar requisicoes de 
uma ficticia User e outra Data, como segue abaixo.

UserInterceptorImpl.java
```java
package com.huntercodexs.sample.component.interceptor;

import com.huntercodexs.integration.core.interfaces.ClientInterceptorIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Component
public class UserInterceptorImpl implements ClientInterceptorIntegration {

    @Autowired
    private UserManagerService userManagerService;

    @Override
    public boolean checkSupport(Object value) {
        return value.toString().equals("user"); // id => feign pom.xml
    }

    @Override
    public String getClientToken() {
        return userManagerService.getClientToken().orElse("");
    }

    @Service
    public static class UserManagerService {
        public Optional<String> getClientToken() {
            System.out.println("calling getClientToken from UserManagerService");
            return Optional.of("Bearer UserManagerTokenFake");
        }
    }
}
```

DataInterceptorImpl.java
```java
package com.huntercodexs.sample.component.interceptor;

import com.huntercodexs.integration.core.interfaces.ClientInterceptorIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Component
public class DataInterceptorImpl implements ClientInterceptorIntegration {

    @Autowired
    private DataService dataService;

    @Override
    public boolean checkSupport(Object value) {
        return value.toString().equals("data"); // id => feign pom.xml
    }

    @Override
    public String getClientToken() {
        return dataService.getClientToken().orElse("");
    }

    @Service
    public static class DataService {
        public Optional<String> getClientToken() {
            System.out.println("calling getClientToken from DataService");
            return Optional.of("Bearer DataTokenFake");
        }
    }

}
```

Observe que em ambos os casos temos uma relacao com a configuracao do arquivo pom.xml com as classes implementadas visto 
que o id do feign deve ser o mesmo do metodo checkSupport(). No caso dos exemplos, estamos apenas interceptando a 
requisicao para inserir um token de client antes da requisicao final.

- Logger

O log e feito durante a requisicao/integracao de uma API e tambem durante o recebimento da resposta dessa integracao. 
Caso seja necessario desabilitar o log use a seguinte propriedade

```properties
huntercodexs.integration.client.config.logging.enabled=true
```

O log deve ter a seguinte aparencia

Requisicao e Recepcao de dados
```text
2025-12-23 16:43:24.962 [http-nio-8080-exec-2] [INFO ] [jereelton-acer-nitro] c.h.i.c.logger.HttpLoggerIntegration.logRequest - Request sent - method: POST | url: http://localhost:8080/api/users/create | headers: {Authorization=Bearer UserManagerTokenFake, Content-Length=53, Content-Type=application/json} | body: {"name":"Username Test","email":"username@email.com"}
2025-12-23 16:43:25.015 [http-nio-8080-exec-2] [INFO ] [jereelton-acer-nitro] c.h.i.c.logger.HttpLoggerIntegration.logAndRebufferResponse - Request received - status: 200 | elapsedTime: 53ms | headers: {date=Tue, 23 Dec 2025 19:43:25 GMT, content-length=0, keep-alive=timeout=60, connection=keep-alive} | body: 
```
Inteceptacao de Request
```text
2025-12-23 16:43:24.961 [http-nio-8080-exec-2] [INFO ] [jereelton-acer-nitro] c.h.i.c.c.ClientInterceptorConfigIntegration.retrieveClientToken - Interceptor applied for target: user, headers: {Authorization=[Bearer UserManagerTokenFake], Content-Length=[53], Content-Type=[application/json]}
```
Retentativas
```text
2025-12-23 17:24:27.919 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.c.ClientInterceptorConfigIntegration.retrieveClientToken - No interceptor found for target: invalid-api
2025-12-23 17:24:27.920 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.logger.HttpLoggerIntegration.logRequest - Request sent - method: POST | url: http://localhost:8085/api/users/create | headers: {Content-Length=53, Content-Type=application/json} | body: {"name":"Username Test","email":"username@email.com"}
2025-12-23 17:24:27.925 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.retry.RetryLoggerIntegration.continueOrPropagate - Retrying request - 1/3 | method: POST | url: http://localhost:8085/api/users/create | message: Connection refused executing POST http://localhost:8085/api/users/create
2025-12-23 17:24:29.927 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.c.ClientInterceptorConfigIntegration.retrieveClientToken - No interceptor found for target: invalid-api
2025-12-23 17:24:29.928 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.logger.HttpLoggerIntegration.logRequest - Request sent - method: POST | url: http://localhost:8085/api/users/create | headers: {Content-Length=53, Content-Type=application/json} | body: {"name":"Username Test","email":"username@email.com"}
2025-12-23 17:24:29.930 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.retry.RetryLoggerIntegration.continueOrPropagate - Retrying request - 2/3 | method: POST | url: http://localhost:8085/api/users/create | message: Connection refused executing POST http://localhost:8085/api/users/create
2025-12-23 17:24:33.932 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.c.ClientInterceptorConfigIntegration.retrieveClientToken - No interceptor found for target: invalid-api
2025-12-23 17:24:33.933 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.c.logger.HttpLoggerIntegration.logRequest - Request sent - method: POST | url: http://localhost:8085/api/users/create | headers: {Content-Length=53, Content-Type=application/json} | body: {"name":"Username Test","email":"username@email.com"}
2025-12-23 17:24:33.936 [http-nio-8080-exec-1] [WARN ] [jereelton-acer-nitro] c.h.i.c.retry.RetryLoggerIntegration.continueOrPropagate - Limit of retries reached, (tries: 3/3) | method: POST | url: http://localhost:8085/api/users/create | message: Connection refused executing POST http://localhost:8085/api/users/create
2025-12-23 17:24:33.951 [http-nio-8080-exec-1] [ERROR] [jereelton-acer-nitro] c.h.i.handler.GlobalExceptionHandler.handleIntegrationRetryAttemptsExceededException - Limit of requests exceeded for Integration: Integration Retries Exceeded: 3
2025-12-23 17:24:33.951 [http-nio-8080-exec-1] [INFO ] [jereelton-acer-nitro] c.h.i.handler.GlobalExceptionHandler.buildErrorResponse - No tracker provided; generated automatically: 238191e2-9f5b-47b0-9d0f-44247023e4d1
2025-12-23 17:24:33.956 [http-nio-8080-exec-1] [ERROR] [jereelton-acer-nitro] c.h.i.handler.GlobalExceptionHandler.logException - [238191e2-9f5b-47b0-9d0f-44247023e4d1] 503 SERVICE_UNAVAILABLE - Limit of requests exceeded for Integration | errors=[Integration Retries Exceeded: 3]
```
Circuit Breaker Aberto (TODO)
```text
2025-12-23 16:43:24.961 [http-nio-8080-exec-2] [INFO ] [jereelton-acer-nitro] c.h.i.c.c.ClientInterceptorConfigIntegration.retrieveClientToken - Interceptor applied for target: user, headers: {Authorization=[Bearer UserManagerTokenFake], Content-Length=[53], Content-Type=[application/json]}
```

![Feign](https://img.shields.io/badge/Feign-Compilacao-green)

Depois feito todos os ajustes e configuracoes descritos anteriormente nessa documentacao, agora basta rodar o comando 
mvn para gerar todos os arquivos necessarios de integracao com as APIs externas ou mesmo internas, por exemplo:

```shell
mvn clean install -DskipTests
```

Com esse comando sera gerada a seguinte estrutura de diretorios dentro da pasta target do projeto

![feign-targets.png](files/img/feign-targets.png)

Podemos notar que existe a pasta (package) chamado integration.users_data.api e integration.users_data.model, os 
quais serao utilizados durante o programacao para servir de base para cada integracao feita.

E importante notar que nesse caso sera necessario mais uma configuracao no arquivo de application.properties, configuracao 
essa que informa a url de integracao da API externa, por exemplo:

```properties

api.usersData.uri=http://localhost:35003/huntercodexs/api/user-data/v1
```

Observe que o nome da propriedade "usersData" e o mesmo nome dado ao title do arquivo pom.xml e isso e imprescindivel 
para o correto funcionanemto da integracao

```xml
<!--FEIGN Sample-->
<execution>
    <id>users-data</id>
    <goals>
        <goal>generate</goal>
    </goals>
    <configuration>
        <inputSpec>./src/main/resources/feign/openapi/USER-DATA-SAMPLE-API.yaml</inputSpec>
        <modelPackage>com.huntercodexs.integration.users_data.model</modelPackage>
        <apiPackage>com.huntercodexs.integration.users_data.api</apiPackage>
        <generatorName>spring</generatorName>
        <library>spring-cloud</library>
        <configHelp/>
        <configOptions>
            <useJakartaEe>true</useJakartaEe>
            <useSpringBoot3>true</useSpringBoot3>
            <performBeanValidation>true</performBeanValidation>
            <sourceFolder>gen</sourceFolder>
            <java21>true</java21>
            <useTags>true</useTags>
            <title>usersData</title> <!-- Used for feign bean name and uri property-->
        </configOptions>
        <templateDirectory>./src/main/resources/feign/templates</templateDirectory>
    </configuration>
</execution>
```

![Feign](https://img.shields.io/badge/Feign-Programacao-cyan)

Agora chegamos no ponto onde vamos de fato aplicar toda essa configuracao em nossa aplicacao, escrevendo uma classe 
para demonstrar como implementar essa funcionalidade. Para isso vamos direto ao ponto, criamos uma classe chamada 
`UserService` que sera executada pelo `UserController` e que estara fazendo a implementacao da APIClient do Feign 
gerada na configuracao anterior, e que esse cliente por sua vez fara entao a integracao com a API Externa conforme 
ilustrado na imagem abaixo

![feign.png](files/img/feign.png)

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/add")
    public void create(UseRequest useRequest) {
        userService.add(useRequest);
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserApiClient userApiClient; //Feign integration
    
    public UseRequest add(UseRequest useRequest) {
      return userApiClient.createUser(useRequest);
    }
}
```

> IMPORTANTE: A classe abaixo e apens um exemplo, ela nao precisa ser edita ou criada, pois ela sera criada automaticamente 
> durante o processo de build da aplicacao

```java
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-12-26T15:22:01.053271491-03:00[America/Sao_Paulo]", comments = "Generator version: 7.16.0")  @Validated
@Tag(name = "UserDataSample", description = "the UserDataSample API")
@FeignClient(name="usersData", url="${api.usersData.uri}", configuration = ClientConfigIntegration.class)
public interface UserApiClient {

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/users",
            produces = "application/json",
            consumes = "application/json"
    )
    UseRequest createUser(
            @Parameter(name = "useRequest", description = "User data to be created", required = true) @Valid @RequestBody UseRequest useRequest
    );
}
```

![Feign](https://img.shields.io/badge/Feign-Execucao-purple)

Supondo que nesse ponto tenhamos a url de integracao usersData com o valor http://localhost:8085/huntercodexs/api/user-data/v1, entao a integracao
sera executado com o seguinte pacote

```text
POST http://localhost:8085/huntercodexs/api/user-data/v1/users
Headers: []
Body: UseRequest
```

E assim esta feita a integracao com a API USER-DATA.

# Circuit Breaker

Resiliência no Java Spring Boot é essencial para construir aplicações robustas que lidam graciosamente com falhas em 
serviços externos. Ao integrar padrões de resiliência como Circuit Breaker, você evita erros em cascata e melhora a 
estabilidade do sistema. No Spring Boot, o Circuit Breaker pode ser combinado facilmente com clientes Feign, especialmente 
quando se utiliza geração de código via OpenAPI. Ao anotar interfaces Feign geradas a partir de especificações OpenAPI com 
`@CircuitBreaker`, você habilita tolerância automática a falhas em chamadas remotas, permitindo que sua aplicação detecte 
falhas e bloqueie temporariamente requisições para serviços instáveis, mantendo a confiabilidade geral.

# Rate Limit (for APIs)

O rate limiting é uma estratégia essencial para controlar o fluxo de requisições às APIs, garantindo a estabilidade do 
sistema e protegendo recursos contra acessos excessivos ou abusivos. Ao definir limites para o número de requisições 
permitidas em um determinado intervalo de tempo, é possível evitar sobrecarga, assegurar uso justo entre clientes e 
mitigar riscos como ataques de negação de serviço (DoS). Em projetos Java Spring Boot, o rate limiting pode ser 
implementado por meio de bibliotecas ou mecanismos nativos, permitindo personalizar limites e respostas para diferentes 
endpoints ou usuários. Essa abordagem contribui para manter a confiabilidade e o desempenho da API, especialmente em 
cenários de alto tráfego ou integrações.

# Rate Limit (Service Bus)

Rate limit, no contexto de consumo de filas como SQS, RabbitMQ e Service Bus, refere-se à restrição do número de 
mensagens que um consumidor pode processar em um determinado intervalo de tempo. Essa limitação é importante para 
evitar sobrecarga nos sistemas, garantir estabilidade, controlar custos (especialmente em serviços cloud) e proteger 
recursos contra picos inesperados de tráfego. Ao aplicar rate limiting, você define quantas mensagens podem ser lidas 
ou processadas por segundo, minuto ou hora, permitindo que o sistema mantenha desempenho consistente e evite falhas 
por excesso de processamento. Em integrações Java Spring Boot, o rate limit pode ser implementado via configurações 
do cliente, middlewares ou bibliotecas específicas, ajustando o consumo conforme a capacidade do serviço e as 
necessidades do negócio.

# MongoDB

MongoDB é um banco de dados NoSQL orientado a documentos, ideal para armazenar dados flexíveis e escaláveis. Em 
projetos Java Spring Boot, a integração é simples usando o `spring-boot-starter-data-mongodb`. Basta adicionar a 
dependência no Maven, configurar a conexão no `application.properties` e criar suas classes de domínio e repositórios. 
O Spring Boot gerencia automaticamente as operações de CRUD, facilitando o uso do MongoDB na aplicação.

# Redis

Redis é um banco de dados NoSQL em memória, amplamente utilizado para armazenamento de dados temporários, cache, filas 
e gerenciamento de sessões. Sua alta performance e suporte a estruturas de dados como strings, hashes, listas e 
conjuntos tornam o Redis ideal para aplicações Java Spring Boot que exigem rapidez e escalabilidade. A integração pode 
ser feita facilmente adicionando a dependência `spring-boot-starter-data-redis` ao projeto e configurando a conexão 
no `application.properties`.

# Kafka

Kafka é uma plataforma distribuída de streaming de eventos, amplamente utilizada para processamento, armazenamento e 
transmissão de grandes volumes de dados em tempo real. Em projetos Java Spring Boot, a integração com Kafka é 
facilitada pelo `spring-kafka`, permitindo a criação de produtores e consumidores de mensagens de forma simples e 
eficiente. Entre as principais vantagens do Kafka em relação a outros sistemas de filas estão a alta escalabilidade, 
tolerância a falhas, persistência de mensagens, capacidade de processamento paralelo e suporte nativo a múltiplos 
consumidores. Essas características tornam o Kafka ideal para arquiteturas modernas, como microsserviços e sistemas 
orientados a eventos, garantindo desempenho, confiabilidade e flexibilidade superiores em cenários de alto tráfego e 
integração entre serviços.

# Service Bus

O Azure Service Bus é um serviço de mensageria totalmente gerenciado na nuvem da Microsoft, projetado para facilitar 
a comunicação assíncrona e desacoplada entre aplicações e serviços distribuídos. Ele suporta filas e tópicos 
(publish/subscribe), garantindo entrega confiável de mensagens, ordenação e escalabilidade. Em projetos 
Java Spring Boot, a integração com o Service Bus permite construir soluções robustas para processamento de eventos, 
orquestração de microsserviços e integração entre sistemas locais e em nuvem, utilizando bibliotecas como o 
Azure SDK para Java.

# RabbitMQ

RabbitMQ é um sistema de mensageria open source baseado no protocolo AMQP, amplamente utilizado para comunicação 
assíncrona entre serviços e aplicações. Ele permite o envio, recebimento e roteamento de mensagens de forma confiável, 
facilitando a integração entre sistemas distribuídos e desacoplados.

**Vantagens:**
- Suporte a múltiplos protocolos (AMQP, MQTT, STOMP)
- Alta disponibilidade e tolerância a falhas com clustering
- Roteamento flexível de mensagens por meio de exchanges e filas
- Facilidade de monitoramento e gerenciamento via interface web
- Integração nativa com diversos frameworks e linguagens

**Desvantagens:**
- Pode exigir configuração e manutenção mais complexa em ambientes grandes
- Overhead de rede e processamento em cenários de altíssimo volume
- Persistência de mensagens pode impactar a performance

**Implementação em projetos Java Spring Boot:**
A integração é simples utilizando o starter `spring-boot-starter-amqp`. Basta adicionar a dependência no `pom.xml`, 
configurar as propriedades de conexão no `application.properties` e criar beans para produtores e consumidores de 
mensagens. O Spring Boot abstrai grande parte da complexidade, permitindo o envio e recebimento de mensagens com 
poucas linhas de código, além de suportar recursos avançados como listeners, conversores e tratamento de erros.

# SQS

SQS (Simple Queue Service) é um serviço gerenciado de filas da AWS que permite a comunicação assíncrona entre sistemas 
por meio do envio e recebimento de mensagens. Ele é ideal para desacoplar componentes, garantir escalabilidade e 
aumentar a resiliência das aplicações. Em projetos Java Spring Boot, o uso do SQS facilita o processamento distribuído 
de tarefas, o controle de picos de demanda e a integração com outros serviços cloud. A integração pode ser feita 
utilizando bibliotecas como o AWS SDK ou starters específicos, permitindo que produtores e consumidores de mensagens 
sejam implementados de forma simples e eficiente.

