package com.dommy.qrcode.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dommy.qrcode.R;
import com.dommy.qrcode.base.BaseActivity;
import com.dommy.qrcode.util.Constant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yhl
 * on 2020/8/5
 * 键入秘钥的方式
 */
public class KeyBoardActivity extends BaseActivity {

    //    private static final String TAG = "KeyBoardActivity";
    private TextView btn_back, btn_add;
    private EditText edit_name, edit_sercet;
    private Context context;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);
        context = this;
        handler = new Handler();
        initView();
    }

    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        btn_add = findViewById(R.id.btn_add);
        edit_name = findViewById(R.id.edit_name);
        edit_sercet = findViewById(R.id.edit_sercet);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        edit_sercet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    for (int i = 0; i < s.length(); i++) {
                        char c = s.charAt(i);
                        if (c >= 0x4e00 && c <= 0X9fff) {
                            // 根据字节码判断
                            // 如果是中文，则清除输入的字符，否则保留
                            s.delete(i, i + 1);
                            Toast.makeText(context, "秘钥不能是汉字，只能是字母和数字的组合", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = "otpauth://totp/";//otpauth://totp/?secret=LOXKOKZZ6AXNQCEQ&issuer=HUOBI
                String user = edit_name.getText().toString();
                String sercet = edit_sercet.getText().toString();
                if (user.length() == 0 || sercet.length() == 0) {
                    Toast.makeText(context, "请填写完整", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isNumeric(user)) {
                    Toast.makeText(context, "别名不能全是数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isNumeric(sercet)) {
                    Toast.makeText(context, "秘钥不能全是数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isNumericChar(sercet) == false) {
                    Toast.makeText(context, "秘钥只能是字母和数字的组合", Toast.LENGTH_SHORT).show();
                    return;
                }
                //                if (sercet.length() < 16) {
                //                    Toast.makeText(context, "秘钥长度不能低于16位", Toast.LENGTH_SHORT).show();
                //                    return;
                //                }
                //                long user = System.currentTimeMillis();
                result = result + user + "?secret=" + sercet.toUpperCase() + "&issuer= ";
                Intent resultIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.INTENT_EXTRA_KEY_QR_SCAN, result);
                resultIntent.putExtras(bundle);
                KeyBoardActivity.this.setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private boolean isNumericChar(String str) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


}
