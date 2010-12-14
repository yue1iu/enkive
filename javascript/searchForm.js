function get_results(){
	
	var content = $("#contentField").val();
	var sender = $("#senderField").val();
	var recipient = $("#recipientField").val();
	var subject = $("#subjectField").val();;
	var dateEarliest = $("#dateEarliestField").val();
	var dateLatest = $("#dateLatestField").val();
	var messageId = $("#messageIdField").val();
	
	var queryString = '?content=' + encodeURIComponent(content) +
						'&sender=' + encodeURIComponent(sender) +
						'&recipient=' + encodeURIComponent(recipient) +
						'&subject=' + encodeURIComponent(subject) +
						'&dateEarliest=' + encodeURIComponent(dateEarliest) +
						'&dateLatest=' + encodeURIComponent(dateLatest) +
						'&messageId=' + encodeURIComponent(messageId);
	
	$('#main').html('<center>' +
			'<p><b>Search is in progress...</b></p><br />' + 
			'<img src=/ediscovery/resource/images/spinner.gif alt="Waiting for results" />' +
			'</center>');
	$('#main').load('/ediscovery/search/results' + queryString);
}

function clearForm(){
	$(".searchField").each(
		function(){
			$(this).val("");
		}
	);
}