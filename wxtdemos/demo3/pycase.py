#!/usr/bin/python
# -*- coding: UTF-8 -*-


from xml.dom.minidom import parse
import cgi, cgitb;cgitb.enable()
import codecs,sys


page="""<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
</head>

<body>
%s
</body>
</html>
"""
#start
# convert on output
def setPrint():
    """ Convert Unicode -> UTF-8"""
    (e,d,sr,sw) = codecs.lookup('utf-8')
    unicode_to_utf8 = sw(sys.stdout)
    sys.stdout = unicode_to_utf8
#stop    
# collect all text in a node
def getText(nodelist):
    rc = ''
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            #t=node.data.encode('iso-8859-1')
            t=node.data
            rc += t
    return rc

def establishDom():
    dom = parse('vildanden.xml')
    return dom

def getEmos(dom):
    t=""
    emolist=dom.getElementsByTagName('EMO')
    for em in emolist:
        t=t+'\n'+getText(em.childNodes)
    return t




setPrint()    
print "Content-type: text/html; charset=utf-8\n"
dom=establishDom()
s=getEmos(dom)
t=page%s

print t
