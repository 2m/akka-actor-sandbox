import akka.actor._
import scala.io.StdIn
import com.typesafe.config.ConfigFactory

object SlowChildren extends App {

  class Child(constDelay: Int, preStartDelay: Int) extends Actor {

    Thread.sleep(constDelay)

    override def preStart() = {
      Thread.sleep(preStartDelay)
    }

    def receive = {
      case msg => println(msg)
    }
  }

  class Parent extends Actor {

    def receive = {
      case "create_children" =>
        println("Creating 'classOf props, sleep in constructor'")
        context.actorOf(Props(classOf[Child], 5000, 0)) ! "classOf props, sleep in constructor"

        println("Creating 'constructor props, sleep in constructor'")
        context.actorOf(Props(new Child(5000, 0))) ! "constructor props, sleep in constructor"

        println("Creating 'classOf props, sleep in prestart'")
        context.actorOf(Props(classOf[Child], 0, 5000)) ! "classOf props, sleep in prestart"

        println("Creating 'constructor props, sleep in prestart'")
        context.actorOf(Props(new Child(0, 5000))) ! "constructor props, sleep in prestart"
    }
  }

  val conf = ConfigFactory.parseString("""
    akka.actor.creation-timeout = 2s
  """).withFallback(ConfigFactory.load)
  val sys  = ActorSystem("SlowChildren", conf)
  sys.actorOf(Props[Parent]) ! "create_children"

  StdIn.readLine()

  sys.shutdown()
  sys.awaitTermination()
}
