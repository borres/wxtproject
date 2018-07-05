/*
JS-library for wxt
- popup as in PI popup:
     simplepopup(url,wname,wstyle)
- toggling visibility of popupfragments
      getPos (obj), popunpop(targetID,e)
- preparing prettifyed code
	  prepareCode    
- simple inline expansions as in PI expandable:
      toggleExpand(elt)
- dragging gadgets
	dragdrop-obj
	addEventSimple
    removeEventSimple
- expanding and collapsing images from thumbs
    - initImages

Style classes for the actual elements must be set
see sample: wxtstyles.css distributed with WXT

B.Stenseth 2011.
*/

// --------- simple page popup
function simplepopup(theURL,wname,wstyle)
{
if(wstyle=='*')
	wstyle='scrollbars=yes,resizable=yes,width=600,height=600,status=no';
try{
	newwindow=window.open(theURL, wname, wstyle);
	if (window.focus) {newwindow.focus()}
}
catch(E){
	alert('You may have blocked popups');
  }
}
//eofpopup

// --------- popfragments
function getPos (obj) {
    var output = new Object();
    var mytop=0, myleft=0;
    while( obj) {
    mytop+= obj.offsetTop;
    myleft+= obj.offsetLeft;
    obj= obj.offsetParent;
    }
    output.left = myleft;
    output.top = mytop;
    return output;
}
function popunpop(targetID,e)
{
	// which event
    if (!e) var e = window.event;
    // which was the element
    var targetElt=document.getElementById(targetID);
    // turn on or off
    if (targetElt.style.display=='none')
        targetElt.style.display='block';
    else
        targetElt.style.display='none';
    // turn off
    if(targetElt.style.display=='none')
        return;

    // turn on
    // where was the mouseclick
    var posx = 0;
    var posy = 0;
	
	var p=getPos(e.SrcElement);
	posx=p.left;
	posy=p.top;
	
    
	if (e.pageX || e.pageY) 	{
        posx = e.pageX;
        posy = e.pageY;
    }
    else if (e.clientX || e.clientY) 	{
        posx = e.clientX + document.body.scrollLeft
            + document.documentElement.scrollLeft;
        posy = e.clientY + document.body.scrollTop
            + document.documentElement.scrollTop;
    }
	
    posx=posx+10;
    posy=posy+10;
    // posx and posy now contain the mouse position relative to the document
    targetElt.style.left=''+posx+'px';
    targetElt.style.top=''+posy+'px';        
}

// --------------- preparecode  
// avoid multiple prettifying when added calls to prettyPrint
// prettyprint and readyandpretty should be defined identically in stylesheet
function prepareCode()
{		
	var list=$('.prettyprint');
	if(list.length >0)
		prettyPrint();
	for(var ix=0;ix<list.length;ix++)
	{
		var elt = list[ix];
		var C=elt.className;
		C=C.replace('prettyprint','readyandpretty');
		elt.className=C;
	};
}

// -------------- toggle expand ---------------
//togglegadget
// Toggles the visibilty og an element as in PI expandable
// and prepares for next toggle
function toggleExpand(elt)
{
	contentElt=elt.parentNode.parentNode.getElementsByTagName('div')[1];
    if(elt.className.indexOf("off")!=-1)
    {
        contentElt.style.display="block";
        elt.className="on";
    }
    else
    {
        contentElt.style.display="none";
        elt.className="off";
    }

}
//eoftogglegadget
// those two are no longer produced by WXT:
function toggleExpandSimple(elt){    toggleExpand(elt)}
function toggleGadget(elt){    toggleExpand(elt)}
//eofexpand


//draggadget 
// modified from: http://www.quirksmode.org/js/dragdrop.html
var MAX_Z_INDEX=1;

function addEventSimple(obj,evt,fn) {
	if (obj.addEventListener)
		obj.addEventListener(evt,fn,false);
	else if (obj.attachEvent)
		obj.attachEvent('on'+evt,fn);
}

function removeEventSimple(obj,evt,fn) {
	if (obj.removeEventListener)
		obj.removeEventListener(evt,fn,false);
	else if (obj.detachEvent)
		obj.detachEvent('on'+evt,fn);
}

