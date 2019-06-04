package com.example.carcontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class SmartCar extends Activity {
	private int light_on = 0;		// �ж��Ƿ񿪵�
	private int cloud_angle = 127;	// ��̨��ʼ�Ƕ� 112 + 15
	/*��ͨ��������*/
	private ImageView sound;
	private ImageView findLight;
	private ImageView hand;
	private ImageView light;
	private ImageView beep;
	private ImageView up;
	private ImageView down;
	private ImageView left;
	private ImageView right;
	private ImageView turnLeft;
	private ImageView turnRight;
	private ImageView leftSpeedUp;
	private ImageView leftSpeedDown;
	private ImageView rightSpeedUp;
	private ImageView rightSpeedDown;
	private ImageView stop;
	private TextView leftSpeed;
	private TextView rightSpeed;
	private int level=1;
	/*�����԰�������*/
	private ImageView blackLine;
	private ImageView infraredRay;
	private ImageView ultrasonic;
	private ImageView trace;
	/*�������ֶ���*/
	private TextView status;
	private final static int REQUEST_CONNECT_DEVICE = 1; // �궨���ѯ�豸���
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP����UUID��
	private InputStream is; // ������������������������
	protected static final int REQUEST_ENABLE = 0;
	BluetoothDevice _device = null; // �����豸
	BluetoothSocket _socket = null; // ����ͨ��socket
	boolean _discoveryFinished = false;
	boolean bRun = true;
	boolean bThread = false;
	boolean hex=true;
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter(); // ��ȡ�����������������������豸
	private ImageView bluetoothBtn;
	private long firstTime=0;  //��¼�ڼ��ε������
//	private int speed_l = 51;
//	private int speed_r = 151;
	private int left_level = 1;
	private int right_level = 1;
	

	@Override
	// onCreate activity����������
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//��ȡȫ��״̬
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);//������Ļ״̬Ϊ��������Ӧ��ת
		// ����ʽ
		  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	            Window window = getWindow();
	            // Translucent status bar
	            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	            // Translucent navigation bar
	            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	        }
		setContentView(R.layout.activity_car);
		
		/*��ʼ������������״̬*/
		sound = (ImageView) findViewById(R.id.sound);
		sound.setOnClickListener(new soundOnClickListener());
		findLight = (ImageView) findViewById(R.id.findLight);
		findLight.setOnClickListener(new findLightOnClickListener());
		hand = (ImageView) findViewById(R.id.hand);
		hand.setOnClickListener(new handOnClickListener());
		light = (ImageView) findViewById(R.id.light);
		light.setOnClickListener(new lightOnClickListener());
		stop = (ImageView) findViewById(R.id.pause);
		stop.setOnClickListener(new stopOnClickListener());
		blackLine = (ImageView) findViewById(R.id.blackLine);
		blackLine.setOnClickListener(new blackLineOnClickListener());
		infraredRay =  (ImageView) findViewById(R.id.infraredRay);
		infraredRay.setOnClickListener(new infraredRayOnClickListener());
		ultrasonic =  (ImageView) findViewById(R.id.ultrasonic);
		ultrasonic.setOnClickListener(new ultrasonicOnClickListener());
		trace =  (ImageView) findViewById(R.id.trace);
		trace.setOnClickListener(new ultrasonicFollowOnClickListener());
		beep = (ImageView) findViewById(R.id.beep);
		beep.setOnTouchListener(new beepOnTouchListener());
		leftSpeed = (TextView) findViewById(R.id.right_speed);	// R.java����û��ʱ����
		rightSpeed = (TextView) findViewById(R.id.left_speed);
		up = (ImageView)findViewById(R.id.up);
		up.setOnTouchListener(new upOnTouchListener());
		down = (ImageView) findViewById(R.id.down);
		down.setOnTouchListener(new downOnTouchListener());
		left = (ImageView) findViewById(R.id.left);
		left.setOnTouchListener(new leftOnTouchListener());
		right = (ImageView) findViewById(R.id.right);
		right.setOnTouchListener(new rightOnTouchListener());
		turnLeft = (ImageView) findViewById(R.id.turnLeft);
		turnLeft.setOnTouchListener(new turnLeftOnTouchListener());
		turnRight = (ImageView) findViewById(R.id.turnRight);
		turnRight.setOnTouchListener(new turnRightOnTouchListener());
		bluetoothBtn = (ImageView) findViewById(R.id.bluetooth);
		leftSpeedUp = (ImageView) findViewById(R.id.left_faster);
		leftSpeedUp.setOnClickListener(new leftSpeedUpOnClickListener());
		leftSpeedDown = (ImageView) findViewById(R.id.left_slower);
		leftSpeedDown.setOnClickListener(new leftSpeedDownOnClickListener());
		rightSpeedUp = (ImageView) findViewById(R.id.right_faster);
		rightSpeedUp.setOnClickListener(new rightSpeedUpOnClickListener());
		rightSpeedDown = (ImageView) findViewById(R.id.right_slower);
		rightSpeedDown.setOnClickListener(new rightSpeedDownOnClickListener());
		bluetoothBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				connect();
			}
		});
		
		if (_bluetooth == null) {
			Toast.makeText(this, "����û���ҵ�����Ӳ����������", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}
		if (_bluetooth.isEnabled() == false) { // ����������񲻿�������ʾ	
			Toast.makeText(SmartCar.this, " ��������...",
					Toast.LENGTH_SHORT).show();	
					
			new Thread() {
				public void run() {
					if (_bluetooth.isEnabled() == false) {
						_bluetooth.enable();
					}
				}
			}.start();	
		}
		
		// ��������δ��
		if (_bluetooth.isEnabled() == false)
		{		
			Toast.makeText(SmartCar.this, "�ȴ������򿪣�5��󣬳������ӣ�", Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable(){   //�ӳ�ִ��
				@Override
				public void run(){
					if (_bluetooth.isEnabled() == false)
					{
						Toast.makeText(SmartCar.this, "�Զ�������ʧ�ܣ����ֶ���������", Toast.LENGTH_SHORT).show();
						//ѯ�ʴ�����
						Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enabler, REQUEST_ENABLE);	
					}
					else
						connect(); //�Զ���������
					}
			}, 5000);
		}
		else
		{
			connect(); //�Զ���������
		}	
		// Sends(level + 10);//���ͳ�ʼ�ٶ�ֵ
	}
	
	// ����Intent��������ӦstartActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE: // ���ӽ������DeviceListActivity���÷���
			// ��Ӧ���ؽ��
			if (resultCode == Activity.RESULT_OK) { // ���ӳɹ�����DeviceListActivity���÷���
				// MAC��ַ����DeviceListActivity���÷���
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �õ������豸���
				_device = _bluetooth.getRemoteDevice(address);
				// �÷���ŵõ�socket
				try {
					_socket = _device.createRfcommSocketToServiceRecord(UUID
							.fromString(MY_UUID));
				} catch (IOException e) {

					Toast.makeText(this, "����ʧ��,�޷��õ�Socket��"+e, Toast.LENGTH_SHORT).show();
			    	SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
					sharedata.clear();
					sharedata.commit();	 

				}
 
				
				// ����socket
				try {
					_socket.connect();

					Toast.makeText(this, "����" + _device.getName() + "�ɹ���",
							Toast.LENGTH_SHORT).show();
					//tvcon.setText(_device.getName() + "\n"+ _device.getAddress());
					
					SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
					sharedata.putString(String.valueOf(0),_device.getName());
					sharedata.putString(String.valueOf(1),_device.getAddress());
					sharedata.commit();	  
					
					bluetoothBtn.setImageDrawable(getResources().getDrawable((R.drawable.bluetooth_ready)));
					
					// bluetoothBtn.setText(getResources().getString(R.string.delete));
					
			        //ע���쳣�Ͽ�������  �����ӳɹ���ע��
					IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
					  this.registerReceiver(mReceiver, filter);
					  
				} catch (IOException e) {
					bluetoothBtn.setImageDrawable(getResources().getDrawable((R.drawable.bluetooth)));
					// bluetoothBtn.setText(getResources().getString(R.string.add));
					try {
						Toast.makeText(this, "����ʧ�ܣ�"+e, Toast.LENGTH_SHORT)
								.show();
						_socket.close();
						_socket = null;
				    	SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
						sharedata.clear();
						sharedata.commit();	 
					} catch (IOException ee) {
					}
					return;
				}

				// �򿪽����߳�
				try {
					is = _socket.getInputStream(); // �õ���������������
				} catch (IOException e) {
					Toast.makeText(this, "�쳣���򿪽����̣߳�"+e, Toast.LENGTH_SHORT).show();
					bluetoothBtn.setImageDrawable(getResources().getDrawable((R.drawable.bluetooth)));
					// bluetoothBtn.setText(getResources().getString(R.string.add));
					
			    	SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
					sharedata.clear();
					sharedata.commit();	 
					return;
				}

			}
			break;
		default:
			break;
		}
	}
	
	// ϵͳ�㲥������
	// ��̬ע��㲥������
	// ��̬ע��: ��������Ž��չ㲥
	// ��̬ע��: ������������Ҳ�ɽ��չ㲥
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {     
		@Override
        public void onReceive(Context context, Intent intent) {
			   String action = intent.getAction();
				if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {	
					disconnect();    			
            }
        }
    };
    
    public void disconnect()
	{
    	//ȡ��ע���쳣�Ͽ�������  
		this.unregisterReceiver(mReceiver);
		light_on = 0;
    	SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
		sharedata.clear();
		sharedata.commit();	 
    	
		Toast.makeText(this, "��·�ѶϿ������������ӣ�", Toast.LENGTH_SHORT).show();
		// �ر�����socket
		try {
			bRun = false; // һ��Ҫ����ǰ��
			is.close();
			_socket.close();
			_socket = null;
			bRun = false;
			bluetoothBtn.setImageDrawable(getResources().getDrawable((R.drawable.bluetooth)));
			// bluetoothBtn.setText(getResources().getString(R.string.add));
		} catch (IOException e) {
		}
	}    
    
    public void connect()
	{
		if (_bluetooth.isEnabled() == false) { // ����������񲻿�������ʾ
			//ѯ�ʴ�����
			// Intent �����ͨ�ŵ���Ŧ
			Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enabler, REQUEST_ENABLE);		
			return;
		}		
		
		// ��δ�����豸���DeviceListActivity�����豸����				
		if (_socket == null) {
			//tvcon.setText("");
			Intent serverIntent = new Intent(SmartCar.this,
					DeviceListActivity.class); // ��ת��������
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // ���÷��غ궨��
		} else {
			disconnect();
		}
		return;
	
	}

    public void Sends(int v) //��������
    {
    	int i=0;
    	int n=0;
    	try{
    		if(_socket != null)
    		{
    		OutputStream os = _socket.getOutputStream();   //�������������
    		os.write(v);	
    		}
    		else
    		{
    			Toast.makeText(SmartCar.this, "������������", Toast.LENGTH_LONG).show();  			
    		}
    	
    	}catch(IOException e){  	    		
    	}  	
    }
    
    // public void debounce( callback, int delay)
    
    // onDestroy activity���������е����ٽ׶�
	public void onDestroy() {
		super.onDestroy();
		if (_socket != null) // �ر�����socket
			try {
				_socket.close();
			} catch (IOException e) {
			}
		
		//_bluetooth.disable(); //�ر���������
		
		 android.os.Process.killProcess(android.os.Process.myPid()); // ��ֹ�߳�
	}

	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN){
            if (System.currentTimeMillis()-firstTime>2000){
                Toast.makeText(SmartCar.this,"�ٴε�������˳�",Toast.LENGTH_SHORT).show();	                
                firstTime=System.currentTimeMillis();
            }else{
                finish();
                System.exit(0);
                
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
		}
	

	// ����ͣ��
	class stopOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Sends(0x66);	// ͣ��
		}
		
	}
	// ����ǰ��
	class upOnTouchListener implements OnTouchListener		//��ȡ����״̬����������ɿ�����ֹͣ����
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN)	//����
	        {
				Sends(0x02);	//����ǰ������	
	         }  
			if (event.getAction() == MotionEvent.ACTION_UP)		//����
	        {
	    	    Sends(0x66);
	        }
			//����true  �Ŵ���ACTION_UP
			return true;
		}
	}
	
	// ��������
	class downOnTouchListener implements OnTouchListener	//��ȡ����״̬����������ɿ�����ֹͣ����
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN)	//����
	        {
				
				Sends(0x03);	//���ͺ�������	
	         }  
			if (event.getAction() == MotionEvent.ACTION_UP)		//����
	        {
				Sends(0x66);
	        }
			//����true  �Ŵ���ACTION_UP
			return true;
		}
	}
	
	// ������ת
	class rightOnTouchListener implements OnTouchListener	//��ȡ����״̬����������ɿ�����ֹͣ����
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN)	//����
	        {		
			
				Sends(0x05);	//������ת����	
	         }  
			if (event.getAction() == MotionEvent.ACTION_UP)		//����
	        {
				Sends(0x66);
	        }
			//����true  �Ŵ���ACTION_UP
			return true;
		}
	}
	
	// ������ת
	class leftOnTouchListener implements OnTouchListener	//��ȡ����״̬����������ɿ�����ֹͣ����
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN)	//����
	        {
				
				Sends(0x04);	//������ת����	
	         }  
			if (event.getAction() == MotionEvent.ACTION_UP)		//����
	        {
				Sends(0x66);
	        }
			//����true  �Ŵ���ACTION_UP
			return true;
		}
	}
	
	// ����˳ʱ����ת��ť
	class turnRightOnTouchListener implements OnTouchListener	//��ȡ����״̬����������ɿ�����ֹͣ����
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN)	//����
	        {
				
				Sends(0x07);	//��������ת����	
	         }  
	    	//����true  ��ʾ�¼�������ϣ����ж�ϵͳ�Ը��¼��Ĵ���false ��ʾδ�������,��������onTouch��ACTION_DOWN(ǰ), ����ִ��ACTION_UP(��)
			return false;
		}
	}
	
	// ������ʱ����ת��ť
	class turnLeftOnTouchListener implements OnTouchListener//��ȡ����״̬����������ɿ�����ֹͣ����
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN)	//����
	        {
				
				Sends(0x06);	//��������ת����	
	         }  
		    //false ��ʾδ�������,����ִ���¼�
			return false;
		}
	}
	
	// �����ҵ�����ٰ�ť
	class rightSpeedUpOnClickListener implements OnClickListener	
	{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(right_level < 5)
				right_level+=1;
			switch(right_level)
			{
				case 1:
					rightSpeed.setText("1��");
					break;
				case 2:
					rightSpeed.setText("2��");
					break;
				case 3:
					rightSpeed.setText("3��");
					break;
				case 4:
					rightSpeed.setText("4��");
					break;
				case 5:
					rightSpeed.setText("5��");
					break;
				default:
					right_level = 1;
					break;
			}
			Sends(right_level + 32);	//����ǰ������	
		}	
	}
	
	// �����ҵ�����ٰ�ť
	class rightSpeedDownOnClickListener implements OnClickListener	
	{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(right_level > 1)
				right_level-=1;
			switch(right_level)
			{	
				case 1:
					rightSpeed.setText("1��");
					break;
				case 2:
					rightSpeed.setText("2��");
					break;
				case 3:
					rightSpeed.setText("3��");
					break;
				case 4:
					rightSpeed.setText("4��");
					break;
				case 5:
					rightSpeed.setText("5��");
					break;
				default:
					right_level = 1;
					break;
			}
			Sends(right_level + 32);	//����ǰ������	
		}
	}
	
		// �����������ٰ�ť
		class leftSpeedUpOnClickListener implements OnClickListener	
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// level���ܴ���5
				if(left_level < 5)
					left_level+=1;
				switch(left_level)
				{
					case 1:
						leftSpeed.setText("1��");
						break;
					case 2:
						leftSpeed.setText("2��");
						break;
					case 3:
						leftSpeed.setText("3��");
						break;
					case 4:
						leftSpeed.setText("4��");
						break;
					case 5:
						leftSpeed.setText("5��");
						break;
					default:
						left_level = 1;
						break;
				}
				Sends(left_level + 32);		
			}	
		}
		
		// �����������ٰ�ť
		class leftSpeedDownOnClickListener implements OnClickListener	
		{

			@Override
			public void onClick(View arg0) {
				// level����Ϊ0
				if(left_level > 1)
					left_level-=1;
				// TODO Auto-generated method stub
				switch(left_level)
				{	
					case 1:
						leftSpeed.setText("1��");
						break;
					case 2:
						leftSpeed.setText("2��");
						break;
					case 3:
						leftSpeed.setText("3��");
						break;
					case 4:
						leftSpeed.setText("4��");
						break;
					case 5:
						leftSpeed.setText("5��");
						break;
					default:
						left_level = 1;
						break;
				}
				Sends(left_level + 32);		
			}
		}
	
	// ����
	class beepOnTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN)	//����
	        {
				Sends(0x08);	// ����	
	         }  
			if (event.getAction() == MotionEvent.ACTION_UP)		//����
	        {
	    		Sends(0x09);	// ֹͣ����
	        }
			//����true, �Ŵ���ACTION_UP, ��ʾ�¼�����, �����ٵ���onTouch
			return true;
			}
		}
	
	// ����Ѱ��
	class blackLineOnClickListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Sends(0x0D);	// ����Ѱ��
		}
	}
	
	// �����߱���
	class infraredRayOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Sends(0x0E);	// �������
		}
		
	}
	
	// ����������
	class ultrasonicOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Sends(0x0C);	// ����������
		}
		
	}
	
	// �����߸���
	class ultrasonicFollowOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Sends(0x0F);	// �����߸���
		}
		
	}
	
	// �����
	class lightOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(light_on % 2 == 0) {	// �������
				Sends(0x0A);	// �����
			} else {	// ż��Ϩ��
				Sends(0x0B);
			}
			light_on++;
		}
	}
	
	// �Զ�Ѱ��
	class findLightOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			if(cloud_angle < 142) {
//				cloud_angle += 5; 
//				Sends(cloud_angle);
//			}
			
			Sends(0X10);
		}
		
	}
	
	// ������ħ����
	class handOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			if(cloud_angle > 112) {
//				cloud_angle -= 5;
//				Sends(cloud_angle);
//			}
			Sends(0X11);	
		}
		
	}
	
	// ����
	class soundOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Sends(0X12);
		}
		
	}
}

