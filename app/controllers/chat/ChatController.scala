package controllers.chat

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket
import services.chat.{ ChatActor, ChatService }

import scala.concurrent.ExecutionContext.Implicits._

class ChatController @Inject()(
    implicit system: ActorSystem,
    materialize: Materializer,
    chatService: ChatService
){

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => ChatActor.props(out))
  }

  def iterateeSocket(roomId:String) = WebSocket.tryAccept[String] { request =>
    val userName = request.queryString("user_name").headOption.getOrElse("anon")

    chatService.start(roomId, userName) map {
      _.left.map(e => play.api.mvc.Results.Ok(e.getMessage))
    }
  }

}
