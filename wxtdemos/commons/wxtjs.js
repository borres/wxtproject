/*
Minimalistic library for wxt
- popup as in PI popup:
     simplepopup(url,wname,wstyle)
- toggling visibility of
    - ajaxexpansions as in PI expand:
      toggleExpandAjax(address,elt)
    - simple inline expansions as in PI expandsimple:
      toggleExpandSimple(elt)
    - gadget expansion as in PI gadget:
      toggleGadget(elt)
- dragging gadgets

Relying on prototype.js (http://www.prototypejs.org/)
Modified material from: http://www.quirksmode.org/js/dragdrop.html

style classes for the actual elements must be set

B.Stenseth 2010.
*/

// ------------  popup ------------------
//popup
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

// -------------- simple expand ---------------
//simpleexpand
function toggleExpandSimple(elt)
{
    contentElt=elt.parentNode.parentNode.getElementsByTagName('div')[1];
    if(elt.getAttribute("class")=="off")
    {
        contentElt.style.display="block";
        elt.setAttribute("class","on");
    }
    else
    {
        contentElt.style.display="none";
        elt.setAttribute("class","off");
    }
}
//eofsimpleexpand

// -------------- ajax expand ---------------
//ajaxexpand
function toggleExpandAjax(address,elt)
{
    contentElt=elt.parentNode.parentNode.getElementsByTagName('div')[1];
    if(elt.getAttribute("class")=="off")
    {
        new Ajax.Request(address,
                 {method:'get',
                 onSuccess:function(transport){
                    var T=transport.responseText;
                    var pos1=T.indexOf('<pre');
                    var pos2=T.lastIndexOf('</pre');
                    if ((pos1 !=-1) && (pos2 > pos1))
                        T=T.substring(pos1,pos2+6);
                    contentElt.innerHTML=T;
                 },
                 onFailure:function(){contentElt.innerHTML="Could not access content"}
                 });
        elt.setAttribute("class","on");
    }
    else
    {
        contentElt.innerHTML=" ";
        elt.setAttribute("class","off");
    }
} 
//eofajaxexpand

//------------- toggle gadget ------------------
//togglegadget
function toggleGadget(elt)
{
    contentElt=elt.parentNode.parentNode.getElementsByTagName('div')[1];
    if(elt.getAttribute("class")=="off")
    {
        contentElt.style.display="block";
        elt.setAttribute("class","on");
    }
    else
    {
        contentElt.style.display="none";
        elt.setAttribute("class","off");
    }
}
//eoftogglegadget

// ------ dragging gadget -----
//draggadget
// modified from: http://www.quirksmode.org/js/dragdrop.html 
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