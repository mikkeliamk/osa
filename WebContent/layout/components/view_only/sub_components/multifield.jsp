<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
    <s:layout-component name="multifield">
        <div class="multifield-container">
            <c:if test="${fn:length(document[mdElement].values) > 0}">
                <c:set var="loopItems" value="${document[mdElement].values}"/>
            </c:if>
            <c:if test="${fn:length(document[mdElement].values) == 0}">
                <c:set var="loopItems" value="0"/>
            </c:if>
                  
            <c:forEach var="multifieldPid" items="${loopItems}" varStatus="loop">
                <div class="multifieldElem positionRelative">
                    <c:if test="${formElement.fieldType == 'textfield'}">
                        <c:out value="${document[mdElement].value}"/>
                    </c:if>

                    <c:if test="${formElement.fieldType == 'relation'}">
                        <c:out value="${actionBean.getRelationName(multifieldPid)}"/>
                    </c:if>
                </div>
            </c:forEach>
        </div> <%-- multifield-container --%>
        
        <div class="clearfix"></div>
    </s:layout-component>
</s:layout-definition>