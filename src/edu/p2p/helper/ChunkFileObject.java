package edu.p2p.helper;

public class ChunkFileObject implements java.io.Serializable {
	/*
	 *  
	*/
	private static final long serialVersionUID = 1L;
	private int fileNum;
	private String fileName;
	private byte[] fileData;
	private int chunksize;
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	public int getChunksize() {
		return chunksize;
	}

	public void setChunksize(int chunksize) {
		this.chunksize = chunksize;
	}

	public int getFileNum() {
		return fileNum;
	}

	public void setFileNum(int fileNum) {
		this.fileNum = fileNum;
	}

}
