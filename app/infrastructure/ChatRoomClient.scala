package infrastructure

import javax.inject.Inject

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.util.Timeout
import domains.ChatRoomRepository

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._

class ChatRoomClient @Inject()(
    system: ActorSystem
) extends ChatRoomRepository {

  implicit val timeout = Timeout(1 second)

  override def chatRoom(roomId: String): Future[ActorRef] = {
    val future = system.actorSelection("/user/" + roomId).resolveOne()
    future recover {
      case _ => system.actorOf(Props(classOf[ChatRoom], roomId), roomId)
    }
  }

}
