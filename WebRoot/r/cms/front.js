Cms = {};
/**
 * 浏览次数
 */
Cms.viewCount = function(base, contentId, viewId, commentId, downloadId, upId,
		downId) {
	viewId = viewId || "views";
	commentId = commentId || "comments";
	downloadId = downloadId || "downloads";
	upId = upId || "ups";
	downId = downId || "downs";
	$.getJSON(base + "/content_view.jspx", {
		contentId : contentId
	}, function(data) {
		if (data.length > 0) {
			$("#" + viewId).text(data[0]);
			$("#" + commentId).text(data[1]);
			$("#" + downloadId).text(data[2]);
			$("#" + upId).text(data[3]);
			$("#" + downId).text(data[4]);
		}
	});
}
Cms.channelViewCount = function(base, channelId, viewId) {
	viewId = viewId || "views";
	$.getJSON(base + "/channel_view.jspx", {
		channelId : channelId
	});
}
/**
 * 站点流量统计
 */
Cms.siteFlow = function(base,page, referer,flowSwitch,
		pvId,visitorId,dayPvId, dayVisitorId,
		weekPvId,weekVisitorId,monthPvId,monthVisitorId) {
	pvId = pvId || "pv";
	visitorId = visitorId || "visitor";
	dayPvId=dayPvId || "dayPv";
	dayVisitorId=dayVisitorId || "dayVisitor";
	weekPvId=weekPvId || "weekPv";
	weekVisitorId=weekVisitorId || "weekVisitor";
	monthPvId=monthPvId || "monthPv";
	monthVisitorId=monthVisitorId || "monthVisitor";
	flowSwitch=flowSwitch||"true";
	if(flowSwitch=="true"){
		$.getJSON(base + "/flow_statistic.jspx", {
			page : page,
			referer : referer
		}, function(data) {
			if (data.length > 0) {
				$("#" + pvId).text(data[0]);
				$("#" + visitorId).text(data[1]);
				$("#" + dayPvId).text(data[2]);
				$("#" + dayVisitorId).text(data[3]);
				$("#" + weekPvId).text(data[4]);
				$("#" + weekVisitorId).text(data[5]);
				$("#" + monthPvId).text(data[6]);
				$("#" + monthVisitorId).text(data[7]);
			}
		});
	}
}
/**
 * 成功返回true，失败返回false。
 */
Cms.up = function(base, contentId, origValue, upId) {
	upId = upId || "ups";
	var updown = $.cookie("_cms_updown_" + contentId);
	if (updown) {
		return false;
	}
	$.cookie("_cms_updown_" + contentId, "1");
	$.get(base + "/content_up.jspx", {
		"contentId" : contentId
	}, function(data) {
		$("#" + upId).text(origValue + 1);
	});
	return true;
}
/**
 * 成功返回true，失败返回false。
 */
Cms.down = function(base, contentId, origValue, downId) {
	downId = downId || "downs";
	var updown = $.cookie("_cms_updown_" + contentId);
	if (updown) {
		return false;
	}
	$.cookie("_cms_updown_" + contentId, "1");
	$.get(base + "/content_down.jspx", {
		contentId : contentId
	}, function(data) {
		$("#" + downId).text(origValue + 1);
	});
	return true;
}
/**
 * 获取评分选项投票数
 */
Cms.scoreCount = function(base, contentId,itemPrefix) {
	itemPrefix=itemPrefix||"score-item-";
	$.getJSON(base + "/content_score_items.jspx", {
		contentId : contentId
	}, function(data) {
			$("span[id^='"+itemPrefix+"']").each(function(){
				var itemId=$(this).prop("id").split(itemPrefix)[1];
				$(this).text(data.result[itemId]);
			});
	});
}
/**
 * 成功返回true，失败返回false。
 */
