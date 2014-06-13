import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	
	public static String lastFileName = null;
	public static int lastFileSize = 0;
	
	public static void main(String[] args) {

		final int PORT = 1977;	
        int bytesRead;
        int current = 0;
        int fileSize = 0;
        String fileName ="";
        String returnMsg = "";
        
		try{
			//set up welcome socket
			ServerSocket welcomeSocket = new ServerSocket (PORT);
			while(true){
				//set up socket to transfer data
				System.out.println("Waiting for Connection...");
				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("Connection accepted from " + getIP(connectionSocket) + " at port number " + getPort(connectionSocket));
				
				//receive image information
				BufferedReader br = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream dos = new DataOutputStream(connectionSocket.getOutputStream());
				String fileInfo = br.readLine();
				fileSize = getFileSize(fileInfo);
				fileName = getFileName(fileInfo);
				System.out.println("Receiving file: " + fileName + ", size: " + fileSize + " bytes.");
				if(fileSize > 0 && fileName != null)
					returnMsg = "true\n";
				else
					returnMsg = "false\n";
				dos.writeBytes(returnMsg);
				
				//receive image
				current = 0;
				DataOutputStream outClient = new DataOutputStream(connectionSocket.getOutputStream());
				// receive file
	            byte[] mybytearray = new byte[fileSize];
	            InputStream is = connectionSocket.getInputStream();
	            FileOutputStream fos = new FileOutputStream(fileName); // destination
	            BufferedOutputStream bos = new BufferedOutputStream(fos);
	            bytesRead = is.read(mybytearray, 0, mybytearray.length);
	            current = bytesRead;
	            while (current != fileSize) {
	            	bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
	                if (bytesRead >= 0)
	                    current += bytesRead;
	            }
	            bos.write(mybytearray, 0, current);
	            bos.flush();
	            bos.close();
				System.out.println("Image received from " + getIP(connectionSocket) + " at port number " + getPort(connectionSocket)); 
				returnMsg  = "HomeBox successfully stored your image.\n";
				outClient.writeBytes(returnMsg);
				System.out.println("Image Confirmation sent!");
				lastFileName = fileName;
				lastFileSize = fileSize;
				connectionSocket.close();
			}
		} 
		catch(IOException ex){
	        System.out.println (ex.toString());
	        System.out.println("Could not connect to socket");
	    }
	}		
	
	public static String getIP(Socket socketAddress){
		String delims = "[/:]";
		String address = socketAddress.getRemoteSocketAddress().toString();
		String [] tokens = address.split(delims);
		return tokens[1];
	}
	
	public static String getPort(Socket socketAddress){
		String delims = "[/:]";
		String address = socketAddress.getRemoteSocketAddress().toString();
		String [] tokens = address.split(delims);
		return tokens[2];
	}
	
	public static String getFileName(String info){
		String delims = "[:]";
		String [] tokens = info.split(delims);
		return tokens[0];
	}
		
	public static int getFileSize(String info){
		String delims = "[:]";
		String [] tokens = info.split(delims);
		return Integer.parseInt(tokens[1]);
	}
}

