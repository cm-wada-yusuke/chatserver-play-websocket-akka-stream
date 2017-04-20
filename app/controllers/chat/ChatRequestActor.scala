package controllers.chat

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import domains.chat.{ Chat, Join, Leave, Talk }
import play.api.libs.json.JsValue

/**
 * Convert input String to chat Message object.
 */
class ChatRequestActor(out: ActorRef, userName: String) extends Actor {

  import ChatMessageConverters._

  override def receive: Receive = {
    case msg: JsValue =>
      val chat = msg.as[Chat]
      out ! Talk(chat.userName, chat.text)
  }

  override def preStart(): Unit = out ! Join(userName)

  override def postStop(): Unit = {
    out ! Leave(userName)
    out ! PoisonPill
  }
}

object ChatRequestActor {
  def props(out: ActorRef, userName: String): Props = Props(new ChatRequestActor(out, userName))
}