Cms.score = function(base, contentId,itemId,itemPrefix) {
	itemPrefix=itemPrefix||"score-item-";
	var score = $.cookie("_cms_score_" + contentId);
	if (score) {
		return false;
	}
	$.cookie("_cms_score_" + contentId, "1");
	$.get(base + "/content_score.jspx", {
		"contentId" : contentId,
		"itemId":itemId
	}, function(data) {
		if(data.succ){
			$("#"+itemPrefix + itemId).text(data.count);
		}
	});
	return true;
}
/**
 * 获取附件地址
 */
Cms.attachment = function(base, contentId, n, prefix) {
	$.get(base + "/attachment_url.jspx", {
		"cid" : contentId,
		"n" : n
	}, function(data) {
		var url;
		for (var i = 0;i < n; i++) {
			url = base + "/attachment.jspx?cid=" + contentId + "&i=" + i
					+ data[i];
			$("#" + prefix + i).attr("href", url);
		}
	}, "json");
}
/**
 * 提交评论
 */
Cms.comment = function(callback, form) {
	form = form || "commentForm";
	$("#" + form).validate( {
		submitHandler : function(form) {
			$(form).ajaxSubmit( {
				"success" : callback,
				"dataType" : "json"
			});
		}
	});
}
/**
 * 获取评论列表
 * 
 * @param siteId
 * @param contentId
 * @param greatTo
 * @param recommend
 * @param orderBy
 * @param count
 */
Cms.commentList = function(base, c, options) {
	c = c || "commentListDiv";
	$("#" + c).load(base + "/comment_list.jspx", options);
}
Cms.commentListMore = function(base, c, options) {
	c = c || "commentListDiv";
	$("#" + c).load(base + "/comment_list.jspx", options);
	$('#commentDialog').dialog('open');
}
/**
 * 评论顶
 */
Cms.commentUp = function(base, commentId, origValue, upId) {
	upId = upId || "commentups";
	var updown = $.cookie("_cms_comment_updown_" + commentId);
	if (updown) {
		return false;
	}
	$.cookie("_cms_comment_updown_" + commentId, "1");
	$.get(base + "/comment_up.jspx", {
		"commentId" : commentId
	}, function(data) {
		$("#" + upId).text(origValue + 1);
	});
	return true;
}
/**
 * 评论踩
 */
Cms.commentDown = function(base, commentId, origValue, downId) {
	downId = downId || "commentdowns";
	var updown = $.cookie("_cms_comment_updown_" + commentId);
	if (updown) {
		return false;
	}
	$.cookie("_cms_comment_updown_" + commentId, "1");
	$.get(base + "/comment_down.jspx", {
		commentId : commentId
	}, function(data) {
		$("#" + downId).text(origValue + 1);
	});
	return true;
}
/**
 * 评论输入框
 */
Cms.commentInputCsi = function(base,commentInputCsiDiv, contentId,commemtId) {
	commentInputCsiDiv = commentInputCsiDiv || "commentInputCsiDiv";
	$("#"+commentInputCsiDiv).load(base+"/comment_input_csi.jspx?contentId="+contentId+"&commemtId="+commemtId);
}
Cms.commentInputLoad= function(base,commentInputCsiPrefix,commentInputCsiDiv,contentId,commemtId) {
	$("div[id^='"+commentInputCsiPrefix+"']").html("");
	Cms.commentInputCsi(base,commentInputCsiDiv,contentId,commemtId);
}
/**
 * 是否是微信打开
 */
Cms.isOpenInWeiXin = function() {
	var ua = navigator.userAgent.toLowerCase();
    if(ua.match(/MicroMessenger/i)=="micromessenger") {
        return true;
     } else {
        return false;
    }
}
/**
 * 客户端包含登录
 */
Cms.loginCsi = function(base, c, options) {
	c = c || "loginCsiDiv";
	$("#" + c).load(base + "/login_csi.jspx", options);
}
/**
 * 向上滚动js类
 */
