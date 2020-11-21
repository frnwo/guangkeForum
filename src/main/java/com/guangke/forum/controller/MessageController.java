package com.guangke.forum.controller;

import com.alibaba.fastjson.JSONObject;
import com.guangke.forum.pojo.Message;
import com.guangke.forum.pojo.Page;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.MessageService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements ForumConstants {

    @Autowired
    MessageService messageService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @GetMapping("/letter/list")
    public String getLetter(Model model, Page page){
        User user = hostHolder.get();
        page.setLimit(5);
        page.setPath("/letter/list");
        //会话数量
        page.setRows(messageService.getConversationsCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(),page.getOffset(),page.getLimit());
        //因为会话还需要显示会话的消息数量和未读数量，因此改装一下
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message : conversationList){
                Map<String,Object> map = new HashMap<>();
                //会话最新消息
                map.put("conversation",message);
                //单个会话的消息数量
                map.put("letterCount",messageService.getMessagesCount(message.getConversationId()));
                //会话的未读数量
                map.put("unreadCount",messageService.getUnreadMessagesCount(user.getId(),message.getConversationId()));
                //会话的用户图片
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        //所有会话的未读数量
        int unreadCount = messageService.getUnreadMessagesCount(user.getId(),null);
        model.addAttribute("unreadCount",unreadCount);
        model.addAttribute("conversations",conversations);

        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetter(@PathVariable("conversationId") String conversationId, Model model, Page page){
        //分页
        page.setLimit(5);
        page.setRows(messageService.getMessagesCount(conversationId));
        page.setPath("/letter/detail/"+conversationId);

        List<Message> messageList = messageService.findMessages(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> messages = new ArrayList<>();
        if(messageList != null){
            for(Message message : messageList){
                Map<String,Object> map = new HashMap<>();
                map.put("message",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                messages.add(map);
            }
        }
        model.addAttribute("messages",messages);
        messages = new ArrayList<>();
        User target = getTargetUser(conversationId);
        model.addAttribute("target",target);

        //查询未读的message的id集合
        List<Integer> ids = getUnreadIds(messageList);
        //如果上面的id集合不为空，则将这些message集合status更新为1，表示已读
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    /**
     * 遍历消息，如果to_id 与当前用户id相同，且status状态未读，就返回这些id
     * @param messageList
     * @return
     */
    private List<Integer> getUnreadIds(List<Message> messageList){
        List<Integer> ids = new ArrayList<>();
        if(messageList != null){
            for(Message message : messageList){
                //注意：toId 不一定就是当前用户，也有发送给其他用户的私信
                if(hostHolder.get().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    /**
     * 私信的目标用户
     * 根据conversation_id来取，私信的目标用户一定是去掉当前用户id的另外一个id
     */
    public User getTargetUser(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(hostHolder.get().getId() == id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }
    }
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendMessage(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target == null){
            return ForumUtils.getJSONString(1,"发送的用户不存在");
        }
        User user = hostHolder.get();
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setFromId(user.getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else{
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        messageService.addMessage(message);
        //0 成功
        return ForumUtils.getJSONString(0);
    }

    //系统通知
    @GetMapping("notice/list")
    public String getNotices(Model model){
        User user = hostHolder.get();

        //评论通知
        Message message = messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);
        Map<String,Object> messageVo = new HashMap<>();
        messageVo.put("message",message);
        if(message != null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.getNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("count",count);

            int unreadCount = messageService.getNoticeUnreadCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("unread",unreadCount);
        }

        model.addAttribute("commentNotice",messageVo);

        //点赞通知
         message = messageService.findLatestNotice(user.getId(),TOPIC_LIKE);
         messageVo = new HashMap<>();
         messageVo.put("message",message);
         if(message != null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.getNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("count",count);

            int unreadCount = messageService.getNoticeUnreadCount(user.getId(),TOPIC_LIKE);
            messageVo.put("unread",unreadCount);
        }

        model.addAttribute("likeNotice",messageVo);

         //关注通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        messageVo.put("message",message);
        if(message != null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));

            int count = messageService.getNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count",count);

            int unreadCount = messageService.getNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("unread",unreadCount);
        }

        model.addAttribute("followNotice",messageVo);

        //私信未读数量
        int letterUnreadCount = messageService.getUnreadMessagesCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //三类通知的所有未读数量
        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    @GetMapping(path = "/notice/detail/{topic}")
    public String getNotices(@PathVariable("topic") String topic,Page page,Model model){

        User user = hostHolder.get();

        page.setLimit(5);
        page.setRows(messageService.getNoticeCount(user.getId(),topic));
        page.setPath("/notice/detail/"+topic);

        List<Message> noticeList = messageService.findNotices(user.getId(),topic,page.getOffset(),page.getLimit());
        List<Map<String,Object>> noticeVoList  = new ArrayList<>();

        if(noticeList != null){
            for(Message notice : noticeList){
                Map<String,Object> map = new HashMap<>();
                map.put("notice",notice);

                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

                User user1 = userService.findUserById((Integer) data.get("userId"));
                map.put("user",user1);
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));

                //系统用户
                map.put("fromUser",userService.findUserById((Integer) notice.getFromId()));

                noticeVoList.add(map);
            }

            //设置已读
            List<Integer> ids = getUnreadIds(noticeList);
            if(!ids.isEmpty()){
                messageService.readMessage(ids);
            }
        }

        model.addAttribute("notices",noticeVoList);

        return "/site/notice-detail";
    }
}
