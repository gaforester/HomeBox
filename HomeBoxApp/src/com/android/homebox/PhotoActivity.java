//TCP Set 2 Project by Roger Bryant and Deuane Hessel

package com.android.homebox;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoActivity extends Activity {

private static final int SELECT_PICTURE = 1;
private static final String IPADDRESS = "10.220.12.161";
private static final int PORTNUM = 1977;

boolean connected = false;
private String selectedImagePath;
private String filemanagerstring;
String filePath;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        
        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        Button buttonSendImage = (Button) findViewById(R.id.sendPicture);
        
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, SELECT_PICTURE);
			}
		});
        
        buttonSendImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
	            if (!connected) {
                    Thread cThread = new Thread(new ClientThread());
                    cThread.start();
	            } 
			}
        });
    }
        public class ClientThread implements Runnable {
        	 
            public void run() {
				try{
					//establish connection with TCP server
					Socket clientSocket = new Socket(IPADDRESS, PORTNUM);
					connected = true;
					File myFile = new File (filePath);
					
					//send file name and file size
					try{
						DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
						BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						dos.writeBytes(myFile.getName() + ":" + myFile.length() + "\n");
						String msgReceived = br.readLine();
						if (msgReceived == "false"){
							runOnUiThread(new Runnable() {
						    	public void run() {
				                     Toast.makeText(PhotoActivity.this,"File name and file sized not sent. Try another file.",Toast.LENGTH_LONG).show();
				                }
				            });
							return;	
						}					
					} 
					catch(IOException ex){
				        System.out.println (ex.toString());
				        runOnUiThread(new Runnable() {
					    	public void run() {
			                     Toast.makeText(PhotoActivity.this,"No data sent/received",Toast.LENGTH_LONG).show();
			                }
			            });
					}
					
					//send image
		            byte [] mybytearray  = new byte [(int)myFile.length()];
		            FileInputStream fis = new FileInputStream(myFile);
		            BufferedInputStream bis = new BufferedInputStream(fis);
		            bis.read(mybytearray,0,mybytearray.length);
		            OutputStream os = clientSocket.getOutputStream();
		            runOnUiThread(new Runnable() {
				    	public void run() {
		                     Toast.makeText(PhotoActivity.this,"Sending...",Toast.LENGTH_LONG).show();
		                }
		            });
		            try{
		            	os.write(mybytearray,0,mybytearray.length);
		            } catch(IOException ex){
		            	runOnUiThread(new Runnable() {
					    	public void run() {
			                     Toast.makeText(PhotoActivity.this,"Error writing bits",Toast.LENGTH_LONG).show();
			                }
			            });
		            }
		            os.flush();
				    BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				    final String confirmation = br.readLine();
				    runOnUiThread(new Runnable() {
				    	public void run() {
		                     Toast.makeText(PhotoActivity.this,confirmation,Toast.LENGTH_LONG).show();
		                }
		            });
				    clientSocket.close();
				    connected = false;
				} catch(IOException ex){
			        System.err.println (ex.toString());
			        System.out.println("Could not connect to socket");
				}
            }
        }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
		if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			
            try {
                // OI FILE Manager
                String filemanagerstring = selectedImage.getPath();

                // MEDIA GALLERY
                String selectedImagePath = getPath(selectedImage);

                if (selectedImagePath != null) {
                    filePath = selectedImagePath;
                    Toast.makeText(getApplicationContext(), 
                            "Image path is " + filePath, Toast.LENGTH_LONG).show();
                } 
                else if (filemanagerstring != null) {
                    filePath = filemanagerstring;
                    Toast.makeText(getApplicationContext(), 
                            "Image path is " + filePath, Toast.LENGTH_LONG).show();
                } 
                else {
                    Toast.makeText(getApplicationContext(), "Unknown path", Toast.LENGTH_LONG).show();
                }
                
                if (filePath != null) {
                	ImageView imageView = (ImageView) findViewById(R.id.imgView);
                	imageView.setImageURI(selectedImage);
                }
     
        	} catch (Exception e) {
                e.getLocalizedMessage();
        	}
		}
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor!=null)
        {
            int column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }
}

