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
      case _ => log.info(s"Quack!")
    }
  }
  
  class Parent extends Actor {
    
    def receive = {
      case _ =>
        val props = Props(new Child).withDispatcher("my-dispatcher")
        context.actorOf(props, "child1") ! "Whadoasay?"
        self.asInstanceOf[RepointableActorRef].underlying.asInstanceOf[ActorCell].attachChild(props, "child2", systemService = false) ! "Whadoasay?"
    }
  }
  
  val config = ConfigFactory.parseString("""
    my-dispatcher = ${akka.actor.default-dispatcher}
  """)
  
  val sys = ActorSystem("ChildDispatcher", config.withFallback(ConfigFactory.load).resolve)
  sys.actorOf(Props[Parent]) ! "What do your children say?"
  
  StdIn.readLine()
  
  sys.shutdown()
  sys.awaitTermination()

}
