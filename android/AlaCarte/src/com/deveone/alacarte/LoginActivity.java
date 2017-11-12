package com.deveone.alacarte;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private final boolean noLogear = false; 
	//private String PrefDomain = "domain";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.ThreadPolicy policy = new StrictMode.
		ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		getDomain();		
		
		if (noLogear){
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		findViewById(R.id.btnLogin).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String user = ((EditText)findViewById(R.id.edtUser)).getText().toString();
				String pass = ((EditText)findViewById(R.id.edtPass)).getText().toString();
				
				MyUtils myu = new MyUtils();

				String cant = myu.getStringFromUrl(MyUtils.urlServidor + "index.php?r=site/loginAndroid&username=" + user + "&password=" + pass);
				if (cant.equals("1")){
					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				} else
					Toast.makeText(LoginActivity.this, R.string.s_error_autenticarse , Toast.LENGTH_SHORT).show();
			}
		}) ;
		
		((EditText)findViewById(R.id.edtDomain)).setOnKeyListener(new OnKeyListener() {			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction()==KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER){
					SharedPreferences settings = getPreferences(0);
				    SharedPreferences.Editor editor = settings.edit();
				    EditText edt = ((EditText)v);
				    editor.putString("domain", edt.getText().toString());
  			        editor.commit();
  			        getDomain();
  			        edt.setVisibility(View.GONE);
				}
				return false;
			}
		});
		((Button)findViewById(R.id.btnMostrarPref)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edt = (EditText)findViewById(R.id.edtDomain);
				if (edt.getVisibility()==View.GONE)
					edt.setVisibility(View.VISIBLE);
				else
					edt.setVisibility(View.GONE);
			}
		});
	}

	private void getDomain() {
		SharedPreferences settings = getPreferences(0);
		MyUtils.domain = settings.getString("domain", "");
        if (! MyUtils.domain.isEmpty()){				
			setCookieDomain();
			((EditText) findViewById(R.id.edtDomain)).setText(MyUtils.domain);
        }
	}

	private void setCookieDomain() {
		MyUtils.urlServidor = "http://" + MyUtils.domain + "/yii/cart/";
		CookieManager cookieManager = new CookieManager();
		CookieManager.setDefault(cookieManager);
		HttpCookie cookie = new HttpCookie("lang", "en");
		cookie.setDomain(MyUtils.domain);
		cookie.setPath("/yii/cart/");
		cookie.setVersion(0);
		try {
			cookieManager.getCookieStore().add(new URI(MyUtils.urlServidor), cookie);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
}
