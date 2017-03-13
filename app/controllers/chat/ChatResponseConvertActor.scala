package controllers.chat


import akka.actor.{ Actor, ActorRef, Props }
import domains.chat.{ Join, Leave, Talk }

/**
 * Convert output Message to WebSocket output String.
 */
class ChatResponseConvertActor(out: ActorRef) extends Actor {


  /**
   * If you want to serialize to json, define json converter here.
   */
  override def receive: Receive = {
    case Talk(userName, msg) =>
      out ! s"$userName : $msg"
    case Join(userName) =>
      out ! s"$userName : joined."
    case Leave(userName) =>
      out ! s"$userName left."
  }

  override def postStop(): Unit = super.postStop()

}

object ChatResponseConvertActor {
  def props(out: ActorRef): Props = Props(new ChatResponseConvertActor(out))
}
