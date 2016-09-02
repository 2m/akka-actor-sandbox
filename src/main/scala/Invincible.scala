import akka.actor._

class Invincible extends Actor with ActorLogging {

  override def preStart() = {
    log.info("prestart")
  }

  override def receive = {
    case m: PoisonPill => log.info("Not this time")
  }

  override def postStop() = {
    log.info("poststop")
  }
}

object Invincible extends App {
  val sys = ActorSystem()
  val act = sys.actorOf(Props(new Invincible))
  act ! PoisonPill
}
