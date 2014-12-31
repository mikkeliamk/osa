function createBreadcrumbAndPageTitle(breadcrumb, pagetitle) {
	$("label#breadcrumb").html(breadcrumb);
	document.title = (pagetitle == null) ? "OSA Web Client" : pagetitle;
}