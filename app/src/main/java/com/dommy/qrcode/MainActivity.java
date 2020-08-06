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
import android.widget.TextView;
import android.widget.Toast;

import com.dommy.qrcode.activity.KeyBoardActivity;
import com.dommy.qrcode.base.BaseActivity;
import com.dommy.qrcode.bean.CodeBean;
import com.dommy.qrcode.googleAuth.GoogleAuthenticator;
import com.dommy.qrcode.util.Constant;
import com.dommy.qrcode.view.RingProgressBar;
import com.google.zxing.activity.CaptureActivity;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 布局实现原理：1不采用list item添加定时器的方式。
 * 2采用动态添加布局的方式。
 * 3数据源的处理，第一次启动APP如果本地有保存数据则读出并做第一次全量显示。
 * 新增时，更新数据源，并只将最新的一条item添加进父布局。
 * 倒计时结束时，要重新起定时器，这里的处理是不更新数据源，但要将这个ietm布局移除，然后再单独添加进入父布局。
 * 删除时，更新数据源，并将这个ietm布局移除，然后再单独添加进入父布局。
 * 修改时，
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private TextView btn_setting, btn_add;
    private List<CodeBean> codeList = new ArrayList<>();
    //    private Set<String> sercetSet = new HashSet<>() ;
    private Context context;
    private Handler handler;
    private LinearLayout ll_item;//item父布局

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
        //        codeList.add(new CodeBean("LOXKOKAZZ6AXNQCEQ", "测试1"));
        //        codeList.add(new CodeBean("52MM34W6OFWETSKF", "测试2"));
        //        codeList.add(new CodeBean("O2QSKW27S4GBTWBM", "测试3"));
        //        codeList.add(new CodeBean("3WRGITAMFPASJMDA", "测试4"));
        //
        SharedPreferences sp = getSharedPreferences("codeList", MODE_PRIVATE);
        String codeStr = sp.getString("codeStr", "");
        Log.i(TAG, "本地存储codeStr= " + codeStr);
        if (codeStr.length() > 0 && codeStr.contains(":")) {
            if (codeStr.contains("-")) {//存多个
                String[] codeStrArr = codeStr.split("-");
                for (String codeStr1 : codeStrArr) {
                    String user = codeStr1.split(":")[0];
                    String sercet = codeStr1.split(":")[1];
                    String issuer = codeStr1.split(":")[2];
                    codeList.add(new CodeBean(user, sercet, issuer));
                    //                    sercetSet.add(sercet);
                }
            } else {//只存一个
                String user = codeStr.split(":")[0];
                String sercet = codeStr.split(":")[1];
                String issuer = codeStr.split(":")[2];
                codeList.add(new CodeBean(user, sercet, issuer));
                //                sercetSet.add(sercet);
            }
        }
        if (codeList.size() > 0)
            dynamicSetData(codeList);
    }

    /**
     * 动态显示数据
     */
    private void dynamicSetData(final List<CodeBean> codeLi) {
        for (int i = 0; i < codeLi.size(); i++) {
            final CodeBean bean = codeLi.get(i);
            final View convertView = LayoutInflater.from(context).inflate(R.layout.item_code, null);
            LinearLayout ll_click = convertView.findViewById(R.id.ll_click);
            TextView code = convertView.findViewById(R.id.code);
            final TextView issuer = convertView.findViewById(R.id.issuer);
            TextView btn_del = convertView.findViewById(R.id.btn_del);
            final RingProgressBar countdown = convertView.findViewById(R.id.countdown);
            String codeStr = getAuthCodeTest(bean.getSecret(), System.currentTimeMillis());
            codeStr = codeStr.substring(0, 3) + " " + codeStr.substring(3, codeStr.length());
            code.setText(codeStr);
            if (bean.getIssuer().equals(" ")) {
                issuer.setText(bean.getUser());
            } else {
                issuer.setText(bean.getIssuer() + " (" + bean.getUser() + ")");
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 0, 0, 0);//4个参数按顺序分别是左上右下
            final View line1 = new View(context);
            layoutParams.height = 20;
            line1.setLayoutParams(layoutParams);
            line1.setBackgroundResource(R.color.light_gray1);
            ll_item.addView(line1);
            final CountDownTimer timer = new CountDownTimer(30 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    countdown.setProgress((int) millisUntilFinished / 1000);
                    //                    countdown.setText("" + millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    //倒计时结束后，不更新数据源，但要将这个ietm布局移除，然后再单独添加进入父布局。
                    CodeBean newCodeBean = null;
                    for (CodeBean codeBean : codeList) {
                        if (codeBean.getSecret().equals(bean.getSecret())) {
                            newCodeBean = new CodeBean(codeBean.getUser(), codeBean.getSecret(), codeBean.getIssuer());
                        }
                    }
                    ll_item.removeView(convertView);
                    ll_item.removeView(line1);
                    List<CodeBean> codeListUpdate = new ArrayList<>();
                    codeListUpdate.add(newCodeBean);
                    dynamicSetData(codeListUpdate);
                }
            }.start();

            ll_item.addView(convertView);
            ll_click.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //弹出修改user值
                    //                    issuer.setText("888888");
                    setModifyDialog(convertView, line1, timer, bean);
                    return false;
                }
            });
            btn_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timer.cancel();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("删除提示");
                    builder.setMessage("删除后秘钥信息将不存在。请提前保存秘钥值或秘钥二维码！");
                    final AlertDialog alertDialog = builder.create();
                    builder.setPositiveButton("继续删除", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                            //更新数据源，并将这个ietm布局移除，然后再单独添加进入父布局。
                            codeList.remove(bean);
                            //保存在本地
                            saveSp();
                            ll_item.removeView(convertView);
                            ll_item.removeView(line1);
                            Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                            ll_item.removeView(convertView);
                            ll_item.removeView(line1);
                            List<CodeBean> codeListUpdate = new ArrayList<>();
                            codeListUpdate.add(bean);
                            dynamicSetData(codeListUpdate);
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
    private void setModifyDialog(final View convertView, final View line1, final CountDownTimer timer, final CodeBean bean) {
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

        /**
         * 修改时倒计时取消
         */
        timer.cancel();

        root.findViewById(R.id.btn_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyDialog.dismiss();
                ll_item.removeView(convertView);
                ll_item.removeView(line1);
                List<CodeBean> codeListUpdate = new ArrayList<>();
                codeListUpdate.add(bean);
                dynamicSetData(codeListUpdate);
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
                    modifyDialog.dismiss();
                    ll_item.removeView(convertView);
                    ll_item.removeView(line1);
                    List<CodeBean> codeListUpdate = new ArrayList<>();
                    codeListUpdate.add(bean);
                    dynamicSetData(codeListUpdate);
                } else {
                    codeList.remove(bean);
                    saveSp();
                    CodeBean newCodeBean = new CodeBean(user, bean.getSecret(), bean.getIssuer());
                    codeList.add(newCodeBean);
                    saveSp();

                    modifyDialog.dismiss();
                    ll_item.removeView(convertView);
                    ll_item.removeView(line1);
                    List<CodeBean> codeListUpdate = new ArrayList<>();
                    codeListUpdate.add(newCodeBean);
                    dynamicSetData(codeListUpdate);
                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 根据秘钥生成此刻的验证码
     */
    public String getAuthCodeTest(String savedSecret, long timeMsec) {
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
            Log.i(TAG, "scanResult= " + scanResult);
            if (scanResult.length() > 0 && scanResult.contains("secret=")) {//要将其加到list里，且重复的不能添加。之后要更新适配器。还得将最新的list保存进本地
                String[] aArr = scanResult.replace("//", "").split("\\?");
                String user = aArr[0].split("/")[1];
                //                Log.i(TAG, "user= " + user);
                String[] scanResultArr = aArr[1].split("&");
                String secret = scanResultArr[0].split("=")[1];
                String issuer = scanResultArr[1].split("=")[1];
                //                Log.i(TAG, "secret= " + secret + "  issuer= " + issuer);
                //更新数据源，并只将最新的一条item添加进父布局。
                if (codeList.size() == 0) {
                    List<CodeBean> codeListAdd = new ArrayList<>();
                    codeListAdd.add(new CodeBean(user, secret, issuer));
                    dynamicSetData(codeListAdd);
                    codeList.add(new CodeBean(user, secret, issuer));

                    //保存在本地
                    saveSp();
                } else {
                    boolean isExistSecret = false;
                    for (CodeBean bean : codeList) {
                        if (secret.equals(bean.getSecret())) {
                            isExistSecret = true;
                            break;
                        }
                    }
                    if (isExistSecret == false) {
                        List<CodeBean> codeListAdd = new ArrayList<>();
                        codeListAdd.add(new CodeBean(user, secret, issuer));
                        dynamicSetData(codeListAdd);
                        codeList.add(new CodeBean(user, secret, issuer));
                        //保存在本地
                        saveSp();
                    } else {
                        Log.i(TAG, secret + " 该秘钥值已存在，不用重复添加。");
                        Toast.makeText(context, "已存在，不用重复添加", Toast.LENGTH_SHORT).show();
                    }
                }
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
     * 将数据保存在SharedPreferences
     */
    private void saveSp() {
        String codeStr = "";
        for (CodeBean bean : codeList) {
            if (codeStr.length() == 0) {
                codeStr = bean.getUser() + ":" + bean.getSecret() + ":" + bean.getIssuer();
            } else {
                codeStr = codeStr + "-" + bean.getUser() + ":" + bean.getSecret() + ":" + bean.getIssuer();
            }
        }
        Log.i(TAG, "codeStr= " + codeStr);
        SharedPreferences sp = getSharedPreferences("codeList", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.putString("codeStr", codeStr);
        editor.commit();
    }

}
