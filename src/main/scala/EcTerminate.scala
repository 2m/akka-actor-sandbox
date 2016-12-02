import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object EcTerminate {
  def main(args: Array[String]): Unit = {
    val executor    = Executors.newSingleThreadExecutor()
    val executionEC = ExecutionContext.fromExecutor(executor)

    val system = ActorSystem("system",
                             ConfigFactory.parseString("""
        akka.loglevel = DEBUG
        akka.stdout-loglevel = DEBUG""").withFallback(ConfigFactory.load()))
    val systemEC = system.dispatcher

    scala.io.StdIn.readLine

    val longRunning = Future(Thread.sleep(5000))(executionEC)
    longRunning.onComplete { _ =>
      println(s"something ${Thread.currentThread.getName}");
      //executor.shutdown
      system.terminate
    }(systemEC)
    systemEC.prepare()

    executor.shutdown()
    system.terminate().onComplete(_ => println("actor sys shut down"))(systemEC)
    println("after terminate")
  }
}
