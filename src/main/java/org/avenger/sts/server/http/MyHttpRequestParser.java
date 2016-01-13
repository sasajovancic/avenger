package org.avenger.sts.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class MyHttpRequestParser {
	private ReadableByteChannel rbytech;
	private byte[] _requestLine = null;
	
	public MyHttpRequestParser(ReadableByteChannel rbytech){
		this.rbytech = rbytech;
	}
	
	public String getRequestLine() {
		if (_requestLine == null) {
			_requestLine = _readRequestLine();
		}
		return new String(_requestLine);
	}
	
	private byte[] _readRequestLine() {
		ByteBuffer inbuf = ByteBuffer.allocate(48);
		try {
			int bytesRead = rbytech.read(inbuf);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	/*
	 * public static void main(String[] args) throws IOException 
    {
        RandomAccessFile aFile = new RandomAccessFile
                ("F:\\DetailsMy1.txt", "r");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1);
        StringBuffer line = new StringBuffer();
        while(inChannel.read(buffer) > 0)
        {
            buffer.flip();
            for (int i = 0; i < buffer.limit(); i++)
            {
                char ch = ((char) buffer.get());
                if(ch=='\r'){
                    System.out.print(line+"[EOL]");
                    line=new StringBuffer();
                }else{
                    line.append(ch);
                }
            }
            buffer.clear(); // do something with the data and clear/compact it.
        }
        inChannel.close();
        aFile.close();
    }
	 * 
	 * */
}
