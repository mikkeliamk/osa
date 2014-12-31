<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
<%-- 	<s:layout-component name="date"> --%>
		<div class="ingest-form-row">
			<s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/></s:label>
			<span data-tooltip="${shorthelp}"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
			
			<s:text name="${fieldname}[${mdElement}].value" class="datepicker required-${formElement.required}" readonly="${formElement.readonly}"/>
			<s:hidden name="${fieldname}[${mdElement}].name" value="${mdElement}"/>
		</div>
<%-- 	</s:layout-component> --%>
</s:layout-definition>
 