# tcpStub
## Description
tcpStub is a simple general purpose tcp listener which will accept and respond to inbound requests.

## Overview

1. The Listener opens a socket and waits for a request
2. The Listener spawns a worker (tcpWorker) to process the request.
3. A **Unique Identifier** is extracted from the input stream
4. The **Unique Identifier** is matched to a data set which contains a **response message**  
5. If a match is found the **response message** is queried to see if it has any **variables** required.
6. If **variables** are required they are processed and embedded in the **response message**
7. The **response message** is sent and the listener waits for next message.

## config.json

```
{
"ListenerVersion": "0.5",
"socketTimeout": 0,
"clientTimeout": 0,
"threadCount": 100
}
```
 - if **socketTimeout** and **clientTimeout** are set to 0 the socket will not timeout, this is normal behaviour
 - **threadcount** is not crtical at low volumes but may need to be tuned if high throughput is required.

## contentcheck.txt

```
substring;0,5
```

- Defines the content that is to be extracted from input line for comparison.
- Only supports substring at this time.

TODO: 
- expand to other options
- move to config.json

## requestresponse.txt

```
DLTBQ;ALTBA%sequenceNumber%%eventId%.....h%randNum%41001402707ITSP05.2501072
DLTBQ;PCTBA08C1F..... 015641000002603
RCTBQ;DCTBA08C1F..... 015641035702606HS01102014212000000000050601020304050612WINaPAANNNNNPa1Pda1QTeDSHWaPAANNNNNPa1Pda1QTReDQU aPAANNNNNPPPPPa1Pda1QWeDEX aPAANNNNNPPPPPa1Pda1PXReDTRIaPAANNNNNPPPPPa1Pda1bDSPRaSAANNNNNPPPPPa1Pda1RReDDD aPAANNNNNPPPPPa1Pda1bDDD1aPAANNNNNPPPPPa1Pda1bDOMNaPAANNNNNPPPPPa1Pda1QTeDEQDaCAANNNNNPPPPPa1Pda1QTReDP04aCAANNNNNPPPPPa1Pda1bDP06aCAANNNNNPPPPPa1Pda1ReD3563302
ACTBQ;RCTBA14C1F..... 015641000002602
DCTBQ14C1F;ACTBA14C1F..... 015641000002561
DCTBQ14C1F;DSTBA20C1F01... 015641027202621C022000C161111111111111111..10WINCe@@SHWCe@@QU Ce@@EX Ce@@TRICe@@SPRCe@@DD C02e@@OMNCe@@EQDC020304e@@P06C0203040506e@@10WIN011-16/SHW011-16/QU 011-16/EX 011-16/TRI011-16/SPR011-16/DD 011-16/1-24/OMN011-16/EQD011-16/1-24/1-15/1-18/P06011-16/1-24/1-15/1-18/1-8/1-8/....36137
ASTBQ20C1F01;RSTBA26C1F01... 015641000002632
DSTBQ26C1F01;ASTBA26C1F01... 015641000002611
DSTBQ26C1F01;endOfStream
```
Contains the responses to be sent when a matching contentcheck is found, 
- needs to be unique per line
- multiple matches will send multiple responses in sequence of entry in the file
- final response of a logical seqeunce should be duplicated with **endOfStream** which terminates connection. 
 - variables are defined in each line formatted %variableName% and must have a matching value in the **datavariables.txt** file

## datavariables.txt

```
sequenceNumber;substring;5,7
eventId;substring;7,10
randNum;randomNumber;0,100,%04d
```

- Each named variable (column 1) will match one defined in **requestresponse.txt**

Column 2 defines the type of the variable 
- Substring - extract variable from input line based on rules (column 3): First Pos, LastPos
- randomNumber - generate a random number based on rules (column 3): Min, Max, Format  

TODO:

- convert to json

