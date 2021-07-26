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
{
  "ListenerVersion": "1.0",
  "socketTimeout": 0,
  "clientTimeout": 0,
  "threadCount": 100,
  "port": 3535,
  "configCheck": [
	{
		"type": "substring",
		"startPos" : 0,
		"endPos" : 5
	}
  ]
}
}
```
 - if **socketTimeout** and **clientTimeout** are set to 0 the socket will not timeout, this is normal behaviour
 - **threadcount** is not crtical at low volumes but may need to be tuned if high throughput is required.
 - **port** port number to listen on.  
-  **configCheck** defines the content that is to be extracted from input line for comparison.
    - Only supports substring at this time.
TODO: 
- expand to other options

## requestresponse.json

```
{
	"response" : [
		{
			"name" : "DLTBQ",
			"response" : "ALTBA%sequenceNumber%%eventId%.....h%currentTime%001402707ITSP05.2501%randNum%"
		},
		{
			"name" : "DLTBQ",
			"response" : "endOfStream"
		}
	]
}
```
Contains the responses to be sent when a matching contentcheck is found, 
- needs to be unique per input line
- multiple matches will send multiple responses in sequence of entry in the file
- the final response of a logical seqeunce should be duplicated with **endOfStream** which terminates connection. 
 - variables are defined in each line formatted %variableName% and must have a matching value in the **datavariables.json** file

## datavariables.json

```
{
	"variable": [
		{
			"name" : "sequenceNumber",
			"type" : "substring",
			"format" : [
				{
					"startPos" : 5,
					"endPos" : 7
				}
			]
		},
		{
			"name" : "randNum",
			"type" : "randomNumber",
			"format" : [
				{
					"min" : 0,
					"max" : 100,
					"format" : "%04d"
				}
			]
		},
		{
			"name" : "eventId",
			"type" : "substring",
			"format" : [
				{
					"startPos" : 7,
					"endPos" : 10
				}
			]
		},
		{
			"name" : "currentTime",
			"type" : "date",
			"format" : [
				{
					"format" : "HHmmss"
				}
			]
		},
		{
			"name" : "GUID",
			"type" : "guid"
		},
		{
			"name" : "incNum",
			"type" : "IncrementNumber",
			"format" : [
				{
					"default" : "0",
					"format" : "%04d"
				}
			]
		}
	]
}
```

- Each variable **name** may match one defined in **requestresponse.json**
- **type** defines the type of variable required
- **format** defines the format of the variable type  

**Support**
- **substring** - extracts from input line based on format: startPos and endPos 
- **randomNumber** - generate a random number based on format: Min, Max and output format
- **date** - generate current date based on format: date format.
- **IncrementNumber** an incrementing number based on format: number format
- **guid** a guid

TODO:

- expand number of types.