#ifndef __QXA51_H__
#define __QXA51_H__

// �������IO����
sbit IN1 = P1^2;	//ֵΪ1, ������ת
sbit IN2 = P1^3;	//ֵΪ1, ������ת
sbit IN3 = P1^6;	//ֵΪ1, �ҵ����ת
sbit IN4 = P1^7;	//ֵΪ1, �ҵ����ת
sbit EN1 = P1^4;	//ֵΪ1, ����ʹ��
sbit EN2 = P1^5;	//ֵΪ1, �ҵ��ʹ��

// ����ʶ��
sbit left_led1 = P3^3;		//��Ѱ���ź�Ϊ0 û��ʶ�𵽺��� Ϊ1ʶ�𵽺���
sbit right_led1 = P3^2;		//��Ѱ���ź�Ϊ0 û��ʶ�𵽺��� Ϊ1ʶ�𵽺���
sbit left_led2 = P3^4;		//������ź�Ϊ0 ʶ���ϰ��� Ϊ1û��ʶ���ϰ���
sbit right_led2 = P3^5;		//�ұ����ź�Ϊ0 ʶ���ϰ��� Ϊ1û��ʶ���ϰ���

// �����
sbit head_light = P2^2;		// 1 Ϊ����

//Ѱ�⴫����
sbit left_sensor = P2^7;  //��Ѱ�⴫����   Ϊ�����ʾʶ�𵽹�Դ
sbit front_sensor = P2^5; //ǰѰ�⴫����   Ϊ�����ʾʶ�𵽹�Դ
sbit right_sensor = P2^6; //��Ѱ�⴫����   Ϊ�����ʾʶ�𵽹�Դ

bit Right_PWM_ON=1;	           //�ҵ��PWM����
bit Left_PWM_ON =1;			   //����PWM����
											
/*��������*/
sbit key_s2 = P3^0;
sbit key_s3 = P3^1;
sbit beep = P2^3;//������
sbit Servo = P3^6;//����ӿ�

sbit voice = P1^0;//������ģ��

#define FOSC 11059200L //ϵͳ����Ƶ��
#define BAUD 9600 //UART ������

#define left_motor_en		EN1 = 1				// ����ʹ��
#define left_motor_stop		IN1 = 0, IN2 = 0	// ����ֹͣ
#define	right_motor_en		EN2 = 1				// �ҵ��ʹ��
#define	right_motor_stop	IN3 = 0, IN4 = 0	// �ҵ��ֹͣ

#define left_motor_go		IN1 = 0, IN2 = 1	// ������ת
#define left_motor_back		IN1 = 1, IN2 = 0	// ������ת
#define right_motor_go		IN3 = 1, IN4 = 0	// �ҵ����ת
#define right_motor_back	IN3 = 0, IN4 = 1	// �ҵ����ת

#endif