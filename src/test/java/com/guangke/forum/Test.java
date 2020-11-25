package com.guangke.forum;

public class Test {
    public Test(){
        System.out.println("hello test");
    }
    public Test(String text){
        System.out.println("hello test "+text);
    }
    private static final Test demo;
    static{
        demo  = new Test("demo");
    }
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        /*
        Long likeCount = 30L;
        Map<String,Object> map  = new HashMap<>();
        map.put("likeCount",likeCount);
        likeCount = 40L;
        System.out.println(map.get("likeCount"));

         */
        new Test();
    }

}

