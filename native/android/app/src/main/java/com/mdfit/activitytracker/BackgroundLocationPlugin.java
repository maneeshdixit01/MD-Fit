package com.mdfit.activitytracker;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import org.json.JSONArray;

@CapacitorPlugin(name="BackgroundLocation", permissions={
 @Permission(alias="location", strings={Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}),
 @Permission(alias="notifications", strings={"android.permission.POST_NOTIFICATIONS"})
})
public class BackgroundLocationPlugin extends Plugin {
 @PluginMethod public void startTracking(PluginCall call) {
  boolean notificationMissing = android.os.Build.VERSION.SDK_INT >= 33 &&
   ContextCompat.checkSelfPermission(getContext(), "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED;
  if (!hasLocationPermission() || notificationMissing) {
   String[] aliases = hasLocationPermission() ? new String[]{"notifications"} : new String[]{"location","notifications"};
   requestPermissionForAliases(aliases, call, "permissionsCallback");
   return;
  }
  launchService(call);
 }
 @PermissionCallback private void permissionsCallback(PluginCall call) {
  if (!hasLocationPermission()) { call.reject("Location permission is required. Allow location access."); return; }
  launchService(call);
 }
 private boolean hasLocationPermission() {
  return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED ||
   ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
 }
 private void launchService(PluginCall call) {
  Intent i=new Intent(getContext(),ActivityLocationService.class);
  i.setAction(ActivityLocationService.ACTION_START);
  i.putExtra(ActivityLocationService.EXTRA_ACTIVITY_TYPE,call.getString("activityType","Run"));
  i.putExtra(ActivityLocationService.EXTRA_RESUME,call.getBoolean("resume",false));
  try {
   if(android.os.Build.VERSION.SDK_INT>=26)getContext().startForegroundService(i); else getContext().startService(i);
   JSObject r=new JSObject();r.put("started",true);call.resolve(r);
  } catch(Exception e){call.reject("Unable to start GPS foreground service: "+e.getMessage());}
 }
 @PluginMethod public void pauseTracking(PluginCall call){sendAction(ActivityLocationService.ACTION_PAUSE);call.resolve();}
 @PluginMethod public void stopTracking(PluginCall call){sendAction(ActivityLocationService.ACTION_STOP);call.resolve();}
 private void sendAction(String action){Intent i=new Intent(getContext(),ActivityLocationService.class);i.setAction(action);try{getContext().startService(i);}catch(Exception ignored){}}
 @PluginMethod public void getTrack(PluginCall call){
  android.content.SharedPreferences p=getContext().getSharedPreferences(ActivityLocationService.PREFS,0);
  JSObject r=new JSObject();try{r.put("points",new JSONArray(p.getString(ActivityLocationService.KEY_POINTS,"[]")));}catch(Exception e){r.put("points",new JSONArray());}
  boolean active=p.getBoolean(ActivityLocationService.KEY_ACTIVE,false);
  long elapsed=p.getLong(ActivityLocationService.KEY_ELAPSED_BEFORE,0L);
  if(active){long started=p.getLong(ActivityLocationService.KEY_START_AT,0L);if(started>0L)elapsed+=Math.max(0L,System.currentTimeMillis()-started);}
  r.put("active",active);r.put("session",p.getBoolean(ActivityLocationService.KEY_SESSION,false));
  r.put("elapsedSeconds",elapsed/1000L);r.put("activityType",p.getString(ActivityLocationService.KEY_TYPE,"Run"));call.resolve(r);
 }
}
