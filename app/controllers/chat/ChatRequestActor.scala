package controllers.chat

import akka.actor.{ Actor, ActorRef, Props }
import domains.chat.{ Join, Leave, Talk }

/**
 * Convert input String to chat Message object.
 */
class ChatRequestActor(out: ActorRef, userName: String, roomId: String) extends Actor {

  override def receive: Receive = {
    case msg: String =>
      out ! Talk(userName, msg)
  }


  override def preStart(): Unit = out ! Join(userName)

  override def postStop(): Unit = out ! Leave(userName)

}

object ChatRequestActor {
  def props(out: ActorRef, userName: String, roomId: String): Props = Props(new ChatRequestActor(out, userName, roomId))
}
