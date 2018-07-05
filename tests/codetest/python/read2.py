import httplib, urllib
'''
Reading some data with a http request
'''
try:
    # prepare parameters
    params = urllib.urlencode({'navn': 'ole', 'adresse': 'mysen'})
    # prepare header
    headers = {"Content-type": "application/x-www-form-urlencoded",
               "Accept": "text/plain"}
    # connect
    conn = httplib.HTTPConnection("www.ia.hiof.no")
    conn.request("POST", "/~borres/cgi-bin/forms/std.py", params, headers)
    r1 = conn.getresponse()
    # ok ?
    if r1.status!=200:
        print 'access problem'
        print r1.status, r1.reason
    else:
        # get data
        data = r1.read()
        print data
    conn.close()
except:
    print 'cannot access data'
