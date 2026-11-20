package com.mdfit.activitytracker;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.*;
import android.os.*;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONObject;

public class ActivityLocationService extends Service implements LocationListener {
 public static final String ACTION_START="mdfit.action.START", ACTION_PAUSE="mdfit.action.PAUSE", ACTION_STOP="mdfit.action.STOP";
 public static final String EXTRA_ACTIVITY_TYPE="activityType", EXTRA_RESUME="resume";
 public static final String PREFS="md_fit_background_location", KEY_POINTS="points", KEY_ACTIVE="active", KEY_TYPE="activityType", KEY_SESSION="session", KEY_START_AT="startAt", KEY_ELAPSED_BEFORE="elapsedBefore";
 private static final String CHANNEL="md_fit_tracking"; private static final int NOTIFICATION_ID=4817;
 private LocationManager manager; private boolean listening=false;

 @Override public void onCreate(){super.onCreate();manager=(LocationManager)getSystemService(LOCATION_SERVICE);createChannel();}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  String action=intent==null?ACTION_START:intent.getAction();
  if(ACTION_PAUSE.equals(action)||ACTION_STOP.equals(action)){stopTracking(ACTION_STOP.equals(action));return START_NOT_STICKY;}
  if(intent!=null){boolean resume=intent.getBooleanExtra(EXTRA_RESUME,false);
   if(!resume)prefs().edit().putString(KEY_POINTS,"[]").putLong(KEY_ELAPSED_BEFORE,0L).putLong(KEY_START_AT,System.currentTimeMillis()).putBoolean(KEY_SESSION,true).apply();
   else if(prefs().getLong(KEY_START_AT,0L)==0L)prefs().edit().putLong(KEY_START_AT,System.currentTimeMillis()).apply();
   String type=intent.getStringExtra(EXTRA_ACTIVITY_TYPE);if(type!=null)prefs().edit().putString(KEY_TYPE,type).apply();}
  if(!hasPermission()){stopSelf();return START_NOT_STICKY;}
  prefs().edit().putBoolean(KEY_ACTIVE,true).apply();promoteToForeground();beginLocation();return START_STICKY;
 }
 private android.content.SharedPreferences prefs(){return getSharedPreferences(PREFS,MODE_PRIVATE);}
 private boolean hasPermission(){return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||
  ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
 private void promoteToForeground(){
  String type=prefs().getString(KEY_TYPE,"Run");String label="Run".equals(type)?"Running":("Ride".equals(type)?"Cycling":"Walking");
  Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
  b.setContentTitle("MD Fit · "+label).setContentText("Your activity route is being recorded")
   .setSmallIcon(android.R.drawable.ic_menu_mylocation).setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_SERVICE);
  Intent launch=getPackageManager().getLaunchIntentForPackage(getPackageName());
  if(launch!=null){int f=PendingIntent.FLAG_UPDATE_CURRENT;if(Build.VERSION.SDK_INT>=23)f|=PendingIntent.FLAG_IMMUTABLE;b.setContentIntent(PendingIntent.getActivity(this,0,launch,f));}
  Notification n=b.build();
  if(Build.VERSION.SDK_INT>=29)startForeground(NOTIFICATION_ID,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);else startForeground(NOTIFICATION_ID,n);
 }
 private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"MD Fit activity tracking",NotificationManager.IMPORTANCE_LOW);c.setDescription("Shown while MD Fit records your GPS route");((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
 private void beginLocation(){
  if(listening||manager==null||!hasPermission())return;
  try{
   String provider=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED?LocationManager.GPS_PROVIDER:LocationManager.NETWORK_PROVIDER;
   if(!manager.isProviderEnabled(provider)){if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))provider=LocationManager.NETWORK_PROVIDER;else return;}
   manager.requestLocationUpdates(provider,1000L,0f,this,Looper.getMainLooper());listening=true;
  }catch(SecurityException|IllegalArgumentException ignored){listening=false;}
 }
 @Override public void onLocationChanged(Location loc){
  if(loc==null||(loc.hasAccuracy()&&loc.getAccuracy()>80f))return;
  try{JSONArray arr=new JSONArray(prefs().getString(KEY_POINTS,"[]"));
   if(arr.length()>0){JSONObject prev=arr.getJSONObject(arr.length()-1);float[] d=new float[1];Location.distanceBetween(prev.getDouble("lat"),prev.getDouble("lng"),loc.getLatitude(),loc.getLongitude(),d);if(d[0]<2f||d[0]>250f)return;}
   JSONObject p=new JSONObject();p.put("lat",loc.getLatitude());p.put("lng",loc.getLongitude());p.put("accuracy",loc.hasAccuracy()?loc.getAccuracy():-1);p.put("time",loc.getTime());arr.put(p);
   prefs().edit().putString(KEY_POINTS,arr.toString()).apply();
  }catch(Exception ignored){}
 }
 private void stopTracking(boolean finalStop){
  android.content.SharedPreferences p=prefs();
  android.content.SharedPreferences.Editor edit=p.edit().putBoolean(KEY_ACTIVE,false);
  if(finalStop)edit.putBoolean(KEY_SESSION,false);
  else if(p.getLong(KEY_START_AT,0L)>0L){long before=p.getLong(KEY_ELAPSED_BEFORE,0L);edit.putLong(KEY_ELAPSED_BEFORE,before+Math.max(0L,System.currentTimeMillis()-p.getLong(KEY_START_AT,System.currentTimeMillis()))).putLong(KEY_START_AT,0L);}
  edit.apply();
  if(manager!=null&&listening){try{manager.removeUpdates(this);}catch(SecurityException ignored){}}
  listening=false;if(Build.VERSION.SDK_INT>=24)stopForeground(STOP_FOREGROUND_REMOVE);else stopForeground(true);stopSelf();
 }
 @Override public void onDestroy(){if(manager!=null&&listening){try{manager.removeUpdates(this);}catch(SecurityException ignored){}}super.onDestroy();}
 @Override public void onProviderDisabled(String p){} @Override public void onProviderEnabled(String p){}
 @Override public void onStatusChanged(String p,int s,Bundle b){}
 @Override public IBinder onBind(Intent i){return null;}
}