Cms.UpRoller = function(rid, speed, isSleep, sleepTime, rollRows, rollSpan,
		unitHight) {
	this.speed = speed;
	this.rid = rid;
	this.isSleep = isSleep;
	this.sleepTime = sleepTime;
	this.rollRows = rollRows;
	this.rollSpan = rollSpan;
	this.unitHight = unitHight;
	this.proll = $('#roll-' + rid);
	this.prollOrig = $('#roll-orig-' + rid);
	this.prollCopy = $('#roll-copy-' + rid);
	// this.prollLine = $('#p-roll-line-'+rid);
	this.sleepCount = 0;
	this.prollCopy[0].innerHTML = this.prollOrig[0].innerHTML;
	var o = this;
	this.pevent = setInterval(function() {
		o.roll.call(o)
	}, this.speed);
}
Cms.UpRoller.prototype.roll = function() {
	if (this.proll[0].scrollTop > this.prollCopy[0].offsetHeight) {
		this.proll[0].scrollTop = this.rollSpan + 1;
	} else {
		if (this.proll[0].scrollTop % (this.unitHight * this.rollRows) == 0
				&& this.sleepCount <= this.sleepTime && this.isSleep) {
			this.sleepCount++;
			if (this.sleepCount >= this.sleepTime) {
				this.sleepCount = 0;
				this.proll[0].scrollTop += this.rollSpan;
			}
		} else {
			var modCount = (this.proll[0].scrollTop + this.rollSpan)
					% (this.unitHight * this.rollRows);
			if (modCount < this.rollSpan) {
				this.proll[0].scrollTop += this.rollSpan - modCount;
			} else {
				this.proll[0].scrollTop += this.rollSpan;
			}
		}
	}
}
Cms.LeftRoller = function(rid, speed, rollSpan) {
	this.rid = rid;
	this.speed = speed;
	this.rollSpan = rollSpan;
	this.proll = $('#roll-' + rid);
	this.prollOrig = $('#roll-orig-' + rid);
	this.prollCopy = $('#roll-copy-' + rid);
	this.prollCopy[0].innerHTML = this.prollOrig[0].innerHTML;
	var o = this;
	this.pevent = setInterval(function() {
		o.roll.call(o)
	}, this.speed);
}
Cms.LeftRoller.prototype.roll = function() {
	if (this.proll[0].scrollLeft > this.prollCopy[0].offsetWidth) {
		this.proll[0].scrollLeft = this.rollSpan + 1;
	} else {
		this.proll[0].scrollLeft += this.rollSpan;
	}
}
/**
 * 收藏信息
 */
Cms.collect = function(base, cId, operate,showSpanId,hideSpanId) {
	$.post(base + "/member/collect.jspx", {
		"cId" : cId,
		"operate" : operate
	}, function(data) {
		if(data.result){
			if(operate==1){
				alert("收藏成功！");
				$("#"+showSpanId).show();
				$("#"+hideSpanId).hide();
			}else{
				alert("取消收藏成功！");
				$("#"+showSpanId).hide();
				$("#"+hideSpanId).show();
			}
		}else{
			alert("请先登录");
		}
	}, "json");
}
/**
 * 列表取消收藏信息
 */
Cms.cmsCollect = function(base, cId, operate) {
	$.post(base + "/member/collect.jspx", {
		"cId" : cId,
		"operate" : operate
	}, function(data) {
		if(data.result){
			if(operate==1){
				alert("收藏成功！");
			}else{
				alert("取消收藏成功！");
				$("#tr_"+cId).remove();
			}
		}else{
			alert("请先登录");
		}
	}, "json");
}
/**
 * 检测是否已经收藏信息
 */
Cms.collectexist = function(base, cId,showSpanId,hideSpanId) {
	$.post(base + "/member/collect_exist.jspx", {
		"cId" : cId
	}, function(data) {
		if(data.result){
			$("#"+showSpanId).show();
			$("#"+hideSpanId).hide();
		}else{
			$("#"+showSpanId).hide();
			$("#"+hideSpanId).show();
		}
	}, "json");
}

