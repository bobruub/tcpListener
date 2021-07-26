FROM java:openjdk-8-jdk-alpine

RUN apk update 

RUN mkdir /tmp/tcpListner
RUN mkdir /tmp/tcpListner/data

WORKDIR /tmp/tcpListener

ADD ./build/tcpListener.jar tcpListener.jar
ADD ./build/javax.json-1.0.jar javax.json-1.0.jar
ADD ./build/runtcplistener.sh runtcplistener.sh
EXPOSE 2525
COPY ./build/data/* /tmp/tcpListner/data/

RUN chmod 777 /tmp/tcpListener/runtcplistener.sh
CMD /tmp/tcpListener/runtcplistener.sh