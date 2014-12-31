/* ------------------------------------------------------------------------
	Pretty Comments
	
	Developped By: Stephane Caron (http://www.no-margin-for-errors.com)
	Inspired By: The facebook textarea :)
	Version: 1.4
	
	Copyright: Feel free to redistribute the script/modify it, as
			   long as you leave my infos at the top.
------------------------------------------------------------------------- */

	$.fn.prettyComments = function(settings) {
		settings = jQuery.extend({
					animate: false, /* If you set it to true, cursor will dissapear in FF3 */
					animationSpeed: 'fast', /* fast/slow/normal */
					maxHeight : 500,
					alreadyAnimated: false, /* DONT CHANGE */
					init: true /* DONT CHANGE */
				}, settings);

		// Create the div in which the content will be copied
		// Harri: Created if sentence so that only one hidden div. Without it creates hidden 
		// div for every textarea yet only uses one for the text
		
		if($('#comment_hidden').length == 0){
			$('body').append('<div id="comment_hidden"></div>');
		}
		

		var setCSS = function(which){
			// Init the div for the current textarea
			$("#comment_hidden").css({
				'position':'absolute',
				'top': -10000,
				'left': -10000,
				'width': $(which).width(),
				'min-height': $(which).height(),
				'font-family': $(which).css('font-family'),
				'font-size': $(which).css('font-size'),
				'line-height': $(which).css('line-height')
			});
			
//			if($.browser.msie && parseFloat($.browser.version) < 7){
//				$("#comment_hidden").css('height',$(which).height());
//			};
		};
		
		var copyContent = function(which){
			// Convert the line feeds into BRs
			theValue = $(which).attr('value') || "";
			theValue = theValue.replace(/\n/g,'<br />');
			
			$("#comment_hidden").html(theValue + '<br />');
			
			if(!settings.init){
				if($("#comment_hidden").height() > $(which).height()){
					if($('#comment_hidden').height() > settings.maxHeight){
						$(which).css('overflow-y','scroll');
					}else{
						$(which).css('overflow-y','hidden');
						expand(which);
					};
				}else if($("#comment_hidden").height() < $(which).height()){
					if($('#comment_hidden').height() > settings.maxHeight){
						$(which).css('overflow-y','scroll');
					}else{
						$(which).css('overflow-y','hidden');
						shrink(which);
					};
				};
			};
		};
		
		
		var expand = function(which){
			if(settings.animate && !settings.alreadyAnimated){
				settings.alreadyAnimated = true;
				$(which).animate({'height':$("#comment_hidden").height()},settings.animationSpeed,function(){
					settings.alreadyAnimated = false;
				});
			}else if(!settings.animate && !settings.alreadyAnimated){
				$(which).height($("#comment_hidden").height());
			};
		};
		
		var shrink = function(which){
			if(settings.animate && !settings.alreadyAnimated){
				settings.alreadyAnimated = true;
				$(which).animate({'height':$("#comment_hidden").height()},settings.animationSpeed,function(){
					settings.alreadyAnimated = false;
				});
			}else{
				$(which).height($("#comment_hidden").height());
			};
		};
		
		//Here's Harri's code
		var minArea = function(which){
			$(which).css({height:"30px", overflow:"auto"});
		};
		
		var maxArea = function(which){
			if($(which).val() != "") {
				theValue = $(which).attr('value') || "";
				theValue = theValue.replace(/\n/g,'<br />');
				
				$("#comment_hidden").html(theValue + '<br />');
				//If textarea has text on page load, copy the content to hidden div so that
				//maximizing and minimizing the textarea works correctly
				if($("#comment_hidden").html() == "<br>") {
					$("#comment_hidden").html($(which).val());
				}
				if($('#comment_hidden').height() > settings.maxHeight) {
	   				$(which).css({'overflow-y':'scroll', height : settings.maxHeight+"px"});
	       		}
				else {
					$(which).height($('#comment_hidden').height());
	   			}
			}
			
		};
		// here ends Harri's code
		
		$(this).each(function(){
			$(this).css({
				'overflow':'hidden'
			})
			.bind('keyup',function(){
				copyContent($(this));
			}).blur(function() {
				minArea($(this));
			}).on("click scroll", function() {
				maxArea($(this));
	   		});

			// Make sure all the content in the textarea is visible
			setCSS(this);
			copyContent($(this));
			
			if($("#comment_hidden").height() > settings.maxHeight){
				$(this).css({
					'overflow-y':'scroll',
					'height':settings.maxHeight
				});
			}else{
				$(this).height($("#comment_hidden").height());
			};
			
			settings.init = false;
		});
	};