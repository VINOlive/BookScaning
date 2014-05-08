package com.fangjie.doubanbook;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fangjie.doubanbook.R;
import com.fangjie.doubanbook.util.Util;
import com.fangjie.doubanbook.bean.BookInfo;

/**
 * 主界面
 * @author Jim
 *
 */
public class MainActivity extends Activity {
    private TextView tx1;
    private Button btn;
    private Handler handler;
    private ProgressDialog progressDialog;
    @SuppressLint("HandlerLeak")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar);  
        StringBuffer str=new StringBuffer();
        str.append("1.按下扫描按钮启动摄像头，扫描并获取书籍的条形码；").append("\n");
        str.append("2.查询书籍相关介绍信息；").append("\n");
        str.append("3.显示在界面上").append("\n");
        tx1=(TextView)findViewById(R.id.main_textview01);
        tx1.setText(str.toString());
        btn=(Button)findViewById(R.id.main_button01);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Intent intent=new Intent(MainActivity.this,CaptureActivity.class);
            	startActivityForResult(intent,100);
            }
        });
        
        //接收来自下载线程的消息
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                BookInfo book= (BookInfo)msg.obj;
                //进度条消失
                progressDialog.dismiss();
                if(book==null)
                {
                	Toast.makeText(MainActivity.this, "没有找到这本书", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Intent intent=new Intent(MainActivity.this,BookView.class);
                    intent.putExtra(BookInfo.class.getName(),book);
                    startActivity(intent);
                }

            }
        };
    }
    //获取扫描之后的ISBN码，并在豆瓣上搜索图书信息
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(((requestCode==100)||(resultCode==Activity.RESULT_OK))&&data!=null)
        {
        	Log.i("OUTPUT","NGYUFU");
        	progressDialog=new ProgressDialog(this);
        	progressDialog.setMessage("请稍候，正在读取信息...");
        	progressDialog.show();
            String urlstr="https://api.douban.com/v2/book/isbn/"+data.getExtras().getString("result");
            //扫到ISBN后，启动下载线程下载图书信息
            new DownloadThread(urlstr).start();
        }
    }

    private class DownloadThread extends Thread
    {
        String url=null;
        public DownloadThread(String urlstr)
        {
            url=urlstr;
        }
        public void run()
        {
            String result=Util.Download(url);
            Message msg=Message.obtain();
			Log.i("OUTPUT",result);
            try {
    			Log.i("OUTPUT","hhhhhh");
				Log.i("OUTPUT","ZZZZZ");
	            BookInfo book=new Util().parseBookInfo(result);
	            //给主线程UI界面发消息，提醒下载信息，解析信息完毕
	            msg.obj=book;
			} catch (Exception e) {
	    		Log.i("OUTPUT","cccc");		
				e.printStackTrace();
			}
            handler.sendMessage(msg);
        }
    }
}
