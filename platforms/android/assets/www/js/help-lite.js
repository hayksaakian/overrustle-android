;(function($, window, document, undefined) {
	var urlParseRE = /^\s*(((([^:\/#\?]+:)?(?:(\/\/)((?:(([^:@\/#\?]+)(?:\:([^:@\/#\?]+))?)@)?(([^:\/#\?\]\[]+|\[[^\/\]@#?]+\])(?:\:([0-9]+))?))?)?)?((\/?(?:[^\/\?#]+\/+)*)([^\?#]*)))?(\?[^#]+)?)(#.*)?/;
	
	//Parse a URL into a structure that allows easy access to
	//all of the URL components by name.
	var parseUrl = function( url ) {
		// If we're passed an object, we'll assume that it is
		// a parsed url object and just return it back to the caller.
		if ( $.type( url ) === "object" ) {
			return url;
		}

		var matches = urlParseRE.exec( url || "" ) || [];

			// Create an object that allows the caller to access the sub-matches
			// by name. Note that IE returns an empty string instead of undefined,
			// like all other browsers do, so we normalize everything so its consistent
			// no matter what browser we're running on.
			return {
				href:         matches[  0 ] || "",
				hrefNoHash:   matches[  1 ] || "",
				hrefNoSearch: matches[  2 ] || "",
				domain:       matches[  3 ] || "",
				protocol:     matches[  4 ] || "",
				doubleSlash:  matches[  5 ] || "",
				authority:    matches[  6 ] || "",
				username:     matches[  8 ] || "",
				password:     matches[  9 ] || "",
				host:         matches[ 10 ] || "",
				hostname:     matches[ 11 ] || "",
				port:         matches[ 12 ] || "",
				pathname:     matches[ 13 ] || "",
				directory:    matches[ 14 ] || "",
				filename:     matches[ 15 ] || "",
				search:       matches[ 16 ] || "",
				hash:         matches[ 17 ] || ""
			};
	};
	
	//Turn relPath into an asbolute path. absPath is
	//an optional absolute path which describes what
	//relPath is relative to.
	var makePathAbsolute = function( relPath, absPath ) {
		var absStack,
			relStack,
			i, d;

		if ( relPath && relPath.charAt( 0 ) === "/" ) {
			return relPath;
		}

		relPath = relPath || "";
		absPath = absPath ? absPath.replace( /^\/|(\/[^\/]*|[^\/]+)$/g, "" ) : "";

		absStack = absPath ? absPath.split( "/" ) : [];
		relStack = relPath.split( "/" );

		for ( i = 0; i < relStack.length; i++ ) {
			d = relStack[ i ];
			switch ( d ) {
				case ".":
					break;
				case "..":
					if ( absStack.length ) {
						absStack.pop();
					}
					break;
				default:
					absStack.push( d );
					break;
			}
		}
		return "/" + absStack.join( "/" );
	};
	
	//Returns true if both urls have the same domain.
	var isSameDomain = function( absUrl1, absUrl2 ) {
		return parseUrl( absUrl1 ).domain === path.parseUrl( absUrl2 ).domain;
	};
	
	//Returns true for any relative variant.
	var isRelativeUrl = function( url ) {
		// All relative Url variants have one thing in common, no protocol.
		return parseUrl( url ).protocol === "";
	};

	//Returns true for an absolute url.
	var isAbsoluteUrl = function( url ) {
		return parseUrl( url ).protocol !== "";
	};
	
	//Turn the specified realtive URL into an absolute one. This function
	//can handle all relative variants (protocol, site, document, query, fragment).
	var makeUrlAbsolute = function( relUrl, absUrl ) {
		if ( !isRelativeUrl( relUrl ) ) {
			return relUrl;
		}

		if ( absUrl === undefined ) {
			absUrl = this.documentBase;
		}

		var relObj = parseUrl( relUrl ),
			absObj = parseUrl( absUrl ),
			protocol = relObj.protocol || absObj.protocol,
			doubleSlash = relObj.protocol ? relObj.doubleSlash : ( relObj.doubleSlash || absObj.doubleSlash ),
			authority = relObj.authority || absObj.authority,
			hasPath = relObj.pathname !== "",
			pathname = makePathAbsolute( relObj.pathname || absObj.filename, absObj.pathname ),
			search = relObj.search || ( !hasPath && absObj.search ) || "",
			hash = relObj.hash;

		return protocol + doubleSlash + authority + pathname + search + hash;
	};
	
	//test if a given url (string) is a path
	//NOTE might be exceptionally naive
	var isPath = function( url ) {
		return ( /\// ).test( url );
	};
	
	var findClosestLink = function( ele )	{
		while ( ele ) {
			// Look for the closest element with a nodeName of "a".
			// Note that we are checking if we have a valid nodeName
			// before attempting to access it. This is because the
			// node we get called with could have originated from within
			// an embedded SVG document where some symbol instance elements
			// don't have nodeName defined on them, or strings are of type
			// SVGAnimatedString.
			if ( ( typeof ele.nodeName === "string" ) && ele.nodeName.toLowerCase() === "a" ) {
				break;
			}
			ele = ele.parentNode;
		}
		return ele;
	};
	
	var _parse = function(html) {
		var page = $(html.split( /<\/?body[^>]*>/gmi )[1] || "" )
		return page;
	};
	
	var _loadSuccess = function(data, textStatus, jqXHR) {
		console.debug("loadSuccess");
		var content = $(_parse(data));
		content.find("script[src]").detach();
		//console.debug(content.html());
		//$(document.body).empty();
		content.prependTo(document.body);
	};
	
	var _loadError = function(jqXHR, textStatus, errorThrown) {
		console.error(textStatus + " - " + errorThrown);
	};
	
	var _loadChat = function(url) {
		url = url || "http://www.destiny.gg/embed/chat";
		console.debug("loadchat");
		// Load the new content.
		$.ajax({
			url: url,
			type: "get",
			data: null,
			contentType: null,
			dataType: "html",
			success: _loadSuccess,
			error: _loadError
		});
	};
	
	$(document).ready(function() {
		// phonegap ready
		var pgReady = $.Deferred();
		document.addEventListener("deviceready", pgReady.resolve, false);
		$.when(pgReady).then(function () {
		    console.debug("allready");
		    _loadChat();
		});

		// click routing - direct to HTTP or Ajax, accordingly
		$(document).bind( "click", function( event ) {
			if ( event.isDefaultPrevented() ) {
				return;
			}

			var link = findClosestLink( event.target ),
				$link = $( link );
			
			if ( !link || event.which > 1 ) {
				return;
			}
			
			var baseUrl = "http://www.destiny.gg";
			var href = makeUrlAbsolute( $link.attr( "href" ) || "#", baseUrl );
			
			if ( href.search( "#" ) !== -1 ) {
				href = href.replace( /[^#]*#/, "" );
				if ( !href ) {
					//link was an empty hash meant purely
					//for interaction, so we ignore it.
					event.preventDefault();
					return;
				} else if ( isPath( href ) ) {
					//we have a path so make it the href we want to load.
					href = makeUrlAbsolute( href, baseUrl );
				} else {
					//we have a simple id so use the documentUrl as its base.
					href = makeUrlAbsolute( "#" + href, baseUrl );
				}
			}
			
			var domain = parseUrl(href).domain;
			
			// Should we handle this link, or let the browser deal with it?
			//var useDefaultUrlHandling = $link.is( "[rel='external']" ) || $link.is( "[target]" );
			var useInAppBrowser = /destiny.gg$/.test(domain);
			var target = useInAppBrowser ? "_blank" : "_system";
			
			var iab = window.open(href, target);
			iab.addEventListener("exit", function() { 
				console.debug("iab exit");
				window.location.reload(true);
			});
			event.preventDefault();
		});
	});
})(jQuery, window, document);