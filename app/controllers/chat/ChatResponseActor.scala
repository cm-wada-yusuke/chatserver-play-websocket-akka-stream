package controllers.chat


import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import domains.chat.{ Chat, Join, Leave, Talk }
import play.api.libs.json.Json

/**
 * Convert output Message to WebSocket output String.
 */
class ChatResponseActor(out: ActorRef, me: String) extends Actor {

  import ChatMessageConverters._

  /**
   * If you want to serialize to json, define json converter here.
   */
  override def receive: Receive = {
    case Talk(u, t) =>
      out ! Json.toJson(Chat(u, t, false))
    case Join(u) =>
      out ! Json.toJson(Chat(u, "joined.", true))
    case Leave(userName) =>
      out ! Json.toJson(Chat(userName, "left.", true))
      if (userName == me) {
        out ! PoisonPill
        self ! PoisonPill
      }
  }

  override def postStop(): Unit = super.postStop()

}

object ChatResponseActor {
  def props(out: ActorRef, me: String): Props = Props(new ChatResponseActor(out, me))
}
