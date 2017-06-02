package com.example.musicshare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;










import java.util.Arrays;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String buff = "";
    public Context appContext = null;
    String userRole = "";
    boolean isSent = false;
    boolean fileFetched = false;
    
    
	public ConnectedThread(BluetoothSocket socket, Context con) {
		
		appContext = con;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
            
            
        } catch (IOException e) { }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
 
    public void run() {
    	int bsize = 1024;
        byte[] buffer = new byte[bsize];  // buffer store for the stream
        int bytes;// bytes returned from read()
        
        
        byte[] files = new byte[1024];
        
        SharedPreferences sharedPreferences = PreferenceManager
    			.getDefaultSharedPreferences(appContext);
		userRole = sharedPreferences.getString("role", "");
		
        while (true) {
            try {
                // Read from the InputStream
           			if(userRole.equalsIgnoreCase("receiver")){
           				while((bytes = mmInStream.read(buffer, 0, buffer.length)) != -1){
           					
               					RecieverActivity.messageRefresh(buffer);
           				}
           				mmInStream.close();
           			}else if(userRole.equalsIgnoreCase("relay")){
           				while((bytes = mmInStream.read(buffer)) != -1){
           					RelayActivity.messageRelay(buffer);
           				}
           				mmInStream.close();
           			}else if(userRole.equalsIgnoreCase("sender")){
           				while((bytes = mmInStream.read(buffer)) != -1){
           				}
           				mmInStream.close();
           			}
            	
         } catch (IOException e) {
                break;
            }
        }
    }
    
    
    public void receiveFiles() {
        try {
            byte[] files = new byte[1024];
			mmInStream.read(files);
			RecieverActivity.getAllFiles(files);
        } catch (IOException e) { }
        
    }
    
    public void fileNamesRelay(){
    	int bytes = 0;
    	byte[] buffer = new byte[1024];
    	try{
			while((bytes = mmInStream.read(buffer)) != -1){
					RelayActivity.messageRefresh(buffer);
				}
				mmInStream.close();
    	}
    	catch(IOException e){
    		
    	}
    }
    
    public void receiveFileName() {
        try {
            byte[] fileName = new byte[1024];
			int size = mmInStream.read(fileName);
			byte[] sendingFileName = new byte[size];
			for(int i = 0; i < size; i++){
				sendingFileName[i] = fileName[i];
			}
			SenderActivity.fileNameToBeSent(sendingFileName);
        } catch (IOException e) { }
        
    }
    
    
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {		
                mmOutStream.write(bytes);
                
        } catch (IOException e) { }
        
    }
 
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

	public void writeFileNames(byte[] bytes) {
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeFile(byte[] bytes) {
		// TODO Auto-generated method stub
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
}
