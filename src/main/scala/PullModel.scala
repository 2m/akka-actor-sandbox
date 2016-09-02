import akka.actor._
import scala.concurrent.Future
import scala.collection.mutable
import scala.concurrent.Promise
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.typesafe.config.ConfigFactory
import akka.event.Logging

case class WorkIsDone(worker: ActorRef)
case class Conf(path: String, uri: String)
case class RegisterWorker(worker: ActorRef)
case class GiveMeWork(worker: ActorRef)
case class DoThisWork[T](work: T)
case object WorkAvailable
case object Done

class Master[Work](work: Seq[Work], completion: Promise[Done.type])
    extends Actor
    with ActorLogging {
  import scala.collection.mutable.Queue

  var workers: Map[ActorRef, Option[Work]] = Map.empty
  val workQ: Queue[Work]                   = work.to[Queue]

  def receive = {
    case RegisterWorker(worker) =>
      log.info(s"Adding a new worker: ${worker}")
      workers += (worker -> None)

    case GiveMeWork(worker) =>
      log.debug(s"Came to take work... ${worker}")
      if (workQ.nonEmpty) {
        if (workers.contains(worker)) {
          val work = workQ.dequeue()
          workers += (worker -> Some(work))
          worker ! DoThisWork(work)
        } else {
          log.debug(s"Invalid worker ${worker} . You need to register")
        }
      } else {
        log.debug("No work left")
      }

    case WorkIsDone(worker) =>
      if (workers.contains(worker)) {
        workers += (worker -> None)
        if (workQ.nonEmpty) {
          worker ! WorkAvailable
        } else {
          val stillWorking = workers.forall {
            case (_, work: Option[Work]) => work.isDefined
          }
          if (!stillWorking) {
            log.info("No work and all workers are no longer working. Signalling that we are done.")
            completion.trySuccess(Done)
          } else {
            log.info("No work left, but there are still some workers working.")
          }
        }
        log.debug("Updated worker infoo")
      } else {
        log.debug(s"Unregistered worker ${worker}")
      }
  }
}

class Worker[Work](master: ActorRef) extends Actor with ActorLogging {
  implicit val ec = context.dispatcher

  override def preStart = {
    master ! RegisterWorker(self)
    master ! GiveMeWork(self)
  }

  def receive = {
    case WorkAvailable =>
      log.debug("Requesting more work")
      master ! GiveMeWork(self)

    case DoThisWork(work: Work) =>
      doWork(work) onComplete {
        case _ =>
          log.debug("Completed a future")
          master ! WorkIsDone(self)
      }
  }
  def doWork(work: Work): Future[Int] = {
    work match {
      case w: String =>
        log.debug(s"File is $w")
        PullModel.work += ((w, true))
      case _ =>
        log.debug("Unknown work")
    }

    Future.successful(200)
  }
}

object PullModel extends App {

  val config = ConfigFactory.parseString("""
    akka.log-level = INFO
  """)

  val system = ActorSystem("pullModel", config)
  val log    = Logging(system, "system")

  val work = mutable.Map[String, Boolean]()
  for (i <- (1 to 1000000)) {
    work += ((s"file-$i", false))
  }

  val completion = Promise[Done.type]
  val master     = system.actorOf(Props(new Master[String](work.keys.toSeq, completion)), "master")

  val workers = 1 to 16 map { _ =>
    system.actorOf(Props(new Worker[Conf](master)))
  }

  import system.dispatcher
  Await.ready(completion.future, Duration.Inf)

  val allDone = work.forall {
    case (_, done) => done
  }

  if (allDone) {
    log.info(s"All ${work.size} files have been processed.")
  } else {
    work.foreach {
      case (work, false) => log.info(s"$work has not been processed.")
      case _             =>
    }
  }

  system.shutdown()
  system.awaitTermination()
}
