package com.deveone.alacarte;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.codebutler.android_websockets.WebSocketClient;

public class MainActivity extends Activity {
	protected static final String TAG = "WS->";
	//public static final List<Integer> platosId = new ArrayList<Integer>();
	public static List<Integer> detallesId = new ArrayList<Integer>();
	//public static List<Integer> detallesEstado = new ArrayList<Integer>();
	public static List<Map<String,String>> detallesList = new ArrayList<Map<String,String>>();
	public static List<Map<String,String>> platosList = new ArrayList<Map<String,String>>();
	public static SparseArray<String> mesasEstados = new SparseArray<String>(); //Map<String> mesasEstados = new Map<String>();
	
	
	//public static List<String> contenidoNotif = new ArrayList<String>();
	//public static List<String> mesaNotif = new ArrayList<String>();
	public static StringBuilder mesaNotif = new StringBuilder();
	public static StringBuilder contenidoNotif = new StringBuilder();
	//private static boolean cambioDetalle = true;
	SimpleAdapter adapter;
	EditText edtSearch;
	EditText cantPlato, edtCantPersonas;
	//Timer timer;
    //Handler handler;
    private ImageView imageView;
    private Bitmap loadedImage;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	StrictMode.ThreadPolicy policy = new StrictMode.
		ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final List<BasicNameValuePair> extraHeaders = Arrays.asList(
			    new BasicNameValuePair("Cookie", "session=abcd")
		);
        
