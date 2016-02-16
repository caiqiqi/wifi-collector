## preview
![preview](https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/device-2015-11-24-210859.mp4_1455616398.gif)

**When we start up the application,a list of APs is displayed.**</br>
<img src="https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/screenshot_2015-11-27-16-56-14.png" width=40% height=40%></br>
**For state/encrytion of each AP**</br>
- AP secured with "WEP" or "PSK" is displayed as "Secured"
- Open AP with no encryption is displayed as "Open".
- AP we've connected to is displayed as "connected".

**For signal strength of each AP**</br>
is indicated by the icon on the left-hand side in each list item.</br>

**When we click on the list item of the AP we've connected to,a popup window is displayed showing the basic parameters that this connection could acquire.**</br>
<img src="https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/screenshot_2015-11-27-16-56-32.png" width=40% height=40%></br>
**When we click on the list item of the AP we're not connected to.**</br>
- For "Secured" AP,a password is required.</br>
<img src="https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/screenshot_2015-11-27-16-56-43.png" width=40% height=40%></br>
- For "Open" AP,you simply need to choose connecting to it or not.</br>
<img src="https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/screenshot_2015-11-27-16-57-01.png" width=40% height=40%></br>

**When we click the physical "menu" button or the icon in the top right corner,three options are to be chosen.**</br>
<img src="https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/screenshot_2015-11-27-16-57-22.png" width=40% height=40%></br>
- "Set Server" means you should choose the IP address and port number your configured server has.Note that the IP address and port number is divided by the colon.You click "OK" and the configuration info is stored in a xml file(If you don't set the server,the default IP and port is acquired by the xml file which you set before)</br>
<img src="https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/screenshot_2015-11-27-16-57-49.png" width=40% height=40%></br>
- When you click the "Start Sync" option,you start sending the parameters of each AP to the set server every 10 seconds(of course you could choose how often).When the server received the info,it stores the info into a .CSV file every 10 seconds accordingly.When the server finished storing the .CSV file and has processed the info of each AP collected by the Android client,it sends the result back to the Android client.When the Android received the info sent by the server,it shows a toast to the client "Synchronized!"</br>
<img src="https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/screenshot_2015-11-27-16-58-24.png" width=40% height=40%></br>
- When you want to stop the Synchronization,you just click the "Stop Sync" option,and the Synchronization stops.
<img src="https://github.com/caiqiqi/android-wifi-connecter1/blob/master/img/screenshots/screenshot_2015-11-27-16-58-32.png" width=40% height=40%></br>
