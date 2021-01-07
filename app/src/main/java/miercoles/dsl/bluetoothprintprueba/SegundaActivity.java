package miercoles.dsl.bluetoothprintprueba;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class SegundaActivity extends AppCompatActivity {

    private Spinner spnFuente, spnNegrita, spnAncho, spnAlto;

    private EditText edtTexto;
    private Button btnImprimirTexto, btnCerrarConexion, btnIr;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segunda);

        edtTexto = (EditText) findViewById(R.id.edt_textoS);
        btnImprimirTexto = (Button) findViewById(R.id.btn_imprimir_textoS);
        spnNegrita = (Spinner) findViewById(R.id.spn_negritaS);
        spnAlto = (Spinner) findViewById(R.id.spn_altoS);
        spnFuente = (Spinner) findViewById(R.id.spn_fuenteS);
        spnAncho = (Spinner) findViewById(R.id.spn_anchoS);
        btnIr = (Button) findViewById(R.id.btnVolver);

    }


}