       if (MyUtils.client==null){        	
        	//Toast.makeText(getBaseContext()	, "ENTROOOOOOOOO", Toast.LENGTH_LONG).show();
	        MyUtils.client = new WebSocketClient(URI.create("ws://" + MyUtils.domain + ":12345/echo/"), new WebSocketClient.Listener() {
			    @Override
			    public void onConnect() {}
			    @Override
			    public void onMessage(final String message) {
			    	try {
			    		JSONObject jo = new JSONObject(message);
			    		String data = jo.getString("data");
			    		if (data.equals("change")){
				    		MyUtils myu = new MyUtils();
							myu.getEstadoPedidos();
				    		if (MyUtils.platosCocinados>0)
							  sendNotification();
							else
							  cleanNotification();
				    		//myu = null;
			    		} else {
			    			//es print
			    		}
						MainActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//fillListViewDetalles();
								if (platosCocinados>0)
								  sendNotification();
								else
								  cleanNotification();
							}
						});
						
					} catch (Exception e) {
						Toast.makeText(getBaseContext()	, "Ocurrió un error" + e.getMessage(), Toast.LENGTH_LONG).show();
					}
			    }
			    @Override
			    public void onMessage(byte[] data) {}
			    @Override
			    public void onDisconnect(int code, String reason) {}
			    @Override
			    public void onError(Exception error) {Log.w(" Error ", error.toString());}
			}, extraHeaders);
	        MyUtils.client.connect();
        }
        
        MyUtils u = new MyUtils();        
        u.fillPlatos();        
        final ListView lvwPlatos = (ListView)findViewById(R.id.lstPlatos);        
        findViewById(R.id.lltPlatos).setVisibility(View.GONE);
        
        adapter = new SimpleAdapter(this, platosList, R.layout.platos_item,  new String[] { "lblPlato", "lblPrecio" },
        		 new int[] { R.id.txvPlato, R.id.lblPrecio });
        lvwPlatos.setAdapter(adapter);
        lvwPlatos.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MyUtils.platoId =Integer.parseInt(((Map<String, String>) parent.getItemAtPosition(position)).get("id").toString());
				//SimpleAdapter adapter = (SimpleAdapter) parent.getAdapter();
				String  nombrePlatoL = ((TextView)view.findViewById(R.id.txvPlato)).getText().toString();
				//String precioL = ((TextView)view.findViewById(R.id.lblPrecio)).getText().toString();
				findViewById(R.id.lltAgregar).setVisibility(View.VISIBLE);
				findViewById(R.id.grdAction).setVisibility(View.VISIBLE);
				TextView nombrePlato = (TextView) findViewById(R.id.txvPlatoEdit);
				nombrePlato.setText(nombrePlatoL);
				
				EditText edtCant = (EditText) findViewById(R.id.edtCantPlato);
				edtCant.requestFocus();
				edtCant.setText("");
				
			}
        	
		});
        lvwPlatos.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {				
				imageView = (ImageView) findViewById(R.id.imgPhoto);
				MyUtils.platoId = Integer.parseInt(((Map<String, String>) parent.getItemAtPosition(position)).get("id").toString());
		        downloadFile(MyUtils.platoId);
		        MyUtils myu = new MyUtils();
		        ((TextView)findViewById(R.id.txvDescription)).setText(myu.getPlatoDescripcion());
		        findViewById(R.id.lltPlatos).setVisibility(View.GONE);
		        findViewById(R.id.lltIzqui).setVisibility(View.GONE);
		        findViewById(R.id.lltPhoto).setVisibility(View.VISIBLE);
				return false;
			}
		});
        
        findViewById(R.id.imgPhoto).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.lltPlatos).setVisibility(View.VISIBLE);
		        findViewById(R.id.lltIzqui).setVisibility(View.VISIBLE);
		        findViewById(R.id.lltPhoto).setVisibility(View.GONE);
		        ((ImageView)findViewById(R.id.imgPhoto)).setImageResource(R.drawable.ic_sinfoto);
			}
		});
        lvwPlatos.setClickable(true);
        findViewById(R.id.btnAgregar).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (findViewById(R.id.lltPlatos).getVisibility()==View.GONE){
					findViewById(R.id.lltPlatos).setVisibility(View.VISIBLE);
				}
				else{
					findViewById(R.id.lltPlatos).setVisibility(View.GONE);
				}
				clearControlEdit();				
			}
		});
        
        //Button btnGuardar = (Button) findViewById(R.id.btnGuardar);
       
        findViewById(R.id.btnGuardar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				guardarDatos();
				fillListViewDetalles();
			}
		});

        findViewById(R.id.btnCancelar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clearControlEdit();
			}
		});
 
        findViewById(R.id.btnEliminar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MyUtils myu = new MyUtils();
				myu.delDetalle();
				fillListViewDetalles();
				clearControlEdit();
				//Toast.makeText(getBaseContext(), "cnt " + ((ListView) findViewById(R.id.lstDetalles)).getChildCount(), Toast.LENGTH_SHORT).show();
			}
		});
        
        findViewById(R.id.btnEnviar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MyUtils myu = new MyUtils();
				myu.enviarCocina("Procesando");
				fillListViewDetalles();
				clearControlEdit();				
				/*findViewById(R.id.btnEnviar).setEnabled(activarEnviar);
				findViewById(R.id.btnEnviar).setBackgroundColor(Color.rgb(29, 31, 31));*/
				findViewById(R.id.lltPlatos).setVisibility(View.GONE);
			}
		});
        cantPlato = (EditText)findViewById(R.id.edtCantPlato);
        cantPlato.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if (arg2.getAction()==KeyEvent.ACTION_UP && arg1 == KeyEvent.KEYCODE_ENTER){
					guardarDatos();
					fillListViewDetalles();
				}
				return false;
			}
		});
        cantPlato.setSelectAllOnFocus(true);
      
        edtCantPersonas = (EditText) findViewById(R.id.edtCantPersonas);
        edtCantPersonas.setSelectAllOnFocus(true);
        edtCantPersonas.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if (arg2.getAction()==KeyEvent.ACTION_UP && arg1 == KeyEvent.KEYCODE_ENTER){
					MyUtils.cantPersona =Integer.parseInt(((EditText)findViewById(R.id.edtCantPersonas)).getText().toString());
					MyUtils myu = new MyUtils();
					MyUtils.pedidoId = Integer.parseInt(myu.cambiarCantPersona());
				}
				return false;
			}
		});
