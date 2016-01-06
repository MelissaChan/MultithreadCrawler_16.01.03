package Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Crawler.DownLoadFile;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class WebCrawler {  
    ArrayList<String> allurl = new ArrayList<String>();//所有的网页url，需要更高效的去重可以考虑HashSet  
    ArrayList<String> unvisited = new ArrayList<String>();//未爬过的网页url  
    HashMap<String, Integer> depth = new HashMap<String, Integer>();//所有网页的url深度  
    int crawlDepth  = 2; //爬虫深度  
    int threadCount = 10; //线程数量  
    int count = 0; //表示有多少个线程处于wait状态  
    public static final Object signal = new Object();   //线程间通信变量  
      
    public static void main(String[] args) {  
        final WebCrawler wc = new WebCrawler();  
        wc.addUrl("http://www.cnblogs.com", 1);  
        long start= System.currentTimeMillis();  
        System.out.println("开始爬虫.........................................");  
        wc.begin();  
          
        while(true){  
            if(wc.unvisited.isEmpty()&& Thread.activeCount() == 1||wc.count==wc.threadCount){  
                long end = System.currentTimeMillis();  
                System.out.println("总共爬了"+wc.allurl.size()+"个网页");  
                System.out.println("总共耗时"+(end-start)/1000+"秒");  
                System.exit(1);  
//              break;  
            }  
              
        }  
    }  
    private void begin() {  
        for(int i=0;i<threadCount;i++){  
            new Thread(new Runnable(){  
                public void run() {  
                    while (true) {   
//                      System.out.println("当前进入"+Thread.currentThread().getName());  
                        String tmp = getAUrl();  
                        if(tmp!=null){  
                            crawler(tmp);  
                        }else{  
                            synchronized(signal) {
                                try {  
                                    count++;  
                                    System.out.println("当前有"+count+"个线程在等待");  
                                    signal.wait();  
                                } catch (InterruptedException e) {  
                                    e.printStackTrace();  
                                }  
                            }  
                              
                              
                        }  
                    }  
                }  
            },"thread-"+i).start();  
        }  
    }  
    public synchronized  String getAUrl() {  
        if(unvisited.isEmpty())  
            return null;  
        String tmpAUrl;  
//      synchronized(unvisited){  
            tmpAUrl= unvisited.get(0);  
            unvisited.remove(0);  
//      }  
        return tmpAUrl;  
    }  

      
    public synchronized void  addUrl(String url,int d){  
            unvisited.add(url);  
            allurl.add(url);  
            depth.put(url, d);  
    }  

  	
    //爬网页sUrl  
    public  void crawler(String sUrl){  
        URL url;  
        try {  
                url = new URL(sUrl);  
                URLConnection urlconnection = url.openConnection();  
                urlconnection.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:43.0) Gecko/20100101 Firefox/43.0)");  
                InputStream is = url.openStream();  
                BufferedReader bReader = new BufferedReader(new InputStreamReader(is));  
                StringBuffer sb = new StringBuffer();//sb为爬到的网页内容  
                String rLine = null;  
                while((rLine=bReader.readLine())!=null){  
                    sb.append(rLine);  
                    sb.append("/r/n");  
                }  
                  
                int d = depth.get(sUrl);  
                System.out.println("爬网页"+sUrl+"成功，深度为"+d+" 是由线程"+Thread.currentThread().getName()+"来爬");  
                if(d<crawlDepth){  
                    //解析网页内容，从中提取链接  
                    parseContext(sb.toString(),d+1);  
                }  
//              System.out.println(sb.toString());  
  
              
        } catch (IOException e) {  
//          crawlurlSet.add(sUrl);  
//          unvisited.remove(sUrl);  
            e.printStackTrace();  
        }  
    }  
  
    //从context提取url地址  
    public  void parseContext(String context,int dep) {  
        String regex = "<a href.*?/a>";  
        Pattern p = Pattern.compile(regex);  
        Matcher m = p.matcher(context);  
        while (m.find()) {  
//          System.out.println(mt.group());  
            Matcher myurl = Pattern.compile("href=\".*?\"").matcher(  
                    m.group());  
            while(myurl.find()){  
                String str = myurl.group().replaceAll("href=\"|\"", "");  
//              System.out.println("网址是:"+ str);  
                if(str.contains("http:")){ //取出一些不是url的地址  
                    if(!allurl.contains(str)){  
                        addUrl(str, dep);//加入一个新的url  
                        if(count>0){ //如果有等待的线程，则唤醒  
                            synchronized(signal) {  //---------------------（2）  
                                count--;  
                                signal.notify();  
                            }  
                        }  
                          
                    }  
                }  
            }  
        }  
    }  
}    