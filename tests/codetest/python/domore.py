"""
 Reading a wikipedia file
"""
import httplib, urllib
import sys
import xml.dom.minidom

try:
    # prepare header.
    # wikipedia demands a useragent, may be anything ?
    headers = {'User-Agent': "myself"}
    # connect
    conn = httplib.HTTPConnection("en.wikipedia.org")
    parameters=None
    conn.request("GET", "/wiki/Snow_leopard", parameters, headers)
    r1 = conn.getresponse()
    # ok ?
    if r1.status!=200:
        print 'access problem'
        print r1.status, r1.reason
        sys.exit(0)
    else:
        # get data as text
        data = r1.read()
        #print data
    conn.close()
except:
    print 'cannot access data'
    sys.exit(0)
    
# since wikipedia pages are wellformed we can build a dom
# from the data we found simply by adding a xml-header
#from
page="""<?xml version="1.0" encoding="utf-8"?>
   %s
   """
try:
    dom=xml.dom.minidom.parseString(page%data)
except:
    res=sys.exc_info()
    print res[1]
    sys.exit(0)
    
# and we can use the dom or/and the tekst, depending on our purpose
paragraphs=dom.getElementsByTagName('p')
print 'Teksten er '+str(len(data))+' tegn lang
print 'Det er '+str(len(paragraphs))+ ' paragrafer i teksten'
#to