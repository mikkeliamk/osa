<%@ include file="/layout/taglibs.jsp" %>

<s:layout-definition>
<%-- 	<s:layout-component name="timerange"> --%>
		<div class="ingest-form-row">
			<s:label for="${mdElement}"><fmt:message key="capture.${mdElement}"/></s:label>
			<span data-tooltip="${shorthelp}"><img src="img/icons/silk/help.png" class="helpicon" alt="[?]"/></span>
			
			<div class="temporal-range-container displayInlineBlock">
				<c:forEach items="${formElement.nestedFormElements}" var="nestedChild" varStatus="loop">
	                <s:text name="${fieldname}[${nestedChild.name}].value" id="${nestedChild.name}" class="datepicker required-${formElement.required}" readonly="${formElement.readonly}"/>
	                ${(loop.index == 0) ? ' - ' : ''}
	                <s:hidden name="${fieldname}[${nestedChild.name}].name" value="${nestedChild.name}"/>
				</c:forEach>
				<div class="timeRangeSelectContainer">
					<div class="displayInlineBlock floatLeft"><button class="openTimeRangeSelect sub-form-button" id="decades"><fmt:message key="button.decades"/></button></div>
					<div class="displayInlineBlock floatLeft"><button class="openTimeRangeSelect sub-form-button" id="centuries"><fmt:message key="button.centuries"/></button></div>
					<div class="clearfix"></div>
				</div>
			</div>
			<div class="clearfix"></div>
		</div>
<%-- 	</s:layout-component> --%>
</s:layout-definition>
