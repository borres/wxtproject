
function drawSinus(cid)
{
	var canvas = document.getElementById(cid);
	if (canvas.getContext)
	{
  		var dc = canvas.getContext('2d');
		dc.strokeStyle = "rgb(0,0,0)";		
		dc.beginPath();
		dc.moveTo(0,100);
		dc.lineTo(360,100);
		dc.stroke();
		
		dc.strokeStyle = "rgb(200,0,0)";
		dc.beginPath();
		dc.moveTo(0,100);
		for (v=0; v<360; v++)
		{
			rad=1.0*v*(2.0*Math.PI)/360.0;
			dc.lineTo(v,100-Math.round(100*Math.sin(rad)));
		}
		dc.stroke();
	} 
	else 
	{
  		// canvas-unsupported
  		alert('cannot do canvas');
	}
}
