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

Enter `./goto step2` in a git bash shell to go to step 2.
