package com.example.musicshare;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import com.example.blucon.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class RecieverActivity extends Activity{
	private BluetoothServerSocket mmServerSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
	private String NAME = "BluCon";
	private String connection;
	private TextView conn_status;
	private static TextView fileReady;
	private ConnectedThread read_write;
	private static TextView incomingMessage;
	static SharedPreferences sharedPreferences;
	private ListView listViewFiles;
	static String messageDisplay = "";
	private static ArrayAdapter<String> mArrayAdapterFiles;
	static RecieverActivity recAct;
	MediaPlayer mediaPlayer;
	static File tempMp3;
	static FileOutputStream fos;
	static FileInputStream fis;
	SharedPreferences.Editor editor;
	String fileName = "";
	static boolean filesReceived = false;
	
	static TextView fileLength;
	
	
	AudioManager audioManager;

	
	public static RecieverActivity getInstance(){
		recAct = new RecieverActivity();   
		return   recAct;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reciever);
		conn_status = (TextView) findViewById(R.id.connection);
		fileReady = (TextView) findViewById(R.id.textViewFileReady);
		fileReady.setVisibility(View.INVISIBLE);
		incomingMessage = (TextView) findViewById(R.id.incoming);

		fileLength = (TextView) findViewById(R.id.textViewFileLength);
		
		mArrayAdapterFiles = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		listViewFiles = (ListView) findViewById(R.id.listViewFiles);
		listViewFiles.setAdapter(mArrayAdapterFiles);
		
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		audioManager.startBluetoothSco();
		
		audioManager.setBluetoothScoOn(true);

        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
        
        //Start of Thread
        Thread thread = new Thread(){
    		@Override
    	    public void run() {
    	        BluetoothSocket socket = null;
    	        // Keep listening until exception occurs or a socket is returned
    	        while (true) {
    	            try {
    	                socket = mmServerSocket.accept();
    	                connection = "Trying to connect";
    	            } catch (IOException e) {
    	                break;
    	            }
    	            
    	            // If a connection was accepted
    	            if (socket != null) {
    	            	
    	            	connection = "Accepted";
    	            	
    	                // Do work to manage the connection (in a separate thread)
    	            	manageConnectedSocket(socket);
    	            	//close socket
    	            	try{
    	            		mmServerSocket.close();
    	            	}
    	            	catch(IOException e){
    	            		
    	            	}
    	                break;
    	            }
    	        }
    	    }
    	};
        thread.start();
        

        
        while(true){
			if(filesReceived)
        		break;
        	
        }

        listViewFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String text = parent.getItemAtPosition(position).toString().trim();
				read_write.start();
				
			}
		});

	}
	
	private class MyOnClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			String text = arg0.getItemAtPosition(arg2).toString().trim();
			read_write.writeFile(text.getBytes());
			read_write.start();
		}
	}
	
	
	public void fetchFileManageSocket(BluetoothSocket socket) {
		// TODO Auto-generated method stub

		read_write = new ConnectedThread(socket, this);
		read_write.receiveFiles();
		
	}
	
	public void manageConnectedSocket(BluetoothSocket socket){
		//Toast toast = Toast.makeText(getApplicationContext(), "connection accepted", Toast.LENGTH_SHORT);
		conn_status.post(new Runnable() {
            public void run() {
                conn_status.setText("Connected");
            }
        });
		
		read_write = new ConnectedThread(socket, this);
		createFile();

		read_write.receiveFiles();
	}
 
    private void createFile() {
		// TODO Auto-generated method stub
		
    	File dir = Environment.getExternalStorageDirectory();
        try {
			tempMp3 = File.createTempFile("temp", "mp3", dir);
	        tempMp3.deleteOnExit();
	        fos = new FileOutputStream(tempMp3, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
    
    public void playAudio(View view){
        mediaPlayer = new MediaPlayer();
        // Tried passing path directly, but kept getting 
        // "Prepare failed.: status=0x1"
        // so using file descriptor instead
        try {
        
        mediaPlayer.setDataSource(fis.getFD());

			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mediaPlayer.start();
    }
    
    public static void messageRefresh(byte[] messageBytes) {
    	try {
    		for(int i = 0; i<messageBytes.length;i++)
    			fos.write(messageBytes[i]);

    		fis = new FileInputStream(tempMp3);
    		
    		final double bytes = tempMp3.length();
    		final double kilobytes = (bytes / 1024);
			final double megabytes = (kilobytes / 1024);
    		
    		System.out.println("bytes : " + bytes);
    		System.out.println("kilobytes : " + kilobytes);
			System.out.println("megabytes : " + megabytes);
    		
			fileLength.post(new Runnable() {
	            public void run() {
	    			fileLength.setText("Size of the file is " + bytes + " bytes or " + kilobytes + "kB or " + megabytes + " MB");
	            }
	        });
    		
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}

	public static void getAllFiles(byte[] files) {
		// TODO Auto-generated method stub
		
		String fileList = new String(files);
		
		final String [] n = fileList.split(",");
		
		int nLength = n.length;
				
		for (int i = 1; i < nLength; i++ ) {
	        mArrayAdapterFiles.add(n[i]);
	    }

		filesReceived = true;
	}
}
