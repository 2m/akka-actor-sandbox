import org.scalatest.WordSpec
import akka.actor.PoisonPill
import akka.testkit.TestProbe
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem

class ExpectPoisonPill extends WordSpec {
  import ExpectPoisonPill._

  "a poison pill" should {
    "be expected" in {
      implicit val system = ActorSystem("ExpectPoisonPill")
      val a               = system.actorOf(Props[Echo])

      val deathwatch = TestProbe()
      deathwatch watch a
      //a ! PoisonPill

      deathwatch.expectTerminated(a) // <== Fails
    }
  }

}

object ExpectPoisonPill {
  class Echo extends Actor {
    def receive = Actor.emptyBehavior
  }
}
