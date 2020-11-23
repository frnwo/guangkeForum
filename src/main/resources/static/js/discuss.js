/**
 * 相当于window.onload=function(){}
 */
$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

function setTop() {
    $.post(
        CONTEXT_PATH+"/discuss/top",
        {"postId":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                $("#topBtn").attr("disabled","disabled");
            }else {
                alert(data.msg)
            }
        }
    )
}
function setWonderful() {
    $.post(
        CONTEXT_PATH+"/discuss/wonderful",
        {"postId":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else {
                alert(data.msg)
            }
        }
    )
}
function setDelete() {
    $.post(
        CONTEXT_PATH+"/discuss/delete",
        {"postId":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                location.href=CONTEXT_PATH+"/index";
            }else {
                alert(data.msg)
            }
        }
    )
}
function like(btn,entityType,entityId,entityUserId,postId) {
    $.post(
        CONTEXT_PATH+"/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data) {
            console.log("return"+data);
            data = $.parseJSON(data);
            if(data.code == 0){
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
                $(btn).children("i").text(data.likeCount);
            }else{
                alert(data.msg);
            }
        }
    );
}
