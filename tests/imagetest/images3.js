// expand and collapse images
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
   showElt.scrollIntoView(true);
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

// collapse the image when we leave the block
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
// eofexpand and collapse images