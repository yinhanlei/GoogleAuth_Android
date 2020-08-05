# GoogleAuth_Android
仿谷歌身份验证器

查找资料
https://www.infoq.cn/article/2014/09/system-verification
https://www.jianshu.com/p/de903c074d77
http://192.168.40.196:11111/#g=1&p=%E8%AE%BE%E7%BD%AE%E5%8A%A8%E6%80%81%E5%8F%A3%E4%BB%A4

验证器的适用场景：
	用户的密码可能会因为各种原因泄漏。
	为此谷歌推出了 Google Authenticator 服务, 其原理是在登录时除了输入密码外, 还需根据 Google Authenticator APP 输入一个实时计算的验证码.。
	凭借此验证码, 即使在密码泄漏的情况下, 他人也无法登录你的账户。

验证器的原理：
	根据一个固定的秘钥+实时的时间点，生成一个6位数字验证码。验证码每隔30s更新一次。
	秘钥：	1秘钥是一个不少于16位、字符和数字组成的字符串。可以随机生成，但后台和APP要对一个相同秘钥生成验证码。
		2保存好秘钥很关键，即使无网，知道秘钥也能生成验证码。
	如果手机丢失，怎么处理？答：知道秘钥就行，重新下载个Google Authenticator APP ，输入记住的秘钥，再次生成。

验证器的流程：
	网页——后台——APP
	网页上会给出一个秘钥或二维码，后台和APP都会根据上述原理，每隔30s更新一个验证码。只有当用户输入APP上生成的验证码，和后台生成的验证码一致时，才有效通过。

我的开发：
	google发布的验证器，不能扫描二维码。需要自己写个APP。但用支付宝、淘宝能扫描出二维码信息。
	AndroidStudio341
	java8
	minSdkVersion 21
	compileSdkVersion 30
	需要commons-codec-1.8.jar，下载网址在 https://www.jianshu.com/p/c1e8cab1a569
开发主要部分：
1、生成验证码

2、30s倒计时更新：item里定时器的实现
3、扫二维码识别：参考原作者项目网址  https://www.cnblogs.com/zoro-zero/p/12068613.html    原作者Demo地址：https://github.com/hongchuanfeng/QRCodeDemo.git
4、同步时间

# QRCodeDemo Android 实现二维码扫描功能
以ZXing开源项目包作为Android应用程序扫码的插件，从ZXing接入、识别二维码效率优化、开关闪光灯、图片二维码识别的角度对ZXing集成展开介绍，并上传了相关项目代码，可供参考。
参考原作者项目网址  https://www.cnblogs.com/zoro-zero/p/12068613.html
原作者Demo地址：https://github.com/hongchuanfeng/QRCodeDemo.git

#本项目开发，在QRCodeDemo项目的基础上进行。


注意事项：
如果复制该项目代码进自己新建的项目里，二维码启动失败。原因极大可能是，新建项目和本项目的配置文件不一致造成的。
注意App build.gredle、proguard-rules.pro、项目 build.gredle、gradle.properties、gradlew、gradlew.bat。


