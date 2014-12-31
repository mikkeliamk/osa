<%@ include file="/layout/taglibs.jsp" %>

<!-- Check if user logged in -->
<c:if test="${empty user || user.getHighestAccessRight() < 10}"><c:redirect url="/" /></c:if>

<fmt:message key="message.removefrombasket" var="removefrombasket"/>
<s:layout-render name="/layout/default.jsp">
	<s:layout-component name="header">
		<jsp:include page="${gui.subHeader}"/>
	</s:layout-component>
	
    <s:layout-component name="html_head">
        <script src="js/jsp-js-functions/js-tab-functionality.js"></script>
        <script src="js/jsp-js-functions/datatables-multiselect.js"></script>
        <script src="js/datatables-plugins/fnReloadAjax.js"></script>
    	<script>
    	var tablerow = "", oBasketTable = "";
    	
    	function showBasketDatatables() {
    		oBasketTable = $('#baskettable').dataTable({
                "bAutoWidth": false,
                "bJQueryUI": false,
//              "iDisplayLength": ${gui.searchResults.rows},
                "aaSorting":[[0, "desc"]],
                "sPaginationType": "full_numbers",
                "bProcessing": false,
                "bServerSide": true,
                "sDom": 't',
                "sAjaxSource": "Basket.action?showBasketDatatables=",
                "fnInitComplete": function(oSettings, json) {
                    $('.datatables-td-container').each(function(){
                        // Set the div's height to its parent td's height. Workaround for 100% div height
                        $(this).height($(this).closest('td').height());
                    });
                  },
                "oLanguage": {
                    "sSearch":          '<fmt:message key="datatable.filter" />',
                    "sProcessing":      '<fmt:message key="datatable.loading" />',
                    "sInfo":            '<fmt:message key="datatable.showing" />',
                    "sInfoFiltered":    '<fmt:message key="datatable.filtered" />',
                    "sZeroRecords":     '<fmt:message key="basket.emptybasket" />',
                    "sInfoEmpty":       '<fmt:message key="basket.emptybasket" />',
                    "oPaginate": {
                        "sNext":        '<fmt:message key="datatable.nextpage" />',
                        "sPrevious":    '<fmt:message key="datatable.previouspage" />',
                        "sFirst":       '<fmt:message key="datatable.firstpage" />',
                        "sLast":        '<fmt:message key="datatable.lastpage" />'
                    }
                },
                "aoColumns": [
                               {"bSortable": false, "sWidth": '20px', "sClass": 'vertical-middle-td'},
                               {"bSortable": false, "sWidth": 'auto'}
                             ]
            });
        }
    	
    	$(function() {
        	$("label#breadcrumb").html("<fmt:message key='link.basket'/>");
        	document.title = "<fmt:message key='link.basket'/> | " + document.title;

            $("#dialog").dialog({
                autoOpen: false,
                modal: true,
                draggable: false,
                resizable: false,
                minWidth: 500,
                maxHeight: 600,
                title: "<fmt:message key='header.confirmationrequired'/>",
                closeText: "<fmt:message key='button.close'/>"
            });
            
        	showBasketDatatables();
        	
        	// Removes one entry
        	$("#baskettable").on("click", ".basketaction", function(){
        		$this = $(this);
	        	var pid = $(this).data("pid");
	        	var pidArray = [];
	        	pidArray.push(pid);
                $.ajax({
                    url: "Basket.action?deleteFromBasket=",
                    data: {pidArray: pidArray},
                    complete: function(data, statusText) {
                        $this.closest('tr').css("background","#FF3700");
                        $this.closest('tr').fadeOut(400, function(){
                            $(this).remove();
                            updateBasketSize();
                        });
                    }
                });
        	});
        	
        	// Multirow remove
        	$(".datatable-tools #removeSelected").click(function(e){
        		e.preventDefault();
			    var pidArray = [];
			    var trArray = [];
			    
			    $(this).closest(".tab_content").find(".dataTable input:checkbox:checked").map(function(){
			    	trArray.push($(this).closest("tr"));
			    	pidArray.push($(this).data("pid"));
			    }).get();
			    
                var aLength = trArray.length;
                $.ajax({
                    url: "Basket.action?deleteFromBasket=",
                    data: {pidArray: pidArray},
                    complete: function(data, statusText) {
                    	for (var i = 0; i < aLength; i++) {
                            trArray[i].fadeOut(400, function(){
                                $(this).remove();
                            });
                        }
                    	updateBasketSize();
                    }
                });
        	});
        	
        	// Multirow delete
        	$(".datatable-tools #deleteSelected").click(function(e){
        		e.preventDefault();
        		
        		var pidArray = [];
                var trArray = [];
                var objectList = "";
                
                $(this).closest(".tab_content").find(".dataTable input:checkbox:checked").map(function(){
                    trArray.push($(this).closest("tr"));
                    pidArray.push($(this).data("pid"));
                    objectList += "<span class='deletelistitem displayBlock'><img src='img/icons/silk/"+types[$(this).data("pid").split(":")[1].split("-")[0]]+"' class='smallicon'/> "+$(this).data("name")+"</span>";
                }).get();
                
                $(".confirmtext").html("<fmt:message key='message.disposemethod.deletefromfedora'/>: <br/>");
                $(".confirmtext").append(objectList);
                
                $("#dialog").dialog('option', 'buttons', {
                      "<fmt:message key='button.confirm'/>": function () {
                          $.ajax({
                              url: "Disposal.action?ajaxDeleteItems=",
                              data: {childrenPids: pidArray},
                              complete: function(data, statusText) {
		                          var aLength = trArray.length;
                                  if (data != "denied") {
                                      for (var i = 0; i < aLength; i++) {
                                          trArray[i].fadeOut(400, function(){
                                              $(this).remove();
                                          });
                                      }
                                  }
                              }
                          });
                          
                          updateBasketSize();
                          
                        $(this).dialog("close");
                      },
                      "<fmt:message key='button.cancel'/>": function () {
                        $(this).dialog("close"); 
                      }
                    });
                $("#dialog").dialog("open");
        	});
    	});
    	</script>
    </s:layout-component>
    <s:layout-component name="content">
    	<h1 class="largeheader-nomargin"><fmt:message key="basket.title" /></h1><div class="header-hr"></div>
        <div id="tabs_wrapper">
	        <div id="tabs_container">
	            <ul id="tabs">
	                <li class="tab-item active"><a href="#basket-content"><fmt:message key="link.basket.content"/></a></li>
	                <li class="tab-item"><a href="#basket-messages"><fmt:message key="link.basket.messages"/> (0)</a></li>
	                <li class="tab-item"><a href="#basket-notes"><fmt:message key="link.basket.notes"/></a></li>
	            </ul>
	        </div>
	        
	        <div id="tabs_content_container" class="workspace-tabs_content_container">
	            <div id="basket-content" class="tab_content">
					<s:form action="/Ingest.action">
	                    <h2 class="largeheader"><fmt:message key="link.basket.content" /></h2>
	                    <div class="datatable-tools">
	                        <input type="checkbox" class="select" title="<fmt:message key="button.select.all"/>">
	                        <button id="removeSelected" title="<fmt:message key="tooltip.basket.remove"/>" disabled><fmt:message key="button.selected.remove.frombasket"/></button>
                            <c:if test="${user.getHighestAccessRight() >= 50}">
                                <button id="deleteSelected" title="<fmt:message key="tooltip.basket.delete"/>" disabled><fmt:message key="button.selected.delete"/></button>
                            </c:if>
	                    </div>
						<table id="baskettable">
	                        <thead>
				                <tr>
	                                <%-- Placeholders, these are not visible --%>
	                                <th>1</th>
	                                <th>3</th>
				                </tr>
	                        </thead>
						</table>
					</s:form>	
	            </div> <%-- Basket-content tab --%>
	            
	            <div id="basket-messages" class="tab_content">
	               <h2 class="largeheader"><fmt:message key="link.basket.messages"/></h2>
                    To be finished
	            </div> <%-- basket-messages tab --%>
	            
	            <div id="basket-notes" class="tab_content">
                    <h2 class="largeheader"><fmt:message key="link.basket.notes"/></h2>
                    To be finished
	            </div> <%-- basket-notes tab --%>
	        </div>
	        <div class="clearfix"></div>
        </div>
	    <div id="dialog">
	      	<div class="confirmicon"></div>
	      	<div class="confirmtext"></div>
    	</div>
	</s:layout-component>
</s:layout-render>
	