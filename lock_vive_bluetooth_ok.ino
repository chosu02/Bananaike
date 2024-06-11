#include <SoftwareSerial.h>  // 시리얼 블루투스 통신을 위한 라이브러리

/* 블루투스 연결을 위한 선언 */
#define BLUETOOTH_RX 2  // 블루투스 모듈 RX 핀 2번에 연결
#define BLUETOOTH_TX 3  // 블루투스 모듈 TX 핀 3번에 연결

/* 진동감지 센서의 선언 */
#define VibrationSignal 9  // 디지털 핀 9에 센서를 연결

/* 능동부저 */
#define BUZZER 8

/* RGB LED 핀 정의 */
#define RPin 12
#define GPin 11
#define BPin 10

/* 자동잠금스위치 */
#define AUTO_LOCK 4

/* 도어락 연결 핀 저장 */
#define REAL_LOCKER 13

/* gps 연결을 위한 선언 */
#define GPS_TX 5  // 블루투스 모듈 RX 핀 2번에 연결
#define GPS_RX 6  // 블루투스 모듈 TX 핀 3번에 연결

SoftwareSerial bt(BLUETOOTH_TX, BLUETOOTH_RX);  // 블루투스 통신을 위한 bt 설정 (RX= 수신 D2, TX= 송신 D3)
SoftwareSerial gps(GPS_TX, GPS_RX);             // gps 통신을 위한 bt 설정

bool buzzerFlag = false;    // 부저 울림 상태 저장용 변수
bool doorOpened = false;    // 도어락 열림 상태 저장용 변수

void setup() {
  /* 블루투스 시리얼 통신 시작 */
  bt.begin(9600);
  Serial.begin(9600);

  /* 도어락 설정 */
  pinMode(REAL_LOCKER, OUTPUT);

  /* RGB LED 핀 설정 */
  pinMode(RPin, OUTPUT);
  pinMode(BPin, OUTPUT);
  pinMode(GPin, OUTPUT);

  /* 진동감지 설정 */
  pinMode(VibrationSignal, INPUT);

  /* 부저 설정 */
  pinMode(BUZZER, OUTPUT);

  /* 자동잠금스위치 설정 */
  pinMode(AUTO_LOCK, INPUT);

  /* 초기 LED 상태 설정 (LED 끄기) */
  analogWrite(RPin, 0);  // 빨강 끄기
  analogWrite(BPin, 0);  // 파랑 끄기
  analogWrite(GPin, 0);  // 초록 끄기
}

void loop() {
  // 자동잠금스위치 상태 읽기
  int autoVal = digitalRead(AUTO_LOCK);
  int ViveState = digitalRead(VibrationSignal);

  if (!doorOpened && autoVal == HIGH) {  // 도어락이 닫혀 있고 자동잠금스위치가 눌렸을 때
    Serial.println("도어락 열림");
    digitalWrite(REAL_LOCKER, HIGH);  // 도어락 열림
    doorOpened = true;                // 도어락 열림 상태 저장

    // 초록 불 켜기
    analogWrite(RPin, 0);    // 빨강 끄기
    analogWrite(BPin, 0);    // 파랑 끄기
    analogWrite(GPin, 255);  // 초록 켜기

    // 부저가 울리지 않았다면 부저 울리기
    if (!buzzerFlag) {
      analogWrite(BUZZER, 128);  // PWM 값 128로 소리 크기 조절
      delay(1000);
      analogWrite(BUZZER, 0);
      buzzerFlag = true;  // 부저 울림 상태 저장
    }
  } 
  else if (doorOpened && autoVal == LOW) {
    // 도어락 닫기 처리
    Serial.println("도어락 닫힘");
    digitalWrite(REAL_LOCKER, LOW);  // 도어락 닫힘
    doorOpened = false;              // 도어락 닫힘 상태 저장

    // 빨간 불 켜기
    analogWrite(RPin, 255);  // 빨강 켜기
    analogWrite(BPin, 0);    // 파랑 끄기
    analogWrite(GPin, 0);    // 초록 끄기

    buzzerFlag = false;  // 도어락이 닫히면 부저 울림 상태 초기화
  }

  // 진동 감지 처리
  if (ViveState == 1) {  // 비교 연산자 수정
    Serial.println("진동 감지");

    // 빨간색 LED 켜기
    analogWrite(RPin, 255);  // 빨강 켜기
    analogWrite(BPin, 0);    // 파랑 끄기
    analogWrite(GPin, 0);    // 초록 끄기

    // 부저 소리
    analogWrite(BUZZER, 128);  // PWM 값 128로 소리 크기 조절
    delay(1000);
    analogWrite(BUZZER, 0);
    delay(1000);

    // 알림 전송 (예: 블루투스 데이터 전송)
    bt.println("진동이 감지되었습니다!");

    // 진동 감지 후 LED 끄기 
    analogWrite(RPin, 0);  // 빨강 끄기
    analogWrite(BPin, 0);  // 파랑 끄기
    analogWrite(GPin, 0);  // 초록 끄기

    ViveState = 0;
  }
}
