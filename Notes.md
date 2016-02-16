# Bugs and corresponding solutions

Today I talked with Gilani,and he mentioned that there will be a famous meeting coming soon next year,and if we could complete our work on that,we could present our work on that meeting ,which motivates me.So I decided to continue working on this project.Now I changed to Android Studio.It is said that this tool is great although you may encounter some issues when you just jump to this from Eclipse.Now hell yeah ,I did encounter some issues ,but no worry ,I searched them through google and stackoverflow and now I've finished it.Here are some pictures illustrating my issues.

I dont know why by default,Android Studio uses `buildToolsVersion：23`，this means I use `C:\Android\sdk\build-tools\23.0.0\aapt.exe`,then it gives me this error.
![23.0.0-aapt](https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/issue-appt-23.PNG) </br>
When I use `21.0.0-aapt.exe`,it gives me this error.
![21.0.0-aapt](https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/21.0.0-aapt.PNG) </br>
When I use `21.1.2-aapt.exe`,it gives me this error.
![21.1.2-aapt](https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/21.1.2-aapt.PNG) </br>

Finally,I searched it in google,and found an ![answer](http://stackoverflow.com/questions/29766830/execution-failed-for-task-appcompiledebugaidl-aidl-is-missing) in stackoverflow.At first,I just modified the buildToolsVersion to 21.0.0 or 21.1.2,but I still failed.At last,I found this same anwer somewhere else,which indicates that this answer really solved their problem.So I took a look back at this answer,and I knew that I should update to carery version,and click "check update" so as to update Android Studio to the latest version.After 5 minutes,problems solved!</br>

![problem solved](https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/%E9%9D%A0-%E6%94%B9%E6%88%9021.1.2%E7%BB%88%E4%BA%8E%E6%90%9E%E5%AE%9A%E4%BA%86.PNG)

Markdown不能设置图片大小，如果必须设置则应使用HTML标记`<img>`</br>
HTML示例：`<img src="/assets/images/jian.jpg" alt="替代文本" title="标题文本" width=50% height=50% />`
