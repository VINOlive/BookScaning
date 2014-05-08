package com.scanbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.scanbook.R;
import com.scanbook.bean.Review;
import com.scanbook.util.Util;

/*
 * 笔记列表界面
 */
public class ReviewActivity extends Activity{
	
	private ListView list;
	private TextView tv_review_more;
	private LoadNews loadnews;
	private List<HashMap<String,String>> ListData;
	private ProgressDialog progressDialog;
	public SimpleAdapter listadpter;
	private List<Review> reviews;	
	private Button btn_back;
	private TextView review_no,title;
	
	private static String book_id;
	private static String book_name;
	private static int curr_num;
	private static int total;
	
	private static int NO_DATA = 0;
	private static int OK = 1;	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.review);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.titlebar);  
        curr_num=0;
		Intent intent=getIntent();
		book_id=intent.getExtras().getString("id");
		book_name=intent.getExtras().getString("name");		
		ListData=new ArrayList<HashMap<String,String>>();
		reviews=new ArrayList<Review>();
		
		review_no=(TextView)findViewById(R.id.review_no);
		title=(TextView)findViewById(R.id.titlebar_lv_title);
		list =(ListView)findViewById(R.id.main_list);			
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在加载，请稍候...");

		loadnews=new LoadNews();
		loadnews.execute();
		
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Review re=reviews.get(position);
				Intent in=new Intent(ReviewActivity.this,ReviewContent.class);
				in.putExtra("content", re.getContent());
				in.putExtra("author", re.getAuthor());
				startActivity(in);
			}
		});
        //LoadMore  TextView         
        LayoutInflater mInflater =(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE); ;
        View LoadMoreView=mInflater.inflate(R.layout.loadmore,null);
        list.addFooterView(LoadMoreView);
        tv_review_more=(TextView)LoadMoreView.findViewById(R.id.review_more);
        tv_review_more.setClickable(true);
        tv_review_more.setFocusable(true);        
        tv_review_more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadnews=new LoadNews();
				loadnews.execute();
			}
		});
        
        //返回按钮
        btn_back=(Button)findViewById(R.id.titlebar_bt_back);
        btn_back.setVisibility(View.VISIBLE);       
        btn_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent1=new Intent(ReviewActivity.this,MainActivity.class);
				startActivity(intent1);
				finish();
			}
		});
        
	}

	//异步加载数据的类
	public class LoadNews extends AsyncTask<Object, Integer, Void>
	{
	    protected void onPreExecute()
	    {
	    	progressDialog.show();
	    }

		protected Void doInBackground(Object... params) {
			if(getData(ListData)==NO_DATA)
			{
				ListData=null;
			}
			return null;
		}

	    protected void onPostExecute(Void result)
	    {
	    	//当没有笔记信息时做的控件显示操作
	    	if(ListData!=null)
	    	{
	    		listadpter = new SimpleAdapter(ReviewActivity.this, ListData, R.layout.main_listview_item,
						new String[]{"author","abstract"}, 
						new int[]{R.id.main_lv_title,R.id.main_lv_abstract});
				list.setAdapter(listadpter);
				list.setEnabled(true);
	    	}
	    	//当有笔记信息时做的控件显示操作
	    	else
	    	{
	    		Toast.makeText(ReviewActivity.this, "当前没有任何笔记,请返回", Toast.LENGTH_LONG).show();
	    		review_no.setVisibility(View.VISIBLE);
	    	}
	    	title.setText("《"+book_name+"》的"+total+"条笔记");
	    	title.setTextSize(12);
	    	progressDialog.dismiss();
	    }
	 }
	
	
	/**
	 * 发送get请求获取JSON数据并解析
	 * 填充List<Review>和List<HashMap<String,String>>内容
	 * @param listdata
	 * @return
	 */
	private int getData(List<HashMap<String,String>> ListData){
		String url="https://api.douban.com/v2/book/"+book_id+"/annotations?start="+curr_num+"&count=5";
		Log.i("OUTPUT",url);
		String result=Util.GetRequest(url);
		int count=5;
		total=0;
		try {
			JSONObject data_obj = new JSONObject(result);
			total=Integer.parseInt(data_obj.getString("total"));
			if(total==0)
			{
				return NO_DATA;
			}
			if(Integer.parseInt(data_obj.getString("total"))-Integer.parseInt(data_obj.getString("start"))<5)
			{
				count=Integer.parseInt(data_obj.getString("total"))-Integer.parseInt(data_obj.getString("start"));
			}
			JSONArray data_arr=data_obj.getJSONArray("annotations");
			for(int i=0;i<count;i++)
			{
				Review review=new Review();
				JSONObject data_arr_obj=data_arr.getJSONObject(i);
				review.setAuthor(data_arr_obj.getJSONObject("author_user").getString("name"));
				review.setAbstract(data_arr_obj.getString("abstract").replace("\n", " ").replace("\n", " ").replace("<原文开始>", "     "));
				review.setContent(data_arr_obj.getString("content").replace("<原文开始>", "     ").replace("</原文结束>", ""));
				reviews.add(review);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//将List<Review>的内容赋到List<HashMap<String,String>>中
		for(int i=curr_num;i<reviews.size();i++)
		{
			HashMap<String,String> ListMap=new HashMap<String,String>();
			ListMap.put("author", reviews.get(i).getAuthor());
			ListMap.put("abstract", reviews.get(i).getAbstract());
			ListMap.put("content",reviews.get(i).getContent());
			ListData.add(ListMap);
		}		
		curr_num+=count;
		return OK;
	}		
	
}
