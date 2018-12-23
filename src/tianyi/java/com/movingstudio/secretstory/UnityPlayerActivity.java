package com.movingstudio.secretstory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.unity3d.player.UnityPlayer;


public class UnityPlayerActivity extends Activity
        implements DialogInterface.OnDismissListener {
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    private String _gameObjectToNotify;
    ProgressDialog mWaitProgress = null;
    static final String TAG = "Secret In Story";
    private int mPayId;
    static public String mLocalString = "";
    boolean isNoAds = false;
    int sceneId = 0;
    boolean enableToast = false;

    {
        PlatformConfig.setWeixin("wxeac9c7ba1045539a", "64020361b8ec4c99936c0e3999a9f249");
        //新浪微博
        PlatformConfig.setSinaWeibo("2489447285", "527f5286205e813b6cd410e36e598986", "http://www.weibo.com/rocheon\n");
        PlatformConfig.setTwitter("NwDjSVm3hB8P3aOd34XS0Gfpx", "fn9aOQa0DRLqGVmcn0k3jtoXRvPt7xqHtom1d6FdwRIGWq9iIV");
    }

    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

        mUnityPlayer = new UnityPlayer(this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();

        MultiDex.install(this);

//        UMU3DCommonSDK.init(this, "5c09ee69f1f5567649000090", "TianYi", UMConfigure.DEVICE_TYPE_PHONE,
//                "669c30a9584623e70e8cd01b0381dcb4");
        UMConfigure.init(this, "5c04f20bb465f5e7d7000084", "tykj", UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setScenarioType(this, EScenarioType.E_UM_NORMAL);
        // 将默认Session间隔时长改为40秒。
        MobclickAgent.setSessionContinueMillis(1000*40);

        android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                initLeaderBoard();
            }
        }, 1000);

    }

    public void startPurchase(String gameObjectToNotify) {
        _gameObjectToNotify = gameObjectToNotify;
    }


    private void showToast(String message) {
        if(enableToast)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Quit Unity
    @Override
    protected void onDestroy() {
        mUnityPlayer.quit();
        super.onDestroy();
    }

    // Pause Unity
    @Override
    protected void onPause() {
        super.onPause();
        mUnityPlayer.pause();
        MobclickAgent.onPause(this);
    }

    // Resume Unity
    @Override
    protected void onResume() {
        super.onResume();
        mUnityPlayer.resume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUnityPlayer.start();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            mUnityPlayer.lowMemory();
        }
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        super.onActivityResult(requestCode, resultCode, data);

        //UMShareAPI.get(this).onActivityResult(requestCode,resultCode, intent);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

        dismissWaitDialog();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    // Notify Unity of the focus change.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
        final View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return mUnityPlayer.injectEvent(event);
        }
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    /*API12*/
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    public boolean hasInternetConnection() {
        try {
            String url = "https://www.google.com";
            if (isSimpleChinese()) url = "https://www.baidu.com";//For Simple Chinese
            //waitProgress.show();
            HttpURLConnection urlc = (HttpURLConnection) (new URL(url).openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            showMessageWithLocalString("Network Failed");
        }
        return false;
    }

    public boolean isSimpleChinese() {
        return true;
    }

    public boolean isTraditionChinese() {
        return false;
    }

    void showMessage(final String msg) {
        this.runOnUiThread(new Runnable() {
            public void run() {
				/*
				AlertDialog.Builder bld = new AlertDialog.Builder(PuzzleNumbers.this);
				bld.setMessage(msg);
				bld.setNeutralButton("OK", null);
				bld.create().show();
				*/

                AlertDialog.Builder builder = new AlertDialog.Builder(UnityPlayerActivity.this);
                //builder.setTitle("My Title");
                builder.setMessage(msg);
                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                });

                LinearLayout.LayoutParams buttonParams;
                Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                buttonParams = (LinearLayout.LayoutParams) buttonPositive.getLayoutParams();
                buttonParams.weight = 1;
                buttonParams.width = buttonParams.MATCH_PARENT;
                buttonPositive.setLayoutParams(buttonParams);

                TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
                messageText.setGravity(Gravity.CENTER);

            }
        });
    }

    void showWaitDialog() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (mWaitProgress == null) {
                    mWaitProgress = ProgressDialog.show(UnityPlayerActivity.this, "", "", true);
                    mWaitProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mWaitProgress.setCancelable(false);
                    mWaitProgress.setOnKeyListener(onKeyListener);
                    mWaitProgress.setContentView(R.layout.progress_dialog);
                    //waitProgress.addContentView(new Spinner(getContext()), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    mWaitProgress.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                } else {
                    mWaitProgress.show();
                }
            }
        });

		/*
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				if(mWaitProgress == null)
				{
					mWaitProgress = ProgressDialog.show(UnityPlayerActivity.this, "", "", true);
					mWaitProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				}
				else
				{
					mWaitProgress.show();
				}
			}
		});
		*/
    }

    private DialogInterface.OnKeyListener onKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                dismissWaitDialog();
            }
            return false;
        }
    };

    void dismissWaitDialog() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (mWaitProgress != null && mWaitProgress.isShowing()) mWaitProgress.dismiss();
            }
        });
    }

    public void showMessageWithLocalString(String msg) {
        UnityPlayer.UnitySendMessage("Main Camera", "getLocalString", msg);
    }

    public void doShowLocalString(String value) {
        mLocalString = value; //Get new one
        showMessage(value);
    }

    public void initPay(int pi) {
        this.initUI();
        //this.initSDK();
        //进入游戏主页后，调用验证掉单接口z
        //checkPay();
    }

    public void exitPay() {

    }

    public void restore() {

    }

    public void purchase() {

    }

