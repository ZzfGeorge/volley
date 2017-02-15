>谷歌2013Google I/O 年发布的HTTP库。（HTTP library announced by google in I/O 2013）

首先，通过分析如何设计一个通用的网络框架（Network Framework），来了解一个网络请求的大致工作流程；然后分析Volley框架的的具体实现；最后对Volley框架进行一些适当的扩展。

##一个简单的网络请求处理过程

![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/app-network-1.jpg)  
处理过程为：  
   1.   应用程序（App）发起发送一个请求（request）。  
   2.   发送请求（request）到网络（Network）。  
   3.   网络处理请求，得到反馈（response）  
   4.   发送反馈结果（response）到应用程序（App）。  

该框架对于一个简单的网络请求是满足的，但是对于数据是固定的，且频繁发生的请求，显示的效果是可以通过缓存（cache）机制来提升的。
##加入本地缓存机制
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/app-network-2.jpg)  
处理过程为：
   1.   应用程序（App）发起发送一个请求（request）。  
   2.   发送请求（request）。  
      2.1   如果需要缓存，那么对请求作缓存处理。  
      2.1.1 如果本地已有缓存，并且没有过期，那么返回缓存结果（cached response）给App。  
      2.1.2 如果本地没有缓存，或者存已经过期的缓存，那么发送请求（request）到网络（Network）。  
   3.   网络（Network）处理请求，并获取反馈（response）    
      3.1   如果request需要缓存，那么将反馈结果（response）写入本地缓存。  
      3.2   发送反馈结果（network response）到应用程序（App）。   

引入本地缓存之后，对于启用本地缓存机制的网络请求，其处理的速度可以大大的增加。对于应用程序，大多是一个主线程负责UI交互，多个辅助线程负责数据处理，而且多个网络请求并发处理（Concurrent Processing），因此需要引入**[线程池（Thread Pool）]**来并行执行多个请求；同时多个网络请求需要作排队处理，那么就需要引入**[请求队列（Reqeust Queue）]**：对于需要作缓存处理的请求，将其加入**[缓存请求队列（Cache Reqeust Queue or Cache Queue）]**，对于其它网络请求，将其加入[网络请求队列（Network Reqeust Queue or Network Queue）]**。
##加入线程池（Thread Pool）和请求队列（Request Queue）
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/app-network-3.jpg)  
处理过程为：
   1.   [UI]应用程序（App）发起发送一个请求（request）。  
   2.   [UI]将请求（request）添加到请求队列（Request Queue）。  
      2.1    如果需要缓存，将请求添加到缓存请求队列（Cache Queue）。  
      2.2    否则，将请求添加到网络请求队列（Network Queue）。  
   3.   [Worker]缓存处理。  
      3.1   从缓存队列中，取出一个请求作缓存处理，得到一个缓存结果（Cached Network Response）  
      3.2   如果存在本地缓存并且没有过期，那么将缓存结果反馈给应用程序。  
      3.3   否则，将请求添加到网络请求队列。  
   4.  [Worker]网络处理。  
      4.1 从网络请求队列中，取出一个请求作网络处理，得到一个网络反馈结果（Network Response）   
      4.2 如果request需要缓存，那么将反馈结果（response）写入本地缓存。  
      4.3 将网络反馈结果反馈给应用程序。    

加入线程池（ThreadPool）和请求队列（RequestQueue）的网络框架，已经满足了我们的需求，接下来要考虑网络（Network）的实现。
## 网络(Network)的选取
Android提供了两种HTTP客户端（Http Clients）：**HttpURLConnection** 和 **Apache Http Client**，都支持HTTPS、流上传和下载（streaming uploads and downloads)、可配置超时时间（configurable timeout）、IPv6和连接池（connection pooling）。  
[Apache HTTP Client](http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-apache-http-client)有两个扩展实现：DefaultHttpClient 和其同胞兄弟 AndroidHttpClient，但均已经被Android废弃，并且在Android6.0中移除。[HttpURLConnection](http://developer.android.com/reference/java/net/HttpURLConnection.html)是一个通用的、轻量级的HTTP客户端（HTTP client），并且适用于大多数应用程序。如果对于低版本API LEVEL 9的不予考虑），那么选择HttpURLConnection是合适的。

#Volley分析
volley的处理过程，和我们上面的分析过程大致相同，也具有请求队列（Request Queue）
##请求队列（RequestQueue）
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/volley-request-queue.jpg)  

##网络（Network)
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/volley-network.jpg)  

##缓存（Cache）
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/volley-cache.jpg)  

##请求（Request）
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/volley-request.jpg)  

##错误处理（Error)
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/volley-error.jpg)  

##图片处理（ImageLoader）
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/volley-imageloader.jpg)  

##请求处理过程
![App network basic design](https://github.com/zzhifu/zhivolley/blob/master/extras/volley-request-processing.jpg)  

#参考引用［References］
1. [Android’s HTTP Clients](http://android-developers.blogspot.com/2011/09/androids-http-clients.html)
2. [Github-Volley-demo](https://github.com/smanikandan14/Volley-demo)
3. [Apache HTTP Client Removal](http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-apache-http-client) 
