# -*- coding: cp1252 -*-
"""
This module simply takes a natural number and
returns the digits as text
"""
wordlist=['null','en','to','tre','fire','fem','seks','sju','åtte','ni']
def convert(N):
    T=str(N)
    res=''
    for c in T:
        pos='0123456789'.find(c)
        if pos!=-1:
            res+=wordlist[pos]+' '
        else:
            res='ikke et naturlig tall what
            return res
    return res[:-1]