package controllers.chat

import javax.inject.Inject

import akka.NotUsed
import akka.actor._
import akka.stream._
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, MergeHub, Sink }
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket
import services.chat.ChatActor

import scala.concurrent.duration._

/**
 * AKka Stream を使った WebSocket チャットサーバ。
 */
class StreamChatController @Inject()(
    implicit val system: ActorSystem,
    implicit val materializer: Materializer
) {

  lazy val channel: Flow[String, String, UniqueKillSwitch] = {
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

  def start = WebSocket.accept[String, String] { request =>
    // Flow[String, String, _] を定義しなければならない。
    // WebSocketからの入力をStringとして扱い、WebSocketの出力をStringとしてsinkするFlowを要求している。
    // それに応えるために、ActorFlow.actorRef を使う。
    // これは、Flow[In, Out]を行うようなActorを作るための関数である。
    // 要求するActorは、outとなるActorRefを扱うPropsでなければならない。
    // ここでは単純に受け取ったメッセージに少し文字列を足し、outに対してメッセージを送る（つまりSinkとなる）PropsをActorから定義している。
    // 要するにSourceとSinkのActorはPlayが知ってるからFlowだけ定義してね、という話か。あれこれブロードキャストとかできる？
    // これを応用してWebSocketのチャットサーバーを作るには以下のようなことを実現する必要がある。
    // Broadcastを使うので少なくともこの形はできそうにない。


    val userInputBus: Flow[String, String, NotUsed] = Flow[String]

    val userOutputBus: Flow[String, String, _] = ActorFlow.actorRef[String, String](out => ChatActor.props(out))

    userInputBus.viaMat(channel)(Keep.right).viaMat(userOutputBus)(Keep.right)
  }
}
