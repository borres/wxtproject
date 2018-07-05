/*
Minialistic library used on my websites
- a simple popup,
     simplepopup(url,wname,wstyle)
- general AJAX with text request/post,
     doRequest(theURL,params,targetId,waiter)
- expand/unexpand text request/get
     expand(address,targetNode)
     unexpand(address,targetNode)
All functions may be used directly, but are also supported by WXT
B.Stenseth 2007.
*/

// ------------  pop ------------------
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
  		// ??
  		alert('If you have blocked pop-ups\n Try Ctrl-Alt when clicking');
	  }
  }
//eofpopup

//------------- std ajax ------------------
function establishRequest()
{
    var theRequest=null;
    if (window.XMLHttpRequest)
        theRequest = new XMLHttpRequest();
    else if (window.ActiveXObject)
    {
        try { theRequest = new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch(e) {
            try {theRequest= new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch(e) {theRequest=null;}
        }
    }
    else
        theRequest=null;
    return theRequest;
}

function postRequest(theRequest,params,targetId,theUrl)
{
    theRequest.onreadystatechange =
        function( ) {
                    processRequestChange(theRequest,targetId);
        };
    theRequest.open("POST", theUrl,true);
    theRequest.setRequestHeader("Content-type", 
                               "application/x-www-form-urlencoded");
    theRequest.setRequestHeader("Content-length", params.length);
    theRequest.setRequestHeader("Connection", "close");
    theRequest.send(params);
}

function processRequestChange( aRequest,aTarget )
{
    if (aRequest.readyState == 4) {
        if ((aRequest.status == 200) || (aRequest.status == 304))
            document.getElementById(aTarget).innerHTML=
                aRequest.responseText;
        else
        {
            alert("Problems accessing data:\n" + aRequest.statusText);
            document.getElementById(aTarget).innerHTML=
                aRequest.statusText;
        }

    }
}

//------------------------ general ajax ------------------------
function doRequest(theURL,params,targetId,waiter)
{
    var myRequest=establishRequest();
    if (myRequest)
    {
        if(waiter)
        {
          if(waiter == 'std')
            waiter='http://www.ia.hiof.no/~borres/common/gfx/waiter.gif';
          try{
          	document.getElementById(targetId).innerHTML=
          	                       '<img src="'+waiter+'" alt=" "/>';
          	}
          catch(E){}
        }
        postRequest(myRequest,params,targetId,theURL);
    }
    else
  	{
        alert('Browser will not do a request');
    }
}

function doGetRequest(theURL,targetId,waiter)
{
    var myRequest=establishRequest();
    if (myRequest)
    {
        if(waiter)
        {
          if(waiter == 'std')
            waiter='http://www.ia.hiof.no/~borres/common/gfx/waiter.gif';
          try{
          	document.getElementById(targetId).innerHTML=
          	                     '<img src="'+waiter+'" alt=" "/>';
          	}
          catch(E){}
        }
        myRequest.onreadystatechange =
          function( ) {
                      processRequestChange(myRequest,targetId);
          };
        myRequest.open("GET", theURL, true);
        myRequest.send(null);
    }
    else
  	{
        alert('Browser will not do a request');
    }
}


// ------------------------ expansion ----------------------
//expand
// dependent of style-class: onoff for visible effect
function expand(address,targetNode){
	var myRequest=establishRequest();
	if(myRequest)
	{
		myRequest.onreadystatechange = function( ) {
                    useExpansion(myRequest,address,targetNode);
        };
		myRequest.open("GET", address, true);
		myRequest.send(null);
	}
}

function useExpansion(theRequest,address,targetNode)
{
    // if the request is complete and successfull
    if (theRequest.readyState == 4) {
        if ((theRequest.status == 200) || (theRequest.status == 304))
        {
            var T=theRequest.responseText;
            var pos1=T.indexOf('<pre');
            var pos2=T.lastIndexOf('</pre');
            if ((pos1 !=-1) && (pos2 > pos1))
            	T=T.substring(pos1,pos2);
            T='<span class="onoff" '+
              'onclick="unexpand(\''+address+'\',this.parentNode)">-</span>'
              +T;
            targetNode.innerHTML=T;
        }
        else
          {
            alert("Problem med tilgang til data:\n" + theRequest.statusText);
            targetNode.innerHTML=theRequest.statusText;
          }
    }
}

function unexpand(address,targetNode){
    T='<span class="onoff" '+
    'onclick="expand(\''+address+'\',this.parentNode);">+</span>';
    targetNode.innerHTML=T;
}
//eofexpand