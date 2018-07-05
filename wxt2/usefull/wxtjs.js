/*
Library for wxt
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
    - dragdrop
    - addEventSimple
    - removeEventSimple
- expanding and collapsing images
    - expandImage
    - collapseImage
    - hide

Relying on jQuery.js (http://jquery.com/)
        or prototype.js (http://www.prototypejs.org/)
Modified material from Quirksmode (http://www.quirksmode.org/js/dragdrop.html)

style classes for the actual elements must be set
see sample: wxtsyles.css distributed with WXT

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
    if(typeof jQuery != 'undefined')
        toggleExpandAjaxWithjQuery(address,elt)
    else if(typeof Prototype != 'undefined')
        toggleExpandAjaxWithPrototype(address,elt)
    else
    {
        contentElt=elt.parentNode.parentNode.getElementsByTagName('div')[1];
        if(elt.className=="off")
        {
        contentElt.innerHTML="<div>Could not access content.<br/> Missing JS-library (jQuery or Prototype)</div>";
        elt.className="on";
        }
        else
        {
        contentElt.innerHTML=' ';
        elt.className="off";
        }
    }
}
function toggleExpandAjaxWithjQuery(address,elt)
{
    contentElt=elt.parentNode.parentNode.getElementsByTagName('div')[1];
    if(elt.className=="off")
    {
        $.ajax({
		url:address,
		dataType:Text,
		success:function(data)//success(data, textStatus, XMLHttpRequest)
		{
                    var T=data;
                    var pos1=T.indexOf('<pre');
                    var pos2=T.lastIndexOf('</pre');
                    if ((pos1 !=-1) && (pos2 > pos1))
                            T=T.substring(pos1,pos2+6);
		  $(contentElt).html(T);
		},
		error:function(data)
		{
		  $(contentElt).text('Could not access content');
		}
		});
        elt.className="on";
    }
    else
    {
        $(contentElt).text(' ');
        elt.className="off";
    }
}
function toggleExpandAjaxWithPrototype(address,elt)
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
// draggadget
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

//expand and collapse images
// shown i wxtthumbwrapper as thumb
/*
<span class="wxtthumbwrapper">
	 <img src="bs8.gif" style="height:60px"
	      alt="bs"
		  onmouseover="expandImage(this,'imself')">
</span>
*/
// kept in wxtimcontainer, moved out of sight
/*
<div id="imself" >
<div  class="wxtimcontainer" >
	<div>
	<img src="self.gif" alt="bs" />
	<div>Dette er bilde self</div>
	</div>
</div>
</div>
*/

// remember which image is expanded
var expanded=null;
var expandedHTML=null;
var expandedID=null;

// expand when we enter the image thumb
function expandImage(hitelt,imid)
{
   //to make sure
   if(expanded)
		hide(expanded);
   // the thumb is wrapped
   var wrapper=hitelt.parentNode;
   expandedHTML=wrapper.innerHTML;
   expanded=wrapper;
   expandedID=imid;

   // the content we want to show is in element: imid
   var sElt=document.getElementById(imid);
   // it contains an image and a div for explanation
   var showElt=sElt.getElementsByTagName("div")[0];
   var imElt=sElt.getElementsByTagName("img")[0];
   // give it a takaway method
   sElt.onmouseout=collapseImage;
   // we want to expand at same place as wrapper
   var posX=wrapper.getElementsByTagName("img")[0].offsetLeft;
   var posY=wrapper.getElementsByTagName("img")[0].offsetTop;
   // and keep it within clientarea
   var imW=imElt.width;
   var imH=imElt.height;


   var cW=document.documentElement.clientWidth+
	      document.body.scrollLeft;
   // chrome issue:
   // document.documentElement.scrollTop allways return 0
   // document.body.scrollTop returns correct
   var deTop=document.documentElement.scrollTop;
   var boTop=document.body.scrollTop;

   var cH=document.documentElement.clientHeight+deTop;
   if(deTop==0 && boTop!=0)
	 cH=cH+boTop;


   if((posX+imW)>cW) posX=cW-imW-20;
   if((posY+imH)>cH) posY=cH-imH-30;
   if(posX<5) posX=5;
   if(posY<5) posY=5;

   // move it in and show it
   showElt.style.left=posX+'px';
   showElt.style.top=posY+'px';
   // ??
   //showElt.scrollIntoView(true);
   showElt.display="block";
}


// hide the expanded image
function hide(elt)
{
   var sElt=document.getElementById(expandedID);
   var showElt=sElt.getElementsByTagName("div")[0];
   // take it away
   showElt.style.left='-1000px';
   showElt.style.top='-1000px';
   showElt.display='none';
   expanded=null;
}

// collapse the image when we leave the imageblock
function collapseImage(e)
{
  // which element
  if(!e)//IE
    var e=window.event;
  var elt=undefined;
  if(e.target)
    elt=e.target;
  else //IE
    elt=e.srcElement;
  // we suppress mouseouts in sub elements
  if(!elt.className || (elt.className!='wxtimcontainer'))
  {
	e.cancelBubble = true;
	if (e.stopPropagation)
		e.stopPropagation();
  }
  else
	hide(elt);
}
//eofexpand and collapse images