/**
 * 申请职位信息
 */
Cms.jobApply = function(base, cId) {
	$.post(base + "/member/jobapply.jspx", {
		"cId" : cId
	}, function(data) {
		if(data.result==-1){
			alert("请先登录");
			location.href=base+"/login.jspx";
		}else if(data.result==-2){
			alert("职位id不能为空");
		}else if(data.result==-3){
			alert("未找到该职位");
		}else if(data.result==-4){
			alert("您还没有创建简历，请先完善简历");
		}else if(data.result==0){
			alert("您今天已经申请了该职位!");
		}else if(data.result==1){
			alert("成功申请了该职位!");
		}
	}, "json");
}
Cms.loginSSO=function(base){
	var username=$.cookie('username');
	var sessionId=$.cookie('JSESSIONID');
	var ssoLogout=$.cookie('sso_logout');
	if(username!=null){
		if(sessionId!=null||(ssoLogout!=null&&ssoLogout=="true")){
			$.post(base+"/sso/login.jspx", {
				username:username,
				sessionId:sessionId,
				ssoLogout:ssoLogout
			}, function(data) {
					if(data.result=="login"||data.result=="logout"){
						location.reload();
					}
			}, "json");
		}
	}
}
Cms.checkPerm = function(base, contentId) {
	$.getJSON(base + "/page_checkperm.jspx", {
		contentId : contentId
	}, function(data) {
		if (data==3) {
			alert("请先登录");
			location.href=base+"/user_no_login.jspx";
		}else if(data==4){
			location.href=base+"/group_forbidden.jspx";
		}else if(data==5){
			location.href=base+"/content/buy.jspx?contentId="+contentId;
		}
	});
}
Cms.collectCsi = function(base,collectCsiDiv, tpl, contentId) {
	collectCsiDiv = collectCsiDiv || "collectCsiDiv";
	$("#"+collectCsiDiv).load(base+"/csi_custom.jspx?tpl="+tpl+"&cId="+contentId);
}
Cms.getCookie=function getCookie(c_name){
	if (document.cookie.length>0)
	  {
	  	c_start=document.cookie.lastIndexOf(c_name + "=");
		  if (c_start!=-1)
		    { 
			    c_start=c_start + c_name.length+1;
			    c_end=document.cookie.indexOf(";",c_start);
			    if (c_end==-1){
			    	c_end=document.cookie.length;
			    } 
			    return unescape(document.cookie.substring(c_start,c_end));
		    } 
		  }
	return "";
}
Cms.MobileUA=function(){
	var ua = navigator.userAgent.toLowerCase();  
    var mua = {  
        IOS: /ipod|iphone|ipad/.test(ua), //iOS  
        IPHONE: /iphone/.test(ua), //iPhone  
        IPAD: /ipad/.test(ua), //iPad  
        ANDROID: /android/.test(ua), //Android Device  
        WINDOWS: /windows/.test(ua), //Windows Device  
        TOUCH_DEVICE: ('ontouchstart' in window) || /touch/.test(ua), //Touch Device  
        MOBILE: /mobile/.test(ua), //Mobile Device (iPad)  
        ANDROID_TABLET: false, //Android Tablet  
        WINDOWS_TABLET: false, //Windows Tablet  
        TABLET: false, //Tablet (iPad, Android, Windows)  
        SMART_PHONE: false //Smart Phone (iPhone, Android)  
    };  
    mua.ANDROID_TABLET = mua.ANDROID && !mua.MOBILE;  
    mua.WINDOWS_TABLET = mua.WINDOWS && /tablet/.test(ua);  
    mua.TABLET = mua.IPAD || mua.ANDROID_TABLET || mua.WINDOWS_TABLET;  
    mua.SMART_PHONE = mua.MOBILE && !mua.TABLET;  
    return mua;  
}

