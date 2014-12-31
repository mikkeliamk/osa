<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
<%-- 	<s:layout-component name="textarea"> --%>
		<div class="ingest-form-row">
			<s:label for="${mdElement}" class="verticalAlignTop"><fmt:message key="capture.${mdElement}"/></s:label>
			<span data-tooltip="${shorthelp}" class="verticalAlignTop"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
			
			<s:textarea name="${fieldname}[${mdElement}].value" class="required-${formElement.required}" readonly="${formElement.readonly}" rows="10" cols="48"/>
			<s:hidden name="${fieldname}[${mdElement}].name" value="${mdElement}"/>
		</div>
<%-- 	</s:layout-component> --%>
</s:layout-definition>
