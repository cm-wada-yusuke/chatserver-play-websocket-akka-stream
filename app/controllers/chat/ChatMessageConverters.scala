package controllers.chat

import domains.chat.Chat
import play.api.libs.functional.syntax._
import play.api.libs.json._

object ChatMessageConverters {

  implicit val ChatMessageReads: Reads[Chat] = (
      (JsPath \ "userName").read[String] and
          (JsPath \ "text").read[String] and
          Reads.pure[Boolean](false)
      ) (Chat)

  implicit val ChatMessageWrites: Writes[Chat] = (
      (JsPath \ "userName").write[String] and
          (JsPath \ "text").write[String] and
          (JsPath \ "systemFlag").write[Boolean]
      ) (unlift(Chat.unapply))

}
