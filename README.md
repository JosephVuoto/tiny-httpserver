# tiny-httpserver
一个用Java NIO写的玩具HTTP服务器

## 启动
``` java
    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress(Config.PORT);
        HttpServer server = new HttpServer(address);
        try {
            server.init();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```

## 截图
![](screenshots/demo.png)