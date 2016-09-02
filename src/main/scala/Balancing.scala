import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.routing._

object Balancing extends App {

  class Parent extends Actor with ActorLogging {
    val router = context.actorOf(FromConfig.props(Props[Worker]), "router")

    def receive = {
      case Parent.Work(count) =>
        for (i <- 1 to count)
          router ! Parent.Task(i, System.currentTimeMillis)
      case Parent.Task(id, ts) =>
        log.info(s"Task $id took ${System.currentTimeMillis - ts}ms")
    }
  }

  object Parent {
    case class Work(count: Int)
    case class Task(id: Int, start: Long)
  }

  class Worker extends Actor with ActorLogging {
    def receive = {
      case t @ Parent.Task(id, ts) =>
        //val sleep = if (self.path.name.contains("a")) 100 else 1000
        val sleep = 2000
        log.info(s"Got $id. Will sleep $sleep")
        Thread.sleep(sleep)
        //spinFor(sleep)
        log.info(s"Done sleeping")
        sender() ! t
    }

    private def spinFor(ms: Int) = {
      val start = System.currentTimeMillis
      var i     = 0
      while (System.currentTimeMillis - start < ms) {
        // spin
        i = i + 1
      }
    }
  }

  val config = ConfigFactory.parseString("""
    akka.actor.deployment {
      /parent/router {
        router = round-robin-pool
        nr-of-instances = 16
        pool-dispatcher {
          executor = "thread-pool-executor"

          thread-pool-executor {
            core-pool-size-min = 16
            core-pool-size-max = 16
          }
        }
      }
    }
  """)

  val sys    = ActorSystem("Balancing", config)
  val parent = sys.actorOf(Props[Parent], "parent")
  //parent ! Parent.Work(16)

  @annotation.tailrec
  def repl(logic: String => Option[String]): Unit = logic(io.StdIn.readLine) match {
    case None      => // exit
    case Some(msg) => println(msg); repl(logic)
  }

  repl {
    case "q" => None
    case msg =>
      parent ! Parent.Work(msg.toInt)
      Some(s"Sending $msg tasks to workers.")
  }

  sys.shutdown()
  sys.awaitTermination()
}
