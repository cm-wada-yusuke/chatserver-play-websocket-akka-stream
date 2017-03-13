package controllers.chat

import javax.inject.Inject

import akka.actor._
import akka.stream._
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, MergeHub, Sink }
import domains.chat.ChatMessage
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket
import services.chat.ChatService

import scala.concurrent.duration._

/**
 * AKka Stream を使った WebSocket チャットサーバ。
 */
class ChatController @Inject()(
    implicit val system: ActorSystem,
    implicit val materializer: Materializer,
    streamChatService: ChatService
) {

  lazy val c: Flow[String, String, UniqueKillSwitch] = {
    val (sink, source) =
      MergeHub.source[String](perProducerBufferSize = 16)
          // マテリアライズしてつなぐ。MergeHubのマテリアライズ結果はSink[String]
          // BroadcastHubのマテリアライズ結果はSource[String]である。これを両方保持する。
          .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
          .run()


    // 最低でもひとつの垂れ流し先を用意しておく。
    source.runWith(Sink.ignore)

    Flow.fromSinkAndSource(sink, source)
        .joinMat(KillSwitches.singleBidi[String, String])(Keep.right)
        .backpressureTimeout(3.seconds)
  }

  def start(roomId: String) = WebSocket.accept[String, String] { request =>

    val userName = request.queryString("user_name").headOption.getOrElse("anon")
    val userInputBus: Flow[String, ChatMessage, _] = ActorFlow.actorRef[String, ChatMessage](out => ChatRequestConvertActor.props(out, userName, roomId))
    val channel = streamChatService.start(roomId, userName)
    val userOutputBus: Flow[ChatMessage, String, _] = ActorFlow.actorRef[ChatMessage, String](out => ChatResponseConvertActor.props(out))

    userInputBus.viaMat(channel)(Keep.right).viaMat(userOutputBus)(Keep.right)
  }
}
