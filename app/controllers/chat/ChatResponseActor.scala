package controllers.chat


import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import domains.chat.{ Join, Leave, Talk }

/**
 * Convert output Message to WebSocket output String.
 */
class ChatResponseActor(out: ActorRef, me: String) extends Actor {


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
      if(userName == me) {
        out ! PoisonPill
        self ! PoisonPill
      }
  }

  override def postStop(): Unit = super.postStop()

}

object ChatResponseActor {
  def props(out: ActorRef, me : String): Props = Props(new ChatResponseActor(out, me))
}
