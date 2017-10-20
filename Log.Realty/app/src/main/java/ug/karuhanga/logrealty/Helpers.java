package ug.karuhanga.logrealty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.orm.query.Condition;
import com.orm.query.Select;
import com.orm.util.NamingHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ug.karuhanga.logrealty.Data.Notification;
import ug.karuhanga.logrealty.Data.Tenant;
import ug.karuhanga.logrealty.Services.ScheduledNotification;

/**
 * Created by karuhanga on 8/25/17.
 */

public class Helpers {

    public static final int FRAGMENT_DUE_PAYMENTS= 1001;
    public static final int FRAGMENT_LOCATIONS= 1002;
    public static final int FRAGMENT_HOUSES= 1003;
    public static final int FRAGMENT_TENANTS= 1004;
    public static final int FRAGMENT_PAYMENTS= 1005;
    public static final int FRAGMENT_NONE= -1000;

    public static final int RESULT_CODE_ADD_PAYMENT= 100;
    public static final int RESULT_CODE_SETTINGS= 101;
    public static final int RESULT_CODE_ADD_LOCATION= 102;
    public static final int RESULT_CODE_ADD_HOUSE= 103;
    public static final int RESULT_CODE_ADD_TENANT= 104;

    public static final int RESULT_CODE_REFRESH= 105;

    public static final int ACTION_SHOW= 200;
    public static final int ACTION_ADD= 201;
    public static final int ACTION_DELETE= 202;

    public static final int REQUEST_CODE_DETAILS= 300;
    public static final int REQUEST_CODE_DELETE= 301;
    public static final int REQUEST_CODE_REPLACE= 302;
    public static final int REQUEST_CODE_EDIT= 303;
    public static final int REQUEST_CODE_NOTIFY= 304;

    public static final int FLAG_NO_FLAGS= 0;
    public static final int FLAG_ONE_DAY_TO= -1;
    public static final int FLAG_ONE_DAY_AFTER= 1;
    public static final int FLAG_EVERY_FIVE_DAYS= 400;

    public static final int AMOUNT_MINIMUM_RENT= 250000;

    public static final String REGEX_EMAIL= "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public static Date dueUpdater(Date oldDue, int amount, int rate){
        int months= amount/rate;
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");
        String date[]= formatter.format(oldDue).split("/");

        int month= Integer.valueOf(date[1]);
        month+= months;
        date[1]= String.valueOf(month);

        Date newDue;

        try {
            newDue= formatter.parse(String.format("%s/%s/%s", date[0], date[1], date[2]));
        } catch (ParseException e) {
            return oldDue;
        }
        return newDue;
    }

    public static Date getLaterDate(Date oldDate, int days){
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");
        String date[]= formatter.format(oldDate).split("/");

        int day= Integer.valueOf(date[0]);
        day+= days;
        date[0]= String.valueOf(day);

        Date newDate;

        try {
            newDate= formatter.parse(String.format("%s/%s/%s", date[0], date[1], date[2]));
        } catch (ParseException e) {
            return oldDate;
        }
        return newDate;
    }

    public static Date makeDate(int day, int month, int year){
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");
        Date result= null;
        try {
            result= formatter.parse(String.format("%d/%d/%d", day, month+1, year));
        } catch (ParseException e) {
            result= null;
        }finally {
            return result;
        }
    }

    public static ArrayList<Integer> breakDate(Date date){
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");
        ArrayList<Integer> result= new ArrayList<>();

        String[] temp= formatter.format(date).split("/");
        for (String item : temp) {
            result.add(Integer.parseInt(item));
        }
        result.set(1, result.get(1)-1);

        return result;
    }

    public static String cleaner(String text){
        if (text==null || text.length()<1){
            return null;
        }
        text= text.substring(0, 1).toUpperCase()+Helpers.toFirstsCapital(text);
        return text;
    }

    @NonNull
    public static String toFirstsCapital(String old){
        String result= old.toLowerCase();
        String[] words= result.split(" ");
        result= "";
        for (String word : words) {
            String first= word.split("")[0].toUpperCase();
            word= word.replaceFirst(word.substring(0, 1), first);
            result+= (word+" ");
        }
        return result.trim();
    }

    public static String getStringByName(Context context,String name){
        Resources res= context.getResources();
        int id= res.getIdentifier(name, "string", context.getPackageName());
        String result;
        try {
            result= res.getString(id);
        }catch (Resources.NotFoundException e){
            result= null;
        }
        return result;
    }

    public static String dateToString(Date date){
        if (date==null){
            return "";
        }
        String[] helper= date.toString().split(" ");
        return helper[0]+", "+helper[2]+" "+helper[1]+" "+helper[5];
    }

    public static String toCurrency(int number){
        String result= String.valueOf(number);
        String[] helper= result.split("");
        return helper[1]+helper[2]+helper[3]+","+helper[4]+helper[5]+helper[6]+"/=";
    }

    public static boolean schedulePaymentNotification(Context context, Tenant tenant, boolean newPayment){
        if (newPayment){
            List<Notification> results= Select.from(Notification.class).where(Condition.prop(NamingHelper.toSQLNameDefault("tenant")).eq(tenant)).list();
            if (results.size()>0){
                results.get(0).delete();
            }
        }
        AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent notifIntent= new Intent(context, ScheduledNotification.class);
        int flag= FLAG_ONE_DAY_TO;

        PendingIntent alarmIntent= PendingIntent.getBroadcast(context, REQUEST_CODE_NOTIFY, notifIntent, FLAG_NO_FLAGS);

        Calendar calendar= Calendar.getInstance();
        List<Integer> date= breakDate(tenant.getRentDue());

        List<Notification> notifs= Select.from(Notification.class).where(Condition.prop(NamingHelper.toSQLNameDefault("tenant")).eq(tenant)).list();

        if (notifs.size()<1){
            flag= FLAG_ONE_DAY_TO;
            notifIntent.putExtra("message", tenant.getHouse().getLocation().getName()+ "\n"+ tenant.getName()+"'s next rent installment is due tomorrow");
            new Notification(tenant).save();
        }
        else if(notifs.get(0).getStatusFlag()==FLAG_ONE_DAY_TO){
            flag= FLAG_ONE_DAY_AFTER;
            notifs.get(0).setStatusFlag(FLAG_ONE_DAY_AFTER);
            notifIntent.putExtra("message", tenant.getHouse().getLocation().getName()+ "\n"+ tenant.getName()+"'s rent installment was due yesterday");
            notifs.get(0).save();
        }
        else{
            flag= notifs.get(0).getStatusFlag()+5;
            notifs.get(0).setStatusFlag(flag);
            notifIntent.putExtra("message", tenant.getHouse().getLocation().getName()+ "\n"+ tenant.getName()+"'s rent installment was due on "+dateToString(tenant.getRentDue()));
            notifs.get(0).save();
        }

        notifIntent.putExtra("date", breakDate(tenant.getRentDue()));
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(date.get(2), date.get(1), date.get(0)+flag);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        return true;
    }

}