/*        *//**
         * Se creo para actualizar se cambiaba los detalles, esto es un parche mal puesto luego hay que resolverlo
         *//*
        findViewById(R.id.lstDetalles).addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, 	int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if (cambioDetalle){
					ListView lvwDetalles = (ListView)findViewById(R.id.lstDetalles);
					for (int i = 0; i < lvwDetalles.getChildCount(); i++) {
						LinearLayout ly = (LinearLayout) lvwDetalles.getChildAt(i);
						if (detallesEstado.get(i)!=2){
							ly.getChildAt(2).setEnabled(false);
							ly.getChildAt(2).setBackgroundResource(R.drawable.ic_plato_des);
						} 
						if (detallesEstado.get(i)==3){
							ly.getChildAt(0).setEnabled(false);
							ly.getChildAt(2).setBackgroundResource(R.drawable.ic_plato_serv);
						}
					}
					cambioDetalle = false;
				}
				
			}
		});*/
        
        edtSearch = (EditText) findViewById(R.id.edtSearch);
        edtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
				adapter.getFilter().filter(cs);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
        edtSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					((EditText)findViewById(R.id.edtSearch)).setText("");
			}
		});
      /*  NotifAndUpdate notif = new NotifAndUpdate();
        timer = new Timer(false);
        //Looper.prepare();
        timer.schedule(notif, 600, 15000);*/
        findViewById(R.id.btnServirTodos).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MyUtils myu = new MyUtils();
				myu.servirTodos();
				fillListViewDetalles();
				clearControlEdit();	
				/*findViewById(R.id.btnServirTodos).setEnabled(activarServirTodos);
				findViewById(R.id.btnEnviar).setBackgroundColor(Color.rgb(29, 31, 31));*/
			}
		});
        findViewById(R.id.btnPrint).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MyUtils myu = new MyUtils();
				myu.imprimir();
				
			}
		});
    }
	
	private void downloadFile(int photo) {
		URL imageUrl = null;
		HttpURLConnection conn = null;
        try {        	
            imageUrl = new URL(MyUtils.urlServidor + "img/pl_" + photo + ".jpg");
            conn = (HttpURLConnection) imageUrl.openConnection();
            conn.connect();
            loadedImage = BitmapFactory.decodeStream(conn.getInputStream());
            imageView.setImageBitmap(loadedImage);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Este plato no tiene foto", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally{
        	conn.disconnect();
        }
	}

/*	class NotifAndUpdate extends TimerTask {
		@Override
		public void run() {
			MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	MyUtils myu = new MyUtils();
        			if (myu.getNotifAndUpdate()){
        				fillListViewDetalles();
	        			if (platosCocinados>0)
	        				sendNotification();
	        			else
	        				cleanNotification();
        			}
                }
            });
        }
	}*/
	
	private void guardarDatos() {
		String cant = ((EditText)findViewById(R.id.edtCantPlato)).getText().toString();
		if (!cant.isEmpty()){
			MyUtils myu = new MyUtils();
			if (MyUtils.detallePedidoId==-1)				
				myu.addDetalle(cant);				
			 else 
				myu.saveDetalle(cant);				
			clearControlEdit();
			
		    
		} else 
			Toast.makeText(getBaseContext(), "Debe poner una cantidad", Toast.LENGTH_LONG).show();
	}
	public void clearControlEdit(){
		((TextView) findViewById(R.id.txvPlatoEdit)).setText("");
		((EditText) findViewById(R.id.edtCantPlato)).setText("");		
		MyUtils.detallePedidoId = -1;
		findViewById(R.id.lltAgregar).setVisibility(View.GONE);
		findViewById(R.id.grdAction).setVisibility(View.GONE);
	}
