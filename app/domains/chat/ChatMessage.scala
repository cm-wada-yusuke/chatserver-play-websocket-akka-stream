package domains.chat

sealed trait ChatMessage

case class Join(userName: String) extends ChatMessage
case class Leave(userName: String) extends ChatMessage
case class NewMember(userName: String) extends ChatMessage
case class Talk(userName: String, text:String) extends ChatMessage



