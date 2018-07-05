var rotimg = new Image();   
rotimg.src = 'http://www.it.hiof.no/~borres/allround/selfimages/bs1.gif';
var angle=0.05; 
var rotcid='c5'

function drawHead()
{
	var canvas = document.getElementById(rotcid);
	if (canvas.getContext)
	{
  		var dc = canvas.getContext('2d');
		dc.restore();
		dc.fillStyle="rgb(256,256,256)";
		dc.fillRect(0,0,400,400);
		dc.translate(50,50);		
		dc.rotate(angle);
		dc.drawImage(rotimg, -25, -25, 50, 50);
		dc.translate(-50,-50);
		if(angle < 90)
			setTimeout(drawHead,100);
		dc.save();

	} 
	else 
	{
  		// canvas-unsupported
  		alert('cannot do canvas');
	}
}
