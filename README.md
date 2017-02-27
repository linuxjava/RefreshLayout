# RefreshLayout

开发者使用 RefreshLayout-Android 对RecycView、Listview、ScrollView等控件实现下拉刷新和上拉加载
项目特点
* 该项目的设计结构和整体业务逻辑清晰，各细节功能点的处理考虑全面；
* 对项目的整体开发步骤以及所有晦涩难懂的点，都有详细注释，非常适合中高端工程师学习并进行二次开发

![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/note.png)

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
|属性|说明|
|----|-----
|refresh_enabled|刷新使能
|load_more_enabled|加载使能
|swipe_style|header和footer模式
|drag_ratio|drag阻尼系数，越小越难拉动
|refresh_trigger_offset|触发刷新的偏移，默认为header高度
|load_more_trigger_offset|触发加载更多的偏移，默认为footer高度
|refresh_final_drag_offset|下拉最大偏移，默认为0
|load_more_final_drag_offset|加载更多最大偏移，默认为0

## 效果图
如下图所示：

![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/1.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/2.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/3.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/4.gif)
