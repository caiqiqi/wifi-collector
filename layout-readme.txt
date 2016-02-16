-- floating.xml
点击某个AP列表项时打开的Activity的基类的配置文件
其中：
<include>是将标题栏的xml文件包含进来
<ScrollView>是显示所有AP列表的一个“可滑动视图”
<include>是包含一个分割的界限   buttons_view_divider.xml
<LinearLayout>包含最下面的三个Button

-- buttons_view_divider.xml
是包含在floating.xml中用于分割视图和三个按钮的

-- listitem_wifiap.xml
显示收集到的AP信息列表的主界面WifiScanActivity

-- base_content.xml
点击某个AP列表项时显示那些参数