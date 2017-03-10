package domains

import akka.actor.ActorRef

import scala.concurrent.Future

trait ChatRoomRepository {

  def chatRoom(roomId: String): Future[ActorRef]

}