////////////////////////////////////////////////Zeus////////////////////////////////////////////////
   // private TextView mResultView;
    private String mDefaultMessage = "Not configured";

    private void initUI() {
//        mResultView = (TextView) findViewById(R.id.result_view);
//        mResultView.setText("未登录");
          showToast("未登录");
    }


    /**
     * 游戏退出确认（必接）
     */
    public void exit() {
    }

    /**
     * 登录渠道(单机游戏不需要接)
     */
    public void login(View v) {
    }

    /**
     * 登出(单机游戏不需要接)
     */
    public void logout(View v) {
    }

    /**
     * 提交游戏角色参数(单机游戏不需要接)
     */
    public void submitUserExtra(View v) {

    }



    /**
     * 游戏内购（必接）
     */
    public void pay(int productId) {
        if(productId == 1)
        {
            //PayParams params = new PayParams();
            // 购买数量，固定1（必须）
            //params.setBuyNum(1);

            // 充值金额(单位：元)（必须）
            //params.setPrice(1);

            // 充值商品id，productId必须是数字，且必须是1、2、3、4、5......（必须）
            //params.setProductId("1");

            // 商品名称，比如：100元宝，500钻石，不可包含特殊字符，只能包含中文、英文字母大小写、数字、下划线。（必须）
            //params.setProductName("6个币");

            // 商品描述，比如：充值100元宝，赠送20元宝，不可包含特殊字符，只能包含中文、英文字母大小写、数字、下划线。（必须）
            //params.setProductDesc("获得6个币");

            // 当前玩家身上拥有的游戏币数量（网游必须，单机不用传）
            //params.setCoinNum(100);

            // 透传参数，充值成功，回调通知游戏服务器的时候，会原封不动返回。（透传字段长度不超过64）（网游可选，单机不用传）
            //params.setExtraMessage("ExtraMessage");

            // 服务器发货模式的订单标识，订单ID要包含支付的信息，订单ID长度不能超过30个字符，不可重复，且只能由字母大小写和数字组成，字母区分大小写（网游必须，单机不用传）
            //params.setOrderID("T" + System.currentTimeMillis());

            //AresPlatform.getInstance().pay(this, params);
        }
        else if(productId == 2)
        {
            //PayParams params = new PayParams();
            // 购买数量，固定1（必须）
            //params.setBuyNum(1);

            // 充值金额(单位：元)（必须）
            //params.setPrice(6);

            // 充值商品id，productId必须是数字，且必须是1、2、3、4、5......（必须）
            //params.setProductId("2");

            // 商品名称，比如：100元宝，500钻石，不可包含特殊字符，只能包含中文、英文字母大小写、数字、下划线。（必须）
            //params.setProductName("解锁所有提示");

            // 商品描述，比如：充值100元宝，赠送20元宝，不可包含特殊字符，只能包含中文、英文字母大小写、数字、下划线。（必须）
            //params.setProductDesc("解锁所有提示");

            // 当前玩家身上拥有的游戏币数量（网游必须，单机不用传）
            //params.setCoinNum(100);

            // 透传参数，充值成功，回调通知游戏服务器的时候，会原封不动返回。（透传字段长度不超过64）（网游可选，单机不用传）
            //params.setExtraMessage("ExtraMessage");

            // 服务器发货模式的订单标识，订单ID要包含支付的信息，订单ID长度不能超过30个字符，不可重复，且只能由字母大小写和数字组成，字母区分大小写（网游必须，单机不用传）
            //params.setOrderID("T" + System.currentTimeMillis());

            //AresPlatform.getInstance().pay(this, params);
        }
        else if(productId == 3)
        {
            //PayParams params = new PayParams();
            // 购买数量，固定1（必须）
            //params.setBuyNum(1);

            // 充值金额(单位：元)（必须）
            //params.setPrice(10);

            // 充值商品id，productId必须是数字，且必须是1、2、3、4、5......（必须）
            //params.setProductId("3");

            // 商品名称，比如：100元宝，500钻石，不可包含特殊字符，只能包含中文、英文字母大小写、数字、下划线。（必须）
            //params.setProductName("解锁提示和60个币");

            // 商品描述，比如：充值100元宝，赠送20元宝，不可包含特殊字符，只能包含中文、英文字母大小写、数字、下划线。（必须）
            //params.setProductDesc("解锁所有提示并获得60个币");

            // 当前玩家身上拥有的游戏币数量（网游必须，单机不用传）
            //params.setCoinNum(100);

            // 透传参数，充值成功，回调通知游戏服务器的时候，会原封不动返回。（透传字段长度不超过64）（网游可选，单机不用传）
            //params.setExtraMessage("ExtraMessage");

            // 服务器发货模式的订单标识，订单ID要包含支付的信息，订单ID长度不能超过30个字符，不可重复，且只能由字母大小写和数字组成，字母区分大小写（网游必须，单机不用传）
            //params.setOrderID("T" + System.currentTimeMillis());

            //AresPlatform.getInstance().pay(this, params);
        }

    }

    /**
     * 验证是否存在支付掉单（单机游戏必接）
     */
    public void checkPay() {
        //AresPlatform.getInstance().checkPay(new AresPayListener() {
        //    @Override
        //    public void onResult(int code, String productId) {
        //        Log.d(TAG, "code=" + code + ", msg=" + productId);
        //        if (code == AresCode.CODE_PAY_SUCCESS) {
        //            //验证成功，根据回调的商品id发货
        //            //mResultView.setText("掉单验证成功：" + productId);
        //            showToast("掉单验证成功：" + productId);
        //        } else {
        //           //mResultView.setText("未查询到掉单");
        //            showToast("未查询到掉单：" + productId);
        //        }
        //    }
        //});
    }

    /**
     * 获取自定义参数
     */
    public void showCustom(View v) {
        //String customParam = InnerTools.getCustomParam();
        //if (TextUtils.isEmpty(customParam)) {
        //    showToast(mDefaultMessage);
        //    //mResultView.setText(mDefaultMessage);
        //} else {
        //    showToast(customParam);
        //    // mResultView.setText(customParam);
        //}
    }

    /**
     * 判断是否显示登录按钮，true显示，false隐藏
     */
    public void showLogin(View v) {
        //boolean showLogin = SdkTools.swichState(SwichType.SHOW_LOGIN);
        //mResultView.setText("Show Login:" + showLogin);
        //showToast("Show Login:" + showLogin);
    }

    /**
     * 判断是否显示为“领取”，true显示为“领取”，false显示为“购买”
     */
    public void showBuy(View v) {
        //boolean showBuy = SdkTools.swichState(SwichType.SHOW_GET);
        //mResultView.setText("Show Buy:" + showBuy);
        //showToast("Show Buy:" + showBuy);
    }

    /**
     * 判断是否显示评价入口，true显示，false隐藏
     */
    public void showMarket(View v) {
        //boolean showMarket = SdkTools.swichState(SwichType.SHOW_ENTRANCE);
        //mResultView.setText("Show Market:" + showMarket);
        //showToast("Show Market:" + showMarket);
    }

    /**
     * 五星好评引导，跳转应用商店游戏详情页
     */
    public void gotoMarket(View v) {
        //SdkTools.gotoMarket(new AresAwardCallback() {
        //    @Override
        //    public void onAward(String productId) {
        //        //收到回调，根据商品id发放奖励，目前productId返回null，未定义发放奖励，游戏根据情况发放奖励商品
        //        Log.d(TAG, "goto market award.");
        //    }
        //});
    }

    /**
     * 使用兑换码
     */
    public void useCDKEY(View v) {
        //if (mCdkeyDialog == null) {
        //    mCdkeyDialog = new CDKEYDialog(this);
        //}
        //mCdkeyDialog.show();
    }

    /**
     * 跳转QQ聊天
     */
    public void skipQQChat(View v) {
        //SdkTools.skipQQChat("");
    }

    /**
     * 添加QQ群
     */
    public void joinQQGroup(View v) {
        //SdkTools.joinQQGroup("");
    }

    /**
     * 调起拨号盘
     */
    public void callPhone(View v) {
        //SdkTools.callPhone("123456");
    }

    /**
     * 统计模块接口
     */
    public void analyticsTest(View v) {
        //Intent intent = new Intent(this, AnalyticsActivity.class);
        //startActivity(intent);
    }

