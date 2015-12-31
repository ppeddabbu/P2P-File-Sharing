P2P File Sharing

This project creates a peer-to-peer network for file downloading. It resembles some features of Bit-torrent, but much simplified. There are two pieces of software – peer and
file owner. 

Steps to execute the project:

First place the 'config' file in this file under folder C:/p2p/ which is available in the current directory

Compile steps:
Go to src directory:
1. $\project_CN\src>javac edu\uf\helper\ChunkFileObject.java (class used by everyone)
2. $\project_CN\src>javac edu\uf\serv\Server_p.java
3. $\project_CN\src>javac edu\uf\cli1\Client_P1.java
4. $\project_CN\src>javac edu\uf\cli2\Client_P2.java
repeat for all the clients till 5

Run (note give forword slash to locate the class files)

from the same cmd window:
first run the server to distribute the non-overlapping chucks to all the peer clients
1. $\project_CN\src>java edu/uf/serv/Server_P
2. $\project_CN\src>java edu/uf/cli1/Client_P1
3. $\project_CN\src>java edu/uf/cli2/Client_P2
repeat to run all the clients

Please refer to all the console outputs under src folder
