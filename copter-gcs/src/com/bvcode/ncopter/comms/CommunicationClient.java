package com.bvcode.ncopter.comms;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.MAVLink.MAVLink;
import com.MAVLink.Messages.IMAVLinkMessage;
import com.MAVLink.Messages.common.msg_request_data_stream;
import com.bvcode.ncopter.CommonSettings;
import com.bvcode.ncopter.AC1Data.ProtocolParser;

// provide a common class for some ease of use functionality
public abstract class CommunicationClient{
	
	Activity parent;
	
	/** Messenger for communicating with service. */
	Messenger mService = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	public static final String PREFS_NAME = "QuadPrefs";
	public static final String DEFAULT_MODEM = "defaultModem";
	public static final String ACTIVE_PROTOCOL= "activeProtocol";
	public static final String LINK_TYPE= "activeLinkType";
	public static final String DEFAULT_ORIENTATION = "defaultOrientation";
	
    
    public boolean startedInit = false;
    
    public ICommunicationModule module = null;
    
	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
	
		@Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        
	        // Bluetooth handlers
	        	case BluetoothModule.MSG_GET_STATE:

	        		int res = module.clientHandleState(mService, msg, parent);
	        		
	        		switch(res){	        		
		        		case ICommunicationModule.NOT_SUPPORTED:
		        			notifyDeviceNotAvailable();
		        			break;
		        			
		        		case ICommunicationModule.NOT_ENABLED:
		        			break;
		        						
		        		case ICommunicationModule.NOT_CONNECTED:
		        			break;	
		        			
		        		case ICommunicationModule.CONNECTED:
		        			notifyConnected();
		        			break;
	        					
	        		}
	        		
	        		break;
	        		
		        case CommunicationService.MSG_DEVICE_CONNECTED:
		        	notifyConnected();
		        	break;
		        		
		        case CommunicationService.MSG_DEVICE_DISCONNECTED:
		        	notifyDisconnected();
		        	break;
		        
		     // USB Handlers
		     // Wifi Handlers
		        	
		     // Received data from... somewhere
		        case CommunicationService.MSG_COPTER_RECEIVED:
	            	Bundle b = msg.getData();
	            	IMAVLinkMessage m = (IMAVLinkMessage) b.getSerializable("msg");
	            	int count = b.getInt("recvCount");
	            	notifyReceivedData(count, m);
	                break;
	                
	            default:
	                super.handleMessage(msg);
	        }
	    }
	}
	
	public CommunicationClient(Activity mainActivity) {
		parent = mainActivity;
		
	}
	
	public abstract void notifyConnected();
	public abstract void notifyDisconnected();
	public abstract void notifyDeviceNotAvailable();
	public abstract void notifyReceivedData(int count, IMAVLinkMessage m);

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Message msg = module.handleOnActivityResult(parent, requestCode, resultCode, data);
			
			if( msg != null){
				try {
					mService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}					
			}else{
				notifyDeviceNotAvailable();
	
			}
		}
	}
	
	public void onDestroy() {
		if(!startedInit)
			return;
		
        try {
        	if( CommonSettings.isProtocolAC1())
        		sendBytesToComm(ProtocolParser.requestStopDataFlow());
        	
        	else if (CommonSettings.isProtocolMAVLink()){
        		msg_request_data_stream req = new msg_request_data_stream();
				req.req_message_rate = 0;
				req.req_stream_id = 0;
				req.start_stop = 0;
				req.target_system = MAVLink.CURRENT_SYSID;
				req.target_component = 0;
				sendBytesToComm( MAVLink.createMessage(req));
        		
        	}
        	
        	if( mService != null){
	    		Message msg = Message.obtain(null, CommunicationService.MSG_UNREGISTER_CLIENT);
	            msg.replyTo = mMessenger;        	
				mService.send(msg);
        	
        	}
        	
       		//Unbinding the service.
       		parent.getApplicationContext().unbindService(mConnection);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void sendBytesToComm(byte[] b){

		if(b == null)
			return;
		
		try {	
			Message msg = Message.obtain(null, CommunicationService.MSG_COPTER_SEND);
			Bundle d = new Bundle();
			d.putByteArray("msgBytes", b);
			msg.setData(d);
			
			if(mService != null)
				mService.send(msg);
			else
				Log.d("Communication Client", "Attempting Send message with mService == null");
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}

	public void init() {
		// which parser to use?
		SharedPreferences settings = parent.getSharedPreferences(CommunicationClient.PREFS_NAME, 0);
		
		//Load in the proper comm module.
		if(settings.contains(CommunicationClient.LINK_TYPE)){
	    	String link = settings.getString(CommunicationClient.LINK_TYPE, "");
	    	
	    	if(link.equals(CommonSettings.LINK_BLUE)){
	        	module = new BluetoothModule();
	        	
	    	//}else if(link.equals(ProtocolParser.LINK_USB_) && IProxy.isUSBSupported()){
	    	//	module = new USBModule(null);
	    		
	    	}else{
	    		//Default
	    		module = new BluetoothModule();
	    		
	    	}
		} else { //Default
    		module = new BluetoothModule();
    		
    	}			

		startedInit = true;
		parent.getApplicationContext().bindService(new Intent(parent, CommunicationService.class), mConnection, Context.BIND_AUTO_CREATE);
		
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
		    
			try {
	            Message msg = Message.obtain(null, CommunicationService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            mService.send(msg);

	        } catch (RemoteException e) {
	        
	        }
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
			
		}
	};

	public void sendMessage(int m) {
		Message msg = Message.obtain(null, m);
        msg.replyTo = mMessenger;        	
		try {
			mService.send(msg);
		
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}
}