////////////////////////////////////////////////Zeus////////////////////////////////////////////////


    /***************************************LeaderBoard *********************************************************/
    public void initLeaderBoard() {
    }

    public void reportScore(int score) {
    }

    public void showLeaderboard() {

    }

    private boolean isSignedIn() {
        return false;
    }

    public void aboutMe() {
        if (isSimpleChinese()) {
            //Simple code for ope web browser in android
            Intent webPageIntent = new Intent(Intent.ACTION_VIEW);
            webPageIntent.setData(Uri.parse("http://weibo.com/rocheon"));
            startActivity(webPageIntent);
        } else {
            //Simple code for ope web browser in android
            Intent webPageIntent = new Intent(Intent.ACTION_VIEW);
            webPageIntent.setData(Uri.parse("http://www.rocheon.com"));
            startActivity(webPageIntent);
        }
    }

    public void more() {
		 /*
		 Intent webPageIntent = new Intent(Intent.ACTION_VIEW);
		 if(this.publishPlatform == PublishPlatform.GOOGLEPLAY )
		 {
			webPageIntent.setData(Uri.parse("http://play.google.com/store/search?q=pub:Luo+Zhi+En"));
		 }
		 else if(this.publishPlatform == PublishPlatform.QIHU360)
		 {
			webPageIntent.setData(Uri.parse("http://rocheon.com/?page_id=2"));
		 }
		 else
		 {
			webPageIntent.setData(Uri.parse("http://rocheon.com/?page_id=2"));
		 }
		 startActivity(webPageIntent);
		 */

        String languageDefault = Locale.getDefault().getLanguage();
        if (isSimpleChinese()) {
            //Simple code for ope web browser in android
            Intent webPageIntent = new Intent(Intent.ACTION_VIEW);
            webPageIntent.setData(Uri.parse("http://blog.sina.com.cn/s/blog_b38c47370102vdbk.html"));
            startActivity(webPageIntent);
        } else {
			/*
			Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.vending");
			ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.activities.LaunchUrlHandlerActivity"); // package name and activity
			launchIntent.setComponent(comp);
			launchIntent.setData(Uri.parse("market://search?q=pub:Luo+Zhi+En"));
			startActivity(launchIntent);
			*/

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://search?q=pub:Luo+Zhi+En"));
            PackageManager pm = getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            boolean isWeb = true;
            for (int a = 0; a < list.size(); a++) {
                ResolveInfo info = list.get(a);
                ActivityInfo activity = info.activityInfo;
                if (activity.name.contains("com.google.android")) {
                    ComponentName name = new ComponentName(
                            activity.applicationInfo.packageName,
                            activity.name);
                    Intent i = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://search?q=pub:Luo+Zhi+En"));
                    //i.addCategory(Intent.CATEGORY_LAUNCHER);
                    //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    //| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    i.setComponent(name);
                    startActivity(i);
                    isWeb = false;
                    //finish();
                }
            }
            if (isWeb) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=pub:Luo+Zhi+En")));
            }


		   /*
			final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Luo+Zhi+En")));
			} catch (android.content.ActivityNotFoundException anfe) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=pub:Luo+Zhi+En")));
			}
			*/
        }
