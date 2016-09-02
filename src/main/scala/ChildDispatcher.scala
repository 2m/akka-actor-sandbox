package akka

import scala.io.StdIn

import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorCell
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.RepointableActorRef
import akka.actor.actorRef2Scala

object ChildDispatcher extends App {

  class Child extends Actor with ActorLogging {
    def receive = {
      case _ => log.info(s"Quack! on dispatcher ${context.props.dispatcher}")
    }
  }

  class Parent extends Actor {

    def receive = {
      case _ =>
        val child1 = Props(new Child).withDispatcher("my-dispatcher")
        context.actorOf(child1, "child1") ! "Whadoasay?"

        val child2 = Props(new Child).withDispatcher("my-dispatcher")
        self
          .asInstanceOf[RepointableActorRef]
          .underlying
          .asInstanceOf[ActorCell]
          .attachChild(child2, "child2", systemService = false) ! "Whadoasay?"

        val child3 = Props(new Child)
        context.actorOf(child3, "child3") ! "Whadoasay?"

        println(child3.dispatcher)

        val child4 = Props(new Child)
        context.actorOf(child4, "child4") ! "Whadoasay?"

    }
  }

  val config = ConfigFactory.parseString("""
    my-dispatcher = ${akka.actor.default-dispatcher}

    akka.actor.deployment {
      /parent/child3 {
        dispatcher = my-dispatcher
      }
    }

  """)

  val sys = ActorSystem("ChildDispatcher", config.withFallback(ConfigFactory.load).resolve)
  sys.actorOf(Props[Parent], "parent") ! "What do your children say?"

  StdIn.readLine()

  sys.shutdown()
  sys.awaitTermination()

}
