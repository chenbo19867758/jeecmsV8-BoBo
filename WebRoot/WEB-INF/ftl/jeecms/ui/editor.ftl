<#--
<textarea name="textarea"></textarea>
-->
<#macro editor
	name value="" height="230"
	fullPage="false" toolbarSet="My"
	label="" noHeight="false" required="false" colspan="" width="100" help="" helpPosition="2" colon=":" hasColon="true"
	maxlength="65535"
	onclick="" ondblclick="" onmousedown="" onmouseup="" onmouseover="" onmousemove="" onmouseout="" onfocus="" onblur="" onkeypress="" onkeydown="" onkeyup="" onselect="" onchange=""
	>
<#include "control.ftl"/><#rt/>
<#--
<textarea id="${name}" name="${name}">${value}</textarea>  
-->
<script id="${name}" name="${name}" type="text/plain">${value}</script>
<script type="text/javascript">
  <#if site??>MARK="${site.mark?string('true','false')}";<#else>MARK="true";</#if>
  <#if sessionId??>SID="${sessionId!}";</#if>
  $(document).ready(function(){
   UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
   UE.Editor.prototype.getActionUrl = function(action) {
	    if (action == 'uploadimage') {
	        return "${base+appBase}/ueditor/upload.do;jsessionid="+SID+"?Type=Image&mark="+MARK;
	    } else if (action == 'uploadvideo') {
	        return "${base+appBase}/ueditor/upload.do;jsessionid="+SID+"?Type=Media";
	    }else if (action == 'uploadfile') {
	        return "${base+appBase}/ueditor/upload.do;jsessionid="+SID+"?Type=File";
	    }else if (action == 'catchimage') {
	        return "${base+appBase}/ueditor/getRemoteImage.do";
	    }else if (action == 'uploadscrawl') {
	        return "${base+appBase}/ueditor/scrawlImage.do?Type=Image";
	    }else if (action == 'listimage'||action == 'listfile') {
	        return "${base+appBase}/ueditor/imageManager.do?picNum=50&insite=false";
	    }else {
	        return this._bkGetActionUrl.call(this, action);
	    }
   }
  var editor= UE.getEditor('${name}');
   //截图快捷键ctrl+shift+A
   editor.addshortcutkey({
        "snapscreen" : "ctrl+shift+65"
    });
  });
</script>

<#include "control-close.ftl"/><#rt/>
</#macro>