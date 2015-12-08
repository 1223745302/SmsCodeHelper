package rikka.smscodehelper.receiver;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.content.ClipboardManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import rikka.smscodehelper.R;

/**
 * Created by Rikka on 2015/12/7.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Object[] pdus = (Object[]) intent.getExtras().get("pdus");

        for (Object p : pdus){
            byte[] sms = (byte[]) p;

            SmsMessage message = SmsMessage.createFromPdu(sms);

            String content = message.getMessageBody();

            String number = message.getOriginatingAddress();

            // 找到 [] 之间内容
            ArrayList<String> sender = new ArrayList<>();
            makeSenderList(sender, content, "[", "]");
            makeSenderList(sender, content, "【", "】");

            Comparator<String> stringComparator = new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    if (o1.length() > o2.length()) {
                        return 1;
                    } else if (o1.length() < o2.length()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };

            // 大概是最短的那个
            Collections.sort(sender, stringComparator);

            // 找到数字
            ArrayList<String> code = new ArrayList<>();

            // 大概应该在 "验证码" 什么的后面吧
            findNumber(code, content, findStart(content));

            // 大概也是最短的那个
            Collections.sort(sender, stringComparator);

            if (sender.size() > 0 && code.size() > 0)
               Toast.makeText(context, String.format(context.getString(R.string.toast_format), code.get(0)), Toast.LENGTH_LONG).show();

            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("SmsCode", code.get(0));
            clipboardManager.setPrimaryClip(clipData);
        }
    }

    public void makeSenderList(ArrayList<String>list, String content, String start, String end) {
        int length = content.length();
        int curpos = 0;
        while (curpos != -1) {

            curpos = content.indexOf(start, curpos);
            if (curpos != -1) {
                curpos ++;

                int endpos = content.indexOf(end, curpos);

                if (endpos != -1) {
                    list.add(content.substring(curpos, endpos));
                }
            }
        }
    }

    public int findStart(String content) {
        int codeFindStart;
        codeFindStart = content.indexOf("验证码");

        if (codeFindStart == -1)
            codeFindStart = content.indexOf("码");

        if (codeFindStart == -1)
            codeFindStart = content.indexOf(" code");

        if (codeFindStart == -1)
            codeFindStart = 0;

        return codeFindStart;
    }

    public void findNumber(ArrayList<String>list, String content, int start) {
        int curpos = start;
        int startpos = -1;
        int length = content.length();

        while (curpos < length) {
            char ch = content.charAt(curpos);

            if (startpos == -1 && Character.isDigit(ch)) {
                startpos = curpos;
            }

            if (startpos != -1 && !Character.isDigit(ch)) {
                list.add(content.substring(startpos, curpos));
                startpos = -1;
            }

            curpos ++;
        }

        if (startpos != -1) {
            list.add(content.substring(startpos, content.length()));
        }
    }
}
