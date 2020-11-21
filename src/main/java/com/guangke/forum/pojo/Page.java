package com.guangke.forum.pojo;

public class Page {
    private int current = 1;
    private int limit = 10;
    private int rows = 0;
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current > 1 ){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit > 1 && limit < 100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows > 0){
            this.rows = rows;
        }
    }

    public int getOffset(){
        return (current-1)*limit;
    }
    //index首页由此判断当前页是否为最后一页
    public int getTotal() {
        int total;
        if(rows % limit == 0){
            total =  rows/limit;
        }else{
            total =  rows/limit + 1;
        }
        return total;
    }
    //index首页由此获取开始页码
    public int getFrom(){
        int from = current-2;

        if(from>1 && from <= getTotal()){
            return from;
        }else if(from > getTotal()){
            return getTotal();
        }else {
            return 1;
        }

    }
    //index首页由此获取结束页码
    public int getTo(){
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
