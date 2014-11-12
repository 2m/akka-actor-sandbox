import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

/**
 * Run with `sbt "runMain LifecycleTrait"`
 * 
 * Prints (may have some interleaving between actors):
 * 
 * Actor[akka://LifecycleTrait/user/$a#1740200879]: UsefulTrait.preStart()
 * Actor[akka://LifecycleTrait/user/$a#1740200879]: MyActor.preStart()
 * Actor[akka://LifecycleTrait/user/$b#2136852580]: EvenMoreUsefulTrait.preStart()
 * Actor[akka://LifecycleTrait/user/$b#2136852580]: UsefulTrait.preStart()
 * Actor[akka://LifecycleTrait/user/$b#2136852580]: MyAnotherActor.preStart()
 */
object LifecycleTrait extends App {
  
  trait PreStartAction { def preStart(): Unit }

  trait UsefulTrait extends PreStartAction { self: Actor =>  
    abstract override def preStart(): Unit = {
      println(s"${self.self}: UsefulTrait.preStart()")
      super.preStart()
    }  
  }
  
  trait EvenMoreUsefulTrait extends PreStartAction { self: Actor =>  
    abstract override def preStart(): Unit = {
      println(s"${self.self}: EvenMoreUsefulTrait.preStart()")
      super.preStart()
    }  
  }
  
  class MyActor extends Actor with UsefulTrait {
    override def receive = { case _ => }
    
    override def preStart() {
      super.preStart()
      println(s"$self: MyActor.preStart()")
    }
  }
  
  class MyAnotherActor extends Actor with UsefulTrait with EvenMoreUsefulTrait {
    override def receive = { case _ => }
    
    override def preStart() {      
      super.preStart()
      println(s"$self: MyAnotherActor.preStart()")      
    }
  }

  val system = ActorSystem("LifecycleTrait")
  
  system.actorOf(Props(new MyActor))
  system.actorOf(Props(new MyAnotherActor))
  
}