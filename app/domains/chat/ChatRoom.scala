package domains.chat

import akka.stream.UniqueKillSwitch
import akka.stream.scaladsl.Flow

/**
 * Chat room.
 */
case class ChatRoom(roomId: String, bus: Flow[ChatMessage, ChatMessage, UniqueKillSwitch])
