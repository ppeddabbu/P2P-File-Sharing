package edu.p2p.serv;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import edu.p2p.helper.ChunkFileObject;
/*
* Main class who breaks the file that needs to be distributed among its client's based on config file
* Once distribution is done it will exit from p2p network
*/
public class Server_P {

	ServerSocket receiveSocket;
	Socket connSocket;
	static String root = "C:/p2p";
	static String baseLocation = root + "/Server";
	int sizeOfEachChunk = 102400;
	private int mainServport;
	// have both these in sync
	static Map<Integer, ArrayList<Integer>> clientMap;
	static Map<Integer, String> clientMapConf;

	public static void main(String[] args) {

		Server_P s = new Server_P();
		try {
			
			// break the input file into chunks
			s.divideInputFile();
			// read conf file
			s.readPortValues();
			String str;
			int clients = 0;
			clientMapConf = new LinkedHashMap<Integer, String>();
			BufferedReader br = new BufferedReader(new FileReader(root + "/config.txt"));
			//first line for server
			br.readLine();
			
			while ((str = br.readLine()) != null)
				clientMapConf.put(++clients, str);

			String chunksLocation = baseLocation + "/chunks";
			File[] files = new File(chunksLocation).listFiles();

			int chunkCount = files.length;
			System.out.println("No. of Chunks:" + chunkCount);

			clientMap = new LinkedHashMap<Integer, ArrayList<Integer>>();
			for (int i = 1; i <= clients; i++) {
				ArrayList<Integer> arr = new ArrayList<Integer>();
				for (int j = i; j <= chunkCount; j += clients) {
					arr.add(j);
				}
				clientMap.put(i, arr);

			}
			System.out.println(clientMap);

			if (chunkCount > 0) {
				// wait for the connections to initiate
				s.TCPServconnect(files);
			} else
				System.out.println("There are no files in chunks folder!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
// may be I can break this code??
	public void TCPServconnect(File[] files) {
		try {
			int client = 0; // initialized to 0
			receiveSocket = new ServerSocket(mainServport);
			System.out.println("Main Server socket created, accepting connections...");
			while (true) {
				System.out.println(clientMap);
				client++;
				if (client <= clientMap.size()) {
					connSocket = receiveSocket.accept();
					System.out.println("new client connection accepted :-" + connSocket);
					// create thread and pass the socket n files to handle
					new ServerThread(connSocket, files, clientMap.get(client), clientMapConf.get(client)).start();

				} else {
					System.out.println("Cannot serve more clients, i am done!");
					break;
				}

			}

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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void divideInputFile() {

		try {
			//xyz.mp4
			// main file that is to be broken into pieces
			Scanner sc=new Scanner(System.in);
			System.out.println("Please place the file in c:p2p folder and Enter the filename:");
			String input=sc.nextLine();
			 //"FLR.pdf"
			File inputFile = new File(baseLocation +"/"+input);
			Long fileLength = inputFile.length();

			System.out.println("Input File size :" + fileLength);

			String newdir = inputFile.getParent() + "/chunks/";
			File outFolder = new File(newdir);
			if (outFolder.mkdirs())
				System.out.println("Chunks Folder created");
			else
				System.out.println("Chunks folder already exits or unable to create folder for chunks");

			byte[] chunk = new byte[102400];

			FileInputStream fileInStream = new FileInputStream(inputFile);

			BufferedInputStream bufferStream = new BufferedInputStream(fileInStream);
			int index = 1;
			int bytesRead;
			// chuck will be populated with data
			while ((bytesRead = bufferStream.read(chunk)) > 0) {
				FileOutputStream fileOutStream = new FileOutputStream(
						new File(newdir, String.format("%04d", index) + "_" + inputFile.getName()));
				BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileOutStream);
				bufferOutStream.write(chunk, 0, bytesRead);
				bufferOutStream.close();
				index++;
			}
			bufferStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class ServerThread extends Thread {

	private Socket socket;
	File[] files;
	ObjectOutputStream outStream;
	ArrayList<Integer> chunkList;
	String configClient;

	ServerThread(Socket s, File[] files, ArrayList<Integer> cl, String str) {
		this.socket = s;
		this.files = files;
		this.chunkList = cl;
		this.configClient = str;
	}

	public void run() {
		try {
			// get output stream
			outStream = new ObjectOutputStream(socket.getOutputStream());

			/*
			 * Total no of files clients need to have... info will be passed to
			 * each client from here
			 */
			outStream.writeObject(configClient);

			outStream.writeObject(files.length);

			outStream.writeObject(chunkList.size());
			Arrays.sort(files);
			for (int i = 0; i < chunkList.size(); i++) {
				// construct the chunk object
				ChunkFileObject sChunkObj = constructChuckFileObject(files[chunkList.get(i) - 1], chunkList.get(i));
				// send file
				sendChunkObject(sChunkObj);
				// let's intro sleep
				Thread.sleep(1000);
			}
			// disconnect
			TCPServDisconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ChunkFileObject constructChuckFileObject(File file, int chunkNum) throws IOException {
		byte[] chunk = new byte[102400]; // should be 100kb, see demo
		System.out.println("construct object - " + file.getName());
		ChunkFileObject chunkObj = new ChunkFileObject();

		chunkObj.setFileNum(chunkNum);

		chunkObj.setFileName(file.getName());
		FileInputStream fileInStream = new FileInputStream(file);

		BufferedInputStream bufferInStream = new BufferedInputStream(fileInStream);

		int bytesRead = bufferInStream.read(chunk);

		chunkObj.setChunksize(bytesRead);

		chunkObj.setFileData(chunk);

		bufferInStream.close();
		fileInStream.close();

		return chunkObj;
	}

	public void sendChunkObject(ChunkFileObject sChunkObj) {
		try {

			outStream.writeObject(sChunkObj);
			outStream.flush();
			System.out.println("send object done...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void TCPServDisconnect() {
		try {
			outStream.close();
			//System.out.println("file out stream closed...");
			socket.close();
			System.out.println("Main Server socket closed:" + socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
