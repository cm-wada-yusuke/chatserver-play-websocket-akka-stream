package infrastructure

import akka.actor.Actor
import play.api.libs.iteratee.{ Concurrent, Enumerator }
import play.api.libs.json.Json

case class ChatRoom(roomId: String) extends Actor {

  // Enumerator : Iterateeに対するプロデューサ
  // Channel : 1つ以上のIterateeに強制的に送るためのチャンネル
  private[this] val (enumerator, channel) = Concurrent.broadcast[String]

  override def receive:Receive = {
    case Join(userName)       => join(userName)
    case Leave(userName)      => dispatch("system", s"$userName is gone")
    case NewMember(userName)  => dispatch("system", s"$userName joined")
    case Talk(userName, text) => dispatch(userName, text)
  }

  def join(userName:String) = {
    sender ! Joined(enumerator)
    self ! NewMember(userName)
  }

  def dispatch(name:String, text:String) = {
    val response = ChatResponse(room = roomId, name = name, text = text)
    channel.push(response.toString)
  }

}

sealed trait ChatStatusMessage

case class Join(userName: String) extends ChatStatusMessage
case class Leave(userName: String) extends ChatStatusMessage
case class NewMember(userName: String) extends ChatStatusMessage
case class Talk(userName: String, text:String) extends ChatStatusMessage

case class Joined(enumerator: Enumerator[String])
case class ChatResponse(room: String, name: String, text: String){
  override def toString: String = Json.toJson(Map("room" -> room, "name" -> name, "text" -> text)).toString
}

