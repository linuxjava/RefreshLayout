# RefreshLayout

开发者使用 RefreshLayout-Android 对RecycView、Listview、ScrollView等控件实现下拉刷新和上拉加载
##  APK下载
[Download](https://github.com/linuxjava/RefreshLayout/raw/master/apk/app-debug.apk)
##  Demo使用
运行demo需删除gradle.properties中的代理
```xml
systemProp.http.proxyHost=dev-proxy.oa.com
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=dev-proxy.oa.com
systemProp.https.proxyPort=8080
```
## Gradle配置

## XML配置
```xml
<xiao.free.refreshlayoutlib.SwipeRefreshLayout
        android:id="@+id/swiperefreshlayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <include
            android:id="@id/swipe_refresh_header"
            layout="@layout/layout_classic_header" />

        <ListView
            android:id="@id/swipe_target"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipToPadding="false" />

        <include
            android:id="@id/swipe_load_more_footer"
            layout="@layout/layout_classic_footer" />

</xiao.free.refreshlayoutlib.SwipeRefreshLayout>
```
## 属性说明


## 效果图
如下图所示：

![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/1.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/2.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/3.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/4.gif)
