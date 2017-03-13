package domains.chat

trait ChatRoomRepository {

  def chatRoom(roomId: String): ChatRoom

}
