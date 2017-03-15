package infrastructure.chat

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import domains.chat.ChatMessage

case class ChatChannel(
    sink : Sink[ChatMessage, NotUsed],
    source: Source[ChatMessage, NotUsed]
)
