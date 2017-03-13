package domains.chat

import akka.stream.scaladsl.Flow

/**
 * Chat room.
 */
case class ChatRoom(roomId: String, channel: Flow[ChatMessage, ChatMessage, _])
