import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._

class TempActorSelectionSpec extends WordSpec with Matchers with ScalaFutures {
  import TempActorSelectionSpec._

  implicit val defaultPatience = PatienceConfig(timeout = 2.seconds, interval = 50.millis)

  "actorselection" should {

    "select temp actors" in {

      val sys = ActorSystem("TempActorSelectionSpec")

      implicit val ec = sys.dispatcher
      implicit val ti = Timeout(10.seconds)

      val rep = sys.actorOf(Props[Replier])
      (rep ? Msg()).futureValue shouldBe "Done"
    }
  }

}

object TempActorSelectionSpec {

  case class Msg()

  class Replier extends Actor {
    def receive = {
      case Msg() =>
        val path = "/temp/$a"
        println(sender())
        //println(sender().path.toStringWithoutAddress == path)
        context.system.actorSelection("temp/$a") ! "Done"
    }
  }

}
