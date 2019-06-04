#include <reg52.h> //51ͷ�ļ�
#include <intrins.h>   //����nop��ϵͳ����
#include "QXA51.h"//QX-A51����С�������ļ�
unsigned char pwm_left_val = 140;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
unsigned char pwm_right_val = 150;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
unsigned char pwm_t;//����
unsigned char control=0X01;//���˶�����ȫ�ֱ�����Ĭ�Ͽ���Ϊͣ��״̬
int na;
int soundId = 0;
/******************************
����������
*******************************/
sbit RX = P2^0;//ECHO������ģ������
sbit TX = P2^1;//TRIG������ģ�鴥����
unsigned long S = 0;//����
bit      flag = 0;//����������Χ��־λ
unsigned char count=0;
unsigned char SEH_count = 15;  //0-30, 0Ϊ���ҷ�, 30Ϊ����
// ��ʾ��
sbit DU  = P2^6;   //����ܶ�ѡ
sbit WE  = P2^7;   //�����λѡ
unsigned int  time = 0;//����ʱ��
unsigned int  timer=0;

void Delay10us(unsigned char i)    	//10us��ʱ���� ����������ģ��ʱʹ��
{ 
   unsigned char j; 
	do{ 
		j = 10; 
		do{ 
			_nop_(); 
		}while(--j); 
	}while(--i); 
}

void delay(unsigned int z)//���뼶��ʱ
{
	unsigned int x,y;
	for(x = z; x > 0; x--)
		for(y = 114; y > 0 ; y--);
}	
/*С��ǰ��*/
void forward()
{
	left_motor_go; //����ǰ��
	right_motor_go; //�ҵ��ǰ��
}
/*С����ת*/
void left_run()
{
	left_motor_stop; //����ֹͣ
	right_motor_go; //�ҵ��ǰ��	
}
/*С����ת*/
void right_run()
{
	right_motor_stop;//�ҵ��ֹͣ
	left_motor_go;    //����ǰ��
}

/*PWM����ʹ�� С������*/
void backward()
{
	left_motor_back; //��������
	right_motor_back; //�ҵ������	
}
/*PWM����ʹ�� С��������ת*/
void right_rapidly()
{
	left_motor_go;
	right_motor_back;	
}
/*С��������ת*/
void left_rapidly()
{
	right_motor_go;
	left_motor_back;	
}
//С��ͣ��
void stop()
{
		left_motor_stop; //��������
	right_motor_stop; //�ҵ������	
}


void keyscan()
{
	for(;;)	//��ѭ��
	{
		if(key_s2 == 0)// ʵʱ���S2�����Ƿ񱻰���
		{
			delay(5); //�������
			if(key_s2 == 0)//�ټ��S2�Ƿ񱻰���
			{
				while(!key_s2);//���ּ��
				beep = 0;	//ʹ����Դ������
				delay(200);//200������ʱ
				beep = 1;	//�ر���Դ������
				break;		//�˳�FOR��ѭ��
			}
		}
	}	
}

//��ʼ��
void Init(void)
{
   	EA = 1;	    //�����ж�

   	SCON |= 0x50; 	// SCON: ģʽ1, 8-bit UART, ʹ�ܽ���
	T2CON |= 0x34; //���ö�ʱ��2Ϊ���ڲ����ʷ�������������ʱ��2
	TL2 = RCAP2L = (65536-(FOSC/32/BAUD)); //���ò�����
	TH2 = RCAP2H = (65536-(FOSC/32/BAUD)) >> 8;
	ES= 1; 			//�򿪴����ж�

	TMOD |= 0x01;//��ʱ��0����ģ��1,16λ��ʱģʽ��T0�ò�ECH0���峤��
	TH0 = 0;
	TL0 = 0;//T0,16λ��ʱ�������ڼ�¼ECHO�ߵ�ƽʱ��
	ET0 = 1;//����ʱ��0�ж�

	TMOD |= 0x20;	//��ʱ��1��8λ�Զ���װģ��
	TH1 = 220;     
    TL1 = 220;	   //11.0592M������ռ�ձ�����ֵ��256,���100HZ
	TR1 = 1;//������ʱ��1
	ET1 = 1;//����ʱ��1�ж�
}