dragDrop = {
	initialMouseX: undefined,
	initialMouseY: undefined,
	startX: undefined,
	startY: undefined,
	draggedObject: undefined,
	initElement: function (element) {
		if (typeof element == 'string')
			element = document.getElementById(element);
		element.onmousedown = dragDrop.startDragMouse;
	//-------
	if (element.style.zIndex)
	{
		thisIndex=element.style.zIndex;
		if (thisIndex > MAX_Z_INDEX)
			MAX_Z_INDEX=thisIndex;
	}
	//-------
	},
	startDragMouse: function (e) {
		dragDrop.startDrag(this);
		var evt = e || window.event;
		dragDrop.initialMouseX = evt.clientX;
		dragDrop.initialMouseY = evt.clientY;
		addEventSimple(document,'mousemove',dragDrop.dragMouse);
		addEventSimple(document,'mouseup',dragDrop.releaseElement);
		return false;
	},
	startDrag: function (obj) {
		if (dragDrop.draggedObject)
			dragDrop.releaseElement();
		dragDrop.startX = obj.offsetLeft;
		dragDrop.startY = obj.offsetTop;
		dragDrop.draggedObject = obj;
		obj.className += ' dragged';
		dragDrop.draggedObject.style.zIndex=MAX_Z_INDEX+1;
		MAX_Z_INDEX=MAX_Z_INDEX+1;
	},
	dragMouse: function (e) {
		var evt = e || window.event;
		var dX = evt.clientX - dragDrop.initialMouseX;
		var dY = evt.clientY - dragDrop.initialMouseY;
		dragDrop.setPosition(dX,dY);
		return false;
	},

	setPosition: function (dx,dy) {
		dragDrop.draggedObject.style.left = dragDrop.startX + dx + 'px';
		dragDrop.draggedObject.style.top = dragDrop.startY + dy + 'px';
	},
	releaseElement: function() {
		removeEventSimple(document,'mousemove',dragDrop.dragMouse);
		removeEventSimple(document,'mouseup',dragDrop.releaseElement);
		dragDrop.draggedObject.className = dragDrop.draggedObject.className.replace(/dragged/,'');
		dragDrop.draggedObject = null;
	}
}
//eofdraggadget 
// --------- imagelists
// Add popup mousemove event
// was:.wxtimagelist
function initImages()
{    $('.wxtthumbwrapper').mousemove(function (e) {
	var popup = $('#wxt-img-popup'); // The one and only wxt-img-popup div
	if (popup.length) { // exist
		// Width and height of popup div
		var width = popup.width();
		var height = popup.height() + 20;			
		// Default position: upper right
		var x = e.pageX + 15;
		var y = e.pageY - height;
		// Move to lower position height > top space
		if (height > e.clientY) {
			y += height + 20;
		}
		// Move to left position if width > right space
		if (width > ($(window).width() - e.clientX - 50)) {
			x = x - width - 45;
		}
		// Update popup position		
		popup.css({
			left: x,
			top: y
		})

	}
});

// Add mouseover popup 
// was: .wxtimagelist .wxtthumbwrapper a img
$('.wxtthumbwrapper img').each(function () {
	$(this).hover(
		function (e) { // mouse enter
			var image = $(this);         // Get image
			var alink=$(this).parent();  // Get pagelink (if we need it)
			// Remove popup div if image clicked (before going to new page)
			image.click(function () {
				$('#wxt-img-popup').remove();
			});
			// Build popup outside viewport to avoid flickering
			html = '<div id="wxt-img-popup" style="left: -9999px;">';
			html += '<h3>'+image.attr('alt')+'</h3>';
			html += '<img src="' + image.attr('src') + '" />';
			html += '</div>';				
			$('body').append(html);
		},
		function () { // mouse leave
			$('#wxt-img-popup').remove(); // Remove popup div
		}
	);
});
}

// initializing
// a cleaner alternative is to call inits on body.onload or with final <script>
var initIsDone=false;
function trigger()
{
	if(!initIsDone)
	{
		if(document.readyState === "complete")
		{
			initImages();
			prepareCode();
			initIsDone=true;
		}
		else
		{
			setTimeout(trigger,100);
		}
	}
}
trigger();