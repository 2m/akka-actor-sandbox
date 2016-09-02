import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.duration._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.actor.Props
import akka.actor.ActorSystem

object Supervision extends App {

  class NoiseMaker extends Actor with ActorLogging {

    import context.dispatcher

    val ticker = context.system.scheduler.schedule(0.seconds, 1.second, self, "tick")
    val hazard = context.system.scheduler.scheduleOnce(10.seconds, self, "boom")

    def receive = {
      case "boom" => throw new IllegalStateException("Lets go restarting!")
      case msg    => log.info(s"[${self.path.toSerializationFormat}] Received $msg")
    }

    override def postStop() = {
      //ticker.cancel()
      //hazard.cancel()
    }
  }

  class Parent extends Actor {

    val child = context.actorOf(Props(new NoiseMaker()))

    import context.dispatcher
    context.system.scheduler.schedule(0.seconds, 1.second, child, "tick from parent")

    override val supervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: IllegalStateException => Restart
      }

    def receive = {
      case _ =>
    }
  }

  class GrandParent extends Actor {

    context.actorOf(Props(new Parent()))

    override val supervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: IllegalStateException => Restart
      }

    def receive = {
      case _ =>
    }
  }

  val sys = ActorSystem()
  val act = sys.actorOf(Props(new GrandParent()))

}
