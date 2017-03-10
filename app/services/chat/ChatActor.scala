package services.chat

import akka.actor.{ Actor, ActorRef, Props }

class ChatActor(out: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: String =>
      out ! ("I received your message:" + msg)
  }

  // 終了時の処理があれば記載する
  override def postStop(): Unit = super.postStop()
}


object ChatActor {
  def props(out: ActorRef):Props = Props(new ChatActor(out))
}
