package controllers.chat

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import akka.stream.Materializer
import domains.chat.{ ChatRoom, Join, Leave, Talk }

/**
 * Convert input String to chat Message object.
 */
class ChatRequestActor(out: ActorRef, userName: String, room: ChatRoom)(implicit materializer: Materializer) extends Actor {

  override def receive: Receive = {
    case msg: String =>
      out ! Talk(userName, msg)
  }

  override def preStart(): Unit = out ! Join(userName)

  override def postStop(): Unit = {
    out ! Leave(userName)
    out ! PoisonPill
  }

}

object ChatRequestActor {
  def props(out: ActorRef, userName: String, room: ChatRoom)(implicit materializer: Materializer): Props = Props(new ChatRequestActor(out, userName, room))
}
