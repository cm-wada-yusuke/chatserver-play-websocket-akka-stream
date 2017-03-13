package controllers.chat

import akka.actor.{ Actor, ActorRef, Props }
import controllers.chat.RequestConverter._
import domains.chat.{ Join, Leave, Talk }

/**
 * Convert input String to chat Message object.
 */
class ChatRequestConvertActor(out: ActorRef, userName: String, roomId: String) extends Actor {

  override def receive: Receive = {
    case msg: String =>
      out ! toTalk(userName, msg)
  }


  override def preStart(): Unit = out ! Join(userName)

  override def postStop(): Unit = out ! toLeave(userName)

}

object ChatRequestConvertActor {
  def props(out: ActorRef, userName: String, roomId: String): Props = Props(new ChatRequestConvertActor(out, userName, roomId))
}


object RequestConverter {
  def toTalk(userName: String, msg: String): Talk = Talk(userName, msg)

  def toLeave(userName: String): Leave = Leave(userName)
}