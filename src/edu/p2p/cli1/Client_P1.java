package edu.p2p.cli1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import edu.p2p.helper.ChunkFileObject;

public class Client_P1 {

	static String root = "C:/p2p/";
	static String baseLocation = root + "/ClientP1";
	static String chunksLocation = baseLocation + "/chunks";
	ServerSocket receiveSocket;
	Socket connSocket;
	Socket cliSocket;
	ObjectInputStream inStream;
	ObjectOutputStream outStream;
	Set<Integer> chunkList;
	private int mainServport;
	private int clientServport;
	private int clientNeighborPort;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client_P1 c = new Client_P1();
		
		// create dir chunks
		new File(baseLocation + "/chunks/").mkdirs();
		
		String clientConf = null;
		int totalFilesToRecv;
		try {
			c.readPortValues();
			c.TCPCliConnect(c.mainServport);

			clientConf = (String) c.inStream.readObject();

			totalFilesToRecv = (int) c.inStream.readObject();

			c.chunkList = Collections.synchronizedSet(new LinkedHashSet<Integer>());
			for (int i = 1; i <= totalFilesToRecv; i++)
				c.chunkList.add(i); // while removing remove using object
									// notation

			int filesToReceive = (int) c.inStream.readObject();
			while (filesToReceive > 0) {
				ChunkFileObject rChunkObj = c.receiveChunk();
				if (rChunkObj != null)
					c.createChunkFile(chunksLocation, rChunkObj);
				else
					System.out.println("chunk received is null");
				filesToReceive--;
			}
			c.TCPCliDisconnect();

		// Done with the server, who are my clients??
		String[] tokens = clientConf.split(" ");
		int port = Integer.parseInt(tokens[1]);
		//int neighbor1 = Integer.parseInt(tokens[2]);
		// int neighbor2=Integer.parseInt(tokens[3]);
		//start my server to serve the files and
		// invoke client neighbour, try to get the chunks you don't have
		// 'periodically' that's it
		// i think you need to send the list of files you have...
		//one you send all the files in the list to some client exit
		//c.TCPCliServconnect(21001); // TODO parallel execute
		
		Thread thread =new Thread(new Runnable(){
			public void run(){
				c.TCPCliServconnect(c.clientServport);
			
		}});
		thread.start();
		
	
		//is set is not emp
		//client's job -ping neighbour server client and get the files you need
		//ping neighbour2
		c.TCPCliConnect(c.clientNeighborPort); // TODO hard coded as of now, need to get from server
		System.out.println("client1 totalFilesToRecv:"+totalFilesToRecv);
		
		while(true)
		{
			System.out.println("client1 files to download:"+c.chunkList);
			//is parallel execution happening?? no
			
			if(!c.chunkList.isEmpty())
			{
				Integer[] a = c.chunkList.toArray(new Integer[c.chunkList.size()]);

				for(int i=0;i<a.length;i++)
				{
				//System.out.println("c111");
				c.outStream.writeObject(a[i]);//itr.next());
				c.outStream.flush();
				ChunkFileObject rChunkObj = c.receiveChunk();
				if (rChunkObj != null)
					c.createChunkFile(chunksLocation, rChunkObj);
				}
			}
			else
			{
				//System.out.println("c1111");
				//I need no more .... go out and combine
				c.outStream.writeObject(-1);
				c.outStream.flush();
				break;
			}
			Thread.sleep(2000);
		}
		c.TCPCliDisconnect();
		c.combineChunks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void TCPCliServconnect( int port) {
		try {
			int neighbourCount = 1; // initialized to 0
			receiveSocket = new ServerSocket(port);
			System.out.println(this.getClass().getName()+"Client1-Server socket created, accepting connections...");
			//only 2 connections with neighbours
			while (true) {
				//System.out.println(clientMap);
				if (neighbourCount>0) {
					neighbourCount--;
					connSocket = receiveSocket.accept();
					System.out.println("new client connection accepted:" + connSocket);
					// create thread and pass the socket n files to handle
					new CliServerThread(connSocket,chunksLocation).start();

				} else {
					System.out.println("Cannot serve more clients, i am done!");
					break;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void TCPCliConnect(int port) throws InterruptedException {
		boolean b=true;
		while(b)
		{
		try {

			b=false;
			cliSocket = new Socket("127.0.0.1", port);
			System.out.println(" client1 connected to :" + cliSocket);
			//
			inStream = new ObjectInputStream(cliSocket.getInputStream());
			//System.out.println(" c1-1");
			outStream = new ObjectOutputStream(cliSocket.getOutputStream());
			//System.out.println(" c1-1-1");
		}
		catch(ConnectException e)
		{

			System.out.println("unable to connect to socket at: "+port+"... trying again...");
		    Thread.sleep(5000);
			b=true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		}
	}

	public ChunkFileObject receiveChunk() {
		ChunkFileObject chunkObj = null;
		try {
			//System.out.println("receive object");
			chunkObj = (ChunkFileObject) inStream.readObject();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return chunkObj;
	}

	public void TCPCliDisconnect() {
		try {
			inStream.close();
			//System.out.println(" closing client p1 connection..." + cliSocket);
			cliSocket.close();
			System.out.println("client1 connection closed:"+cliSocket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void createChunkFile(String chunksLocation, ChunkFileObject rChunkObj) {
		try {
			System.out.println("create back received chunk - " + rChunkObj.getFileName());
			FileOutputStream fileOutStream = new FileOutputStream(new File(chunksLocation, rChunkObj.getFileName()));
			BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileOutStream);
			bufferOutStream.write(rChunkObj.getFileData(), 0, rChunkObj.getChunksize());

			// update chunklist to be received
			chunkList.remove(rChunkObj.getFileNum());

			bufferOutStream.flush();
			bufferOutStream.close();
			//System.out.println("done...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void readPortValues() throws FileNotFoundException
	{
		String str=null;
		
		BufferedReader br = new BufferedReader(new FileReader(root + "/config.txt"));
		try {
			str = br.readLine();
			String[] tokens = str.split(" ");
			mainServport = Integer.parseInt(tokens[1]);
			//for client1 read line1
			for(int i=1 ;i<=1;i++)
			{
				str = br.readLine();
			}
			tokens = str.split(" ");
			clientServport = Integer.parseInt(tokens[1]);
			int clientNeighbor= Integer.parseInt(tokens[2]);
			
			br.close();
			
			BufferedReader br1 = new BufferedReader(new FileReader(root + "/config.txt"));
			for(int i=0;i<=clientNeighbor;i++)
			{
				str = br1.readLine();
			}
			tokens = str.split(" ");
			 clientNeighborPort = Integer.parseInt(tokens[1]);
			 br1.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void combineChunks() {

		String chunksLocation = baseLocation + "/chunks";
		File[] files = new File(chunksLocation).listFiles();
		byte[] chunk = new byte[102400]; // this buffer size could be
											// anything
		new File(baseLocation + "/out/").mkdirs();
		try {

			Random r = new Random();
			FileOutputStream fileOutStream = new FileOutputStream(
					new File(baseLocation + "/out/" + r.nextInt(500) + files[0].getName()));
			BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileOutStream);
			for (File f : files) {
				FileInputStream fileInStream = new FileInputStream(f);
				BufferedInputStream bufferInStream = new BufferedInputStream(fileInStream);
				int bytesRead = 0;
				while ((bytesRead = bufferInStream.read(chunk)) > 0) {
					bufferOutStream.write(chunk, 0, bytesRead);
				}
				fileInStream.close();
			}
			fileOutStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class CliServerThread extends Thread {

	private Socket socket;
	//File[] files;
	ObjectOutputStream outStream;
	ObjectInputStream inStream;
	String chunkLoc;
	//ArrayList<Integer> chunkList;
	//String configClient;

	CliServerThread(Socket s, String chunkLoc){//, ArrayList<Integer> cl, String str) {
		this.socket = s;
		//this.files = files;
		this.chunkLoc=chunkLoc;
		//this.chunkList = cl;
		//this.configClient = str;
	}

	public void run() {
		try {
			//System.out.println("cs1");
			// get output stream
			outStream = new ObjectOutputStream(socket.getOutputStream());
			//get in stream
			inStream = new ObjectInputStream(socket.getInputStream());
			//System.out.println("cs11");

			while(true)
			{
			//assume client will ask what it chunk num it wants...so instream
			int ChunkNum= (int)inStream.readObject();
			if(ChunkNum<0)//client it done
				break;
			//check whether you have it, yes send it else send dummy -1 chunk object
			//check
			File[] files = new File(chunkLoc).listFiles();
			String[] s;
			File currentFile=null;
			boolean haveFile=false;
			for(int i=0;i< files.length;i++)
			{
				//this guy is the culprit.... u have maintain the have list...or does it contain??
				// sort the files... in server and send alw
				currentFile=files[i];
				s=files[i].getName().split("_");
				if(ChunkNum == Integer.parseInt(s[0]))
				{
					haveFile=true;
					break;		
				}
			}
			ChunkFileObject sChunkObj;
			if(haveFile)
			{
				//send right object that currentFile is holding
				sChunkObj = constructChuckFileObject(currentFile, ChunkNum);
			}
			else
			{
				//send dummy object to inform no object is present -1
				sChunkObj = constructChuckFileObject(null, -1);
			}
			/*
			 * Total no of files clients need to have... info will be passed to
			 * each client from here
			 */
			//outStream.writeObject(configClient);

			//outStream.writeObject(files.length);

			//outStream.writeObject(chunkList.size());

			//for (int i = 0; i < chunkList.size(); i++) {
				// construct the chunk object
				 
				// send file
				sendChunkObject(sChunkObj);
				// let's intro sleep
			//}
			// disconnect only when client is done!!! receive some interrupt.. what about  -1
			// what about instance of check for requested object??
				//System.out.println("cs111");
				//if((int)inStream.readObject()<0)
				//		
				//Thread.sleep(1000);
			}		
			TCPServDisconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized ChunkFileObject constructChuckFileObject(File file, int chunkNum) throws IOException {
		ChunkFileObject chunkObj = null;
		if(chunkNum>0)
		{
			byte[] chunk = new byte[102400]; // should be 100kb, see demo
			chunkObj = new ChunkFileObject();
			System.out.println("construct chunk object to send- " + file.getName());
	
			chunkObj.setFileNum(chunkNum);
	
			chunkObj.setFileName(file.getName());
			FileInputStream fileInStream = new FileInputStream(file);
	
			BufferedInputStream bufferInStream = new BufferedInputStream(fileInStream);
	
			int bytesRead = bufferInStream.read(chunk);
	
			chunkObj.setChunksize(bytesRead);
	
			chunkObj.setFileData(chunk);
	
			bufferInStream.close();
			fileInStream.close();
		}
		return chunkObj;
	}

	public void sendChunkObject(ChunkFileObject sChunkObj) {
		try {
			//System.out.println("send chunk object");
			outStream.writeObject(sChunkObj);
			outStream.flush();
			//System.out.println("done...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void TCPServDisconnect() {
		try {
			outStream.close();
			//System.out.println("file out stream closed...");
			socket.close();
			System.out.println("Client1- server socket closed :" + socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
