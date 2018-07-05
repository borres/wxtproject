
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
