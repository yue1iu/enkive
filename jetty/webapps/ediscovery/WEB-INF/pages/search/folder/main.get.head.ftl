<link rel="stylesheet" type="text/css" href="${url.context}/resource/css/jquery.fancybox-1.3.4.css" media="screen" />
<script src="${url.context}/resource/javascript/jquery.fancybox-1.3.4.js"></script>
<script type="text/javascript">
	function toggleChecked(status) {
		$(".idcheckbox").each( function() {
		$(this).attr("checked",status);
		})
	}
	$('tr.message td').live('click', function(){
		var currentId = $(this).parent().attr('id');
		if(currentId && !$(this).hasClass('search_action')) {
			$(this).fancybox({
				'width'				: '75%',
				'height'			: '75%',
				'autoScale'			: true,
				'transitionIn'		: 'none',
				'transitionOut'		: 'none',
				'type'				: 'iframe',
				'showCloseButton'	: 'true',
				'href'				: '${url.context}/message?messageid=' + currentId
			}).trigger("click");
		}
	});
	
	$('.pagingLink').live('click', function() {
	    var link = $(this).attr("href");
	    $('#main').load(link + " #main");
	    $(this).removeAttr("href");
	});
	
	$('.sortable').live('click', function() {
	    var link = $(this).attr("href");
	    $('#main').load(link + " #main");
	});
</script>
