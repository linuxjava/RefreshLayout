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
|属性|说明|
|----|-----
|refresh_enabled|刷新使能
|load_more_enabled|_斜体2_
|swipe_style|test1|test2
|drag_ratio|__粗体2__
|refresh_trigger_offset|这是一个 ~~删除线~~
|load_more_trigger_offset|***斜粗体1***
|refresh_final_drag_offset|___斜粗体2___
|load_more_final_drag_offset|***~~斜粗体删除线1~~***
|swiping_to_refresh_to_default_scrolling_duration|~~***斜粗体删除线2***~~
|release_to_refreshing_scrolling_duration|~~***斜粗体删除线2***~~
|refresh_complete_delay_duration|~~***斜粗体删除线2***~~
|refresh_complete_to_default_scrolling_duration|~~***斜粗体删除线2***~~
|default_to_refreshing_scrolling_duration|~~***斜粗体删除线2***~~
|swiping_to_load_more_to_default_scrolling_duration|~~***斜粗体删除线2***~~
|release_to_loading_more_scrolling_duration|~~***斜粗体删除线2***~~
|load_more_complete_delay_duration|~~***斜粗体删除线2***~~
|load_more_complete_to_default_scrolling_duration|~~***斜粗体删除线2***~~
|default_to_loading_more_scrolling_duration|~~***斜粗体删除线2***~~


## 效果图
如下图所示：

![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/1.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/2.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/3.gif)
![image](https://github.com/linuxjava/RefreshLayout/raw/master/gif/4.gif)
