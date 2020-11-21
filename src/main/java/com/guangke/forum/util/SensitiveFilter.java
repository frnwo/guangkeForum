package com.guangke.forum.util;

import com.alibaba.druid.sql.visitor.functions.Char;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    //替换符
    private static final String REPLACEMENT = "***";
    //根节点 空的
    private TrieNode root = new TrieNode();

    /**
     * 初始化字典树,把所有敏感字符串添加到树上
     * @PostConstruct 当Spring容器调用构造器实例化bean，并且依赖注入后，自动调用@PostConstruct注解的方法
     */
    @PostConstruct
    private void init(){
        try(
               InputStream is  =  this.getClass().getClassLoader().getResourceAsStream("sensitive-keyword.txt");
               BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
                String keyWord = null;

                while((keyWord=reader.readLine())!=null){
                    this.addKeyWord(keyWord);
                }
        }catch (IOException e){
            logger.error("读取敏感词文件失败: "+e.getMessage());
        }
    }
    //将一个敏感词添加到字典树
    private void addKeyWord(String keyWord){
        TrieNode tempNode = root;
        for(int i=0;i<keyWord.length();i++){
           char c = keyWord.charAt(i);
           TrieNode subNode = tempNode.getSubNode(c);
           if(subNode == null){
               subNode = new TrieNode();
               tempNode.addSubNode(c,subNode);
           }
           tempNode = subNode;
           if(i == keyWord.length() -1){
               tempNode.setKeyWordEnd(true);
           }
        }
    }

    /**
     * 如果该字符是东亚文字以外的并且不是英文和数字，就说明该字符是符号
     * @param c
     * @return
     */
    public boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 是东亚文字范围
        return (c < 0x2E80 || c > 0x9FFF) && !CharUtils.isAsciiAlphanumeric(c);
    }
    /**
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //结果
        StringBuilder sb = new StringBuilder();
        //指针1 指向根节点
        TrieNode tempNode = root;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //当position指针移动到text的长度时停止检测
        while(position < text.length()){
            char c = text.charAt(position);
            if(isSymbol(c)){
                //此时的position虽然指向个符号，但如果当前节点是根节点,说明还没有敏感词的开头，直接把该符号添加到sb
                if(tempNode == root){
                    sb.append(c);
                    begin++;
                }
                //position放外面是因为tempNode也可能不为root,但position也要往后走一步
                position++;
                //检测下一个字符啦~
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            //如果当前节点没有字符c对应的节点，则说明这个字符不是敏感词的开头
            if(tempNode == null){
                sb.append(text.charAt(begin));
                //begin和position指针走向后一步
                position = ++begin;
                //重新开始下一个字符的检测
                tempNode = root;
                continue;
            //如果这个字符是敏感词的终点
            }else if(tempNode.isKeyWordEnd()){
                //替换从begin~position的字符串
                sb.append(REPLACEMENT);
                //begin从position的下一个字符开始检测
                begin = ++position;
                //既然找到了终点就重新从root节点开始检测
                tempNode = root;
             //否则找到了疑是敏感词的字符开头
            }else{
                //position往后走一步
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }
    //节点
    private class TrieNode{
        private boolean isKeyWordEnd = false;

        //本节点的所有的子节点都在这个Map里，通过key(字符)查找子节点
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        //添加子节点(key为字符，value为字符对应的节点)
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }
        //取出key为所查找字符的子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

    }
}
