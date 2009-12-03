package com.jros;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class downloader extends Service {
	private static String LOGID = "downloader";
	private static final int POLL_PERIOD=60000;
	private AtomicBoolean active=new AtomicBoolean(true);
	//public static String[] url = {"http://v2d7b.sheepserver.net/cgi/query.cgi?q=recent_frames&n=50"};
	public static String url = "http://twitpic.com/photos/jros/feed.rss";
	
	private static InputStream[] isArray;
	static int downloadCount =10;
	String[] imageUrls = new String[downloadCount];
    static Drawable[] drawables = new Drawable[downloadCount];
    
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOGID, "onCreate");
		new Thread(threadBody).start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return(null);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		active.set(false);
	}
	
	private void poll() {
		Log.i(LOGID, "begin polling");
        try {
        	//parseSheepXML(isArray[0]);
        	parseTwitterPicXML(getSingleUrlData(url));
	        //for(int j=0;j<imageUrls.length;j++){
	        	getUrlData(imageUrls,isArray);
	        	//drawables[j] = Drawable.createFromStream(isArray[j], "yyy");
	        //}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Runnable threadBody=new Runnable() {
		public void run() {
			while (active.get()) {
				poll();				
				SystemClock.sleep(POLL_PERIOD);
			}
		}
	};
	
    public void getUrlData(String[] urls, InputStream[] isArray)
	throws URISyntaxException, ClientProtocolException, IOException {
    	
    	for(int x=0; x<imageUrls.length;x++){
    		Log.i(LOGID, "imageUrls... " + imageUrls[x]);
    	}
    	
    	DefaultHttpClient client = new DefaultHttpClient();
		GetThread[] threads = new GetThread[urls.length];
  	  	for (int i = 0; i < threads.length; i++) {
	  	  	HttpGet get = new HttpGet(urls[i]);
	  	  	Log.i(LOGID, "client... " + client);
	  	  	Log.i(LOGID, "get... " + get);
	  	  	Log.i(LOGID, "count... " + i);
		  	threads[i] = new GetThread(client, get, i + 1, i);
		  	threads[i].start();
  	  	} 
    }
    
    public InputStream getSingleUrlData(String url)
	throws URISyntaxException, ClientProtocolException, IOException {
    	DefaultHttpClient client = new DefaultHttpClient();
	  	  	HttpGet get = new HttpGet(new URI(url));
            HttpResponse res = client.execute(get);
            InputStream is = res.getEntity().getContent();
            if (is == null)
            	Log.i(LOGID, "inputstream in null");
            
            return is;
    }
    
    /**
     * A thread that performs a GET.
	 */
    static class GetThread extends Thread {
        
		/*DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI(url));
		HttpResponse res = client.execute(method);
		return  res.getEntity().getContent();*/
    	
        private DefaultHttpClient httpClient;
        private HttpGet method;
        private int id;
        private InputStream is;
        private int count;
        
        public GetThread(DefaultHttpClient httpClient, HttpGet method, int id, int count) {
            this.httpClient = httpClient;
            this.method = method;
            this.id = id;
            this.count = count;
            //this.is = isArray;
        }
        
        /**
         * Executes the GetMethod and prints some satus information.
         */
        public void run() {
            
            try {
                HttpResponse response = httpClient.execute(method);
                if (response == null)
                	Log.i(LOGID, "res in null");
                is = response.getEntity().getContent();
                if (is == null)
                	Log.i(LOGID, "is in null");
                isArray[id] = is;
                drawables[count] = Drawable.createFromStream(isArray[count], "yyy");
                
            } catch (Exception e) {
                System.out.println(id + " - error: " + e);
            } finally {
                // always release the connection after we're done 
                //System.out.println(id + " - connection released");
            }
        }
       
    }
    
    public void parseSheepXML(InputStream is){
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			try{
				Document doc = db.parse(is);
				String textVal = null;
				NodeList nl = (NodeList) doc.getElementsByTagName("frame");
				//int pulseSize = ((org.w3c.dom.NodeList) nl).getLength();
				//Log.i(LOGID, "pulseSize: '" + pulseSize +"'");
				//pulseSize = 5;
				for (int i=0;i<downloadCount;i++){
					Log.i(LOGID, "inside for loop");
					if(nl != null && ((org.w3c.dom.NodeList) nl).getLength() > 0) {
						Element el = (Element)((org.w3c.dom.NodeList) nl).item(i);
						textVal = el.getAttribute("src");
						imageUrls[i] = textVal;
						Log.i(LOGID, "imageUrls: " + textVal);
					}
				}								
			}
			
			catch(SAXException e){
				Log.i(LOGID, "SAXException: '" + e + "'");
			}
		}
		catch(Exception e){
			Log.i(LOGID, "Exception: '" + e + "'");
		}
    }
    
    public void parseTwitterPicXML(InputStream is){
        XmlPullParserFactory factory;
        Log.i(LOGID, "in parseTwitterPicXML");
        
        if( is == null){
        	Log.i(LOGID, "InputStream is null");
        }
        
        int i1 = 0;
			try {
				factory = XmlPullParserFactory.newInstance();
	            factory.setNamespaceAware(true);
	            XmlPullParser xpp = factory.newPullParser();
	            
	            xpp.setInput(is, "UTF8");
	            //xpp.setInput(new StringReader ( "<foo>Hello World!</foo>" ));
	            if (xpp == null){
	            	Log.i(LOGID, "xpp is null");
	            }
	            
	            int eventType = xpp.getEventType();
	            
	            Log.i(LOGID, "eventType: " + eventType + " xpp.getName(): " + xpp.getName());
	            //eventType != XmlPullParser.END_DOCUMENT
	            while (eventType != xpp.END_DOCUMENT && i1 < downloadCount){
	            	 
			       	 xpp.next();
			       	 eventType = xpp.getEventType();
			       	Log.i(LOGID, "eventType: " + eventType + " xpp.getName(): " + xpp.getName());
			       	 if(eventType == XmlPullParser.TEXT){
			       		if (xpp.getText().contains(".jpg"))
			       		{
			       			Log.i(LOGID, "contains a jpg");
				            int begin = xpp.getText().indexOf("src=");
				            int end = xpp.getText().indexOf(".jpg");
				            imageUrls[i1] = xpp.getText().substring(begin+5, end +4);	
				            Log.i(LOGID,"imageUrls[" + i1 + "]" + imageUrls[i1] );
				            i1++;
			       		}
			       	 }
	            } 
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    
    }
}
