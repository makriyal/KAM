package com.muniryenigul.kam.activities;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.Toast;
import com.google.android.gms.vision.barcode.Barcode;
import com.muniryenigul.kam.MainActivity;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.interfaces.ApiService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.List;
import info.androidhive.barcode.BarcodeReader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import static com.muniryenigul.kam.MainActivity.favSingleItem;
public class ScanActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {
    private BarcodeReader barcodeReader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);
    }
    public void bringValues(String searchURL, final String barcode) {
        try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.bkmkitap.com/").build())
                    .create(ApiService.class).getPrices(searchURL).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Document doc = Jsoup.parse(response.body());
                        if (doc.select("div.box.col-6.col-sm-12 > div > span").text().contains("bulunamadı")) check(barcode, "no found", "", "", "", "", "", "");
                        else if (contaIns(barcode)) { check(barcode, "old", doc.select("a.fl.col-12.text-description.detailLink").text(),
                                    doc.select("#productModelText").text(), doc.select("a.col.col-12.text-title.mt").text(), doc.select("div:nth-child(1) > a > span > img").attr("src"),
                                    "https://www.bkmkitap.com" + doc.select("a.fl.col-12.text-description.detailLink").attr("href"), doc.select("#productDetailTab > div").text());
                        } else { check(barcode, "new from Scan", doc.select("a.fl.col-12.text-description.detailLink").text(),
                                    doc.select("#productModelText").text(), doc.select("a.col.col-12.text-title.mt").text(), doc.select("div:nth-child(1) > a > span > img").attr("src"),
                                    "https://www.bkmkitap.com" + doc.select("a.fl.col-12.text-description.detailLink").attr("href"), doc.select("#productDetailTab > div").text());
                        }
                    } else { check(barcode, "scan error", "", "", "", "", "", "");
                    }
                }
                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    check(barcode, "scan error", "", "", "", "", "", "");
                }
            });
        } catch (Exception e) {
            check(barcode, "scan error", "", "", "", "", "", "");
            e.printStackTrace();
        }
    }
    private void check(String barcode, String info, String name, String author, String publisher, String cover, String individual, String description) {
        if (info.equals("old") || info.equals("new from Scan")) {
            startActivity(new Intent(ScanActivity.this, PriceActivity.class).putExtra("info", info).putExtra("isbn", barcode).putExtra("name", name).putExtra("author", author).putExtra("publisher", publisher).putExtra("cover", cover).putExtra("individual", individual).putExtra("coverBig", cover).putExtra("description", description));
            finish();
        } else if (info.equals("scan error") || info.equals("no found")) {
            Intent intent=new Intent(ScanActivity.this, MainActivity.class);
            intent.putExtra("info", info).putExtra("isbn", barcode);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
    public boolean contaIns(String barcode) {
        if (favSingleItem != null && favSingleItem.size() != 0) for (int y = 0; y < favSingleItem.size(); y++) { if (favSingleItem.get(y).getIsbn().equals(barcode)) return true; }
        return false;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ScanActivity.this, MainActivity.class));
        finish();
    }
    @Override
    public void onScanned(Barcode barcode) {
        String searchURL = "https://www.bkmkitap.com/arama?q=" + barcode.displayValue;
        barcodeReader.playBeep();
        bringValues(searchURL, barcode.displayValue);
    }
    @Override
    public void onScannedMultiple(List<Barcode> barcodes) { }
    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) { }
    @Override
    public void onScanError(String errorMessage) { Toast.makeText(ScanActivity.this, "Error occurred while scanning " + errorMessage, Toast.LENGTH_SHORT).show(); }
    @Override
    public void onCameraPermissionDenied() { ScanActivity.this.finish(); }
}