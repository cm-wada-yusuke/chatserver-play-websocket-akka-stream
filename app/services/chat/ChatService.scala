package services.chat

import javax.inject.Inject

import domains.chat.{ ChatRoom, ChatRoomRepository }

class ChatService @Inject()(
    repository: ChatRoomRepository
) {

  /**
   * Get or create chat room and join.
   */
  def start(roomId: String, userName: String): ChatRoom = repository.chatRoom(roomId, userName)
}

