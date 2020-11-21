$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	var entityId =$(btn).prev().val();
	if($(btn).hasClass("btn-info")) {
		// 关注操作
		$.post(
			CONTEXT_PATH+"/follow/",
			{"entityType":3,"entityId":entityId},
			function (data) {
				data = $.parseJSON(data);
				if(data.code == 0){
					window.location.reload();
				}else {
					alert(data.msg);
				}
			}
		);
	} else {
		// 取消关注操作
		$.post(
			CONTEXT_PATH+"/unfollow",
			{"entityType":3,"entityId":entityId},
			function (data) {
				data = $.parseJSON(data);
				if(data.code == 0){
					window.location.reload();
				}else {
					alert(data.msg);
				}
			}
		);
	}
}
