package services.chat

import javax.inject.Inject

import akka.actor.ActorRef
import akka.util.Timeout
import domains.ChatRoomRepository
import infrastructure._
import play.api.libs.iteratee.{ Enumerator, Iteratee }

import scala.concurrent.duration._
import akka.pattern.ask

import scala.concurrent.Future


import scala.concurrent.ExecutionContext.Implicits._

class ChatService @Inject()(
    repository: ChatRoomRepository,
    roomService: ChatRoomService
) {

  def start(roomId: String, userName: String): Future[Either[Throwable,(Iteratee[String, Unit], Enumerator[String])]] =
  // 該当するIDのチャットルームを探すか生成する。得られたチャットルーム（actor）を使い、joinする。
  // joinは裏でチャットルームに対してjoinメッセージをaskする。
  // 成功すると、(Iterate[String,Unit], enumerator) が返る。
  // 入力値をStringで受け取り、副作用を起こすという意味になる。
    repository.chatRoom(roomId) flatMap {
      chatRoom => roomService.join(chatRoom, userName)
    }
}

class ChatRoomService @Inject()() {
  private[this] implicit val timeout = Timeout(1 second)

  // chatRoom Actorに対してJoinメッセージを送る(ask)
  // Joinedメッセージが返ってきたら、enumeratorが手に入るので、Iterateeを定義し, Enumeratorとともに返す
  def join(chatRoom: ActorRef, userName: String): Future[Either[Throwable,(Iteratee[String, Unit], Enumerator[String])]] = (chatRoom ? Join(userName)).map {
    case Joined(enumerator) => Right((in(chatRoom, userName), enumerator))
    case _ => Left(new RuntimeException("can not join"))
  }

  private def in(chatRoom: ActorRef, userName: String):Iteratee[String, Unit] = {
    // これはチャンクごとに実行される関数である：chatRoomに対してTalkメッセージを送る。ユーザ名と入力値を送る。入力値はおそらくIterateeに入っている。
    // IterateeがDoneしたとき、chatRoomに対してLeaveメッセージを送る。
    // これを行うようなIterateeを定義して返す。
    Iteratee.foreach[String](text => chatRoom ! Talk(userName, text)).map(_ => chatRoom ! Leave(userName))
  }

  private def error(chatRoom: ActorRef) = {
    val response = ChatResponse(room = chatRoom.toString, name = "system", text = "can not join")
    (Iteratee.ignore[String], Enumerator[String](response.toString))
  }
}
