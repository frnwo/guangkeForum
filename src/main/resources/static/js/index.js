$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	//获取title和content
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//异步发送
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{"title":title,"content":content},
		function (data) {
			//服务器返回处理消息
			data = $.parseJSON(data);
			//发布之后的提示信息
			$("#hintBody").text(data.msg);
			$("#hintModal").modal("show");

			setTimeout(function(){
				$("#hintModal").modal("hide");
				//如果code为0,则发布成功，刷新网页
				window.location.reload();
			}, 2000);
		}
	);

}
