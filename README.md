# Sample Code for Introducing Akka Typed Blog Posts

## Akka Typed Version

Use `akka-typed` **`2.5.5`** or later.

## Blog Posts

Blog Posts and their main classes that can be run with sbt or maven:

* **[Akka Typed: Hello World in the new API](https://akka.io/blog/2017/05/05/typed-intro)**
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
* **[Akka Typed: Coexistence](https://akka.io/blog/2017/05/06/typed-coexistence)**
  * Java
    * blog.typed.javadsl.CoexistenceApp1
    * blog.typed.javadsl.CoexistenceApp2
  * Scala
    * blog.typed.scaladsl.CoexistenceApp1
    * blog.typed.scaladsl.CoexistenceApp2
* **[Akka Typed: Mutable vs. Immutable](https://akka.io/blog/2017/05/08/typed-mutable-vs-immutable)**
  * Java
    * blog.typed.javadsl.MutableRoundRobinApp
    * blog.typed.javadsl.ImmutableRoundRobinApp
  * Scala
    * blog.typed.scaladsl.MutableRoundRobinApp
    * blog.typed.scaladsl.ImmutableRoundRobinApp
* **[Akka Typed: Supervision](https://akka.io/blog/2017/05/16/supervision)**
  * Java
    * blog.typed.javadsl.FlakyWorkerApp
  * Scala
    * blog.typed.scaladsl.FlakyWorkerApp
* **[Akka Typed: Signal](https://akka.io/blog/2017/05/19/signals)**
  * Java
    * blog.typed.javadsl.FlakyWorkerApp2
  * Scala
    * blog.typed.scaladsl.FlakyWorkerApp2
* **[Akka Typed: Timers](https://akka.io/blog/2017/05/26/timers)**
  * Java
    * blog.typed.javadsl.BuncherApp
  * Scala
    * blog.typed.scaladsl.BuncherApp    
* **[Akka Typed: New Cluster API](https://akka.io/blog/2017/09/28/typed-cluster)**
  * Local router 
    * blog.typed.cluster.scaladsl.LocalRouterApp
  * Cluster router 
    * blog.typed.cluster.scaladsl.ClusterRouterApp 2551
    * blog.typed.cluster.scaladsl.ClusterRouterApp 2552
    * blog.typed.cluster.scaladsl.ClusterRouterApp 2553

## How to Run with sbt

```
sbt run
```

Select the main class to run.
Press ENTER to exit the system

## How to Run with maven

```
mvn compile exec:java -Dexec.mainClass="blog.classic.javadsl.HelloWorldApp1"
```

Press ENTER to exit the system
 