/**************************************************
����������
***************************************************/
void  StartModule() 		         //����������ģ��
{
	TX=1;			                     //����һ��ģ��
  	Delay10us(2);
  	TX=0;
}

/*���㳬�����������*/
void Conut(void)
{
	time=TH0*256+TL0;
	TH0=0;
	TL0=0;
	S=(float)(time*1.085)*0.17;     //�������MM
	if((S>=7000)||flag==1) 
	 {	 
	  flag=0;
	 }
}

/*����������*/
void	Avoid()
{
	if(S < 400)//���ñ��Ͼ��� ����λ����	ɲ������
	{
		stop();//ͣ��
		backward();//����
		delay(100);//����ʱ��Խ��������ԽԶ��������Ϊ����������ת��Ŀռ�
		do{
			left_rapidly();//������ת
			delay(90);//ʱ��Խ�� ת��Ƕ�Խ����ʵ����ʻ�����й�
			stop();//ͣ��
			delay(200);//ʱ��Խ�� ֹͣʱ��Խ�ó�
			forward();

			StartModule();	//����ģ���࣬�ٴ��ж��Ƿ�
			while(!RX);		//��RX��ECHO�źŻ��죩Ϊ��ʱ�ȴ�
			TR0=1;			    //��������
			while(RX);			//��RXΪ1�������ȴ�
			TR0=0;				//�رռ���
			Conut();			//�������
			}while(S < 280);//�ж�ǰ���ϰ������
	}
	else
	{
		forward();//ǰ��
	}	
}


//����Ѱ��
void BlackLine()
{
		//Ϊ0 û��ʶ�𵽺��� Ϊ1ʶ�𵽺���
	if(left_led1 == 1 && right_led1 == 1)//����Ѱ��̽ͷʶ�𵽺���
	{
		forward();//ǰ��
	}
	else
	{
		if(left_led1 == 1 && right_led1 == 0)//С���ұ߳��ߣ���ת����
		{
			left_run();//��ת
		}
		if(left_led1 == 0 && right_led1 == 1)//С����߳��ߣ���ת����
		{
			right_run();//��ת
		}		
	}	
}

//�������
void IRAvoid()
{
				//Ϊ0 ʶ���ϰ��� Ϊ1û��ʶ���ϰ���
	if(left_led2 == 1 && right_led2 == 1)//���Ҷ�ûʶ���ϰ���
	{
		pwm_left_val = 140;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 150;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
		forward();//ǰ��
	}
	if(left_led2 == 1 && right_led2 == 0)//С���Ҳ�ʶ���ϰ����ת���
	{
		pwm_left_val = 180;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 110;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
		left_run();//��ת
		delay(40);//��ת40���루ʵ����С��ת��
	}
	if(left_led2 == 0 && right_led2 == 1)//С�����ʶ���ϰ����ת���
	{
		pwm_left_val = 100;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 180;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
		right_run();//��ת
		delay(40);//��ת40���루ʵ����С��ת��
	}
	if(left_led2 == 0 && right_led2 == 0) //С���������඼ʶ���ϰ�����˵�ͷ
	{
		pwm_left_val = 150;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 160;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���	
		backward();//����
		delay(100);//���˵�ʱ��Ӱ����˵ľ��롣����ʱ��Խ�������˾���ԽԶ��
		pwm_left_val = 140;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 150;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
		right_run();	//��ת
		delay(180);//��ʱʱ��Խ����ת��Ƕ�Խ��	
	}		
}