/*
			//Simple code for ope web browser in android
			Intent webPageIntent = new Intent(Intent.ACTION_VIEW);
			webPageIntent.setData(Uri.parse("https://play.google.com/store/search?q=pub:Luo+Zhi+En"));
			startActivity(webPageIntent);
*/
    }


    // 在您准备好展示插页式广告时调用displayInterstitial()。
    public void ShowInterstialAd() { }

    public boolean AskForShowRewardBasedVideo() {
        return false;
    }

    public boolean AskForShowHintVideo() {
        return false;
    }

    public boolean AskForShowKeyVideo() {
        return false;
    }


    void setScene(int id) {
        sceneId = id;
    }

    public boolean IsRewardBasedVideoReady() {
        return false; //Not used anymore
    }

    public boolean IsHintVideoReady() {
        return false;//Not used anymore
    }


    public void initSocial() {
    }


    public void TestScene(String sceneName) {
        UnityPlayer.UnitySendMessage("Main Camera", "TestScene", sceneName);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    public void OnRewardAdsClosed(int placementId, boolean rewarded) {
        Log.e("OnRewardAdsClosed", Integer.toString(placementId));
        // Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        if (placementId == 1) {
            if (rewarded) {
                UnityPlayer.UnitySendMessage("Scene", "OnRewardBasedVideoAdDidClose", "AddHP");
            } else {
                UnityPlayer.UnitySendMessage("Scene", "OnRewardBasedVideoAdDidClose", "GameOver");
            }
        } else if (placementId == 2) {
            if (rewarded) {
                UnityPlayer.UnitySendMessage("Scene", "OnRewardBasedVideoAdDidClose", "ShowHint");
            } else {
                UnityPlayer.UnitySendMessage("Scene", "OnRewardBasedVideoAdDidClose", "HideHint");
            }
        } else if (placementId == 4) {
            if (rewarded) {
                UnityPlayer.UnitySendMessage("ShopCanvas", "OnRewardBasedVideoAdDidClose", "AddKey");
            } else {
                UnityPlayer.UnitySendMessage("ShopCanvas", "OnRewardBasedVideoAdDidClose", "DontAddKey");
            }
        }
    }

    public int getChannelId()
    {
        return Helper.getChannelId(getBaseContext());
    }

    public boolean IsNoAdsVersion()
    {
        return  true;
    }

}
