package controllers.chat

import javax.inject.Inject

import akka.actor._
import akka.stream._
import akka.stream.scaladsl.{ Flow, Keep }
import domains.chat.ChatMessage
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket
import services.chat.ChatService

/**
 * WebSocket Chat Server using AKka Stream.
 */
class ChatController @Inject()(
    implicit val system: ActorSystem,
    implicit val materializer: Materializer,
    streamChatService: ChatService
) {

  def start(roomId: String) = WebSocket.accept[String, String] { request =>

    val userName = request.queryString("user_name").headOption.getOrElse("anon")
    val userInputBus: Flow[String, ChatMessage, _] = ActorFlow.actorRef[String, ChatMessage](out => ChatRequestActor.props(out, userName, roomId))
    val channel = streamChatService.start(roomId, userName)
    val userOutputBus: Flow[ChatMessage, String, _] = ActorFlow.actorRef[ChatMessage, String](out => ChatResponseActor.props(out))

    userInputBus.viaMat(channel)(Keep.right).viaMat(userOutputBus)(Keep.right)
  }
}
