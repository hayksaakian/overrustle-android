(function($) {
	var onChatPreload = function(content) {
		$(content).find("script[src]").detach();
//		$(content).find("#destinychat").replaceWith($(content).find("#destinychat").wrap('<div id="chat-embedded" data-enhance="false"></div>'));
	};
	
	var onChatLoad = function(content) {
	};
	
	var jqmReady = $.Deferred();
	var pgReady = $.Deferred();
	
	// jqm ready
	$(document).on("mobileinit", function() {
		$.support.cors = true;
	    $.mobile.allowCrossDomainPages = true;
	    $.mobile.autoInitializePage = false;
	    //$.mobile.ignoreContentEnabled = true;
		jqmReady.resolve();
	});
	
	//$(document).on("pagecreate", "#page", jqmReady.resolve);
	$(document).on("pagecontainerbeforeload", function(event, data) {
		console.debug("pcbl");
	});
	
	$(document).on("pagecontainerload", function(_, data) {
		console.debug("pcl");
		if (/\/embed\/chat/.test(data.url)) {
			console.debug("onChatPreload");
			onChatPreload(data.content);
		}
	});
	
	$(document).on("pageload", function(event, data) {
		console.debug("pl");
	});
	
	$(document).on("pagecontainershow", function(event, data) {
		console.debug('pageshow');
		if (destiny.chat) {
			destiny.chat.debug = true;
		}
		//console.debug($('html').html());
		//console.log($(event.target).find('script').count());
	});
	
	// phonegap ready
	document.addEventListener("deviceready", pgReady.resolve, false);
	
	// all ready :)
	$.when(jqmReady, pgReady).then(function () {
	    console.log("allready");
	    $.mobile.initializePage();
	    $.mobile.changePage("http://www.destiny.gg/embed/chat");
	});
})(jQuery);