// ������ħ����
void UltrasonicHand() {
	StartModule();	//����ģ����
	while(!RX);		//��RX��ECHO�źŻ��죩Ϊ��ʱ�ȴ�
	TR0=1;			    //��������
	while(RX);			//��RXΪ1�������ȴ�
	TR0=0;				//�رռ���
	Conut();			//�������
	if(S > 150)//���ñ��Ͼ��루��λ���ף�
	{
		forward();
	}
	else
	{
		backward();	
	}
	delay(65);			//�������ڲ�����60MS
}

//����������
void Ultrasonic()
{
	StartModule();	//����ģ����
	while(!RX);		//��RX��ECHO�źŻ��죩Ϊ��ʱ�ȴ�
	TR0=1;			    //��������
	while(RX);			//��RXΪ1�������ȴ�
	TR0=0;				//�رռ���
	Conut();			//�������
	Avoid();			//����
	delay(65);			//�������ڲ�����60MS	
}

//�����������
void IRTracking()
{
	// Ϊ0 ʶ���ϰ��� Ϊ1û��ʶ���ϰ���
	if(left_led2 == 0 && right_led2 == 0)//����ʶ���ϰ��ǰ������
	{
		forward();//ǰ��
	}
	else if(left_led2 == 1 && right_led2 == 0)//С���Ҳ�ʶ���ϰ����ת����
	{
		right_run();//��ת
	}
	else if(left_led2 == 0 && right_led2 == 1)//С�����ʶ���ϰ����ת����
	{
		left_run();//��ת
	}
	else {
		stop();
	}
}

// �Զ�Ѱ��
void findLight() {
	//Ϊ0 ʶ�𵽹�Դ Ϊ1û��ʶ�𵽹�Դ
	if(front_sensor == 0 && left_sensor == 1 && right_sensor == 1)//ֻ��ǰ��Ѱ�⴫����ʶ�𵽹�Դ��ǰ��
	{
		pwm_left_val = 100;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 100;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
		forward();//ǰ��
	}
	else if(front_sensor == 0 && left_sensor == 0 && right_sensor == 1)//ǰ�������ͬʱ���ź�
	{
		//��PWN�ź�ת��
		pwm_left_val = 170;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 70;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
		left_motor_go; //�ҵ��ǰ��
		right_motor_go; //�ҵ��ǰ��
	}
	else if(front_sensor == 0 && left_sensor == 1 && right_sensor == 0)//ǰ�������ͬʱ���ź�
	{
		//��PWN�ź�ת��
		pwm_left_val = 70;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 170;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
		left_motor_go; //�ҵ��ǰ��
		right_motor_go; //�ҵ��ǰ��
	}
	else if(front_sensor == 1 && left_sensor == 1 && right_sensor == 0)//ֻ���ұ�Ѱ�⴫����ʶ�𵽹�Դ����ת
	{
		pwm_left_val = 100;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 100;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���	
		right_run();//��ת
	}
	else if(front_sensor == 1 && left_sensor == 0 && right_sensor == 1)//ֻ�����Ѱ�⴫����ʶ�𵽹�Դ����ת
	{
		pwm_left_val = 100;//����ռ�ձ�ֵ ȡֵ��Χ0-170��0���
		pwm_right_val = 100;//�ҵ��ռ�ձ�ֵȡֵ��Χ0-170 ,0���
		left_run();//��ת
	}
	else if(front_sensor == 0 && left_sensor == 0 && right_sensor == 0)	// ǰ�����������涼���ź� ѡ��ǰ��
	{
		forward();	// ǰ��
	}
	else
	{
		stop();
	}
}

// ����
void sound() {
	if(voice == 0){
		soundId++;
		if(soundId % 2 == 0) {
		  	forward();
			delay(300);	 
			stop();
		} else if(soundId % 3 == 0){

		    backward();
			delay(300);
			stop();
		} else if(soundId % 5 == 0) {
			left_rapidly();
			delay(300);
			stop();
		} else {
			right_rapidly();
			delay(300);
			stop();
		}
	} else {
		stop();
	}
}

