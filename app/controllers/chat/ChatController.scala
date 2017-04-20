package controllers.chat

import javax.inject.Inject

import akka.actor._
import akka.stream._
import akka.stream.scaladsl.{ Flow, Keep }
import domains.chat.ChatMessage
import play.api.libs.json.JsValue
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

  def start(roomId: String) = WebSocket.accept[JsValue, JsValue] { request =>


    val userName = request.queryString("user_name").headOption.getOrElse("anon")

    val userInput: Flow[JsValue, ChatMessage, _] = ActorFlow.actorRef[JsValue, ChatMessage](out => ChatRequestActor.props(out, userName))
    val room = streamChatService.start(roomId, userName)
    val userOutPut: Flow[ChatMessage, JsValue, _] = ActorFlow.actorRef[ChatMessage, JsValue](out => ChatResponseActor.props(out,userName))

    userInput.viaMat(room.bus)(Keep.right).viaMat(userOutPut)(Keep.right)
  }
}
