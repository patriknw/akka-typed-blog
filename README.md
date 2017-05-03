# Sample Code for Introducing Akka Typed Blog Posts

Blog Posts and their main classes that can be run with sbt or maven:

* [Akka Typed: Hello World in the new API](http://blog.akka.io/typed/2017/05/05/typed-intro)
  * Java
    * blog.classic.javadsl.HelloWorldApp1
    * blog.typed.javadsl.HelloWorldApp1
    * blog.classic.javadsl.HelloWorldApp2
    * blog.typed.javadsl.HelloWorldApp2
  * Scala
    * blog.classic.scaladsl.HelloWorldApp1
    * blog.typed.scaladsl.HelloWorldApp1
    * blog.classic.scaladsl.HelloWorldApp2
    * blog.typed.scaladsl.HelloWorldApp2
* [Akka Typed: Coexistence](http://blog.akka.io/typed/2017/05/06/typed-coexistence)
  * Java
    * blog.typed.javadsl.CoexistenceApp1
    * blog.typed.javadsl.CoexistenceApp2
  * Scala
    * blog.typed.scaladsl.CoexistenceApp1
    * blog.typed.scaladsl.CoexistenceApp2
* [Akka Typed: Mutable vs. Immutable](http://blog.akka.io/typed/2017/05/08/typed-mutable-vs-immutable) 
  * Java
    * blog.typed.javadsl.MutableRoundRobinApp
    * blog.typed.javadsl.ImmutableRoundRobinApp
  * Scala
    * blog.typed.scaladsl.MutableRoundRobinApp
    * blog.typed.scaladsl.ImmutableRoundRobinApp

## How to Run with sbt

```
sbt run
```

Select the main class to run.
Press ENTER to exit the system

## How to Run with maven

```
mvn exec:java -Dexec.mainClass="blog.classic.javadsl.HelloWorldApp1"
```

Press ENTER to exit the system
 