void main()
{
	Init();//��ʱ�������ڳ�ʼ��
	while(1)
	{
		if(control==0X66)//	ͣ������
	   {
	   	stop();	//  ͣ�� 
	   }
	    // ��̨��ת	 SEH_count -> [0,30]
		// 0x70 -> 0x8D, 112 ->  142
	   	if(control >= 0x70 && control <= 0x8D) {
			//na = (int)control;
	   		//SEH_count = na - 112;	
	   	}
		switch(control)
		{
			case 0X02:	forward();			break;	// ǰ��
			case 0X03:	backward();			break;	// ����
			case 0X04:	left_run();			break;	// ��ת
			case 0X05:	right_run();		break;	// ��ת
			case 0X01:	stop();				break;	// ͣ��
			case 0X06:	left_rapidly();		break;	// ����ת
			case 0X07:	right_rapidly();	break;	// ����ת
			case 0X08:	beep = 0;			break;	// ����
			case 0X09:	beep = 1;			break;	// ֹͣ����
			case 0x0A:	head_light = 1;		break;	// ǰ�յ�
			case 0x0B:	head_light = 0;		break;	// Ϩ��
			case 0X0C:	Ultrasonic();		break;	// ����������
			case 0X0D:	BlackLine();		break;	// ����Ѱ��
			case 0X0E:	IRAvoid();			break;	// �������
			case 0X0F:	IRTracking();		break;	// ����-��������
			case 0X10:	findLight();		break;	// �Զ�Ѱ��
			case 0X11:  UltrasonicHand();	break;	// ������ħ����
			case 0X12:  sound();			break;	// ����
		}
	}
}

//��ʱ��0�ж�
void timer0() interrupt 1
{
	flag = 1;					 
}
  
void timer1() interrupt 3 		 //T1�ж��������������
{
	pwm_t++;//���ڼ�ʱ��
	if(pwm_t == 255)
		pwm_t = EN1 = EN2 = 0;
	if(pwm_left_val == pwm_t)//����ռ�ձ�	
		EN1 = 1;		
	if(pwm_right_val == pwm_t)//�ҵ��ռ�ձ�
		EN2 = 1;

   	TR1 = 0;      //�رն�ʱ��1
    TH1 = 0xff;   //��װ��ֵ0.1ms
    TL1 = 0xa3;
    //���1
    if(count <= SEH_count) //����ռ�ձ�����
    {
        //���count�ļ���С�ڣ�5-25��Ҳ����0.5ms-2.5ms�����Сt���ڳ����ߵ�ƽ����������
        //Servo = 1;
    }
    else
    {
        //Servo = 0;
		//TR0 = 0; //�ض�ʱ��0�� ��������BUG, ת�����κ�Ͷ�·��
    }

	//count++;
    if (count >= 200) //T = 20ms��ʱ������������0
    {
        //count = 0;
    }
	TR1 = 1; //������ʱ��1 			 
}
 /******************************************************************/
/* �����жϳ���*/
/******************************************************************/
void UART_SER () interrupt 4
{
	unsigned char n; 	//������ʱ����

	if(RI) 		//�ж��ǽ����жϲ���
	{
		RI=0; 	//��־λ����
		n=SBUF; //���뻺������ֵ

		control=n;
		// С����λ
		switch(n)
		{
			case 0x21: pwm_left_val = 170; pwm_right_val = 170;	break;	// һ��
			case 0x22: pwm_left_val = 136; pwm_right_val = 136;	break;	// ����
			case 0x23: pwm_left_val = 102; pwm_right_val = 102;	break;	// ����
			case 0x24: pwm_left_val = 68; pwm_right_val = 68;	break;	// �ĵ�
			case 0x25: pwm_left_val = 0; pwm_right_val = 0;		break;	// �嵵
		}
	
	}

}