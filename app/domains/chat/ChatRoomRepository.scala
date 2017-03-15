package domains.chat

trait ChatRoomRepository {

  def chatRoom(roomId: String, userName: String): ChatRoom

}
