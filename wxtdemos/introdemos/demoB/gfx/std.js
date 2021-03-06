/*
Minimalistic library for wxt
- popup:
     simplepopup(url,wname,wstyle)
	 used by PI: popup
- ajaxbased expand/unexpand:
     expand(address,targetNode)
     unexpand(address,targetNode)
	 Relying on prototype.js (http://www.prototypejs.org/)
	 used by PI: expand
B.Stenseth 2009.
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

//------------- expansion ------------------
// dependent of style-class: onoff for visible effect
// and prototype.js v1.6 from http://www.prototypejs.org/
//expand
function expand(address,targetNode){
	new Ajax.Request(address,
	                 {method:'get',
					 onSuccess:function(transport){
						var T=transport.responseText;
						var pos1=T.indexOf('<pre');
						var pos2=T.lastIndexOf('</pre');
						if ((pos1 !=-1) && (pos2 > pos1))
							T=T.substring(pos1,pos2+6);
						T='<span class="expand" '+
						  'onclick="unexpand(\''+address+
						  '\',this.parentNode)"><span class="off">-</span></span>'
						  +T;
						targetNode.innerHTML=T;
					 },
					 onFailure:function(){targetNode.innerHTML="Could not access content"}
					 });
}
//eofexpand
//unexpand
function unexpand(address,targetNode){
    T='<span class="expand" '+
    'onclick="expand(\''+address+'\',this.parentNode);"><span class="on">+</span></span>';
    targetNode.innerHTML=T;
}
//eofunexpand