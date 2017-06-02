package com.example.musicshare;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.example.blucon.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SenderActivity extends Activity{
	private static final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
	private final static int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mArrayAdapter;
	private static final int REQUEST_PICK_FILE = 1;
	private ListView listView;
	public String[] paired;
	private static ConnectedThread read_write;
	TextView pathSelected;
	byte[] bytes;
	String filePath;
	boolean startPlaying = false;
	static InputStream is;
	private ArrayList<File> fileList = new ArrayList<File>();
	String fileNames = "";
	static String allFiles = "";
	File root;
	boolean sendFiles = false;
	
	static TextView bytesSent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sender);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		paired = new String[10];
		
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(mArrayAdapter);
		
		
		bytesSent = (TextView) findViewById(R.id.textViewBytesSent);
		
		root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		
		allFiles = getfile(root);
		

		 
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
		}
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		showPaired();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				createSocket(paired[position]);
			}
		});
	}
	
	@SuppressLint("NewApi")
	protected void createSocket(String devAddr){
		BluetoothSocket mSocket = null;
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(devAddr);
		Toast toast;
		//toast = Toast.makeText(getBaseContext(), device.getName(), 1);
		//toast.show();
		try{
			mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		}
		catch(IOException e){
			toast = Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
		try{
			mSocket.connect();
		}
		catch(IOException closeException){ 
			toast = Toast.makeText(getBaseContext(), closeException.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
		manageConnectedSocket(mSocket);
		//BluetoothSocket mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
	}
	
	private void manageConnectedSocket(BluetoothSocket mSocket){
		Toast.makeText(getBaseContext(), "Ready to send messages", Toast.LENGTH_SHORT).show();
		read_write = new ConnectedThread(mSocket, this);

		read_write.writeFileNames(allFiles.getBytes());
		
		int sec = (int) System.currentTimeMillis();
		int newsec = 0;
		while(newsec != sec+30000)
		{
			newsec = (int) System.currentTimeMillis();
		}
	
	}
	
	
	public void sendFile(View view){
		
		sendFiles = true;
		startPlaying = true;
	}
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
        if(resultCode == RESULT_OK) {
        	
            switch(requestCode) {
            
            case REQUEST_PICK_FILE:
            	
                              break;
            }
         }
             
        }
	
	
	private void startSendingFile() {
		// TODO Auto-generated method stub
	}

	
	
	public String getfile(File dir) {
		File listFile[] = dir.listFiles();
		if (listFile != null && listFile.length > 0) {
			for (int i = 0; i < listFile.length; i++) {
/*				if (listFile[i].isDirectory()) {	
					if (listFile[i].getName().endsWith(".mp3")){
						fileNames = fileNames + "," + listFile[i];
					}
					getfile(listFile[i]);
				} else {
*/					if (listFile[i].getName().endsWith(".mp3")){
						fileNames = fileNames + "," + listFile[i];
					}
//				}
			}
		}
		return fileNames;
	}
	
	
	
	
	private void showPaired(){
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
			int cnt = 0;
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		        paired[cnt] = device.getAddress();
		        cnt++;
		    }
		}
	}

	public static void fileNameToBeSent(byte[] fileName){
		
	}
	
	public static void sendAgain() {
		// TODO Auto-generated method stub
		
		String fileReceived = allFiles;
		
		String filename = fileReceived.substring(fileReceived.lastIndexOf("/")+1);
		
		Log.e("Mess", filename);
		
		
    	File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    	
		File aFile = new File(dir, filename.trim());
		
		double lengthOfFile = aFile.length();
		double kilobytes = (lengthOfFile / 1024);
		double megabytes = (kilobytes / 1024);
		
			
    	try {
			is = new FileInputStream(aFile);
			Thread thread = new Thread(){
	    		@Override
	    	    public void run() {
	    			ByteArrayOutputStream bos = null;
	    			try {
	    				bos = new ByteArrayOutputStream();
	    				int bytesRead = 0;

    					double finalBytesRead = 0;
    					

    					int bsize = 1024;
	    				byte[] b = new byte[bsize];
	    				while ((bytesRead = is.read(b)) != -1) {
	    					read_write.write(b);
	    						    					
	    					//Counting bytes sent
	    					finalBytesRead += bytesRead;
	    					final double finalRead = finalBytesRead;
	    		    		final double kilobytes = (finalRead / 1024);
	    					final double megabytes = (kilobytes / 1024);
	    					
	    					bytesSent.post(new Runnable() {
	    						public void run() {
	    	    					bytesSent.setText("Number of Bytes Sent = " + megabytes);
	    						}
	    					});
	    					
	    				}
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	    	    }
	    	};
	        thread.start();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}