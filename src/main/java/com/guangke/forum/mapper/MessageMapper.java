package com.guangke.forum.mapper;

import com.guangke.forum.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    /**
     * 私信： message表的一行消息
     * 会话: conversation_id相同的私信集合称为一个会话
     */

    //用户所有的会话列表，每个会话只查出最新的一条消息。支持分类，
    List<Message> selectConversations(int userId,int offset,int limit);

    //用户的会话数量,用于分页，page.setRows()
    int  selectConversationsCount(int userId);

    //某个会话的所有消息
    List<Message> selectMessages(String conversationId,int offset,int limit);

    //某个会话的所有消息数量
    int selectMessagesCount(String conversationId);

    //用户或者某个会话的未读消息数量 ,当conversationId为null时，表示查询用户的未读消息
    int selectUnreadMessagesCount(int userId,String conversationId);

    int insertMessage(Message message);

    //更新状态(已读，删除)
    int updateStatus(List<Integer> ids,int status);

    //当前用户的某个主题的最新通知
    Message selectLatestNotice(int userId,String topic);

    //当前用户的某个主题的通知数量
    int selectNoticeCount(int userId,String topic);

    //当前用户的某个主题的未读通知数量，当topic为null时，查询所有主题的未读通知数量
    int selectNoticeUnreadCount(int userId,String topic);

    //获取某个主题的所有通知,支持分页
    List<Message> selectNotices(int userId,String topic,int offset,int limit);
}
