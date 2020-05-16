package mx.edu.ittepic.ladm_u4_ejercicio1_smspermisos

import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val siPermiso = 1 //1 si 0 no
    val siPermisoReceiver = 2
    val siPermisoLectura = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.RECEIVE_SMS),siPermisoReceiver)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_SMS),siPermisoLectura)
        }else{
            leerSMSEntrada()
        }

        button.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.SEND_SMS),siPermiso)//array con todos los permisos
            }else{//si ya tiene los permisos
                envioSMS()
            }
        }

        textView.setOnClickListener {
            try {

                val cursor = BaseDatos(this,"entrantes",null,1)
                    .readableDatabase.rawQuery("SELECT * FROM ENTRANTES",null)

                var ultimo = ""

                if(cursor.moveToFirst()){
                    do{
                        ultimo = "Ultimo mensaje Recibido\nCelular de Origen: " + cursor.getString(0)+"\nMensajo:"+cursor.getString(1)

                    }while (cursor.moveToNext())
                }else{
                    ultimo = "Bandeja/Tabla Vacia (Nadie te quiere)"
                }
                textView.setText(ultimo)
            }catch (err:SQLiteException){
                Toast.makeText(this,"",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== siPermiso){
            envioSMS()
        }
        if(requestCode == siPermisoReceiver){
            mensajeRecibir()
        }
        if(requestCode == siPermisoLectura){
            leerSMSEntrada()
        }
    }

    private fun leerSMSEntrada() {
        //referencia a bandeja
        var cursor = contentResolver.query(Uri.parse("content://SMS/"),null,null,null,null)

        var resultado = ""

        if(cursor!!.moveToFirst()){

            var posColumnaCelularOrigen = cursor.getColumnIndex("address")
            var posColumnaMensaje = cursor.getColumnIndex("body")
            var posColumnaFecha = cursor.getColumnIndex("date")

            do{

                val fechaMensaje = cursor.getShort(posColumnaFecha)
                resultado += "Origen: "+cursor.getString(posColumnaCelularOrigen)+
                             "\nMensajo: "+cursor.getString(posColumnaMensaje)+
                             "\nFecha: "+ Date(fechaMensaje.toLong())+
                             "\n------------------------------------\n"

            }while (cursor.moveToNext())
        }else{
            resultado = "No hay SMS en bandeja de entrada"
        }
        textView2.setText(resultado)
    }

    private fun mensajeRecibir() {
        AlertDialog.Builder(this).setMessage("Se otorgo Recibir").show()
    }

    private fun envioSMS() {
        SmsManager.getDefault().sendTextMessage(editText.text.toString(),null,editText2.text.toString(),null,null)
        Toast.makeText(this,"Se envio el SMS",Toast.LENGTH_LONG).show()
    }
}
