package com.dommy.qrcode;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dommy.qrcode.activity.KeyBoardActivity;
import com.dommy.qrcode.base.BaseActivity;
import com.dommy.qrcode.bean.CodeBean;
import com.dommy.qrcode.googleAuth.GoogleAuthenticator;
import com.dommy.qrcode.util.Constant;
import com.dommy.qrcode.util.UrlDecodeUtils;
import com.dommy.qrcode.view.RingProgressBar;
import com.google.zxing.activity.CaptureActivity;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 布局实现原理：1不采用list item添加定时器的方式。
 * 2采用动态布局+倒计时结束重启的方式去更新item。
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private TextView btn_setting, btn_add;
    private Map<String, CodeBean> codeMap = new HashMap<>();//key秘钥，value信息
    private Context context;
    private Handler handler;
    private LinearLayout ll_item;//item父布局

    private static final String splitChars1 = "ΞυяにソΞ";//用特殊符号做一条好友信息的属性间分隔符，永久固定，不可更改。
    private static final String splitChars2 = "にΞυソяに";//用特殊符号做多条好友信息间分隔符，永久固定，不可更改。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        handler = new Handler();
        initView();
    }

    private void initView() {
        //        btn_setting = findViewById(R.id.btn_setting);
        btn_add = findViewById(R.id.btn_add);
        ll_item = findViewById(R.id.ll_item);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAddDialog();
            }
        });
        SharedPreferences sp = getSharedPreferences("codeList", MODE_PRIVATE);
        String codeStr = sp.getString("codeStr", "");
        Log.i(TAG, "本地存储codeStr= " + codeStr);
        if (codeStr.length() > 0 && codeStr.contains(splitChars1)) {
            if (codeStr.contains(splitChars2)) {//存多个
                String[] codeStrArr = codeStr.split(splitChars2);
                for (String codeStr1 : codeStrArr) {
                    String[] codeStr1Arr = codeStr1.split(splitChars1);
                    String user = codeStr1Arr[0];
                    String sercet = codeStr1Arr[1];
                    String issuer = codeStr1Arr[2];
                    codeMap.put(sercet, new CodeBean(user, sercet, issuer));
                }
            } else {//只存一个
                String[] codeStrArr = codeStr.split(splitChars1);
                String user = codeStrArr[0];
                String sercet = codeStrArr[1];
                String issuer = codeStrArr[2];
                codeMap.put(sercet, new CodeBean(user, sercet, issuer));
            }
        }
        if (codeMap.size() > 0)
            dynamicSetData(codeMap);
    }

    /**
     * 动态布局，显示数据，位置固定不变。
     * 下面两个情况执行dynamicSetData方法：
     * 1、启动APP首次进来，全部显示。
     * 2、新增秘钥时，仅处理新增item。
     *
     * @param codeMa
     */
    private void dynamicSetData(final Map<String, CodeBean> codeMa) {
        for (final CodeBean bean : codeMa.values()) {
            final View convertView = LayoutInflater.from(context).inflate(R.layout.item_code, null);
            LinearLayout ll_click = convertView.findViewById(R.id.ll_click);
            TextView btn_del = convertView.findViewById(R.id.btn_del);
            final RingProgressBar countdown = convertView.findViewById(R.id.countdown);

            final TextView code = convertView.findViewById(R.id.code);
            TextView issuer = convertView.findViewById(R.id.issuer);
            final TextView user = convertView.findViewById(R.id.user);

            issuer.setText(bean.getIssuer());
            user.setText(bean.getUser());
            String codeStr = getAuthCodeTest(bean.getSecret(), System.currentTimeMillis());
            Log.i(TAG, "codeStr= " + codeStr);
            if (codeStr == null || codeStr.length() == 0)
                continue;
            codeStr = codeStr.substring(0, 3) + " " + codeStr.substring(3, codeStr.length());
            code.setText(codeStr);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 20, 0, 0);//4个参数按顺序分别是左上右下

            convertView.setLayoutParams(layoutParams);//设置布局参数
            ll_item.addView(convertView);

            final CountDownTimer timer = new CountDownTimer(30 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    countdown.setProgress((int) millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    //倒计时结束后，直接更新code值

                    String codeStr = getAuthCodeTest(bean.getSecret(), System.currentTimeMillis());
                    codeStr = codeStr.substring(0, 3) + " " + codeStr.substring(3, codeStr.length());
                    code.setText(codeStr);

                    this.start();//特别注意，CountDownTimer倒计时结束后，onFinish内重启只能用this.start()。
                }
            }.start();
            ll_click.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //弹出修改user值
                    setModifyDialog(user, bean);
                    return false;
                }
            });
            btn_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("删除提示");
                    builder.setMessage("删除后秘钥信息将不存在。请提前保存秘钥值或秘钥二维码！");
                    final AlertDialog alertDialog = builder.create();
                    builder.setPositiveButton("继续删除", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                            //更新数据源
                            codeMap.remove(bean.getSecret());
                            //保存在本地
                            saveSp();
                            ll_item.removeView(convertView);
                            Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //                                alertDialog.dismiss();
                        }
                    });
                    builder.show();
                }
            });
        }
    }

    private void setAddDialog() {
        final Dialog mCameraDialog = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.pop_dialog_meun, null);
        //初始化视图
        mCameraDialog.setContentView(root);
        Window dialogWindow = mCameraDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        //        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
        mCameraDialog.show();

        root.findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraDialog.dismiss();
                startQrCode();
            }
        });
        root.findViewById(R.id.btn_keyboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, KeyBoardActivity.class);
                startActivityForResult(intent, Constant.REQ_QR_CODE);
            }
        });
    }

    /**
     * 修改别名
     */
    private void setModifyDialog(final TextView userTv, final CodeBean bean) {
        final Dialog modifyDialog = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.pop_dialog_modify, null);
        //初始化视图
        modifyDialog.setContentView(root);
        Window dialogWindow = modifyDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
        modifyDialog.setCanceledOnTouchOutside(false);
        modifyDialog.show();
        final EditText edit_name_modify = root.findViewById(R.id.edit_name_modify);
        edit_name_modify.setText(bean.getUser());

        root.findViewById(R.id.btn_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyDialog.dismiss();
            }
        });
        root.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = edit_name_modify.getText().toString();
                if (user.length() == 0) {
                    Toast.makeText(context, "请填写", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (user.equals(bean.getUser())) {
                    Toast.makeText(context, "请修改", Toast.LENGTH_SHORT).show();
                } else {
                    codeMap.remove(bean.getSecret());
                    CodeBean newCodeBean = new CodeBean(user, bean.getSecret(), bean.getIssuer());
                    codeMap.put(bean.getSecret(), newCodeBean);
                    saveSp();
                    modifyDialog.dismiss();
                    userTv.setText(user);
                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 根据秘钥生成此刻的验证码
     */
    public static String getAuthCodeTest(String savedSecret, long timeMsec) {
        String codeStr = "";
        try {
            codeStr = GoogleAuthenticator.getAuthCode(savedSecret, timeMsec) + "";
            if (codeStr.length() == 5) {
                codeStr = "0" + codeStr;
            } else if (codeStr.length() == 4) {
                codeStr = "00" + codeStr;
            } else if (codeStr.length() == 3) {
                codeStr = "000" + codeStr;
            } else if (codeStr.length() == 2) {
                codeStr = "0000" + codeStr;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } finally {
            return codeStr;
        }
    }

    /**
     * 开始扫码
     */
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        //        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        //        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //            // 申请权限
        //            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
        //            return;
        //        }
        // 二维码扫码
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    /**
     * 扫描结果回调。
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来，otpauth://totp/13126687200?secret=LOXKOKZZ6AXNQCEQ&issuer=HUOBI
            //            scanResult = "otpauth://totp/admin?secret=LOXKOKZZ6AXNQCEA&issuer=%E8%96%AA%E9%85%AC%E7%B3%BB%E7%BB%9F";
            Log.i(TAG, "scanResult= " + scanResult);
            if (scanResult.length() > 0 && scanResult.contains("secret=")) {//要将其加到list里，且重复的不能添加。之后要更新适配器。还得将最新的list保存进本地
                String[] aArr = scanResult.replace("//", "").split("\\?");
                String user = aArr[0].split("/")[1];
                if (user != null && user.length() > 0)
                    user = UrlDecodeUtils.toURLDecoder(user);
                String[] scanResultArr = aArr[1].split("&");
                String secret = scanResultArr[0].split("=")[1];
                String issuer = scanResultArr[1].split("=")[1];
                if (issuer != null && issuer.length() > 0)
                    issuer = UrlDecodeUtils.toURLDecoder(issuer);

                //更新数据源，并只将最新的一条item添加进父布局。
                if (codeMap.size() > 0 && codeMap.containsKey(secret)) {
                    Toast.makeText(context, "秘钥已存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                codeMap.put(secret, new CodeBean(user, secret, issuer));
                saveSp();
                Map<String, CodeBean> codeMapUpdate = new HashMap<>();
                codeMapUpdate.put(secret, new CodeBean(user, secret, issuer));
                dynamicSetData(codeMapUpdate);

                //                if (codeList.size() == 0) {
                //                    List<CodeBean> codeListAdd = new ArrayList<>();
                //                    codeListAdd.add(new CodeBean(user, secret, issuer));
                //                    dynamicSetData(codeListAdd);
                //                    codeList.add(new CodeBean(user, secret, issuer));
                //
                //                    //保存在本地
                //                    saveSp();
                //                } else {
                //                    boolean isExistSecret = false;
                //                    for (CodeBean bean : codeList) {
                //                        if (secret.equals(bean.getSecret())) {
                //                            isExistSecret = true;
                //                            break;
                //                        }
                //                    }
                //                    if (isExistSecret == false) {
                //                        List<CodeBean> codeListAdd = new ArrayList<>();
                //                        codeListAdd.add(new CodeBean(user, secret, issuer));
                //                        dynamicSetData(codeListAdd);
                //                        codeList.add(new CodeBean(user, secret, issuer));
                //                        //保存在本地
                //                        saveSp();
                //                    } else {
                //                        Log.i(TAG, secret + " 该秘钥值已存在，不用重复添加。");
                //                        Toast.makeText(context, "已存在，不用重复添加", Toast.LENGTH_SHORT).show();
                //                    }
                //                }
            } else {
                Toast.makeText(context, "扫描结果错误！请重新扫描", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(MainActivity.this, "请至设置打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            //            case Constant.REQ_PERM_EXTERNAL_STORAGE:
            //                // 文件读写权限申请
            //                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //                    // 获得授权
            //                    startQrCode();
            //                } else {
            //                    // 被禁止授权
            //                    Toast.makeText(QrCodeActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
            //                }
            //                break;
        }
    }

    /**
     * 将数据保存在SharedPreferences。不能存重复的
     */
    private void saveSp() {
        String codeStr = "";
        for (CodeBean bean : codeMap.values()) {
            if (codeStr.length() == 0) {
                codeStr = bean.getUser() + splitChars1 + bean.getSecret() + splitChars1 + bean.getIssuer();
            } else {
                codeStr = codeStr + splitChars2 + bean.getUser() + splitChars1 + bean.getSecret() + splitChars1 + bean.getIssuer();
            }
        }
        Log.i(TAG, "存储最新codeStr= " + codeStr);
        SharedPreferences sp = getSharedPreferences("codeList", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.putString("codeStr", codeStr);
        editor.commit();
    }

}
