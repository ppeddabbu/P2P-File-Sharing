P2P File Sharing

This project creates a peer-to-peer network for file downloading. It resembles some features of Bit-torrent, but much simplified. There are two pieces of software – peer and
file owner. 

Steps to execute the project:

First place the 'config' file under the folder C:/p2p/ (Since I am using hardcoded location, change this if needed)

Importing the code into eclipse and run it. This is a straight forward else else if you want to do the same from cmd then follow the below steps

Compile steps:
Go to src directory:
1. $\project\src>javac edu\p2p\helper\ChunkFileObject.java (class used by everyone)
2. $\project\src>javac edu\p2p\serv\Server_p.java
3. $\project\src>javac edu\p2p\cli1\Client_P1.java
4. $\project\src>javac edu\p2p\cli2\Client_P2.java
repeat for all the clients till 5

Run (note give forword slash to locate the class files)

from the same cmd window:
first run the server to distribute the non-overlapping chucks to all the peer clients
1. $\project\src>java edu/p2p/serv/Server_P
2. $\project\src>java edu/p2p/cli1/Client_P1
3. $\project\src>java edu/p2p/cli2/Client_P2
repeat to run all the clients

Please refer to all the console outputs under src folder
