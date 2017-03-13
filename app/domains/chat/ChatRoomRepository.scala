package domains.chat

trait ChatRoomRepository {

  def streamChatRoom(roomId: String): ChatRoom

}
