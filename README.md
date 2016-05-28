# Spring Cloud demo in Scala

## Introduction

This is a step-by-step demo of how to use [Spring Cloud](http://projects.spring.io/spring-cloud/) to build microservices.
The demo shows the basics of how to use Spring Cloud's configuration service and some of the [Netflix](https://github.com/Netflix) microservices components that are supported by Spring Cloud: Eureka, Ribbon, Feign and Hystrix.

This demo is not more than a basic introduction to give you an idea of how you can use Spring Cloud to build microservices.
There are many more features and components that aren't shown here.

The source code is written in Scala.

The git repository for this demo contains a number of steps, that are tagged with git tags.
After cloning this repository, use the command `./goto step1` from a git bash shell to start with the first step.

Scripts are provided to build and run the services, but you can also load the project in an IDE and setup the run configuration so that you can run the services from your IDE.

## Step 1: Whiteboard Service

In the first step we will create a simple REST webservice using [Spring Boot](http://projects.spring.io/spring-boot/).

Creating a REST webservice with Spring Boot is very easy.
You can use the [Spring Initializr](http://start.spring.io/) to generate a Spring Boot project with the dependencies that you need.

For this demo we will create a simple service that allows users to write notes on a whiteboard.
Our whiteboard service is a Spring Boot application with [Spring Data JPA](http://projects.spring.io/spring-data-jpa/), [Spring Data REST](http://projects.spring.io/spring-data-rest/) and [Spring HATEOAS](http://projects.spring.io/spring-hateoas/).

Since we want to use Scala for this project, we need to add some things to the `pom.xml`:

* a dependency on the Scala library (`org.scala-lang:scala-library`)
* the build helper plugin (`org.codehaus.mojo:build-helper-maven-plugin`), which is used to add the `src/main/scala` and `src/test/scala` directories as source directories to the project
* the Scala plugin (`net.alchim31.maven:scala-maven-plugin`)

The Spring Boot application consists of a class `WhiteboardServiceApplication` and a companion object.
The class is annotated with the `@SpringBootApplication` annotation, and the companion object contains what we'd normally put in the `main()` method of a Spring Boot application if this were Java.

There is also a JPA entity `Note` and a corresponding Spring Data JPA repository, `NoteRepository`.

JPA is very much based on the Java way of doing things, and therefore there are a few things that are ugly when you use Scala instead of Java.

First of all, class `Note` is not at all idiomatic Scala - we are really just writing Java code with Scala syntax.
If this would have been idiomatic Scala, we would have made this a one-line immutable case class:

    case class Note(id: Long, createdDateTime: Date, authorName: String, content: String)

Second, the type of the `id` member has to be `java.lang.Long` (which we've imported here as `JavaLong`).
(Making it a `scala.Long` leads to an error message about serialization later on).

Finally, we have to use the `@BeanProperty` annotation on the members to generate JavaBeans-compatible getter and setter methods, because that is what JPA expects.

The repository is simple.
In Java this would be an interface, in Scala it's a trait.

We can now run the application.
The embedded Tomcat that Spring Boot starts up by default listens on port 8080, so we can go to [http://localhost:8080](http://localhost:8080) and see that our REST webservice is running, with HATEOAS / HAL support.

Note that there's a file `data.sql` in the `src/main/resources` directory which inserts some test data into the database - this will be picked up and executed automatically at startup by Spring Boot.
We're using an embedded, in-memory database ([H2](http://h2database.com)) for this demo.

## Step 2: Configuration Service

When you have a system that consists of many microservices that run across many servers, you'll want to centralize configuration.
It would be very cumbersome to manage the configuration of each microservice instance on each node where it is deployed.
Spring Cloud has a configuration service that very nicely fits in with the configuration mechanism of Spring Boot and the Spring Framework in general.

### Creating the configuration service

What we will do in this step is create another microservice - the configuration service.
The other services, such as the whiteboard service that we created, will get their configuration from the configuration service instead of from their own local `application.properties` file.

We add a module `config-service` to the project, which is another very plain and simple Spring Boot application.
This is the first module that will actually use components from Spring Cloud.
In the `pom.xml` we have to import the Spring Cloud dependencies POM in the `dependencyManagement` section, and then we add a dependency to `org.springframework.cloud:spring-cloud-config-server`.

To enable the configuration server, we add the `@EnableConfigServer` annotation to the application class.

We'll also need to set a few properties in `application.properties`.

First, we'll set the server port to 8888, because configuration clients will by default search for the configuration service on `localhost:8888`.

The configuration service by default gets the configuration properties that it serves from files in a git repository.
We have to set `spring.cloud.config.server.git.uri` to the URI of the git repository that will contain the configuration files.

### Making the whiteboard service a client of the configuration service

Now were are going to make some modifications to the whiteboard service so that it gets its configuration from the configuration service instead of from its own `application.properties` file.

You'll see that this is very easy and that we don't need to modify the source code.
The configuration server will act as just another source of configuration properties via Spring's property source mechanism.

We add a dependency on `org.springframework.cloud:spring-cloud-starter-config` to the whiteboard service.
Spring Boot's auto-configuration mechanism will then automatically configure the application so that it calls the configuration service to get configuration properties.

We must rename the `application.properties` of the whiteboard service to `bootstrap.properties` and add a property `spring.application.name` there.
The file `bootstrap.properties` is picked up early in the configuration process, and the application name is what the configuration service uses to find the configuration for this service.

We set `spring.application.name` to `whiteboard-service`, and then we make sure that there is a file `whiteboard-service.properties` in the configuration file git repository.

We set `server.port` in `whiteboard-service.properties` to 9090, so that we can see that if we start the config service and the whiteboard service, everything works.

Now we start the configuration service and the whiteboard service.
You'll see that the whiteboard service now gets its configuration from the config service and that it's running on port 9090 instead of the default port 8080.

## Step 3: Discovery Service

If you have a system with lots of microservices distributed across many servers, you'll want to have a [service registry](http://microservices.io/patterns/service-registry.html) that keeps track of where all the services are in the system.
If a service needs to collaborate with another service, it will look it up in the service registry so that it knows where to send its request to.

Different implementations of service registries already exist.
Spring Cloud currently has support for [Consul](https://www.consul.io/), [Zookeeper](http://zookeeper.apache.org/) and [Eureka](https://github.com/Netflix/eureka).

In this demo we will use Eureka, which is one of the open source microservices components from Netflix.

### Creating the discovery service

The discovery service will be another microservice, and creating it is just as simple as creating th configuration service.

We add a module `discovery-service` to the project, which is again a very simple Spring Boot application.

To make this the discovery service, we add a dependency on `org.springframework.cloud:spring-cloud-starter-eureka-server` and then we enable the Eureka server by adding the `@EnableEurekaServer` annotation to the application class.

The discovery service is, just like the whiteboard service, a client of the configuration service.
So, just like with the whiteboard service, we set `spring.application.name` in `bootstrap.properties` and make sure that there is a corresponding configuration file in the config file git repository.

There, we make the discovery service run on port 8761, again because clients by default look at `localhost:8761` for the discovery service.
We also set a few other Eureka configuration parameters.

Now we start the config service and the discovery service.
When they are running, you can go to [http://localhost:8761](http://localhost:8761) where you'll see the status of the Eureka server.
Currently there are no services registered.
We need to make the whiteboard service a discovery client, so that it will register itself with Eureka when it starts.

### Making the whiteboard service a client of the discovery service

To make the whiteboard service register itself with Eureka, we add a dependency on `org.springframework.cloud:spring-cloud-starter-eureka` to the whiteboard service and we add the `@EnableDiscoveryClient` annotation to the application class.

Now we start the whiteboard service.
When it has started we can again go to [http://localhost:8761](http://localhost:8761) and we'll see that the whiteboard service is registered in Eureka.

## Step 4: Whiteboard Client

In this step we will create a simple Spring Boot webapp which is going to lookup the whiteboard service using the discovery service.

We add another Spring Boot application, `whiteboard-client`.
This is a webapp that uses the Thymeleaf template engine.

This webapp is a client of the discovery service, so it has a dependency on `org.springframework.cloud:spring-cloud-starter-eureka` and its application class is annotated with `@EnableDiscoveryClient`.

We don't want the client to register in Eureka, so we set `eureka.client.registerWithEureka` to `false` in `application.properties`.
(Note that the client is not a client of the configuration service, so we configure it the normal Spring Boot way, using `application.properties`).

The controller, `WhiteboardClientController`, shows how we use the `DiscoveryClient` to lookup instances of the whiteboard service, which we can then call to get the list of notes or to add a new note.
We use a Spring `RestTemplate` to make REST calls to the whiteboard service.

Start the config service, the discovery service, the whiteboard service and the whiteboard client.
We can now go to [http://localhost:8080](http://localhost:8080) and use the client to see the content of the whiteboard and to add new notes.

Before we move on, there are a few things to remark.

First of all, using annotations in Scala is a bit ugly in some ways.
Constructor injection looks a bit weird, with the `@Autowired()` annotation with parentheses before the constructor argument list.
Also, the `path` and other parameters of the `@RequestMapping` annotation are actually arrays, and in Scala we must explicitly pass an array - in Java, you can either pass an array or a single parameter.

Second, we didn't think about transactions in this simple example.
Note that there is no distributed transaction that encompasses everything that happens in a POST request to the `/add` endpoint.
We could set things up to use distributed transactions, but that makes the system [complex and hard to scale](http://ivoroshilin.com/2014/03/18/distributed-transactions-and-scalability-issues-in-large-scale-distributed-systems/).
Your system will be more scalable and more resilient if you go for eventual consistency instead.
Instead of directly doing a POST from the whiteboard client to the whiteboard service you could put the new note on a message queue.
The whiteboard service can then pick up new notes from that queue and do writes asynchronously.

## Step 5: Client-side load balancing with Ribbon

In this step we are going to use Ribbon, another microservices component by Netflix.
Ribbon is a library that for inter-process communication using REST calls with client-side load balancing.
It also works together with other Netflix components such as Eureka and Hystrix (which we'll see later).

To use Ribbon, we put a `@LoadBalanced` annotation on the `RestTemplate` bean factory method in the application class of the whiteboard client.
This will make Spring Cloud wire up the `RestTemplate` so that it uses Ribbon.

There are also some changes to the controller.
Ribbon automatically uses Eureka to find the location of the actual service instance that we want to call, so we don't have to use the `DiscoveryClient` anymore to lookup the service instance.
Instead, we replace the hostname in the URI with the name of the service, so in this case we are going to use the URI: http://whiteboard-service/notes

Ribbon will lookup instances of the service using Eureka and use a load balancing algorithm to pick one of the instances to send the request to.
(In this demo, there's only a single instance of the whiteboard service).

## Step 6: Declarative REST service with Feign

In this step we are going to use another Netflix component: Feign.
This is a library that makes writing clients for REST webservices very easy.

The idea that Feign implements is the same as in Spring Data JPA.
We write an interface (Java) or a trait (Scala) that declares the methods that are available on the REST webservice that we want to call and then Feign will automatically generate an implementation that calls the webservice.
We don't have to deal with the low-level details of creating an HTTP request with JSON and parsing the HTTP response anymore, which makes it super-easy to create clients for REST webservices.

First, we add a dependency to `org.springframework.cloud:spring-cloud-starter-feign` to the whiteboard client.

Then we create a trait `WhiteboardClient` which contains two method declarations with a `@RequestMapping` annotation, which point to the methods of the whiteboard service.
The `WhiteboardClient` trait has a `@FeignClient` annotation with the name of the service that we want to call.
Feign uses Ribbon, which uses Eureka to lookup a service instance to call, so this name is the name under which the service is registered in Eureka.

We also need to make some changes to the application class.

We don't need the `RestTemplate` anymore, so we remove that.

We add an `@EnableFeignClients` annotation so that Spring is going to look for the `@FeignClient` traits and generate implementations.

To make it work correctly with our HATEOAS / HAL webservice, we need to explicitly enable Spring HATEOAS by adding an `@EnableHypermediaSupport` annotation (without this, the Feign client will not parse the response from the webservice correctly).

The controller now becomes very simple.
The `index` and `add` methods now only have to call the methods in the `WhiteboardClient`.

## Step 7: Circuit breaker with Hystrix

One of the design patterns that is often used in microservices systems to make the system more robust is the [circuit breaker pattern](http://martinfowler.com/bliki/CircuitBreaker.html).
You use this pattern to avoid cascading failures.

The basic idea is that you wrap calls to services in a circuit breaker object, which monitors failures.
When the number of failures reaches a certain threshold, the circuit breaker opens and further calls to the service immediately return with an error (no remote call is made).
After certain criteria have been met (for example, after a period of time, and when the service is up again) the circuit breaker closes again and things return to normal.

The goal is to prevent cascading failures - when you have a chain of services, you want to catch failures at the beginning of the chain to prevent a storm of failures in the network.

### Adding Hystrix to the whiteboard client

In this step we will use Hystrix, another Netflix component, to use the circuit breaker pattern.

First, we add a dependency to `org.springframework.cloud:spring-cloud-starter-hystrix` to the whiteboard client.

Hystrix allows you to define a fallback method that should be called when a service call fails.
We are going to configure this for our `WhiteboardClient`.
We do this by specifying the `fallback` attribute on Spring Cloud's `@FeignClient` annotation that's on the `WhiteboardClient` trait.
That attribute must point to a Spring bean that implements the Feign client trait, so we add a class `WhiteboardClientFallback` which extends trait `WhiteboardClient`.
The implementation of the fallback is very simple for this demo; the `getAllNotes` method returns a collection with a single `Note` that says that the whiteboard is not available, and the `addNote` method does nothing (so, writes are lost if the whiteboard service is not available!).

We add the `@EnableCircuitBreaker` annotation to the application class to enable the circuit breaker.

Now we can start everything (config service, discovery service, whiteboard service, whiteboard client).

Hystrix provides an interface to monitor what's happening with the circuit breaker.
We can go to [http://localhost:8080/hystrix.stream](http://localhost:8080/hystrix.stream) and see that Hystrix is emitting information.

### Using the Hystrix dashboard

To make monitoring the circuit breaker more convenient, we'll use the Hystrix dashboard.

We add another small Spring Boot application to the project, `hystrix-dashboard`.
Like the other services, it's a very simple Spring Boot application.
It has a dependency on `org.springframework.cloud:spring-cloud-starter-hystrix-dashboard` and the `@EnableHystrixDashboard` annotation on its application class.
It's configured to run on port 8081 in its `application.properties`.

We can now start the Hystrix dashboard and go to [http://localhost:8081/hystrix](http://localhost:8081/hystrix).
We enter the URL of the Hystrix stream that we want to monitor: http://localhost:8080/hystrix.stream

When we refresh the dashboard at http://localhost:8080 a few times we can see in the dashboard that the requests are being monitored by Hystrix.
Everything is working and the circuit is closed.

Now, we can see what happens if a failure occurs.
Stop the whiteboard service, and refresh the whiteboard in the browser.
You'll see that the fallback message appears.

When you look at the Hystrix dashboard, you'll see that there was a failure, but the circuit is still closed.
The circuit doesn't break after just one failure.
In this demo, it's configured to open if more than 10 requests fail in a window of 3 seconds.

If we go to the whiteboard client again and quickly hit refresh (F5) a number of times, we'll see in the Hystrix dasboard that eventually the circuit opens.
When the circuit is open, Hystrix won't try to call the service for a period of time if a new request is made; it will immediately call the fallback.

If you wait a while and do another request, it will try to call the service again.
If the service is still unavailable, the circuit stays open and further requests will again immediately fail for a certain time period.
If the service is available again, the circuit closes and things will return to normal.

Now we start the whiteboard service again (and wait a while until it's fully started and has registered itself, and until the whiteboard client has refreshed its cache from Eureka).
If you now refresh the whiteboard, the call to the service will succeed and the circuit will close.

Enter `./goto step1` in a git bash shell to go back to step 1.
