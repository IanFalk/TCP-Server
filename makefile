JFLAGS = -g
JC = javac
JVM= java 
FILE=
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        ManualTCPClient.java \
        AutoTCPClient.java \
        TCPServer.java \
        TCPConnection.java

default: classes
classes: $(CLASSES:.java=.class)

run:
	start cmd /k "$(JVM) TCPServer 6789"
	TIMEOUT /T 1
	start cmd /k $(JVM) AutoTCPClient 6789 6789 Sarah
	start cmd /k $(JVM) AutoTCPClient 6790 6789 John
	start cmd /k $(JVM) AutoTCPClient 6791 6789 Smith
