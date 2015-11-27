When we start up the application,a list of APs is displayed.
## For state/encrytion of each AP
- For AP secured with "WEP" or "PSK" is displayed as "Secured"
- For open AP with no encryption is displayed as "Open".
- For AP we've connected to is displayed as "connected".
## For signal strength of each AP:
is indicated by the icon.
![](https://raw.githubusercontent.com/caiqiqi/android-wifi-connector1/img/screenshots/screenshot_2015-11-27-16-56-14.png)


When we click on the list item of the AP we've connected to,a popup window is displayed showing
the basic parameters that this connection could acquire.
![](https://raw.githubusercontent.com/caiqiqi/android-wifi-connector1/img/screenshots/screenshot_2015-11-27-16-56-32.png)

When we click on the list item of the AP we're not connected to.
For "Secured" AP,a password is required.
![](https://raw.githubusercontent.com/caiqiqi/android-wifi-connector1/img/screenshots/screenshot_2015-11-27-16-56-43.png)
For "Open" AP,you simply need to chose connect to it or not.
![](https://raw.githubusercontent.com/caiqiqi/android-wifi-connector1/img/screenshots/screenshot_2015-11-27-16-57-01.png)


When we click the physical "menu" button or the icon in the top right corner.Three options are to be chosen.
![](https://raw.githubusercontent.com/caiqiqi/android-wifi-connector1/img/screenshots/screenshot_2015-11-27-16-57-22.png)
- "Set Server" means you should choose the IP address and port number your configured server has.Note that the IP address and port number is divided by the colon.You click "OK" and the configuration info is stored in a xml file(If you don't set the server,the default IP and port is acquired by the xml file which you set before)
![](https://raw.githubusercontent.com/caiqiqi/android-wifi-connector1/img/screenshots/screenshot_2015-11-27-16-57-49.png)

- When you click the "Start Sync" option,you start sending the parameters of each AP to the set server every 10 seconds(of course you could choose how often).When the server received the info,it stores the info into a .CSV file every 10 seconds accordingly.When the server finished storing the .CSV file and has processed the info of each AP collected by the Android client,it sends the result back to the Android client.When the Android received the info sent by the server,it shows a toast to the client "Synchronized!"
![](https://raw.githubusercontent.com/caiqiqi/android-wifi-connector1/img/screenshots/screenshot_2015-11-27-16-58-24.png)
- When you want to stop the Synchronization,you just click the "Stop Sync" option,and the Synchronization stops.
![](https://raw.githubusercontent.com/caiqiqi/android-wifi-connector1/img/screenshots/screenshot_2015-11-27-16-58-32.png)
