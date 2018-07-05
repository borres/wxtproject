function unexpand(address,targetNode){
    T='<span class="expand" '+
    'onclick="expand(\''+address+'\',this.parentNode);"><span class="on">+</span></span>;
    targetNode.innerHTML=T;
}