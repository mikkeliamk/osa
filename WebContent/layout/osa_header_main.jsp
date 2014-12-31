<%@ include file="/layout/taglibs.jsp" %>
<fmt:message key="message.mainsearchPlaceholder" var="placeholder"/>

<div class="header-image-container-main">
	<jsp:include page="/layout/menubar.jsp"/>
	<div class="header-image-main">
		<img src="${pageContent.request.contextPath}img/${gui.logoFile}" id="mainlogo" alt="${gui.logoFileAlt}"/><br/>
		<s:form action="/Search.action" id="mainpage-search" focus="search.freetext">
			<sd:text id="mainpage-search-box" name="search.freetext" placeholder="${placeholder}"/>
			<s:submit id="mainpage-search-button" name="search"><fmt:message key="button.search"/></s:submit>
			<s:label for="advancedsearch">&#9654; <s:link href="search.jsp" class="whitelink"><fmt:message key="link.advancedsearch"/></s:link></s:label>
			<input type="hidden" id="placeholderText" value="<fmt:message key="${placeholder}"/>" />
		</s:form>
	</div>
</div>

<script>
$(function(){
	// Fallback if browser does not support placeholder attribute
	if("placeholder" in document.createElement('input') == false){
		var placeholderText = $("#placeholderText").val();
		$('#mainpage-search-button').click(function(){
			if($('#mainpage-search-box').val() == placeholderText){
				$('#mainpage-search-box').val("");
			}
		});
	
		$('#mainpage-search-box').focus(function() {
		    if($(this).val() == placeholderText){
		        $(this).css('color','black');
		        $(this).val("");
		    }
		});
		
		$('#mainpage-search-box').blur(function() {
		    if($(this).val() == ""){
		        $(this).css('color','#aaa');
		        $(this).val(placeholderText);
		    }
		});
	}
});
</script>