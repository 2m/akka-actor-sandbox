import akka.actor._

object SimpleActor extends App {

  class Printer extends Actor {
    var counter = 0
    override def receive = {
      case _ =>
        counter += 1
        if (counter % 50000000 == 0)
          println(counter)
    }
  }

  val sys = ActorSystem("SimpleActor")
  val act = sys.actorOf(Props[Printer])
  while (true) act ! "tick"
}
