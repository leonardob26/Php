package com.deveone.alacarte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codebutler.android_websockets.WebSocketClient;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MyUtils {
	public static int mesaId, pedidoId, detallePedidoId, platosCocinados, cantPersona, platoId, mesasProceso;
	public static WebSocketClient client;
	public static String urlServidor, domain;
	public static boolean activarEnviar = false, activarServirTodos=false;
	public static float importeTotal;
	public static String mesaName, estadoPedido;
	public Menu fillMesas(Menu menu) {
		//Retorna list[id, nombreMesa, EstadoPedido, platosCocinadosSinServir];
		menu.clear();
		try {
	    	JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "mesas");
			for (int i = 0; i < ja.length(); i++) {
	            JSONArray mesa = (JSONArray) ja.get(i);
				MenuItem item1 = menu.add(0, Integer.valueOf((String) mesa.get(0)), 1, (String) mesa.get(1) );					
				//item1.setIcon(R.drawable.ic_launcher);
				if (i<12)
					item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				else 
					item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);			        
	        }	
			ja = null;
		} catch (JSONException e) { Log.w(" Error ", e.toString()); 	e.printStackTrace();} 
		catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace();	}    		
    	return menu;
	}

	/**
	 * @return String[id, nombreMesa]
	 */
	public String[] getFirtMesa(){
		String[] result = new String[2];
		try {
	    	JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "firstMesa");
	    	result[0] = ja.get(0).toString();
	    	result[1] = ja.get(1).toString();
	    	ja = null;
		} catch (JSONException e) { Log.w(" Error ", e.toString()); e.printStackTrace();} 
		catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace(); } 
		return result;
	}
	
	public void  fillPlatos(){				
		/*Retorna list[platoId, nombre, precio]	*/
		try {
			MainActivity.platosList.clear();
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "platos");
			for (int i = 0; i < ja.length(); i++) {
	            JSONArray plato	= (JSONArray) ja.get(i);
				//MainActivity.platosId.add(Integer.parseInt(plato.get(0).toString())); //Es el converdor de indices	
				Map<String,String> map = new HashMap<String, String>();
				 map.put("id", plato.getString(0));
				 map.put("lblPlato", plato.getString(1));
				 map.put("lblPrecio", plato.getString(2));
				 //map.put("photoIcon", R.drawable.ic_fotoplato);
				 MainActivity.platosList.add(map);       
	        }
		} catch (JSONException e) { Log.w(" Error ", e.toString()); 	e.printStackTrace();} 
		catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace();	} 
		
	}

	public void fillDetallesPedido(){
		try {
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "changeMesa&mesaId=" + mesaId);		
			fillDetalles(ja);
		} catch (JSONException e) { Log.w(" Error ", e.toString());	e.printStackTrace();}  
		catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace();	} 
	}

	private void fillDetalles(JSONArray ja) throws JSONException {
		/* Si tiene pedido devuelve [pedidoId, estadoPedido, cantPersona, importeTotal, list[detalleId, estado, cantidad, plato, precio, importe], activarEnviar]
		 Sino devuelve [-1] */
		
		try {
			MainActivity.detallesId.clear();
			MainActivity.detallesList.clear();
			
			pedidoId =  ja.getInt(0);		
			if (pedidoId!=-1){
				estadoPedido =  ja.getString(1);	
				cantPersona =  ja.getInt(2);	
				importeTotal =  (float) ja.getDouble(3);	
				activarEnviar = (ja.getInt(5)==1);
				activarServirTodos = (ja.getInt(6)==1);
				JSONArray jaDetalles = (JSONArray) ja.get(4);
				for (int i = 0; i < jaDetalles.length(); i++) {
			        JSONArray pedido = (JSONArray) jaDetalles.get(i);
					MainActivity.detallesId.add(pedido.getInt(0)); //Es el converdor de indices	
					
					Map<String,String> map = new HashMap<String, String>();
					map.put("lblPlato", pedido.getString(3));
					map.put("Precio",  pedido.getString(2) + "x" + pedido.getString(4) + "=" + pedido.getString(5));
					map.put("est", pedido.getString(1));
					 //map.put("photoIcon", R.drawable.ic_fotoplato);
					MainActivity.detallesList.add(map);
				}
			} else {
				activarEnviar = false;
				activarServirTodos = false;
				estadoPedido = "";
				cantPersona = 0;	
				importeTotal = 0;
				pedidoId = -1;
			}
		} catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace(); }
	}
	
	public void delDetalle() {
		try {
			//actionDeleteDetalle($mesaId, $detalleId)
			String parameter = "mesaId=" + mesaId + "&detalleId=" + detallePedidoId;
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "deleteDetalle&" + parameter);
			fillDetalles(ja);
		}  catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace();} 
	}
	public void saveDetalle(String cantPlato){
		//Envía $mesaId, $pedidoId, $detalleId, $platoId, $cantidad
		try {
			String parameter = "mesaId=" + mesaId + "&detalleId=" + detallePedidoId + "&cantidad=" + cantPlato;
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "saveDetalle&" + parameter);
			fillDetalles(ja);
		}  catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace();} 
	}
	public void addDetalle(String cantPlato){
		//Envía $mesaId, $pedidoId, $detalleId, $platoId, $cantidad
		try {
			String parameter = "mesaId=" + mesaId + "&pedidoId=" + pedidoId + 
						"&platoId=" + platoId + "&cantidad=" + cantPlato;
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "addDetalle&" + parameter);		
			fillDetalles(ja);
		}  catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace();} 
	}
	/**
	 * @return idPedido
	 */
	public String cambiarCantPersona(){
		//actionPonerCantidadPersonas($mesaId, $pedidoId, $cantPersona=0)
		String parameter = "mesaId=" + mesaId + "&pedidoId=" + pedidoId + "&cantPersona=" + cantPersona;
		return getStringFromUrl(urlServidor + "index.php?r=mov/" + "ponerCantidadPersonas&" + parameter);
	}
	
	public void enviarCocina(String estado){
		//actionCambiarEstadoPedido($mesaId, $pedidoId, $estado)		
		try {
			String parameter = "mesaId=" + mesaId + "&pedidoId=" + pedidoId + "&estado=" + estado;
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "cambiarEstadoPedido&" + parameter);
			fillDetalles(ja);
			activarEnviar = false;
		}
		catch (Exception e) { Log.w(" Error ", e.toString()); 	e.printStackTrace(); } 
	}
	public String getPlatoDescripcion(){
		//actionCambiarEstadoPedido($mesaId, $pedidoId, $estado)		
		try {
			String parameter = "platoId=" + platoId;
			return getStringFromUrl(urlServidor + "index.php?r=mov/" + "platoDescripcion&" + parameter);			
		}
		catch (Exception e) { Log.w(" Error ", e.toString());
			return e.getMessage();
		} 
	}	
	public void servirPlato(){
		//actionPutPlatoServido($mesaId, $detalleId)
		try {
			String parameter = "mesaId=" + mesaId + "&detalleId=" + detallePedidoId;
			                  getStringFromUrl(urlServidor + "index.php?r=mov/" + "putPlatoServido&" + parameter);
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "putPlatoServido&" + parameter);		
			fillDetalles(ja);
		} catch (Exception e) { Log.w(" Error ", e.toString()); e.printStackTrace();	} 
	}
	public void servirTodos() {
		try {
			String parameter = "mesaId=" + mesaId+ "&pedidoId=" + pedidoId;
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "servirTodos&" + parameter);
			fillDetalles(ja);
			activarServirTodos  = false;
		}
		catch (Exception e) { Log.w(" Error ", e.toString()); 	e.printStackTrace(); } 
	}
	public boolean getNotifAndUpdate() {
		/** getEstadosPedidos devuelve [mesaId, mesaName, estado, cantCocinados]
		 * getPedidos devuelve [pedidoId, estadoPedido, cantPersona, importeTotal, [detalleId, estado, cantidad, plato, precio, importe], activarEnviar]
		 * Al final devuelve [1, [estadosPedidos],[pedidosMesa]]*/
		boolean update = false;
		try {
			
			platosCocinados = 0;
			String parameter = "mesaId=" + mesaId ;
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "getRefresh&" + parameter);
			if (ja.get(0).toString().equals("1")){
				// Estado Pedidos				
				getNotif((JSONArray) ja.get(1));					
				//Detalles
				fillDetalles((JSONArray) ja.get(2));
				update = true;
			}
		} catch (JSONException e){ Log.w(" Error ", e.toString()); e.printStackTrace();	}  
		catch (Exception e){ Log.w(" Error ", e.toString()); e.printStackTrace(); }
		return update;
	}
	public boolean getEstadoPedidos() {
		/** getEstadosPedidos devuelve [mesaId, mesaName, estado, cantCocinados]
		 * getPedidos devuelve [pedidoId, estadoPedido, cantPersona, importeTotal, [detalleId, estado, cantidad, plato, precio, importe], activarEnviar]
		 * Al final devuelve [1, [estadosPedidos],[pedidosMesa]]*/
		boolean update = false;
		try {
			
			platosCocinados = 0;
			//JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "estadosPedidos&" + parameter);
			// Estado Pedidos				
			JSONArray ja = getJSONArrayFromUrl(urlServidor + "index.php?r=mov/" + "estadosPedidos");
			getNotif(ja);					
			//Detalles
			//fillDetalles((JSONArray) ja.get(1));
			update = true;
		} catch (JSONException e){ Log.w(" Error ", e.toString()); e.printStackTrace();	}  
		catch (Exception e){Log.w(" Error ", e.toString());  e.printStackTrace(); }
		return update;
	}
	public void imprimir() {
		getStringFromUrl(urlServidor + "index.php?r=mov/" + "imprimir&pedidoId=" + pedidoId);
	}
	private void getNotif(JSONArray ja) throws JSONException{
		//[mesaId, mesaName, estado, cantCocinados]
		try {
			MainActivity.contenidoNotif.setLength(0);
			MainActivity.mesaNotif.setLength(0);
			mesasProceso = 0;
			for (int i = 0; i < ja.length(); i++) {
				JSONArray estadoPedido = (JSONArray) ja.get(i);
				platosCocinados += estadoPedido.getInt(3);
				//Lo siguiente es para ver si se puede cambiar el estilo del menu del action bar para cambiarle el color
				MainActivity.mesasEstados.append(estadoPedido.getInt(0), estadoPedido.getString(2));
				
				if (estadoPedido.getString(2).equals("Procesando")){
					MainActivity.mesaNotif.append("(M" + estadoPedido.getString(1).substring(4) + "), ");
					mesasProceso++;
				}
					
				if (estadoPedido.getInt(3)!=0)
					MainActivity.contenidoNotif.append("(M" + estadoPedido.getString(1).substring(4) + ": " + estadoPedido.getString(3) + "), ");			
			}
		} catch (Exception e) {Log.w(" Error ", e.toString()); 	e.printStackTrace();}
		
	}
	 public String getStringFromUrl(String url) {
		 BufferedReader reader = null;
		 URL url1 = null;
		 HttpURLConnection con = null;
		 String result = "";
	        try{
	        	url1 = new URL(url);
	        	con = (HttpURLConnection) url1.openConnection();
	        	reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      		    String line = reader.readLine();
      		    if (line!=null)
      		    	result = line;        		    
	        }catch (IOException e) { 
	        	Log.w(" Error ", e.toString()); e.printStackTrace();
	        } 
	        catch(Exception e){ 
	        	Log.w(" Error ", e.toString()); e.printStackTrace();
	        }
	        finally {
	            if (reader!=null) {
	              try {
	                reader.close();
	                con.disconnect();
	              } catch (IOException e) {Log.w(" Error ", e.toString()); e.printStackTrace();}
	            }
	        }     
	        return result;
	 }

	 public JSONObject getJSONObjectFromUrl(String url) {
		 BufferedReader reader = null;
		 JSONObject jo = null;
		 URL url1 = null;
		 HttpURLConnection con = null;
	        try{
	        	url1 = new URL(url);
	        	con = (HttpURLConnection) url1.openConnection();
	        	reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      		    String line = reader.readLine();
      		    if (line!=null)
      		    	jo = new JSONObject(line);  
	        } catch (JSONException e) { 
	        	Log.w(" Error ", e.toString()); e.printStackTrace(); 
	        } 
	        catch (IOException e) { 
	        	Log.w(" Error ", e.toString()); e.printStackTrace();
	        } 
	        catch(Exception e){ Log.w(" Error ", e.toString()); e.printStackTrace();}
	        finally {
	            if (reader != null) {
	              try {
	                reader.close();
	                con.disconnect();
	              } catch (IOException e) {Log.w(" Error ", e.toString()); e.printStackTrace();}
	            }
	        }     
	        return jo;
	 }
	 public JSONArray getJSONArrayFromUrl(String url) {
		 JSONArray ja = null;
		 BufferedReader reader = null;
		 URL url1 = null;
		 HttpURLConnection con = null;
		 try {
			 url1 = new URL(url);
	    	 con = (HttpURLConnection) url1.openConnection();
  		 
  		    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
  		    String line = "";
  		    line = reader.readLine();
  		    if (line!=null)
  		    	ja = new JSONArray(line);      		    
  		 } catch (JSONException e){ 
  			Log.w(" Error ", e.toString()); e.printStackTrace(); 
  		 } catch (IOException e){ Log.w(" Error ", e.toString()); e.printStackTrace();} 
		 catch(Exception e){ Log.w(" Error ", e.toString()); e.printStackTrace();} 
		 finally {
  		    if (reader != null) {
  		      try {
  		        reader.close();
  		        con.disconnect();
  		      } catch (IOException e) { Log.w(" Error ", e.toString()); e.printStackTrace();}
  		    }    
          } 
  		 return ja;
	 }


}
/*private void readStream(InputStream in) {

ArrayList<String[]> items = new ArrayList<String[]>();

  BufferedReader reader = null;
  StringBuilder sb = new StringBuilder();
  try {
    reader = new BufferedReader(new InputStreamReader(in));
    String line = "";
    line = reader.readLine();
    if (line!=null){
    	JSONArray mesas = new JSONArray(line);
    	String[] dato = new String[4];		    	
		for (int i = 0; i < mesas.length(); i++) {
            JSONArray mesa = (JSONArray) mesas.get(i);
            TypeMesas datamesa = new TypeMesas();
            datamesa.id = (String) mesa.get(0);
            datamesa.mesa = (String) mesa.get(1);
            datamesa.estado = (String) mesa.get(2);
            datamesa.cocinados = (String) mesa.get(3);
            items.add(datamesa);
        }
    }
    json = sb.toString();
  } catch (JSONException e) {
		e.printStackTrace();
  }catch (IOException e) {
    e.printStackTrace();
  } finally {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
        }
    }
  }
  
} */
/*
if (line!=null){
    	JSONArray mesas = new JSONArray(line);
    	String[] dato = new String[4];		    	
		for (int i = 0; i < mesas.length(); i++) {
            JSONArray mesa = (JSONArray) mesas.get(i);
            TypeMesas datamesa = new TypeMesas();
            datamesa.id = (String) mesa.get(0);
            datamesa.mesa = (String) mesa.get(1);
            datamesa.estado = (String) mesa.get(2);
            datamesa.cocinados = (String) mesa.get(3);
            items.add(datamesa);
        }
    }

* */