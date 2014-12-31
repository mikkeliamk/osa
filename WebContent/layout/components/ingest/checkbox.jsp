<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
<%--     <s:layout-component name="checkbox"> --%>
        <div class="ingest-form-row">
            <s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/></s:label>
            <span data-tooltip="${shorthelp}"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
            
            <s:checkbox name="${fieldname}[${mdElement}].value" disabled="${formElement.readonly}"/>
            <s:hidden name="${fieldname}[${mdElement}].name" value="${mdElement}"/>
        </div>
<%--     </s:layout-component> --%>
</s:layout-definition>
