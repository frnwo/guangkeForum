package com.guangke.forum.service;

import com.guangke.forum.mapper.MessageMapper;
import com.guangke.forum.pojo.Message;
import com.guangke.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId,int offset,int limit){
       return messageMapper.selectConversations(userId,offset,limit);
    }

    public int getConversationsCount(int userId){
        return messageMapper.selectConversationsCount(userId);
    }

    public List<Message> findMessages(String conversationId,int offset,int limit){
        return messageMapper.selectMessages(conversationId,offset,limit);
    }

    public int getMessagesCount(String conversationId){
        return messageMapper.selectMessagesCount(conversationId);
    }

    public int getUnreadMessagesCount(int userId,String conversationId){
        return messageMapper.selectUnreadMessagesCount(userId,conversationId);
    }
    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

    public Message findLatestNotice(int userId,String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }

    public int getNoticeCount(int userId,String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }

    public int getNoticeUnreadCount(int userId,String topic){
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    public List<Message> findNotices(int userId,String topic,int offset,int limit){
        return messageMapper.selectNotices(userId,topic,offset,limit);
    }

}
