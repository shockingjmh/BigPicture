package com.example.appcounter;


import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shockingjmh on 2017. 5. 13..
 */

public class ConnectServer extends Thread{

    private static final String LOG_NAME = "";
    private Socket theSocket = null;
    private InputStream is;
    private BufferedReader reader, userInput;
    private OutputStream os;
    private BufferedWriter writer;

    private boolean recv;
    private String theLine = "";
    private String recv_data= null;
    private String time;
    private String host = "192.168.91.1";

    private Context context;

    public ConnectServer(Context context){
        this.context = context;
    }

    public void run(){
        try{
            Log.d(LOG_NAME, "run start");
            theSocket = new Socket(host, 9999); // echo 서버에 접속한다.
            Log.d(LOG_NAME, "connect!!!!!");
            is = theSocket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            userInput = new BufferedReader(new InputStreamReader(System.in));
            os = theSocket.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os));

            recv = true;
            while((recv_data = reader.readLine()) != null){
                Log.d(LOG_NAME, recv_data + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }catch(Exception e){
            Log.d(LOG_NAME, "호스트를 찾을 수 없습니다.");
        }
    }

    /*public Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what == 0){
                new AlertDialog.Builder(context)
                        .setTitle("Big Picture")
                        .setMessage(recv_data)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                                Intent intent = context.getPackageManager().getLaunchIntentForPackage(recv_data);
                                context.startActivity(intent);
                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();
                    }
                }).show();
            }
        }
    };*/

    public void sendData(String data, double latitude, double longitude){

        theLine += data;
        theLine += "/";
        theLine += Double.toString(latitude);
        theLine += "/";
        theLine += Double.toString(longitude);

        if(writer != null && theLine.compareTo("") != 0) {
            try {
                writer.write(theLine + '\r' + '\n');
                writer.flush(); // 서버에 데이터 전송
                theLine = "";
                Log.d(LOG_NAME, theLine+"sadfdsjklfjkl;dsjfkl;dsajfkl;asdjkl;fjsdal;fjadklsfkl;");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRecvData(){
        boolean temp_recv = false;
        if(recv){
            temp_recv = recv;
            recv = false;
            return temp_recv;
        }
        return false;
    }

    public String receiveData(){
        Log.d("name", recv_data+"333333333333333333333333333333333333333333333");
        return recv_data;
    }

    public void quit(){
        try{
            theSocket.close(); // 소켓을 닫는다.
        }catch(IOException e){
            System.out.println(e);
        }
    }
}