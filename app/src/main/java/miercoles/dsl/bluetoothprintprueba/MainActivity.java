package miercoles.dsl.bluetoothprintprueba;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_DISPOSITIVO = 425;
    private static final String TAG_DEBUG = "tag_debug";
    private static final int CODIGO_SEGUNDA_ACTIVITY = 1313;

    private TextView txtLabel;
    private EditText edtTexto;
    private Button btnImprimirTexto, btnCerrarConexion, btnIr;

    private Spinner spnFuente, spnNegrita, spnAncho, spnAlto;
    // Para la operaciones con dispositivos bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice dispositivoBluetooth;
    private BluetoothSocket bluetoothSocket;

    // identificador unico default
    private UUID aplicacionUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Para el flujo de datos de entrada y salida del socket bluetooth
    private OutputStream outputStream;
    private InputStream inputStream;

    // volatile: no guarda una copia en chaché para cada hilo, si no que los sincroliza cuando cambien la variable
    // de esa manera todos manejaran el mismo valor de la variable y no una copia que puede estar con valor anterior
    private volatile boolean pararLectura;

    String dirDisp = null, nomDisp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLabel = (TextView) findViewById(R.id.txt_label);
        edtTexto = (EditText) findViewById(R.id.edt_texto);
        btnImprimirTexto = (Button) findViewById(R.id.btn_imprimir_texto);
        spnNegrita = (Spinner) findViewById(R.id.spn_negrita);
        spnAlto = (Spinner) findViewById(R.id.spn_alto);
        spnFuente = (Spinner) findViewById(R.id.spn_fuente);
        spnAncho = (Spinner) findViewById(R.id.spn_ancho);
        btnCerrarConexion = (Button) findViewById(R.id.btn_cerrar_conexion);
        btnIr = (Button) findViewById(R.id.btnSiguiente);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnImprimirTexto.setOnClickListener(this);
        btnCerrarConexion.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_imprimir_texto) {

            String texto = edtTexto.getText().toString() + "\n";

            int fuente = Integer.parseInt(spnFuente.getSelectedItem().toString());
            int negrita = spnNegrita.getSelectedItem().toString().equals("Si") ? 1 : 0;
            int ancho = Integer.parseInt(spnAncho.getSelectedItem().toString());
            int alto = Integer.parseInt(spnAlto.getSelectedItem().toString());

            imprimir(texto, fuente, negrita, ancho, alto);


        }
        if (view.getId() == R.id.btn_cerrar_conexion) {
            cerrarConexion();
        }
    }

    private void imprimir(String texto, int fuente, int negrita, int ancho, int alto) {
        if (bluetoothSocket != null) {
            try {
                Toast.makeText(this, "Se esta imprimiendo [ " + texto + " ] con fuente " + fuente + ", negrita " + negrita + ", ancho " + ancho + " y alto " + alto, Toast.LENGTH_SHORT).show();
                // Para que acepte caracteres espciales
                outputStream.write(0x1C);
                outputStream.write(0x2E); // Cancelamos el modo de caracteres chino (FS .)
                outputStream.write(0x1B);
                outputStream.write(0x74);
                outputStream.write(0x10); // Seleccionamos los caracteres escape (ESC t n) - n = 16(0x10) para WPC1252

                outputStream.write(Objects.requireNonNull(getByteString(texto, negrita, fuente, ancho, alto)));
                outputStream.write("\n\n".getBytes());
            } catch (IOException e) {
                Log.e(TAG_DEBUG, "Error al escribir en el socket");

                Toast.makeText(this, "Error al interntar imprimir texto", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG_DEBUG, "Fallo por otro error");
                Toast.makeText(this, "Se ha producido un error inesperado", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Log.e(TAG_DEBUG, "Socket nulo");

            txtLabel.setText("Impresora no conectada");
        }
    }

    public void clickBuscarDispositivosSync(View btn) {
        // Cerramos la conexion antes de establecer otra
        cerrarConexion();
        Intent intentLista = new Intent(this, ListaBluetoohtActivity.class);
        startActivityForResult(intentLista, REQUEST_DISPOSITIVO);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CODIGO_SEGUNDA_ACTIVITY) {
                imprimir(data.getStringExtra("texto"), data.getIntExtra("fuente", 0), data.getIntExtra("negrita", 0)
                        , data.getIntExtra("ancho", 0), data.getIntExtra("alto", 0));
            }
            if (requestCode == REQUEST_DISPOSITIVO) {
                txtLabel.setText("Cargando...");

                dirDisp = Objects.requireNonNull(data.getExtras()).getString("DireccionDispositivo");
                nomDisp = data.getExtras().getString("NombreDispositivo");
                System.out.println("DIRECCIÓN: " + dirDisp);
                System.out.println("NOMBRE: " + nomDisp);

                // Obtenemos el dispositivo con la direccion seleccionada en la lista
                dispositivoBluetooth = bluetoothAdapter.getRemoteDevice(dirDisp);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Conectamos los dispositivos

                            // Creamos un socket
                            bluetoothSocket = dispositivoBluetooth.createRfcommSocketToServiceRecord(aplicacionUUID);
                            bluetoothSocket.connect();// conectamos el socket
                            outputStream = bluetoothSocket.getOutputStream();
                            inputStream = bluetoothSocket.getInputStream();

                            //empezarEscucharDatos();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtLabel.setText(nomDisp + " conectada");
                                    Toast.makeText(MainActivity.this, "Dispositivo Conectado", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } catch (IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtLabel.setText("");
                                    Toast.makeText(MainActivity.this, "No se pudo conectar el dispositivo", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Log.e(TAG_DEBUG, "Error al conectar el dispositivo bluetooth");

                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    private void cerrarConexion() {
        try {
            if (bluetoothSocket != null) {
                if (outputStream != null) outputStream.close();
                pararLectura = true;
                if (inputStream != null) inputStream.close();
                bluetoothSocket.close();
                txtLabel.setText("Conexion terminada");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static byte[] getByteString(String str, int bold, int font, int widthsize, int heigthsize) {

        if (str.length() == 0 | widthsize < 0 | widthsize > 3 | heigthsize < 0 | heigthsize > 3
                | font < 0 | font > 1)
            return null;

        byte[] strData = null;
        try {
            strData = str.getBytes("iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        byte[] command = new byte[strData.length + 9];

        byte[] intToWidth = {0x00, 0x10, 0x20, 0x30};//
        byte[] intToHeight = {0x00, 0x01, 0x02, 0x03};//

        command[0] = 27;// caracter ESC para darle comandos a la impresora
        command[1] = 69;
        command[2] = ((byte) bold);
        command[3] = 27;
        command[4] = 77;
        command[5] = ((byte) font);
        command[6] = 29;
        command[7] = 33;
        command[8] = (byte) (intToWidth[widthsize] + intToHeight[heigthsize]);

        System.arraycopy(strData, 0, command, 9, strData.length);
        return command;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cerrarConexion();
    }

    public void irSiguiente(View vista) {
        Context contexto = getApplicationContext();
        Intent intento = new Intent(contexto, SegundaActivity.class);
        startActivityForResult(intento, CODIGO_SEGUNDA_ACTIVITY);
    }

}
