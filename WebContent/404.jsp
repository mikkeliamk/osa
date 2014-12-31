<%@ include file="/layout/taglibs.jsp" %>
<fmt:message key="message.mainsearchPlaceholder" var="placeholder"/>

<s:layout-render name="/layout/default.jsp">
    <s:layout-component name="header">
        <jsp:include page="/layout/osa_header.jsp"/>
    </s:layout-component>
    
    <s:layout-component name="html_head">
        <script>
        $(function(){
            $("label#breadcrumb").html("<fmt:message key='header.error.404'/>");
        	
            // Fallback if browser does not support placeholder attribute
			if("placeholder" in document.createElement('input') == false){
			    var placeholderText = $("#placeholderText").val();
			    $('#mainpage-search-button').click(function(){
			        if($('#mainpage-search-box').val() == placeholderText){
			            $('#mainpage-search-box').val("");
			        }
			    });
			
			    $('#mainpage-search-box').focus(function() {
			        if(this.value == placeholderText){
			            this.style.color = 'black';
			            this.value = "";
			        }
			    });
			    
			    $('#mainpage-search-box').blur(function() {
			        if(this.value == ""){
			            this.style.color = '#aaa';
			            this.value = placeholderText;
			        }
			    });
			}
        });
		</script>
    </s:layout-component>
    
    <s:layout-component name="content">
        <h1 class="largeheader"><fmt:message key="header.error.404"/></h1><div class="header-hr"></div>
        <h2><fmt:message key="message.error.404"/></h2>
        <h3><fmt:message key="message.error.404.guide"/></h3>
        <s:form action="/Search.action" focus="search.freetext">
            <sd:text id="mainpage-search-box" name="search.freetext" placeholder="${placeholder}"/>
            <s:submit id="mainpage-search-button" name="search"><fmt:message key="button.search"/></s:submit>
            <input type="hidden" id="placeholderText" value="${placeholder}" />
        </s:form>
    </s:layout-component>
</s:layout-render>