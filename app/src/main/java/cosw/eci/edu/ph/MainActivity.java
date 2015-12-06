package cosw.eci.edu.ph;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.view.View.OnClickListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.http.Header;

public class MainActivity extends ActionBarActivity  {
    public int idPaciente;
    public static ArrayList<Integer> idPedidos=new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bIngresar = (Button)findViewById(R.id.bIngresar);
        Button confirmar = (Button)findViewById(R.id.entrega);
        final EditText codigo=(EditText)findViewById(R.id.Codigo);
        bIngresar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String dr = "https://fast-wildwood-9614.herokuapp.com/rest/pacientes/biometricos/" + codigo.getText();
                new Obtener().execute(dr);

            }
        });

                confirmar.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                Log.e("ooooooo"+idPedidos.size(), "num pedidos!");
                for (int i=0;i<idPedidos.size();i++){
                    String dr = "https://fast-wildwood-9614.herokuapp.com/rest/despachos/pedidos/"+idPedidos.get(i);
                    new CambiarEstado().execute(dr);

                }

            }
        });
    }


    private class MostrarPedido extends AsyncTask<String, Void, Void>{
        JSONArray respJSON;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet del;
        HttpResponse resp;
        String respStr;
        ArrayList<Integer> idPedidos=new ArrayList<Integer>();
        ArrayList<String> urlmedicamentos;
        ArrayList<String> medicamentos=new ArrayList<String>();

        @Override
        protected Void doInBackground(String... params) {
            try {
                del = new HttpGet(params[0]);
                resp = httpClient.execute(del);
                respStr = EntityUtils.toString(resp.getEntity());
                respJSON = new JSONArray(respStr);

                for(int i=0;i<respJSON.length();i++){
                    JSONObject objJSON=respJSON.getJSONObject(i);

                    idPedidos.add(objJSON.getInt("idPedidos"));
                }
                MainActivity.idPedidos=idPedidos;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {

            urlmedicamentos=new  ArrayList<String>();
            for(int i=0;i<idPedidos.size();i++){
                TextView resultado1=(TextView)findViewById(R.id.resultado1);
                urlmedicamentos.add("https://fast-wildwood-9614.herokuapp.com/rest/medicamentos/pedido/" + idPedidos.get(i));

                MostrarMedicamentos mm=new MostrarMedicamentos();
                mm.pedido(idPedidos.get(i));
                mm.execute(urlmedicamentos.get(i));

            }
        }


    }




    private class CambiarEstado extends AsyncTask<String, Void, Void>{
        JSONObject respJSON;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPut del;
        HttpResponse resp;
        String respStr;
        int idPaciente;

        @Override
        protected Void doInBackground(String... params) {

            try {
                HttpPut httpPut= new HttpPut(params[0]);
                //del = new HttpPut(params[0]);
                //resp = httpClient.execute(del);
                //respStr = EntityUtils.toString(resp.getEntity());
                //respJSON = new JSONObject(respStr);
                respJSON = new JSONObject();
                respJSON.put("estado", "entregado");
                respStr = respJSON.toString();
                StringEntity se = new StringEntity(respStr);

                httpPut.setEntity(se);
                httpPut.addHeader("Accept", "application/json");
                httpPut.addHeader("Content-type", "application/json");
                HttpResponse httpResponse = httpClient.execute(httpPut);
                Log.e("estadoo " + respJSON.getString("estado"), " 1 trae estado !");

                Log.e("estadoo "+respJSON.getString("estado"), " 2 cambia estadoo !");

            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
            }
            return null;

        }





    }
    private class Obtener extends AsyncTask<String, Void, Void> {
        JSONObject respJSON;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet del;
        HttpResponse resp;
        String respStr;
        String nombre;
        int idPaciente;

        @Override
        protected Void doInBackground(String... params) {
            try {
                del = new HttpGet(params[0]);
                resp = httpClient.execute(del);
                respStr = EntityUtils.toString(resp.getEntity());
                respJSON = new JSONObject(respStr);
                idPaciente = respJSON.getInt("idPacientes");
                nombre = respJSON.getString("nombre");
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            TextView resultado = (TextView) findViewById(R.id.resultado);
            if (idPaciente != 0) {
                resultado.setText(idPaciente + "\n" +nombre);

                String dr = "https://fast-wildwood-9614.herokuapp.com/rest/pedidos/paciente/" + idPaciente;
                new MostrarPedido().execute(dr);
            } else {
                resultado.setText("este pacieente no existe");
            }
        }
    }



    private class MostrarMedicamentos extends AsyncTask<String, Void, Void>{
        JSONArray respJSON;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet del;
        HttpResponse resp;
        String respStr;
        int numPedido=0;
        ArrayList<String> medicamentos=new ArrayList<String>();



        @Override
        protected Void doInBackground(String... params) {

            try {

                del = new HttpGet(params[0]);
                resp = httpClient.execute(del);
                respStr = EntityUtils.toString(resp.getEntity());
                respJSON = new JSONArray(respStr);

                for(int i=0;i<respJSON.length();i++){
                    JSONObject objJSON=respJSON.getJSONObject(i);
                    medicamentos.add(objJSON.getString("nombre") + " ");
                }



            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
            }

            Log.e(" medicamentos " + medicamentos.size(), " Error!  11");
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            TextView resultado1=(TextView)findViewById(R.id.resultado1);
            Log.e("medikmentos" + medicamentos.size(), " tamaÃ±o!");
            resultado1.setText(resultado1.getText() + " Pedido:  # :  " + numPedido + "\n");
            resultado1.setText(resultado1.getText() + " " + medicamentos + "\n");


        }

        public void pedido(Integer integer) {
            numPedido=integer;
        }
    }

}
