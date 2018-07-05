/*
Minialistic library for obama
- popup:
	simplepopup(url,wname,wstyle)
- change visibility when using summary in xlink
	changestyle(eltId,style)
- expand/unexpand:
	expand(address,targetNode)
	unexpand(address,targetNode)
Using prototype from : http://www.prototypejs.org(version 1.6) 
B.Stenseth 2008.
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


//-----------  change style ----------
//change style    
function changestyle(eltId, style) 
{
	var elt=document.getElementById(eltId);
	if(elt)
		elt.style.visibility = style;
}
//eofchange style

//------------- expansion ------------------
// dependent of style-classes: on and off for visible effect
// dependant of prototype.js
// expand
function expand(address,targetNode)
{
	new Ajax.Request(address,
	{
		method:'get',
		onSuccess:function(transport)
		{
			var T=transport.responseText || "no respons";
			var pos1=T.indexOf('<pre');
			var pos2=T.lastIndexOf('</pre');
			if ((pos1 !=-1) && (pos2 > pos1))
				T=T.substring(pos1,pos2);
			T='<span class="expand" '+
			'onclick="unexpand(\''+address+'\',this.parentNode)"><span class="off">-</span></span>'
			+T;
			targetNode.innerHTML=T;
		},
		onFailure:function(){targetNode.innerHTML="no data loaded";}
		
	});
}

function unexpand(address,targetNode)
{
	T='<span class="expand" '+
	'onclick="expand(\''+address+'\',this.parentNode);"><span class="on">+</span></span>';
	targetNode.innerHTML=T;
}
//eofexpand