/*    public void servidoClickHandler(View v){    

    }	*/
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MyUtils u = new MyUtils();
    	menu = u.fillMesas(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.activity_main, menu);
    	ActionBar actionBar = getActionBar();
    	//actionBar.setDisplayShowHomeEnabled(false);
    	actionBar.setDisplayShowTitleEnabled(false);
    	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    	
    	String[] mesaInicial = u.getFirtMesa();
    	MyUtils.mesaId = Integer.parseInt(mesaInicial[0]);
    	MyUtils.mesaName =  mesaInicial[1];
    	loadDataMesa();
        return true;
    }
	private void getDataFillDetalles(){
	    MyUtils u = new MyUtils();	        
	    u.fillDetallesPedido();	    
	    fillListViewDetalles();
	}
	private void fillListViewDetalles() {

		ListView lvwDetalles = (ListView)findViewById(R.id.lstDetalles);
		
		/*SimpleAdapter adapterDetalles = new SimpleAdapter(this, detallesList, R.layout.detalles_item,  new String[] { "lblPlato", "Precio" },
	        		 new int[] { R.id.txvPlatoEdit, R.id.Precio });*/
		MyAdapter adapterDetalles = new MyAdapter(getBaseContext(), detallesList, R.layout.detalles_item,  new String[] { "lblPlato", "Precio", "est" },
       		 new int[] { R.id.txvPlatoEdit, R.id.Precio, R.id.btnServido });
	    lvwDetalles.setAdapter(adapterDetalles);
		lvwDetalles.setClickable(true);
	
	    EditText edtCantPersona = (EditText)findViewById(R.id.edtCantPersonas);
	    //if (!edtCantPersona.isFocused())
	    edtCantPersona.setText(String.valueOf(MyUtils.cantPersona));
    	TextView impTotal = (TextView)findViewById(R.id.tvwTotal);
    	impTotal.setText(String.valueOf(MyUtils.importeTotal));
    	findViewById(R.id.btnEnviar).setEnabled(MyUtils.activarEnviar);
    	//cambioDetalle = true;
    	if (MyUtils.activarEnviar) 
    		findViewById(R.id.btnEnviar).setBackgroundResource(R.drawable.for_boton_enviar);
			//findViewById(R.id.btnEnviar).setBackgroundColor(Color.rgb(167, 24, 40));
    	else
    		findViewById(R.id.btnEnviar).setBackgroundResource(R.drawable.for_boton_disable);
    		//findViewById(R.id.btnEnviar).setBackgroundColor(Color.rgb(29, 31, 31));		
    	findViewById(R.id.btnServirTodos).setEnabled(MyUtils.activarServirTodos);
    	//cambioDetalle = true;
    	if (MyUtils.activarServirTodos) 
    		findViewById(R.id.btnServirTodos).setBackgroundResource(R.drawable.for_boton1);
    	else
    		findViewById(R.id.btnServirTodos).setBackgroundResource(R.drawable.for_boton_disable);
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	MyUtils.mesaId = item.getItemId();
    	MyUtils.mesaName =  item.getTitle().toString();
    	ListView lvwDetalles = (ListView)findViewById(R.id.lstDetalles);
    	//Toast.makeText(getBaseContext(), "cnt " + lvwDetalles.getChildCount(), Toast.LENGTH_SHORT).show();
    	loadDataMesa();    	
    	findViewById(R.id.lltAgregar).setVisibility(View.GONE);
    	findViewById(R.id.grdAction).setVisibility(View.GONE);
    	findViewById(R.id.lltPlatos).setVisibility(View.GONE);
    	lvwDetalles.refreshDrawableState();
    	MyAdapter sad =(MyAdapter) lvwDetalles.getAdapter();
    	sad.notifyDataSetChanged();
    	lvwDetalles.refreshDrawableState();
    	
    	//sendNotification("Platos cocinados en la mesa " + String.valueOf(item.getItemId())," Que tal " + String.valueOf(item.getItemId()));
    	
    	return true;
    	//return super.onOptionsItemSelected(item);
    }
    public void loadDataMesa(){    	
    	TextView nameMesa = (TextView)findViewById(R.id.lblMesa);
    	nameMesa.setText(MyUtils.mesaName);
    	getDataFillDetalles();
    	
    }

    public void sendNotification(){
    	NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	//nm.cancelAll();
    	nm.cancel(1);
    	//nm.cancel(2);
    	String textCocinado = contenidoNotif.length()>0?contenidoNotif.substring(0, contenidoNotif.length()-2):"";  
		Intent toLaunch = new Intent(MainActivity.this, 	MainActivity.class);
		Notification noti1 = new Notification.Builder(getBaseContext())
		.setWhen(System.currentTimeMillis())
		.setSmallIcon(R.drawable.ic_plato)
		.setAutoCancel(true)
		.setContentTitle("Platos a servir")
		.setContentText(textCocinado)
		.setTicker("Platos a servir")
		.setNumber(MyUtils.platosCocinados)
		.setContentIntent(PendingIntent.getActivity(MainActivity.this, 0, toLaunch, 0))	.setContentText(textCocinado).getNotification();
		noti1.audioStreamType = AudioManager.STREAM_NOTIFICATION;
		noti1.sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://com.deveone.alacarte/" + R.raw.bells);
		nm.notify(1, noti1);

	/*	String textMesaProceso = mesaNotif.length()>0?(mesaNotif.substring(0, mesaNotif.length()-2)):""; 				
		Notification noti2 = new Notification.Builder(getBaseContext())
		.setWhen(System.currentTimeMillis())
		.setSmallIcon(R.drawable.ic_mesa)
		.setAutoCancel(false)
		.setContentTitle("Mesas con platos en proceso")
		.setContentText(textMesaProceso)
		.setTicker("Mesas con platos en proceso")
		.setNumber(mesasProceso)
		.setContentIntent(PendingIntent.getActivity(MainActivity.this, 0, toLaunch, 0))	.setContentText(textMesaProceso).getNotification();
		noti2.audioStreamType = AudioManager.STREAM_NOTIFICATION;
		noti2.sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://com.deveone.alacarte/" + R.raw.bells);
		nm.notify(2, noti2);	*/	
		
    }
    private void cleanNotification() {
    	NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	//nm.cancelAll();
    	nm.cancel(1);
    	nm.cancel(2);
	}
	public class MyAdapter extends SimpleAdapter {
		public List<Map<String, String>> detLists;
		
		public MyAdapter(Context context, List<Map<String, String>> detList, int resource, String[] from, int[] to) {
			super(context, detList, resource, from, to);
			this.detLists = detList;
		}
	    public View getView(int position, View convertView, ViewGroup parent){
	    	 View row = convertView;
	         if (row == null) {
	             LayoutInflater mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	             row = mInflater.inflate(R.layout.detalles_item, parent, false);
	             
	             TextView tvwPlatoNombre = (TextView)row.findViewById(R.id.txvPlatoEdit);
	 	        TextView txvPrecio = (TextView)row.findViewById(R.id.Precio);
	 	        Button btn1 = (Button)row.findViewById(R.id.btnServido);
	 	        Map<String,String> map = detLists.get(position);
	 	        tvwPlatoNombre.setText(map.get("lblPlato"));
	 	        txvPrecio.setText(map.get("Precio"));
	 	        
	 	        if (!map.get("est").equals("2")){
	 	        	btn1.setBackgroundResource(R.drawable.ic_plato_des);
	 	        } else{
	 	        	row.setBackgroundColor(Color.rgb(121, 66, 66));
	 	        	btn1.setOnClickListener(new OnClickListener() {
	 					@Override
	 					public void onClick(View v) {
	 				        try {
								ListView lvwDetalles = (ListView)findViewById(R.id.lstDetalles);
								int pos = lvwDetalles.getPositionForView(v);
								MyUtils.detallePedidoId = detallesId.get(pos);  
								MyUtils myu = new MyUtils();
								myu.servirPlato();
								fillListViewDetalles();
								clearControlEdit();
								
								findViewById(R.id.lltAgregar).setVisibility(LinearLayout.GONE);
								findViewById(R.id.grdAction).setVisibility(View.GONE);
							} catch (Exception e) {
								Toast.makeText(getBaseContext()	, "Ocurrió un error" + e.getMessage(), Toast.LENGTH_LONG).show();
							}
	 					}
	 				});
	 	        }
	 	        
	 	        if (map.get("est").equals("3")){	        	
	 	        	row.setBackgroundColor(Color.rgb(68, 80, 68));
	 	        	btn1.setBackgroundResource(R.drawable.ic_plato_serv);
	 	        } else {
	 	        	tvwPlatoNombre.setOnClickListener(new OnClickListener() {
	 					@Override
	 					public void onClick(View v) {
	 						try{
	 					    	ListView lvwDetalles = (ListView)findViewById(R.id.lstDetalles);
	 					    	
	 					        int pos = lvwDetalles.getPositionForView(v);
	 					        findViewById(R.id.lltPlatos).setVisibility(View.GONE);
	 					    	clearControlEdit();
	 					    	MyUtils.detallePedidoId = detallesId.get(pos);
	 					    	TextView nombrePlato = (TextView) v;
	 					    	
	 					    	((TextView) findViewById(R.id.txvPlatoEdit)).setText(nombrePlato.getText().toString());
	 					    	LinearLayout lltParentRow = (LinearLayout)v.getParent();	
	 					    	String[] cantidad = ((TextView)lltParentRow.getChildAt(1)).getText().toString().split("x");	    	
	 							((EditText) findViewById(R.id.edtCantPlato)).setText(cantidad[0]);	    	
	 					    	findViewById(R.id.lltAgregar).setVisibility(View.VISIBLE);
	 					    	findViewById(R.id.grdAction).setVisibility(View.VISIBLE);
	 					    	findViewById(R.id.edtCantPlato).requestFocus();
	 					    	
	 						} catch (Exception e) {
	 							Toast.makeText(getBaseContext()	, "Ocurrió un error" + e.getMessage(), Toast.LENGTH_LONG).show();
	 						}
	 					}
	 				});
	 	        }
	         }
	        return row;
	    }
	}
}
