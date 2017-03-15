package infrastructure.chat

import java.util.concurrent.atomic.AtomicReference
import javax.inject.{ Inject, Singleton }

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, MergeHub, Sink }
import akka.stream.{ KillSwitches, Materializer, UniqueKillSwitch }
import domains.chat.{ ChatMessage, ChatRoom, ChatRoomRepository }

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * Chat room client.
 */
@Singleton
class ChatRoomClient @Inject()(
    implicit val materializer: Materializer,
    implicit val system: ActorSystem
) extends ChatRoomRepository {

  import ChatRoomClient._

  override def chatRoom(roomId: String, userName: String): ChatRoom = synchronized {
    roomPool.get.get(roomId) match {
      case Some(chatRoom) =>
        chatRoom
      case None =>
        val room = create(roomId)
        roomPool.get() += (roomId -> room)
        room
    }
  }

  private def create(roomId: String): ChatRoom = {
    // Create bus parts.
    val (sink, source) =
      MergeHub.source[ChatMessage](perProducerBufferSize = 16)
          .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
          .run()

    // Connect "drain outlet".
    source.runWith(Sink.ignore)

    val channel = ChatChannel(sink, source)

    val bus: Flow[ChatMessage, ChatMessage, UniqueKillSwitch] = Flow.fromSinkAndSource(channel.sink, channel.source)
        .joinMat(KillSwitches.singleBidi[ChatMessage, ChatMessage])(Keep.right)
        .backpressureTimeout(3.seconds)
        .map { e =>
          println(s"$e $channel")
          e
        }


    ChatRoom(roomId, bus)
  }
}

object ChatRoomClient {

  private var rooms: scala.collection.mutable.Map[String, ChatRoom] = scala.collection.mutable.Map()

  val roomPool: AtomicReference[scala.collection.mutable.Map[String, ChatRoom]] =
    new AtomicReference[mutable.Map[String, ChatRoom]](rooms)

}
