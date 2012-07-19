<script type="text/javascript">
	function toggleChecked(status) {
		$(".checkbox").each( function() {
		$(this).attr("checked",status);
		})
	}
    $('tr.search_result td').live('click', function() {
        var id = $(this).parent().attr("id");
        if(id && !$(this).hasClass('search_action')) {
            window.location = "${url.context}/search/recent/view?searchid=" + id;
        }
    });	
    $('th input').live('click', function() {
        var id = $(this).parent().attr("id");
        if(id && !$(this).hasClass('search_action')) {
            window.location = "${url.context}/search/recent/view?searchid=" + id;
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
