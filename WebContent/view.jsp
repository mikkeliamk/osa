<%@ include file="/layout/taglibs.jsp" %>

<%-- Check if user logged in --%>
<c:if test="${empty user}"><c:redirect url="/" /></c:if>

<s:useActionBean var="ingestaction" beanclass="fi.mamk.osa.stripes.IngestAction"/>
<s:useActionBean var="basketaction" beanclass="fi.mamk.osa.stripes.BasketAction"/>
<c:set var="index" value="${param.index}"/>
<c:set var="pid" value="${ingestaction.pid}" />
<c:set var="tabs" value="${gui.getGUITabs(index)}" />
<c:set var="names" value="${gui.getGUIElements(index)}" />
<c:set var="document" value="${actionBean.allFedoraBeans[0].captureData.metaDataElements}"/>

<c:set var="previousUrl" value="${actionBean.isFromSearch()}" />


<fmt:message key="message.downloadtargz" var="downloadtargz"/>
<fmt:message key="message.addtobasket" var="addtobasket"/>
<fmt:message key="message.removefrombasket" var="removefrombasket"/>
<fmt:message key="tooltip.original.shorthelp" var="showoriginal"/>
<fmt:message key="tooltip.thumb.shorthelp" var="dontshoworiginal"/>

<s:layout-render name="/layout/default.jsp">
    <s:layout-component name="header">
        <jsp:include page="${gui.subHeader}"/>
    </s:layout-component>
    
    <s:layout-component name="html_head">
    <script src="js/jsp-js-functions/js-tab-functionality.js"></script>
        <script>       
        var currentpid  = "${pid}";
        var contentType = "${index}";
    
        function getOrigThumb(){
        	var $thumbContainer = $("#orig-thumbnail-container");
            // If editing existing object
            if(currentpid != "") {
                $thumbContainer.append("<img src='img/ajax-loader-bar.gif' id='origloader'/>");
                $.ajax({
                    url: "Ingest.action?checkIfHasOriginal=",
                    data: {pid: currentpid},
                    success: function(data, textStatus, jqXHR) {
                        $("#origloader").remove();
                        var hasOrig = data.split("|")[0];
                        var hasThumb = data.split("|")[1];
                        // If object has original file
                        if (hasOrig == "true") {
                            var hasThumb    = data.split("|")[1];
                            var tooltip     = "${showoriginal}";
                            var imgThumb    = '<img src="Ingest.action?pid='+currentpid+'&dsName=ORIGINAL&hasThumbnail='+hasThumb+'&getFileThumb=" class="thumbnail-image" alt="original" title="'+tooltip+'"/>';
                            var imgIcon     = '<img src="img/icons/silk/page_save.png" class="smallicon" alt="original" title="'+tooltip+'"/>';
                            
                            var htmlThumb   = '<s:link href="Ingest.action?pid='+currentpid+'&dsName=ORIGINAL&showFile=">'+imgThumb+'</s:link>';
                            var htmlIcon    = '<s:link href="Ingest.action?pid='+currentpid+'&dsName=ORIGINAL&showFile=">'+imgIcon+'</s:link>';
                            $thumbContainer.append(htmlThumb);
                            $(".download-container").append(htmlIcon);
                        } else if (hasThumb == "true") {
                        	var tooltip     = "${dontshoworiginal}";
                        	var imgThumb    = '<img src="Ingest.action?pid='+currentpid+'&dsName=ORIGINAL&hasThumbnail='+hasThumb+'&getFileThumb=" class="thumbnail-image" alt="original" title="'+tooltip+'"/>';
                            
                            $thumbContainer.append(imgThumb);
                        // If has not
                        } else {
                            $thumbContainer.append("<fmt:message key='header.nooriginalfound'/>");
                            $thumbContainer.find("img").remove();
                        }
                    }
                });
            }
        }
        
        function getAttachmentThumbs(){
        	var $attThumbContainer = $("#att-thumbnail-container"); 
            $attThumbContainer.append("<img src='img/ajax-loader-bar.gif' id='attloader'/>");
            $.ajax({
                url: "Ingest.action?checkIfHasAttachments=",
                data: {pid: currentpid},
                dataType: "json",
                success: function(data) {
                    $("#attloader").remove();
                    // Object has some attachments
                    if(!jQuery.isEmptyObject(data)) {
                        var html = "";
                        for(var datastream in data) {
                            if(data.hasOwnProperty(datastream)){
                                // value = boolean hasThumbnail 
                                var value = data[datastream];
                                html += '<s:link href="Ingest.action?pid='+currentpid+'&dsName='+datastream+'&showFile="><img src="Ingest.action?pid='+currentpid+'&dsName='+datastream+'&hasThumbnail='+value+'&getFileThumb=" class="thumbnail-image" alt="'+datastream+'"/></s:link>';
                            }
                            $attThumbContainer.append(html);
                        }
                    }
                    // Object does not have any attachments
                    else {
                        $attThumbContainer.append("<fmt:message key='header.noattachmentfound'/>");
                    }
                }
            });
        }
        
        $(function() {
        	
            // Remove file forms from sessionStorage
            if(getURLParameter("rmst") == "true") {
                sessionStorage.removeItem("fileForm");
                sessionStorage.removeItem("attFileForm");
            }
            
            getOrigThumb();
            if(currentpid != ""){
                getAttachmentThumbs();
            }
        	
			// Generate breadcrumb via ajax call incase it takes time to construct it
			$("label#breadcrumb").html("<fmt:message key='message.creatingpath'/>");
			$.ajax({
			    url: "Ingest.action?getObjectBreadcrumb=",
			    data: {pid: currentpid, plainView: true},
			    dataType: "html",
			    success: function(path){
			        $("label#breadcrumb").html(path);
			        // Set currently open object's name to page title
			        document.title = $("label#breadcrumb .italic").text()+" | "+document.title;
			    },
			    error: function(){
			        $("label#breadcrumb").html("<fmt:message key='error.creatingpath'/>");
			    }
			});
			
            $(".download-container").on("click", ".basketaction", function(e){
                var $this = $(this);
                var action = $(this).attr("data-action");
                var pid = this.id;
                $this.prop("src", "img/ajax-loader-arrow.gif");
                $.ajax({
                    url: "Basket.action?"+action+"=",
                    data: {pidArray: [pid]},
                    dataType: "text",
                    success: function(data, statusText) {
                        var icon = (action == "addToBasket") ? "basket_delete.png" : "basket_put.png";
                        setTimeout(function(){
                            $this.prop("src", "img/icons/silk/"+icon);
                        },1000);
                        updateBasketSize();
                    }
                });
                var actionVal = (action == "addToBasket") ? "deleteFromBasket" : "addToBasket";
                $this.prop("title", (action == "addToBasket") ? "<fmt:message key='message.removefrombasket'/>" : "<fmt:message key='message.addtobasket'/>");
                $this.attr("data-action", actionVal);
            });
        });
        </script>
    </s:layout-component>
    <s:layout-component name="content">
        <%-- Display messages to user, f.ex. after a successful update --%>
        <s:messages/>
        
        <h1 id="mainheader" class="largeheader-nomargin">
            <%-- Displays object's title or preferred name --%>
            <c:set var="elem" value="${(empty document['title'].value) ? 'preferredName' : 'title'}"/>
            ${document[elem].value}
        </h1>
        <div class="ingest-header-container">
			<c:if test="${previousUrl == true}">
			    <s:link href="Search.action?previousSearch=1&search=" class="previousSearchLink"> &laquo; <fmt:message key="link.lastsearch"/></s:link>&nbsp;&nbsp;&nbsp;
			</c:if>
            <c:if test="${!empty pid}">
                <div class="download-container floatRight">
					<c:if test="${user.getHighestAccessRight() >= 50}">
					    <s:link href="Ingest.action?pid=${pid}&view=" class="previousSearchLink"><img src="img/icons/silk/page_edit.png" class="smallicon" title="<fmt:message key="button.edit"/>"/></s:link>
					</c:if>
					<osa:show requiredLevel="20" objectId="${pid}">
                        <s:link href="Workflow.action?pid=${pid}&downloadObject="><img src="img/icons/silk/compress.png" alt="tar.gz" class="smallicon" title="${downloadtargz}"/></s:link>
                    </osa:show>
                    <c:if test="${!user.isAnonymous()}">
	                    <c:set var="exists" value="${basketaction.checkIfExists(pid)}"/>
	                    <c:if test="${exists}">
	                        <img src="img/icons/silk/basket_delete.png" id="${pid}" data-action="deleteFromBasket" class="basketaction pointerCursor smallicon" title="${removefrombasket}" alt="[-]"/>
	                    </c:if>
	                    <c:if test="${!exists}">
	                        <img src="img/icons/silk/basket_put.png" id="${pid}" data-action="addToBasket" class="basketaction pointerCursor smallicon" title="${addtobasket}" alt="[+]"/>
	                    </c:if>
                    </c:if>
                    <c:if test="${user.isAnonymous()}">
                        <img src="img/icons/silk/basket_error.png" class="smallicon" title="<fmt:message key="basket.notavailable.anon"/>" alt="-"/>
                        <img src="img/icons/silk/page_edit.png" class="smallicon" title="<fmt:message key="edit.notavailable.anon"/>" alt="-"/>
                    </c:if>
                </div>
            </c:if>
        </div>
		<div class="header-hr"></div>
                
        <!-- Create tabs to the form -->
        <div id="tabs_wrapper">
            <c:if test="${!empty pid && 
                             (index == 'audio' 
                             || index == 'document' 
                             || index == 'drawing' 
                             || index == 'image'
                             || index == 'map' 
                             || index == 'movingimage')}">
                <div id="orig-thumbnail-container" class="object-container view-only"></div>
            </c:if>
            <div id="tabs_container">
                <ul id="tabs">
                    <c:forEach items="${tabs}" var="tab" varStatus="loop">
                        <c:if test="${loop.index == 0}">
                            <li class="tab-item active"><a href="#${tab}"><fmt:message key="capture.${tab}"/></a></li>
                        </c:if>
                        <c:if test="${loop.index != 0}">
                            <li class="tab-item"><a href="#${tab}"><fmt:message key="capture.${tab}"/></a></li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
            <div id="tabs_content_container" class="view-only">
               <c:forEach items="${tabs}" var="currenttab" varStatus="looptab">
                    <div id="${currenttab}" class="tab_content">
                    <c:forEach items="${names}" var="currentelement" varStatus="loop">
                        <%-- Set to variables so code looks more neat --%>
                        <c:set var="mdElement" value="${currentelement.key}"/>
                        <c:set var="formElement" value="${currentelement.value}"/>
                        <fmt:message key="tooltip.${mdElement}.shorthelp" var="shorthelp"/>
                        
                        <c:if test="${formElement.tab == currenttab}">
                            <%-- 
                                Renders different kind of form elements by using stripe's layout-render component.
                                Supplies the renderer with element-specific jsp files that contain the markup.
                                Pass values to renderer via attributes
                            --%>
                            <s:layout-render name="/layout/components/view_only/${formElement.fieldType}.jsp" 
                                mdElement="${mdElement}" 
                                formElement="${formElement}" 
                                pid="${pid}" 
                                pageIndex="${index}" 
                                document="${document}"
                            />
                        </c:if>
                    </c:forEach>
                    </div>
                </c:forEach>
            </div>
            <div class="clearfix"></div>
        </div>
    </s:layout-component>
</s:layout-render>
    